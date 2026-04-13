import SwiftUI

/// Determines the default app language based on the iOS system locale.
let systemDefaultLanguage: String = {
    if Locale.current.language.languageCode?.identifier == "es" {
        return "es"
    }
    return "en"
}()

@main
struct CatholicSaintsApp: App {
    @AppStorage("appLanguage") private var appLanguage = systemDefaultLanguage
    @AppStorage("hasSeenWelcome") private var hasSeenWelcome = false

    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            ZStack {
                if hasSeenWelcome {
                    ContentView()
                        .environment(\.appLanguage, appLanguage)
                } else {
                    WelcomeView()
                        .environment(\.appLanguage, appLanguage)
                }
            }
            .overlay {
                if showSplash {
                    SplashView()
                        .transition(.opacity)
                        .zIndex(1)
                }
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    withAnimation(.easeOut(duration: 0.5)) {
                        showSplash = false
                    }
                }
            }
        }
    }
}

// MARK: - Language Environment Key

private struct AppLanguageKey: EnvironmentKey {
    static let defaultValue: String = "en"
}

extension EnvironmentValues {
    var appLanguage: String {
        get { self[AppLanguageKey.self] }
        set { self[AppLanguageKey.self] = newValue }
    }
}
