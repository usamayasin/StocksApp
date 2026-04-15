# Stocks App

Real-time StocksApp built with Jetpack Compose. The app streams mock market updates over WebSocket, shows a live sorted list, and supports symbol-level detail navigation (including deep links).

## Project Summary

This project demonstrates an end-to-end Android architecture for real-time UI:

- Live stream ingestion through a WebSocket transport.
- Domain mapping and business-state handling in ViewModels.
- Reactive Compose screens for list and detail experiences.
- Light/dark theme support and edge-to-edge UI.

## Feature Set

- Stocks list with continuous price updates.
- Sorted list rendering by latest price.
- Price movement indicators (up/down/unchanged).
- Stock detail screen for a selected symbol.
- Manual start/stop feed control.
- Connection-state feedback and error messaging.
- In-app light/dark theme toggle.
- Type-safe navigation with deep-link support.

## Screenshots and Demo
<img width="208" alt="Screenshot_20260415_203519" src="https://github.com/user-attachments/assets/4627a606-6a60-433d-8864-3bd42068268e" />
<img width="208" alt="Screenshot_20260415_203536" src="https://github.com/user-attachments/assets/d2b8bcd4-2fb8-4f62-a331-ba72103ec3f3" />
<img width="208"  alt="Screenshot_20260415_203544" src="https://github.com/user-attachments/assets/84b0e3ff-a533-4c12-8862-0d7bb77779cb" />
<img width="208" alt="Screenshot_20260415_203551" src="https://github.com/user-attachments/assets/9ff3d90f-d150-454e-8d34-8f0d5aef9e81" />

https://github.com/user-attachments/assets/482f15d5-210a-4c62-9edd-47970f772a7a



## Architecture

The project is organized by responsibility (`data`, `domain`, `ui`) inside a single app module.

- `data`: transport, DTOs, mappers, repository implementation.
- `domain`: models, repository contract, and use cases.
- `ui`: Compose screens/components, navigation, theming, ViewModels.

### Data flow

1. `MockStockPriceDataSource` generates batches of stock updates.
2. `WebSocketDataSourceImpl` sends updates over `wss://ws.postman-echo.com/raw` and receives echoed messages.
3. Incoming wire payloads are parsed into DTOs and emitted as flows.
4. `StocksRepositoryImpl` maps DTOs to domain models.
5. ViewModels observe flows and expose UI state to Compose.

### Data contract and API summary

- Transport: WebSocket (`wss://ws.postman-echo.com/raw`).
- Payload contract: symbol, company name, logo URL, price, previous price, timestamp.
- Domain conversion derives change and movement direction for UI rendering.

To switch from mock/echo transport to a real provider, minimal changes are:

1. Replace the WebSocket URL with the real endpoint.
2. Update the wire parsing/serialization to match provider schema.
3. Keep DTO-to-domain mapping aligned with the provider payload.

## Tech Stack and Libraries

- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Navigation: Navigation Compose (typed routes)
- DI: Hilt
- Async/State: Kotlin Coroutines + Flow
- Networking: OkHttp WebSocket
- Serialization: Kotlinx Serialization
- Image loading: Coil
- Testing: JUnit4, MockK, Coroutines Test
- CI: GitHub Actions (unit tests on push to `main`)

## Deep linking

This app opens the symbol details destination from `stocks://symbol/{SYMBOL}`.

- Example: `stocks://symbol/AAPL`
- Manifest intent filter is configured on `MainActivity`.
- Navigation route is registered with `navDeepLink { uriPattern = "stocks://symbol/{symbol}" }`.

ADB example:

```bash
adb shell am start -a android.intent.action.VIEW -d "stocks://symbol/NVDA" com.example.stocksapp
```

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 17

### Quick start

```bash
git clone <your-repo-url>
cd StocksApp
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Or open the project in Android Studio and run the `app` configuration.

## Improvements

- Move to multi-module architecture (`core`, `feature-*`, `data-*`) for clearer ownership and faster incremental builds. For this assignment scope, a single module kept delivery lean, but current package boundaries are already prepared for modularization.
- Add Compose UI tests for critical journeys (list rendering, navigation to detail, theme toggle, deep-link entry).
- Add offline-first persistence (for example Room cache + last known quote) to reduce perceived loading delay and improve resiliency during network drops.
