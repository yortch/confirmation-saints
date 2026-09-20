import XCTest
@testable import CatholicSaints

/// Coverage for the two contract points Gandalf flagged as missing from the
/// original `AppStoreReviewTests` revision:
///
///  1. The "Rate & Review" section must sit between "App Info" and
///     "Onboarding" in Settings (`Language → App Info → Rate & Review →
///     Onboarding → Support & Legal → Content Sources`).
///  2. The rate action's accessibility label must explicitly name the App
///     Store destination, not just say "Rate".
///
/// `SettingsView.body` renders sections by iterating
/// `SettingsSectionKind.allCases` (see `SettingsView.swift`), so this is not
/// a parallel/duplicated ordering list that could silently drift from the
/// real UI — asserting the enum's case order *is* asserting the rendered
/// section order.
final class SettingsRateReviewContractTests: XCTestCase {

    // MARK: - Section ordering

    func test_settingsSectionOrder_matchesFullContractSequence() {
        XCTAssertEqual(
            SettingsSectionKind.allCases,
            [
                .language,
                .appInfo,
                .rateAndReview,
                .onboarding,
                .supportAndLegal,
                .contentSources
            ],
            "Settings sections must render in the exact contract order: " +
            "Language → App Info → Rate & Review → Onboarding → Support & Legal → Content Sources."
        )
    }

    func test_rateAndReviewSection_isPlacedBetweenAppInfoAndOnboarding() {
        let order = SettingsSectionKind.allCases
        guard
            let appInfoIndex = order.firstIndex(of: .appInfo),
            let rateAndReviewIndex = order.firstIndex(of: .rateAndReview),
            let onboardingIndex = order.firstIndex(of: .onboarding)
        else {
            return XCTFail("Expected App Info, Rate & Review, and Onboarding to all be present in SettingsSectionKind.allCases.")
        }

        XCTAssertTrue(
            appInfoIndex < rateAndReviewIndex,
            "Rate & Review must render after App Info."
        )
        XCTAssertTrue(
            rateAndReviewIndex < onboardingIndex,
            "Rate & Review must render before Onboarding."
        )
        XCTAssertEqual(
            rateAndReviewIndex,
            appInfoIndex + 1,
            "Rate & Review must be the section immediately following App Info, with no section in between."
        )
        XCTAssertEqual(
            onboardingIndex,
            rateAndReviewIndex + 1,
            "Onboarding must be the section immediately following Rate & Review, with no section in between."
        )
    }

    func test_rateAndReviewSection_hasStableAccessibilityIdentifier() {
        // Used to locate the section in UI automation independent of
        // localized header text.
        XCTAssertEqual(SettingsSectionKind.rateAndReview.accessibilityIdentifier, "settings.section.rateAndReview")
    }

    // MARK: - Accessibility label naming the App Store

    func test_rateAction_accessibilityLabel_namesAppStore_english() {
        let label = AppStrings.localized("Rate Confirmation Saints in the App Store", language: "en")

        XCTAssertTrue(
            label.contains("App Store"),
            "Rate action's accessibility label must explicitly name the App Store, not just 'Rate'. Got: \"\(label)\""
        )
        XCTAssertNotEqual(
            label,
            AppStrings.localized("Rate Confirmation Saints", language: "en"),
            "The accessibility label must be more descriptive than the visible row label — it must name the destination."
        )
    }

    func test_rateAction_accessibilityLabel_namesAppStore_spanish() {
        let label = AppStrings.localized("Rate Confirmation Saints in the App Store", language: "es")

        XCTAssertTrue(
            label.contains("App Store"),
            "Spanish accessibility label must also explicitly name the App Store. Got: \"\(label)\""
        )
    }

    func test_rateAndReviewSectionHeader_labelMatchesContract_bothLanguages() {
        XCTAssertEqual(AppStrings.localized("Rate & Review", language: "en"), "Rate & Review")
        XCTAssertEqual(AppStrings.localized("Rate & Review", language: "es"), "Calificar y Reseñar")
    }
}
