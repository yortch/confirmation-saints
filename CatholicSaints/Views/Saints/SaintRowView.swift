import SwiftUI

struct SaintRowView: View {
    let saint: Saint

    var body: some View {
        HStack(spacing: 12) {
            // Colored circle with saint's initial
            ZStack {
                Circle()
                    .fill(colorForSaint(saint).gradient)
                    .frame(width: 50, height: 50)
                Text(String(saint.name.prefix(1)))
                    .font(.title2.bold())
                    .foregroundStyle(.white)
            }
            .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                Text(saint.name)
                    .font(.headline)
                HStack(spacing: 4) {
                    Image(systemName: "calendar")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    Text(saint.formattedFeastDay)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                if let country = saint.country {
                    HStack(spacing: 4) {
                        Image(systemName: "globe")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                        Text(country)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            Spacer()

            if saint.isYoung {
                Image(systemName: "sparkles")
                    .foregroundStyle(.orange)
                    .font(.caption)
            }
        }
        .padding(.vertical, 4)
    }

    private func colorForSaint(_ saint: Saint) -> Color {
        let colors: [Color] = [.purple, .blue, .indigo, .teal, .pink, .orange, .mint, .cyan]
        let index = abs(saint.id.hashValue) % colors.count
        return colors[index]
    }
}
