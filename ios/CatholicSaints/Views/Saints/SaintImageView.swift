import SwiftUI

/// Reusable saint image view that tries to load an actual image from the bundle,
/// falling back to a colored circle with the saint's initial.
struct SaintImageView: View {
    let saint: Saint
    let size: CGFloat

    /// Saints whose portrait is an official logo/crest that legally must not be cropped
    /// or have its design altered. These render with aspect-fit (contain) inside the
    /// circular frame instead of the default aspect-fill used for photographic portraits.
    private static let logoPortraitSaintIDs: Set<String> = ["maria-troncatti"]

    private var mustNotCropImage: Bool {
        Self.logoPortraitSaintIDs.contains(saint.id)
    }

    var body: some View {
        if let uiImage = Self.loadImage(for: saint) {
            if mustNotCropImage {
                // The full official logo must remain uncropped and unaltered, so it is
                // never masked by the circular frame used for photographic portraits.
                Image(uiImage: uiImage)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: size, height: size)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: size * 0.12))
                    .overlay(
                        RoundedRectangle(cornerRadius: size * 0.12)
                            .stroke(Color.red.opacity(0.3), lineWidth: size > 60 ? 2 : 1)
                    )
            } else {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: size, height: size)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.red.opacity(0.3), lineWidth: size > 60 ? 2 : 1))
            }
        } else {
            ZStack {
                Circle()
                    .fill(colorForSaint(saint).gradient)
                    .frame(width: size, height: size)
                Text(String(saint.name.prefix(1)))
                    .font(.system(size: size * 0.44, weight: .bold))
                    .foregroundStyle(.white)
            }
        }
    }

    static func loadImage(for saint: Saint) -> UIImage? {
        guard let filename = saint.image?.filename else { return nil }
        return loadAssetImage(named: filename) ?? loadBundleImage(named: filename)
    }

    /// Try loading from asset catalog (filename without extension).
    private static func loadAssetImage(named filename: String) -> UIImage? {
        let name = filename
            .replacingOccurrences(of: ".jpg", with: "")
            .replacingOccurrences(of: ".png", with: "")
        return UIImage(named: name)
    }

    /// Try loading from SharedContent/images/ in the bundle.
    private static func loadBundleImage(named filename: String) -> UIImage? {
        if let url = Bundle.main.url(forResource: filename, withExtension: nil, subdirectory: "SharedContent/images") {
            return UIImage(contentsOfFile: url.path)
        }
        let name = (filename as NSString).deletingPathExtension
        let ext = (filename as NSString).pathExtension
        if let url = Bundle.main.url(forResource: name, withExtension: ext, subdirectory: "SharedContent/images") {
            return UIImage(contentsOfFile: url.path)
        }
        return nil
    }

    private func colorForSaint(_ saint: Saint) -> Color {
        let colors: [Color] = [.red, .blue, .indigo, .teal, .pink, .orange, .mint, .cyan]
        let index = abs(saint.id.hashValue) % colors.count
        return colors[index]
    }
}
