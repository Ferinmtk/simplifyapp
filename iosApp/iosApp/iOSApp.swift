import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        // Bootstrap Kotlin-side DI before any Compose UI is created.
        // Mirrors what Android's SimplifyBizApplication.onCreate does.
        KoinIosKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}