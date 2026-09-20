import SwiftUI

/// Canonical, ordered identity for each `SettingsView` section.
///
/// `allCases` (via `CaseIterable`) is the single source of truth for the
/// on-screen section order: `SettingsView.body` renders sections by
/// iterating this array, so asserting order here is equivalent to asserting
/// the rendered order — there is no separate, possibly-diverging list to
/// keep in sync. Per Gandalf's Rate & Review Settings contract, `rateAndReview`
/// must sit between `appInfo` and `onboarding`.
enum SettingsSectionKind: String, CaseIterable, Identifiable {
    case language
    case appInfo
    case rateAndReview
    case onboarding
    case supportAndLegal
    case contentSources

    var id: String { rawValue }

    /// Stable accessibility identifier for the section header, usable by
    /// UI/automation tooling without depending on localized header text.
    var accessibilityIdentifier: String { "settings.section.\(rawValue)" }
}

struct SettingsView: View {
    @Bindable var viewModel: SaintListViewModel
    @AppStorage("appLanguage") private var appLanguage = systemDefaultLanguage
    @AppStorage("hasSeenWelcome") private var hasSeenWelcome = true
    @Environment(\.appLanguage) private var language
    @Environment(\.openURL) private var openURL

    var body: some View {
        NavigationStack {
            List {
                ForEach(SettingsSectionKind.allCases) { section in
                    sectionContent(for: section)
                        .accessibilityElement(children: .contain)
                        .accessibilityIdentifier(section.accessibilityIdentifier)
                }
            }
            .navigationTitle(AppStrings.localized("Settings", language: language))
        }
    }

    @ViewBuilder
    private func sectionContent(for section: SettingsSectionKind) -> some View {
        switch section {
        case .language: languageSection
        case .appInfo: appInfoSection
        case .rateAndReview: rateAndReviewSection
        case .onboarding: onboardingSection
        case .supportAndLegal: supportAndLegalSection
        case .contentSources: contentSourcesSection
        }
    }

    private var languageSection: some View {
        Section {
            Picker(AppStrings.localized("Language", language: language), selection: $appLanguage) {
                Text("English").tag("en")
                Text("Español").tag("es")
            }
            .pickerStyle(.inline)
        } header: {
            Label(AppStrings.localized("Language", language: language), systemImage: "globe")
        } footer: {
            Text(AppStrings.localized("Changing the language updates all saint content and app text.", language: language))
        }
    }

    private var appInfoSection: some View {
        Section {
            HStack {
                Text(AppStrings.localized("Version", language: language))
                Spacer()
                Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0")
                    .foregroundStyle(.secondary)
            }
            HStack {
                Text(AppStrings.localized("Saints Included", language: language))
                Spacer()
                Text("\(viewModel.saints.count)")
                    .foregroundStyle(.secondary)
            }
            HStack {
                Text(AppStrings.localized("Languages", language: language))
                Spacer()
                Text(AppStrings.localized("English, Spanish", language: language))
                    .foregroundStyle(.secondary)
            }
        } header: {
            Label(AppStrings.localized("App Info", language: language), systemImage: "info.circle")
        }
    }

    private var rateAndReviewSection: some View {
        Section {
            Button {
                AppStoreReview.open(using: openURL)
            } label: {
                Label(AppStrings.localized("Rate Confirmation Saints", language: language), systemImage: "star.fill")
            }
            .accessibilityLabel(AppStrings.localized("Rate Confirmation Saints in the App Store", language: language))
        } header: {
            Label(AppStrings.localized("Rate & Review", language: language), systemImage: "star.fill")
        } footer: {
            Text(AppStrings.localized("Enjoying the app? A quick rating helps other families find it.", language: language))
        }
    }

    private var onboardingSection: some View {
        Section {
            Button {
                hasSeenWelcome = false
            } label: {
                Label(AppStrings.localized("Show Welcome Screen", language: language), systemImage: "sparkles")
            }
        } header: {
            Label(AppStrings.localized("Onboarding", language: language), systemImage: "hand.wave.fill")
        } footer: {
            Text(AppStrings.localized("Replay the welcome screen to revisit how the app works.", language: language))
        }
    }

    private var supportAndLegalSection: some View {
        Section {
            Link(destination: URL(string: "https://yortch.github.io/confirmation-saints/privacy-policy.html")!) {
                Label(AppStrings.localized("Privacy Policy", language: language), systemImage: "hand.raised.fill")
            }
            Link(destination: URL(string: "https://yortch.github.io/confirmation-saints/support.html")!) {
                Label(AppStrings.localized("Support", language: language), systemImage: "questionmark.circle.fill")
            }
            Link(destination: URL(string: "https://yortch.github.io/confirmation-saints/support.html")!) {
                Label(AppStrings.localized("Contact Us", language: language), systemImage: "envelope.fill")
            }
        } header: {
            Label(AppStrings.localized("Support & Legal", language: language), systemImage: "shield.fill")
        }
    }

    private var contentSourcesSection: some View {
        Section {
            ForEach(contentSourceLinks, id: \.name) { source in
                Link(destination: source.url) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(source.name)
                                .font(.subheadline)
                                .foregroundStyle(.primary)
                            Text(AppStrings.localized(source.description, language: language))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: "arrow.up.right.square")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .accessibilityLabel("\(AppStrings.localized("Open", language: language)) \(source.name) \(AppStrings.localized("in browser", language: language))")
            }
        } header: {
            Label(AppStrings.localized("Content Sources", language: language), systemImage: "book.fill")
        } footer: {
            Text(AppStrings.localized("Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution.", language: language))
        }
    }

    private var contentSourceLinks: [ContentSource] {
        [
            ContentSource(name: "Loyola Press", url: URL(string: "https://www.loyolapress.com/")!, description: "Biographical information"),
            ContentSource(name: "Focus", url: URL(string: "https://www.focus.org/")!, description: "Biographical information"),
            ContentSource(name: "Lifeteen", url: URL(string: "https://lifeteen.com/")!, description: "Biographical information"),
            ContentSource(name: "Ascension Press", url: URL(string: "https://ascensionpress.com/")!, description: "Biographical information"),
            ContentSource(name: "Hallow", url: URL(string: "https://hallow.com/")!, description: "Biographical information"),
            ContentSource(name: "Catholic Encyclopedia", url: URL(string: "https://www.newadvent.org/cathen/")!, description: "Biographical information"),
            ContentSource(name: "Wikipedia", url: URL(string: "https://en.wikipedia.org/")!, description: "Biographical information"),
            ContentSource(name: "Wikimedia Commons", url: URL(string: "https://commons.wikimedia.org/")!, description: "Public domain images")
        ]
    }
}

struct ContentSource {
    let name: String
    let url: URL
    let description: String
}

#Preview {
    SettingsView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
