import SwiftUI

struct SaintListView: View {
    @State private var viewModel = SaintListViewModel()

    var body: some View {
        NavigationStack {
            List(viewModel.filteredSaints) { saint in
                NavigationLink(value: saint) {
                    SaintRowView(saint: saint)
                }
            }
            .navigationTitle(String(localized: "Saints"))
            .searchable(text: $viewModel.searchText,
                        prompt: String(localized: "Search saints..."))
            .navigationDestination(for: Saint.self) { saint in
                SaintDetailView(saint: saint)
            }
            .onAppear {
                viewModel.loadData()
            }
        }
    }
}

#Preview {
    SaintListView()
}
