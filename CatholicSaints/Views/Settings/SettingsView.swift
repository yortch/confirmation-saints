import SwiftUI

struct SettingsView: View {
    @AppStorage("appLanguage") private var appLanguage = "en"

    var body: some View {
        NavigationStack {
            List {
                // Language Section
                Section {
                    Picker(String(localized: "Language"), selection: $appLanguage) {
                        Text("English").tag("en")
                        Text("Español").tag("es")
                    }
                    .pickerStyle(.inline)
                } header: {
                    Label(String(localized: "Language"), systemImage: "globe")
                } footer: {
                    Text(String(localized: "Changing the language updates all saint content and app text."))
                }

                // About Section
                Section {
                    HStack {
                        Text(String(localized: "Version"))
                        Spacer()
                        Text("0.1.0")
                            .foregroundStyle(.secondary)
                    }
                    HStack {
                        Text(String(localized: "Saints Included"))
                        Spacer()
                        Text("25")
                            .foregroundStyle(.secondary)
                    }
                    HStack {
                        Text(String(localized: "Languages"))
                        Spacer()
                        Text(String(localized: "English, Spanish"))
                            .foregroundStyle(.secondary)
                    }
                } header: {
                    Label(String(localized: "App Info"), systemImage: "info.circle")
                }

                // Content Sources
                Section {
                    ForEach(contentSources, id: \.self) { source in
                        Label(source, systemImage: "doc.text.fill")
                            .font(.subheadline)
                    }
                } header: {
                    Label(String(localized: "Content Sources"), systemImage: "book.fill")
                } footer: {
                    Text(String(localized: "Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution."))
                }
            }
            .navigationTitle(String(localized: "Settings"))
        }
    }

    private var contentSources: [String] {
        ["Loyola Press", "Focus", "Lifeteen", "Ascension Press", "Hallow", "Catholic Encyclopedia"]
    }
}

#Preview {
    SettingsView()
        .environment(\.appLanguage, "en")
}
