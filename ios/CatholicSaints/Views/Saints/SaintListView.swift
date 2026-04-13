import SwiftUI

struct SaintListView: View {
    @Bindable var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if viewModel.hasActiveFilters {
                    activeFiltersBar
                }

                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        FilterChip(
                            title: AppStrings.localized("Young Saints", language: language),
                            systemImage: "sparkles",
                            isActive: viewModel.selectedAgeCategory == "young"
                        ) {
                            viewModel.selectedAgeCategory = viewModel.selectedAgeCategory == "young" ? nil : "young"
                        }

                        FilterChip(
                            title: AppStrings.localized("Female Saints", language: language),
                            systemImage: "person.fill",
                            isActive: viewModel.selectedGender == "female"
                        ) {
                            viewModel.selectedGender = viewModel.selectedGender == "female" ? nil : "female"
                        }

                        FilterChip(
                            title: AppStrings.localized("Male Saints", language: language),
                            systemImage: "person.fill",
                            isActive: viewModel.selectedGender == "male"
                        ) {
                            viewModel.selectedGender = viewModel.selectedGender == "male" ? nil : "male"
                        }

                        ForEach(["married", "religious", "single"], id: \.self) { state in
                            FilterChip(
                                title: AppStrings.localized(state.capitalized, language: language),
                                systemImage: iconForLifeState(state),
                                isActive: viewModel.selectedLifeState == state
                            ) {
                                viewModel.selectedLifeState = viewModel.selectedLifeState == state ? nil : state
                            }
                        }

                        ForEach(["Europe", "Latin America", "North America", "Africa", "Asia"], id: \.self) { region in
                            FilterChip(
                                title: AppStrings.localized(region, language: language),
                                systemImage: "globe",
                                isActive: viewModel.selectedRegion == region.lowercased()
                            ) {
                                let key = region.lowercased()
                                viewModel.selectedRegion = viewModel.selectedRegion == key ? nil : key
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

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
            }
            .navigationTitle(AppStrings.localized("Saints", language: language))
            .searchable(text: $viewModel.searchText,
                        prompt: AppStrings.localized("Name, interest, country...", language: language))
            .navigationDestination(for: String.self) { saintId in
                SaintDetailView(saintId: saintId, viewModel: viewModel)
            }
        }
    }

    @ViewBuilder
    private var activeFiltersBar: some View {
        HStack {
            Text("\(viewModel.filteredSaints.count) \(AppStrings.localized("results", language: language))")
                .font(.caption)
                .foregroundStyle(.secondary)
            Spacer()
            Button(AppStrings.localized("Clear All", language: language)) {
                viewModel.clearFilters()
            }
            .font(.caption.bold())
            .foregroundStyle(.purple)
        }
        .padding(.horizontal)
        .padding(.vertical, 6)
        .background(Color(.systemGray6))
    }

    private func iconForLifeState(_ state: String) -> String {
        switch state {
        case "married": return "heart.fill"
        case "religious": return "cross.fill"
        case "single": return "person"
        default: return "person"
        }
    }
}

#Preview {
    SaintListView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
