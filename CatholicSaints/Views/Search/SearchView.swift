import SwiftUI

struct SearchView: View {
    @State private var viewModel = SaintListViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Filter chips
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        FilterChip(
                            title: String(localized: "Young Saints"),
                            isActive: viewModel.showYoungSaintsOnly
                        ) {
                            viewModel.showYoungSaintsOnly.toggle()
                        }

                        FilterChip(
                            title: String(localized: "Married Saints"),
                            isActive: viewModel.showMarriedSaintsOnly
                        ) {
                            viewModel.showMarriedSaintsOnly.toggle()
                        }

                        ForEach(Affinity.allCases, id: \.self) { affinity in
                            FilterChip(
                                title: affinity.localizedName,
                                systemImage: affinity.systemImage,
                                isActive: viewModel.selectedAffinity == affinity
                            ) {
                                if viewModel.selectedAffinity == affinity {
                                    viewModel.selectedAffinity = nil
                                } else {
                                    viewModel.selectedAffinity = affinity
                                }
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

                List(viewModel.filteredSaints) { saint in
                    NavigationLink(value: saint) {
                        SaintRowView(saint: saint)
                    }
                }
                .navigationDestination(for: Saint.self) { saint in
                    SaintDetailView(saint: saint)
                }
            }
            .navigationTitle(String(localized: "Find Your Saint"))
            .searchable(text: $viewModel.searchText,
                        prompt: String(localized: "Name, interest, country..."))
            .onAppear {
                viewModel.loadData()
            }
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
            .background(isActive ? Color.accentColor : Color(.systemGray5))
            .foregroundStyle(isActive ? .white : .primary)
            .clipShape(Capsule())
        }
    }
}

#Preview {
    SearchView()
}
