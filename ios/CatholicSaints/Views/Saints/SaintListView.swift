import SwiftUI

struct SaintListView: View {
    @Bindable var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.saints.isEmpty {
                    ContentUnavailableView(
                        AppStrings.localized("No Saints Found", language: language),
                        systemImage: "person.crop.circle.badge.questionmark",
                        description: Text(AppStrings.localized("Saints data is loading...", language: language))
                    )
                } else if viewModel.filteredSaints.isEmpty {
                    ContentUnavailableView.search(text: viewModel.searchText)
                } else {
                    List(viewModel.filteredSaints) { saint in
                        NavigationLink(value: saint.id) {
                            SaintRowView(saint: saint)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle(AppStrings.localized("Saints", language: language))
            .searchable(text: $viewModel.searchText,
                        prompt: AppStrings.localized("Search saints...", language: language))
            .navigationDestination(for: String.self) { saintId in
                SaintDetailView(saintId: saintId, viewModel: viewModel)
            }
        }
    }
}

#Preview {
    SaintListView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
