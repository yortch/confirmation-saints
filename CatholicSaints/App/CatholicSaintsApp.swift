import SwiftUI

@main
struct CatholicSaintsApp: App {
    @AppStorage("appLanguage") private var appLanguage = "en"

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(\.appLanguage, appLanguage)
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
