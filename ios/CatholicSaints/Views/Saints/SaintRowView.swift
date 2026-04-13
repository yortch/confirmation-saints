import SwiftUI

struct SaintRowView: View {
    let saint: Saint

    var body: some View {
        HStack(spacing: 12) {
            SaintImageView(saint: saint, size: 50)
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
                    .accessibilityHidden(true)
            }
        }
        .padding(.vertical, 4)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(saint.name), \(saint.formattedFeastDay)\(saint.country.map { ", \($0)" } ?? "")\(saint.isYoung ? ", Young Saint" : "")")
    }
}
