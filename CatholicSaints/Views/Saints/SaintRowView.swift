import SwiftUI

struct SaintRowView: View {
    let saint: Saint

    var body: some View {
        HStack(spacing: 12) {
            if let imageName = saint.imageName {
                Image(imageName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 50, height: 50)
                    .clipShape(Circle())
            } else {
                Image(systemName: "person.circle.fill")
                    .font(.system(size: 40))
                    .foregroundStyle(.secondary)
                    .frame(width: 50, height: 50)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(saint.name.localized)
                    .font(.headline)
                Text(saint.shortDescription.localized)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 4)
    }
}
