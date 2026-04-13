import SwiftUI

struct CategoryBrowseView: View {
    @Bindable var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

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
            .navigationTitle(AppStrings.localized("Explore", language: language))
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
                            groupId: group.id,
                            valueId: value.id,
                            viewModel: viewModel
                        )
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(value.label)
                                    .font(.subheadline.bold())
                                    .foregroundStyle(.primary)
                                    .multilineTextAlignment(.leading)
                                Text("\(matchCount) \(matchCount == 1 ? AppStrings.localized("saint", language: language) : AppStrings.localized("saints", language: language))")
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
    let groupId: String
    let valueId: String
    var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

    private var saints: [Saint] {
        viewModel.saints(forCategoryGroup: groupId, valueId: valueId)
    }

    var body: some View {
        Group {
            if saints.isEmpty {
                ContentUnavailableView(
                    AppStrings.localized("No Saints Found", language: language),
                    systemImage: "person.crop.circle.badge.questionmark",
                    description: Text(AppStrings.localized("No saints match this category.", language: language))
                )
            } else {
                List(saints) { saint in
                    NavigationLink {
                        SaintDetailView(saintId: saint.id, viewModel: viewModel)
                    } label: {
                        SaintRowView(saint: saint)
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}

#Preview {
    CategoryBrowseView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
