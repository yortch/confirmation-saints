import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            SaintListView()
                .tabItem {
                    Label(String(localized: "Saints"), systemImage: "person.3.fill")
                }

            SearchView()
                .tabItem {
                    Label(String(localized: "Search"), systemImage: "magnifyingglass")
                }

            AboutConfirmationView()
                .tabItem {
                    Label(String(localized: "About"), systemImage: "book.fill")
                }
        }
    }
}

#Preview {
    ContentView()
}
