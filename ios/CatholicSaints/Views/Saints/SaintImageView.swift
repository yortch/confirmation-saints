import SwiftUI

/// Reusable saint image view that tries to load an actual image from the bundle,
/// falling back to a colored circle with the saint's initial.
struct SaintImageView: View {
    let saint: Saint
    let size: CGFloat

    var body: some View {
        if let imageFilename = saint.image?.filename,
           let uiImage = loadAssetImage(named: imageFilename) ?? loadBundleImage(named: imageFilename) {
            Image(uiImage: uiImage)
                .resizable()
                .scaledToFill()
                .frame(width: size, height: size)
                .clipShape(Circle())
                .overlay(Circle().stroke(Color.red.opacity(0.3), lineWidth: size > 60 ? 2 : 1))
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

    /// Try loading from asset catalog (filename without extension).
    private func loadAssetImage(named filename: String) -> UIImage? {
        let name = filename
            .replacingOccurrences(of: ".jpg", with: "")
            .replacingOccurrences(of: ".png", with: "")
        return UIImage(named: name)
    }

    /// Try loading from SharedContent/images/ in the bundle.
    private func loadBundleImage(named filename: String) -> UIImage? {
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
