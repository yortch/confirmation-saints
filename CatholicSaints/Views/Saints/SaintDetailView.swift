import SwiftUI

struct SaintDetailView: View {
    let saint: Saint

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // Header
                headerSection

                // Biography
                biographySection

                // Patron of
                if !saint.patronOf.isEmpty {
                    patronSection
                }

                // Sources
                if !saint.sources.isEmpty {
                    sourcesSection
                }
            }
            .padding()
        }
        .navigationTitle(saint.name.localized)
        .navigationBarTitleDisplayMode(.large)
    }

    @ViewBuilder
    private var headerSection: some View {
        VStack(alignment: .center, spacing: 8) {
            if let imageName = saint.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 200)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                if let attribution = saint.imageAttribution {
                    Text(attribution)
                        .font(.caption2)
                        .foregroundStyle(.tertiary)
                }
            }

            Text(saint.feastDay)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            if let country = saint.countryOfOrigin {
                Text(country)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity)
    }

    @ViewBuilder
    private var biographySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Biography", comment: "Section header for saint biography")
                .font(.title2.bold())
            Text(saint.biography.localized)
                .font(.body)
        }
    }

    @ViewBuilder
    private var patronSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Patron Of", comment: "Section header for patron saint associations")
                .font(.title2.bold())
            FlowLayout(spacing: 8) {
                ForEach(saint.patronOf, id: \.en) { patron in
                    Text(patron.localized)
                        .font(.subheadline)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(.tint.opacity(0.1))
                        .clipShape(Capsule())
                }
            }
        }
    }

    @ViewBuilder
    private var sourcesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Sources", comment: "Section header for content attribution")
                .font(.title2.bold())
            ForEach(saint.sources, id: \.name) { source in
                if let urlString = source.url, let url = URL(string: urlString) {
                    Link(source.name, destination: url)
                        .font(.subheadline)
                } else {
                    Text(source.name)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
        }
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
