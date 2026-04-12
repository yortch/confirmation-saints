import Foundation

/// Loads saint, category, and confirmation info from bundled bilingual JSON files.
/// Files live in SharedContent/ — platform-agnostic, reusable for Android.
@MainActor
final class SaintDataService: Sendable {
    static let shared = SaintDataService()

    private init() {}

    /// Load saints for the given language code ("en" or "es").
    func loadSaints(language: String) -> [Saint] {
        let filename = "saints-\(language)"
        guard let url = findBundleURL(filename: filename, ext: "json", subdirectory: "SharedContent/saints") else {
            print("⚠️ \(filename).json not found in bundle")
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode(SaintsFile.self, from: data)
            return decoded.saints
        } catch {
            print("⚠️ Failed to decode \(filename).json: \(error)")
            return []
        }
    }

    /// Load categories for the given language code.
    func loadCategories(language: String) -> [CategoryGroup] {
        let filename = "categories-\(language)"
        guard let url = findBundleURL(filename: filename, ext: "json", subdirectory: "SharedContent/categories") else {
            print("⚠️ \(filename).json not found in bundle")
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode(CategoriesFile.self, from: data)
            return decoded.categories
        } catch {
            print("⚠️ Failed to decode \(filename).json: \(error)")
            return []
        }
    }

    /// Load confirmation info content for the given language code.
    func loadConfirmationInfo(language: String) -> [ConfirmationSection] {
        let filename = "confirmation-info-\(language)"
        guard let url = findBundleURL(filename: filename, ext: "json", subdirectory: "SharedContent/content") else {
            print("⚠️ \(filename).json not found in bundle")
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode(ConfirmationInfoFile.self, from: data)
            return decoded.sections
        } catch {
            print("⚠️ Failed to decode \(filename).json: \(error)")
            return []
        }
    }

    /// SharedContent is added as a folder reference, so files are at SharedContent/subpath.
    private func findBundleURL(filename: String, ext: String, subdirectory: String) -> URL? {
        // Folder reference: look inside the subdirectory
        if let url = Bundle.main.url(forResource: filename, withExtension: ext, subdirectory: subdirectory) {
            return url
        }
        // Fallback: flat bundle (all files at root)
        return Bundle.main.url(forResource: filename, withExtension: ext)
    }
}
