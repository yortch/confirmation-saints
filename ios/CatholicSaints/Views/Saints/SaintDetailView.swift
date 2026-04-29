import SwiftUI

struct SaintDetailView: View {
    let saintId: String
    var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language
    @State private var isShowingImagePreview = false

    /// Reactively looks up the saint from the viewModel's current (language-appropriate) data.
    private var saint: Saint? {
        viewModel.saints.first { $0.id == saintId }
    }

    var body: some View {
        Group {
            if let saint {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        headerSection(saint)
                        quoteSection(saint)
                        whySection(saint)
                        biographySection(saint)
                        detailsSection(saint)
                        patronSection(saint)
                        tagsSection(saint)
                        sourcesSection(saint)
                    }
                    .padding()
                }
                .navigationTitle(saint.name)
            } else {
                ContentUnavailableView(
                    AppStrings.localized("Content Loading...", language: language),
                    systemImage: "person.crop.circle.badge.questionmark"
                )
            }
        }
        .navigationBarTitleDisplayMode(.large)
        .sheet(isPresented: $isShowingImagePreview) {
            if let saint, let image = SaintImageView.loadImage(for: saint) {
                SaintImagePreview(saint: saint, image: image, language: language)
            }
        }
    }

    // MARK: - Header

    @ViewBuilder
    private func headerSection(_ saint: Saint) -> some View {
        VStack(spacing: 12) {
            if SaintImageView.loadImage(for: saint) != nil {
                Button {
                    isShowingImagePreview = true
                } label: {
                    VStack(spacing: 6) {
                        ZStack(alignment: .bottomTrailing) {
                            SaintImageView(saint: saint, size: 120)
                            Image(systemName: "magnifyingglass.circle.fill")
                                .font(.title2)
                                .symbolRenderingMode(.palette)
                                .foregroundStyle(.white, .blue)
                                .background(.blue, in: Circle())
                                .accessibilityHidden(true)
                        }

                        Label(AppStrings.localized("View larger image", language: language), systemImage: "arrow.up.left.and.arrow.down.right")
                            .font(.caption.bold())
                            .foregroundStyle(.blue)
                    }
                }
                .buttonStyle(.plain)
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(String(format: AppStrings.localized("View larger image of %@", language: language), saint.name))
                .accessibilityHint(AppStrings.localized("Opens a larger view of this saint image.", language: language))
            } else {
                SaintImageView(saint: saint, size: 120)
            }

            if let attribution = saint.image?.attribution {
                Text(attribution)
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }

            Text(saint.formattedFeastDay)
                .font(.title3)
                .foregroundStyle(.secondary)

            HStack(spacing: 16) {
                if let country = saint.country {
                    Label(country, systemImage: "globe")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                if let lifeState = saint.lifeState {
                    Label(AppStrings.localized(lifeState, language: language).capitalized, systemImage: "person.fill")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }

            if saint.isYoung {
                Label(AppStrings.localized("Young Saint", language: language), systemImage: "sparkles")
                    .font(.subheadline.bold())
                    .foregroundStyle(.orange)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(.orange.opacity(0.12))
                    .clipShape(Capsule())
            }
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Quote

    @ViewBuilder
    private func quoteSection(_ saint: Saint) -> some View {
        if let quote = saint.quote {
            VStack(spacing: 8) {
                Image(systemName: "quote.opening")
                    .font(.title2)
                    .foregroundStyle(.red.opacity(0.6))
                Text(quote)
                    .font(.body.italic())
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.primary.opacity(0.85))
                Text("— \(saint.name)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(.red.opacity(0.06))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Why This Saint

    @ViewBuilder
    private func whySection(_ saint: Saint) -> some View {
        if let why = saint.whyConfirmationSaint {
            VStack(alignment: .leading, spacing: 8) {
                Label(AppStrings.localized("Why Choose This Saint?", language: language), systemImage: "heart.fill")
                    .font(.title3.bold())
                    .foregroundStyle(.red)
                Text(why)
                    .font(.body)
            }
            .padding()
            .background(.red.opacity(0.04))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Biography

    @ViewBuilder
    private func biographySection(_ saint: Saint) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(AppStrings.localized("Biography", language: language), systemImage: "book.fill")
                .font(.title3.bold())
            Text(saint.biography)
                .font(.body)
        }
    }

    // MARK: - Details

    @ViewBuilder
    private func detailsSection(_ saint: Saint) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(AppStrings.localized("Details", language: language), systemImage: "info.circle.fill")
                .font(.title3.bold())

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                if let birth = saint.birthDate {
                    detailCard(label: AppStrings.localized("Born", language: language), value: SaintDateFormatter.format(birth, language: language), icon: "calendar")
                }
                if let death = saint.deathDate {
                    detailCard(label: AppStrings.localized("Died", language: language), value: SaintDateFormatter.format(death, language: language), icon: "calendar.badge.clock")
                }
                if let canon = saint.canonizationDate {
                    detailCard(label: AppStrings.localized("Canonized", language: language), value: SaintDateFormatter.format(canon, language: language), icon: "star.fill")
                }
                if let region = saint.region {
                    detailCard(label: AppStrings.localized("Region", language: language), value: AppStrings.localized(region, language: language), icon: "map.fill")
                }
            }
        }
    }

    @ViewBuilder
    private func detailCard(label: String, value: String, icon: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Label(label, systemImage: icon)
                .font(.caption.bold())
                .foregroundStyle(.secondary)
            Text(value)
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(10)
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    // MARK: - Patron Of

    @ViewBuilder
    private func patronSection(_ saint: Saint) -> some View {
        let displayValues = saint.displayPatronOf ?? saint.patronOf
        if !displayValues.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(AppStrings.localized("Patron Of", language: language), systemImage: "shield.fill")
                    .font(.title3.bold())
                FlowLayout(spacing: 8) {
                    ForEach(displayValues, id: \.self) { patron in
                        Text(patron.capitalized)
                            .font(.subheadline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(.red.opacity(0.1))
                            .clipShape(Capsule())
                    }
                }
            }
        }
    }

    // MARK: - Tags

    @ViewBuilder
    private func tagsSection(_ saint: Saint) -> some View {
        if !saint.affinities.isEmpty || !saint.tags.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(AppStrings.localized("Interests & Tags", language: language), systemImage: "tag.fill")
                    .font(.title3.bold())
                FlowLayout(spacing: 8) {
                    ForEach((saint.displayAffinities ?? saint.affinities) + (saint.displayTags ?? saint.tags), id: \.self) { tag in
                        Text(tag.capitalized)
                            .font(.caption)
                            .padding(.horizontal, 10)
                            .padding(.vertical, 5)
                            .background(.blue.opacity(0.1))
                            .clipShape(Capsule())
                    }
                }
            }
        }
    }

    // MARK: - Sources

    @ViewBuilder
    private func sourcesSection(_ saint: Saint) -> some View {
        if !saint.sources.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(AppStrings.localized("Sources", language: language), systemImage: "doc.text.fill")
                    .font(.title3.bold())
                ForEach(saint.sources, id: \.name) { source in
                    Link(destination: URL(string: source.url)!) {
                        HStack {
                            Text(source.name)
                                .font(.subheadline)
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                                .font(.caption)
                        }
                        .foregroundStyle(.blue)
                    }
                }
            }
        }
    }
}

private struct SaintImagePreview: View {
    let saint: Saint
    let image: UIImage
    let language: String
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .accessibilityLabel(saint.name)

                if let attribution = saint.image?.attribution {
                    Text(attribution)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding()
            .navigationTitle(saint.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(AppStrings.localized("Done", language: language)) {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.large])
    }
}

/// Simple flow layout for tags.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrange(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrange(proposal: proposal, subviews: subviews)
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x,
                                      y: bounds.minY + result.positions[index].y),
                          proposal: .unspecified)
        }
    }

    private func arrange(proposal: ProposedViewSize, subviews: Subviews) -> (positions: [CGPoint], size: CGSize) {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var maxX: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            rowHeight = max(rowHeight, size.height)
            x += size.width + spacing
            maxX = max(maxX, x)
        }

        return (positions, CGSize(width: maxX, height: y + rowHeight))
    }
}
