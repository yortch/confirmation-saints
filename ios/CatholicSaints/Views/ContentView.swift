import SwiftUI

struct ContentView: View {
    @Environment(\.appLanguage) private var language
    @State private var viewModel = SaintListViewModel()

    var body: some View {
        TabView {
            SaintListView(viewModel: viewModel)
                .tabItem {
                    Label(String(localized: "Saints"), systemImage: "person.3.fill")
                }

            CategoryBrowseView(viewModel: viewModel)
                .tabItem {
                    Label(String(localized: "Explore"), systemImage: "square.grid.2x2.fill")
                }

            SearchView(viewModel: viewModel)
                .tabItem {
                    Label(String(localized: "Search"), systemImage: "magnifyingglass")
                }

            AboutConfirmationView(viewModel: viewModel)
                .tabItem {
                    Label(String(localized: "About"), systemImage: "book.fill")
                }

            SettingsView()
                .tabItem {
                    Label(String(localized: "Settings"), systemImage: "gearshape.fill")
                }
        }
        .tint(.purple)
        .onAppear {
            viewModel.loadData(language: language)
        }
        .onChange(of: language) { _, newLang in
            viewModel.loadData(language: newLang)
        }
    }
}

#Preview {
    ContentView()
        .environment(\.appLanguage, "en")
}
