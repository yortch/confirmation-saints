import SwiftUI
import XCTest
@testable import CatholicSaints

/// Unit coverage for the Rate & Review App Store helpers, per Gandalf's
/// Rate & Review Settings contract.
///
/// Contract under test:
///  - The write-review deep link is built from the centralized
///    `AppStoreConfig.appID` constant (no inlined numeric ID in the view).
///  - A generic App Store search URL is available as the fallback if the
///    direct write-review link fails to open.
///  - `AppStoreReview.open(using:)` always attempts the fallback when the
///    primary open is rejected — it never silently no-ops.
final class AppStoreReviewTests: XCTestCase {

    func test_writeReviewURL_isBuiltFromCentralizedAppID() {
        let url = AppStoreReview.writeReviewURL

        XCTAssertEqual(
            url.absoluteString,
            "https://apps.apple.com/app/id\(AppStoreConfig.appID)?action=write-review",
            "Write-review URL must be derived from AppStoreConfig.appID, not an inlined literal."
        )
        XCTAssertTrue(url.absoluteString.contains(AppStoreConfig.appID))
        XCTAssertEqual(url.scheme, "https")
        XCTAssertEqual(url.host, "apps.apple.com")
    }

    func test_fallbackSearchURL_isGenericAppStoreSearch() {
        let url = AppStoreReview.fallbackSearchURL

        XCTAssertEqual(
            url.absoluteString,
            "https://apps.apple.com/search?term=Confirmation%20Saints"
        )
        XCTAssertEqual(url.scheme, "https")
        XCTAssertEqual(url.host, "apps.apple.com")
    }

    func test_writeReviewURL_and_fallbackSearchURL_areDistinct() {
        // The fallback must be a genuinely different destination, not a
        // duplicate of the primary write-review link.
        XCTAssertNotEqual(AppStoreReview.writeReviewURL, AppStoreReview.fallbackSearchURL)
    }

    @MainActor
    func test_open_fallsBackToSearchURL_whenPrimaryOpenIsRejected() {
        var openedURLs: [URL] = []
        let openURL = OpenURLAction { url in
            openedURLs.append(url)
            return .discarded
        }

        AppStoreReview.open(using: openURL)

        XCTAssertEqual(
            openedURLs,
            [AppStoreReview.writeReviewURL, AppStoreReview.fallbackSearchURL],
            "When the primary write-review URL fails to open, the fallback search URL must always be attempted next."
        )
    }

    @MainActor
    func test_open_doesNotAttemptFallback_whenPrimaryOpenSucceeds() {
        var openedURLs: [URL] = []
        let openURL = OpenURLAction { url in
            openedURLs.append(url)
            return .handled
        }

        AppStoreReview.open(using: openURL)

        XCTAssertEqual(openedURLs, [AppStoreReview.writeReviewURL])
    }
}
