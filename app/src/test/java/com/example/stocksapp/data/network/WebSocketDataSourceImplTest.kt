package com.example.stocksapp.data.network

import com.example.stocksapp.data.model.ConnectionState
import com.example.stocksapp.data.source.MockStockPriceDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.UnknownHostException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WebSocketDataSourceImplTest {

    @Test
    fun `connect moves to connected when onOpen is received`() = runTest {
        val mockDataSource = mockk<MockStockPriceDataSource>()
        val client = mockk<OkHttpClient>()
        val socket = mockk<WebSocket>(relaxed = true)
        val response = mockk<Response>(relaxed = true)
        val listeners = mutableListOf<WebSocketListener>()

        every { mockDataSource.generatePriceUpdates() } returns emptyFlow()
        every { client.newWebSocket(any(), any()) } answers {
            listeners += secondArg<WebSocketListener>()
            socket
        }

        val dataSource = WebSocketDataSourceImpl(mockDataSource, client, this)
        val connectJob = launch { dataSource.connect() }
        advanceUntilIdle()

        listeners.single().onOpen(socket, response)
        connectJob.join()

        val state = dataSource.observeConnectionStatus().first()
        assertTrue(state is ConnectionState.Connected)
    }

    @Test
    fun `onFailure maps obvious network failure to user-friendly message`() = runTest {
        val mockDataSource = mockk<MockStockPriceDataSource>()
        val client = mockk<OkHttpClient>()
        val socket = mockk<WebSocket>(relaxed = true)
        val listeners = mutableListOf<WebSocketListener>()

        every { mockDataSource.generatePriceUpdates() } returns emptyFlow()
        every { client.newWebSocket(any(), any()) } answers {
            listeners += secondArg<WebSocketListener>()
            socket
        }

        val dataSource = WebSocketDataSourceImpl(mockDataSource, client, this)
        val errorStateDeferred = async {
            dataSource.observeConnectionStatus()
                .filterIsInstance<ConnectionState.Error>()
                .first()
        }
        val connectJob = launch { dataSource.connect() }
        advanceUntilIdle()

        // Disable reconnect path for this test, so runTest has no long-lived jobs.
        dataSource.disconnect()
        listeners.single().onFailure(socket, UnknownHostException("No network"), null)
        connectJob.join()
        val errorState = errorStateDeferred.await()

        assertEquals("No internet connection. Check your network and try again.", errorState.message)
    }

    @Test
    fun `unexpected onClosed triggers reconnect scheduling`() = runTest {
        val mockDataSource = mockk<MockStockPriceDataSource>()
        val client = mockk<OkHttpClient>()
        val socket = mockk<WebSocket>(relaxed = true)
        val response = mockk<Response>(relaxed = true)
        val listeners = mutableListOf<WebSocketListener>()

        every { mockDataSource.generatePriceUpdates() } returns emptyFlow()
        every { client.newWebSocket(any(), any()) } answers {
            listeners += secondArg<WebSocketListener>()
            socket
        }

        val dataSource = WebSocketDataSourceImpl(mockDataSource, client, this)
        val connectJob = launch { dataSource.connect() }
        advanceUntilIdle()
        listeners[0].onOpen(socket, response)
        connectJob.join()

        listeners[0].onClosed(socket, 1006, "abnormal")
        advanceTimeBy(3_100L)
        advanceUntilIdle()

        verify(exactly = 2) { client.newWebSocket(any(), any()) }

        // Cleanup suspended reconnect connect attempt in test scope.
        dataSource.disconnect()
    }

    @Test
    fun `disconnect sets disconnected and does not reconnect afterwards`() = runTest {
        val mockDataSource = mockk<MockStockPriceDataSource>()
        val client = mockk<OkHttpClient>()
        val socket = mockk<WebSocket>(relaxed = true)
        val response = mockk<Response>(relaxed = true)
        val listeners = mutableListOf<WebSocketListener>()

        every { mockDataSource.generatePriceUpdates() } returns emptyFlow()
        every { client.newWebSocket(any(), any()) } answers {
            listeners += secondArg<WebSocketListener>()
            socket
        }
        every { socket.close(any(), any()) } returns true

        val dataSource = WebSocketDataSourceImpl(mockDataSource, client, this)
        val connectJob = launch { dataSource.connect() }
        advanceUntilIdle()
        listeners[0].onOpen(socket, response)
        connectJob.join()

        dataSource.disconnect()
        val stateAfterDisconnect = dataSource.observeConnectionStatus().first()
        assertTrue(stateAfterDisconnect is ConnectionState.Disconnected)

        advanceTimeBy(4_000L)
        advanceUntilIdle()
        verify(exactly = 1) { client.newWebSocket(any(), any()) }
    }
}
