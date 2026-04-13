import SwiftUI

struct SearchView: View {
    @Bindable var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Active filters bar
                if viewModel.hasActiveFilters {
                    activeFiltersBar
                }

                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        // Age category
                        FilterChip(
                            title: AppStrings.localized("Young Saints", language: language),
                            systemImage: "sparkles",
                            isActive: viewModel.selectedAgeCategory == "young"
                        ) {
                            viewModel.selectedAgeCategory = viewModel.selectedAgeCategory == "young" ? nil : "young"
                        }

                        // Gender
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

                        // Life states
                        ForEach(["married", "religious", "single"], id: \.self) { state in
                            FilterChip(
                                title: state.capitalized,
                                systemImage: iconForLifeState(state),
                                isActive: viewModel.selectedLifeState == state
                            ) {
                                viewModel.selectedLifeState = viewModel.selectedLifeState == state ? nil : state
                            }
                        }

                        // Regions
                        ForEach(["Europe", "Latin America", "North America", "Africa", "Asia"], id: \.self) { region in
                            FilterChip(
                                title: region,
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

                // Results
                Group {
                    if viewModel.filteredSaints.isEmpty {
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
            .navigationTitle(AppStrings.localized("Find Your Saint", language: language))
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

struct FilterChip: View {
    let title: String
    var systemImage: String?
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if let systemImage {
                    Image(systemName: systemImage)
                        .font(.caption)
                }
                Text(title)
                    .font(.subheadline)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isActive ? Color.purple : Color(.systemGray5))
            .foregroundStyle(isActive ? .white : .primary)
            .clipShape(Capsule())
        }
    }
}

#Preview {
    SearchView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
