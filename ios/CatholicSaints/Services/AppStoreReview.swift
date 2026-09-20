import SwiftUI

/// Centralized App Store identifiers so the numeric App ID is a one-line
/// change if the app is ever re-listed under a different ID.
enum AppStoreConfig {
    /// Numeric App Store ID for Confirmation Saints.
    static let appID = "6762463641"
}

/// Builds and opens the App Store "Rate & Review" destination.
///
/// Intentionally avoids `SKStoreReviewController`/`AppStore.requestReview`
/// for this explicit, user-initiated action: that API is throttled by the OS
/// (~3 prompts/365 days, no guarantee of display) and is meant for organic
/// in-flow moments, not a deliberate "Rate Us" tap. Instead this opens the
/// direct App Store write-review deep link, which always routes to the App
/// Store (or Safari as a fallback destination in environments without the
/// App Store app installed, such as simulators).
enum AppStoreReview {
    /// Direct deep link that opens the write-a-review flow for this app.
    static var writeReviewURL: URL {
        URL(string: "https://apps.apple.com/app/id\(AppStoreConfig.appID)?action=write-review")!
    }

    /// Generic App Store search fallback used if the direct write-review
    /// link fails to open (e.g. malformed/future ID rotation).
    static var fallbackSearchURL: URL {
        URL(string: "https://apps.apple.com/search?term=Confirmation%20Saints")!
    }

    /// Opens the write-review destination using the environment's
    /// `OpenURLAction`, falling back to a generic App Store search if the
    /// primary URL fails to open. Never silently no-ops: the fallback is
    /// always attempted when the primary open is rejected.
    @MainActor
    static func open(using openURL: OpenURLAction) {
        openURL(writeReviewURL) { accepted in
            if !accepted {
                openURL(fallbackSearchURL)
            }
        }
    }
}
