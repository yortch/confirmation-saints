import SwiftUI

struct SettingsView: View {
    @Bindable var viewModel: SaintListViewModel
    @AppStorage("appLanguage") private var appLanguage = systemDefaultLanguage
    @AppStorage("hasSeenWelcome") private var hasSeenWelcome = true
    @Environment(\.appLanguage) private var language

    var body: some View {
        NavigationStack {
            List {
                // Language Section
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

                // About Section
                Section {
                    HStack {
                        Text(AppStrings.localized("Version", language: language))
                        Spacer()
                        Text("0.1.0")
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

                // Welcome Screen Section
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

                // Content Sources
                Section {
                    ForEach(contentSources, id: \.self) { source in
                        Label(source, systemImage: "doc.text.fill")
                            .font(.subheadline)
                    }
                } header: {
                    Label(AppStrings.localized("Content Sources", language: language), systemImage: "book.fill")
                } footer: {
                    Text(AppStrings.localized("Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution.", language: language))
                }
            }
            .navigationTitle(AppStrings.localized("Settings", language: language))
        }
    }

    private var contentSources: [String] {
        ["Loyola Press", "Focus", "Lifeteen", "Ascension Press", "Hallow", "Catholic Encyclopedia"]
    }
}

#Preview {
    SettingsView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
