import SwiftUI

struct SaintListView: View {
    @Bindable var viewModel: SaintListViewModel

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.saints.isEmpty {
                    ContentUnavailableView(
                        String(localized: "No Saints Found"),
                        systemImage: "person.crop.circle.badge.questionmark",
                        description: Text(String(localized: "Saints data is loading..."))
                    )
                } else if viewModel.filteredSaints.isEmpty {
                    ContentUnavailableView.search(text: viewModel.searchText)
                } else {
                    List(viewModel.filteredSaints) { saint in
                        NavigationLink(value: saint) {
                            SaintRowView(saint: saint)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle(String(localized: "Saints"))
            .searchable(text: $viewModel.searchText,
                        prompt: String(localized: "Search saints..."))
            .navigationDestination(for: Saint.self) { saint in
                SaintDetailView(saint: saint)
            }
        }
    }
}

#Preview {
    SaintListView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
