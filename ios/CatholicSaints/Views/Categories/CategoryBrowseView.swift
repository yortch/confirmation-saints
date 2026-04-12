import SwiftUI

struct CategoryBrowseView: View {
    @Bindable var viewModel: SaintListViewModel

    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 24) {
                    ForEach(viewModel.categories) { group in
                        categoryGroupSection(group)
                    }
                }
                .padding()
            }
            .navigationTitle(String(localized: "Explore"))
            .navigationDestination(for: Saint.self) { saint in
                SaintDetailView(saint: saint)
            }
        }
    }

    @ViewBuilder
    private func categoryGroupSection(_ group: CategoryGroup) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(group.name, systemImage: group.icon)
                .font(.title3.bold())
                .foregroundStyle(.primary)

            Text(group.description)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 10),
                GridItem(.flexible(), spacing: 10)
            ], spacing: 10) {
                ForEach(group.values) { value in
                    let matchCount = viewModel.saints(forCategoryGroup: group.id, valueId: value.id).count
                    NavigationLink {
                        CategorySaintsListView(
                            title: value.label,
                            saints: viewModel.saints(forCategoryGroup: group.id, valueId: value.id)
                        )
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(value.label)
                                    .font(.subheadline.bold())
                                    .foregroundStyle(.primary)
                                    .multilineTextAlignment(.leading)
                                Text("\(matchCount) \(matchCount == 1 ? String(localized: "saint") : String(localized: "saints"))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.caption)
                                .foregroundStyle(.tertiary)
                        }
                        .padding(12)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
            }
        }
    }
}

struct CategorySaintsListView: View {
    let title: String
    let saints: [Saint]

    var body: some View {
        Group {
            if saints.isEmpty {
                ContentUnavailableView(
                    String(localized: "No Saints Found"),
                    systemImage: "person.crop.circle.badge.questionmark",
                    description: Text(String(localized: "No saints match this category."))
                )
            } else {
                List(saints) { saint in
                    NavigationLink(value: saint) {
                        SaintRowView(saint: saint)
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(for: Saint.self) { saint in
            SaintDetailView(saint: saint)
        }
    }
}

#Preview {
    CategoryBrowseView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
