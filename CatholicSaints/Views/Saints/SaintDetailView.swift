import SwiftUI

struct SaintDetailView: View {
    let saint: Saint

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                headerSection
                quoteSection
                whySection
                biographySection
                detailsSection
                patronSection
                tagsSection
                sourcesSection
            }
            .padding()
        }
        .navigationTitle(saint.name)
        .navigationBarTitleDisplayMode(.large)
    }

    // MARK: - Header

    @ViewBuilder
    private var headerSection: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(colorForSaint(saint).gradient)
                    .frame(width: 100, height: 100)
                Text(String(saint.name.prefix(1)))
                    .font(.system(size: 44, weight: .bold))
                    .foregroundStyle(.white)
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
                    Label(lifeState.capitalized, systemImage: "person.fill")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }

            if saint.isYoung {
                Label(String(localized: "Young Saint"), systemImage: "sparkles")
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
    private var quoteSection: some View {
        if let quote = saint.quote {
            VStack(spacing: 8) {
                Image(systemName: "quote.opening")
                    .font(.title2)
                    .foregroundStyle(.purple.opacity(0.6))
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
            .background(.purple.opacity(0.06))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Why This Saint

    @ViewBuilder
    private var whySection: some View {
        if let why = saint.whyConfirmationSaint {
            VStack(alignment: .leading, spacing: 8) {
                Label(String(localized: "Why Choose This Saint?"), systemImage: "heart.fill")
                    .font(.title3.bold())
                    .foregroundStyle(.purple)
                Text(why)
                    .font(.body)
            }
            .padding()
            .background(.purple.opacity(0.04))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    // MARK: - Biography

    @ViewBuilder
    private var biographySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(String(localized: "Biography"), systemImage: "book.fill")
                .font(.title3.bold())
            Text(saint.biography)
                .font(.body)
        }
    }

    // MARK: - Details

    @ViewBuilder
    private var detailsSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Label(String(localized: "Details"), systemImage: "info.circle.fill")
                .font(.title3.bold())

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                if let birth = saint.birthDate {
                    detailCard(label: String(localized: "Born"), value: birth, icon: "calendar")
                }
                if let death = saint.deathDate {
                    detailCard(label: String(localized: "Died"), value: death, icon: "calendar.badge.clock")
                }
                if let canon = saint.canonizationDate {
                    detailCard(label: String(localized: "Canonized"), value: canon, icon: "star.fill")
                }
                if let region = saint.region {
                    detailCard(label: String(localized: "Region"), value: region, icon: "map.fill")
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
    private var patronSection: some View {
        if !saint.patronOf.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(String(localized: "Patron Of"), systemImage: "shield.fill")
                    .font(.title3.bold())
                FlowLayout(spacing: 8) {
                    ForEach(saint.patronOf, id: \.self) { patron in
                        Text(patron.capitalized)
                            .font(.subheadline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(.purple.opacity(0.1))
                            .clipShape(Capsule())
                    }
                }
            }
        }
    }

    // MARK: - Tags

    @ViewBuilder
    private var tagsSection: some View {
        if !saint.affinities.isEmpty || !saint.tags.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(String(localized: "Interests & Tags"), systemImage: "tag.fill")
                    .font(.title3.bold())
                FlowLayout(spacing: 8) {
                    ForEach(saint.affinities + saint.tags, id: \.self) { tag in
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
    private var sourcesSection: some View {
        if !saint.sources.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                Label(String(localized: "Sources"), systemImage: "doc.text.fill")
                    .font(.title3.bold())
                ForEach(saint.sources, id: \.self) { source in
                    Text(source)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private func colorForSaint(_ saint: Saint) -> Color {
        let colors: [Color] = [.purple, .blue, .indigo, .teal, .pink, .orange, .mint, .cyan]
        let index = abs(saint.id.hashValue) % colors.count
        return colors[index]
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
