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

    var body: some Scene {
        WindowGroup {
            if hasSeenWelcome {
                ContentView()
                    .environment(\.appLanguage, appLanguage)
            } else {
                WelcomeView()
                    .environment(\.appLanguage, appLanguage)
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
