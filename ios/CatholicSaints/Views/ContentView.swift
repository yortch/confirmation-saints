import SwiftUI

struct ContentView: View {
    @Environment(\.appLanguage) private var language
    @State private var viewModel = SaintListViewModel()
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            AboutConfirmationView(viewModel: viewModel)
                .tabItem {
                    Label(AppStrings.localized("About", language: language), systemImage: "book.fill")
                }
                .tag(0)

            CategoryBrowseView(viewModel: viewModel)
                .tabItem {
                    Label(AppStrings.localized("Explore", language: language), systemImage: "square.grid.2x2.fill")
                }
                .tag(1)

            SaintListView(viewModel: viewModel)
                .tabItem {
                    Label(AppStrings.localized("Saints", language: language), systemImage: "person.3.fill")
                }
                .tag(2)

            SettingsView(viewModel: viewModel)
                .tabItem {
                    Label(AppStrings.localized("Settings", language: language), systemImage: "gearshape.fill")
                }
                .tag(3)
        }
        .tint(.red)
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
