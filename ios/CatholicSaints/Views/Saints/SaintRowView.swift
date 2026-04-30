import SwiftUI

struct SaintRowView: View {
    let saint: Saint
    @Environment(\.appLanguage) private var language

    var body: some View {
        let feastDay = saint.formattedFeastDay(language: language)

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
                    Text(feastDay)
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
        .accessibilityLabel("\(saint.name), \(feastDay)\(saint.country.map { ", \($0)" } ?? "")\(saint.isYoung ? ", \(AppStrings.localized("Young Saint", language: language))" : "")")
    }
}
