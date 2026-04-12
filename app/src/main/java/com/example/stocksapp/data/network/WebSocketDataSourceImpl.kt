package com.example.stocksapp.data.network

import com.example.stocksapp.data.model.ConnectionState
import com.example.stocksapp.data.model.StockDto
import com.example.stocksapp.data.model.parseStockDtoFromWire
import com.example.stocksapp.data.model.toWireJson
import com.example.stocksapp.data.source.MockStockPriceDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * WebSocket data source using OkHttp.
 *
 * Connects to a WebSocket endpoint (Postman Echo) where outgoing mock price updates
 * are sent and echoed back, then parsed into [StockDto].
 *
 * Responsibilities:
 * - Manage WebSocket lifecycle via [connect] and [disconnect]
 * - Send mock price updates from [mockDataSource]
 * - Receive and emit parsed price updates via Flow
 * - Expose transport connection state through [connectionStateFlow] ([ConnectionState], not domain types)
 *
 * Reconnection:
 * - Automatically attempts to reconnect when the connection fails or closes unexpectedly
 *   (via OkHttp callbacks like onFailure / onClosed)
 * - Reconnection is scheduled on the injected [scope] with a fixed delay
 * - Reconnection is disabled when [disconnect] is called (controlled by [manualDisconnect])
 *
 * Concurrency:
 * - A single [lock] is used to guard access to [webSocket] and connection flags
 * - Flows and coroutines handle asynchronous data and lifecycle safely
 */


@Singleton
class WebSocketDataSourceImpl @Inject constructor(
    private val mockDataSource: MockStockPriceDataSource,
    private val client: OkHttpClient,
    private val scope: CoroutineScope
) : WebSocketDataSource {

    companion object {
        private const val WEBSOCKET_URL = "wss://ws.postman-echo.com/raw"
        private const val NORMAL_CLOSE_CODE = 1000
        private const val NORMAL_CLOSE_REASON = "Disconnected"

        // Balanced reconnect (simple + safe)
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val lock = Any()

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var manualDisconnect = false

    private var sendingJob: Job? = null
    private var reconnectJob: Job? = null

    private val connectionStateFlow =
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private val priceUpdates = MutableSharedFlow<StockDto>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override suspend fun connect() {
        synchronized(lock) {
            if (connectionStateFlow.value is ConnectionState.Connected ||
                connectionStateFlow.value is ConnectionState.Connecting
            ) return

            manualDisconnect = false
        }

        reconnectJob?.cancel()
        openSocket()
    }

    override suspend fun disconnect() {
        synchronized(lock) {
            manualDisconnect = true
        }

        reconnectJob?.cancel()
        reconnectJob = null

        stopSending()

        val socket = synchronized(lock) {
            val current = webSocket
            webSocket = null
            current
        }

        try {
            socket?.close(NORMAL_CLOSE_CODE, NORMAL_CLOSE_REASON)
        } catch (_: Exception) {
            connectionStateFlow.value = ConnectionState.Error("Socket Disconnect Failed!")
        }

        connectionStateFlow.value = ConnectionState.Disconnected
    }

    override fun observePriceUpdates(): Flow<StockDto> =
        priceUpdates.asSharedFlow()

    override fun observeConnectionStatus(): Flow<ConnectionState> =
        connectionStateFlow.asStateFlow()

    override fun isConnected(): Boolean =
        connectionStateFlow.value is ConnectionState.Connected

    private suspend fun openSocket() {
        connectionStateFlow.value = ConnectionState.Connecting

        suspendCancellableCoroutine { continuation ->
            var opened = false
            val request = Request.Builder().url(WEBSOCKET_URL).build()
            val listener = object : WebSocketListener() {
                override fun onOpen(socket: WebSocket, response: Response) {
                    synchronized(lock) {
                        webSocket = socket
                        opened = true
                    }

                    connectionStateFlow.value = ConnectionState.Connected
                    startSending()

                    if (continuation.isActive) continuation.resume(Unit)
                }

                override fun onMessage(socket: WebSocket, text: String) {
                    parseStockDtoFromWire(text)?.let {
                        priceUpdates.tryEmit(it)
                    }
                }

                override fun onClosing(socket: WebSocket, code: Int, reason: String) {
                    socket.close(code, reason)
                }

                override fun onClosed(socket: WebSocket, code: Int, reason: String) {
                    handleDisconnect(socket, "Closed: $code $reason")
                }

                override fun onFailure(socket: WebSocket, t: Throwable, response: Response?) {
                    val beforeOpen = !opened
                    handleDisconnect(socket, t.message ?: "Connection failed")
                    if (beforeOpen && continuation.isActive) {
                        continuation.resumeWithException(t)
                    }
                }
            }

            val call = client.newWebSocket(request, listener)
            continuation.invokeOnCancellation { call.cancel() }
        }
    }

    private fun handleDisconnect(socket: WebSocket, reason: String) {
        val shouldReconnect: Boolean

        synchronized(lock) {
            if (webSocket !== socket) return
            webSocket = null
            shouldReconnect = !manualDisconnect
        }

        stopSending()
        connectionStateFlow.value = ConnectionState.Error(reason)
        if (shouldReconnect) {
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()

        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            synchronized(lock) {
                if (manualDisconnect) return@launch
            }
            openSocketSafely()
        }
    }

    private suspend fun openSocketSafely() {
        try {
            openSocket()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            connectionStateFlow.value =
                ConnectionState.Error("Reconnect failed: ${e.message}")
            scheduleReconnect()
        }
    }

    private fun startSending() {
        stopSending()

        sendingJob = scope.launch {
            try {
                mockDataSource.generatePriceUpdates().collect { batch ->
                    if (connectionStateFlow.value is ConnectionState.Connected) {
                        for (update in batch) {
                            sendPriceUpdate(update)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                connectionStateFlow.value =
                    ConnectionState.Error("Mock error: ${e.message}")
            }
        }
    }

    private fun sendPriceUpdate(priceUpdate: StockDto) {
        val socket = synchronized(lock) {
            if (connectionStateFlow.value !is ConnectionState.Connected) return
            webSocket
        } ?: return

        try {
            if (!socket.send(priceUpdate.toWireJson()))
                connectionStateFlow.value = ConnectionState.Error("Send failed!")
        } catch (e: Exception) {
            connectionStateFlow.value =
                ConnectionState.Error("Send failed with exception ${e.message}")
        }
    }

    private fun stopSending() {
        sendingJob?.cancel()
        sendingJob = null
    }
}