import Foundation

/// Loads saint and category data from bundled JSON files.
/// JSON lives in SharedContent/Data/ — platform-agnostic, reusable for Android.
@MainActor
final class SaintDataService: Sendable {
    static let shared = SaintDataService()

    private init() {}

    func loadSaints() -> [Saint] {
        guard let url = Bundle.main.url(forResource: "saints", withExtension: "json") else {
            print("⚠️ saints.json not found in bundle")
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode(SaintsWrapper.self, from: data)
            return decoded.saints
        } catch {
            print("⚠️ Failed to decode saints.json: \(error)")
            return []
        }
    }

    func loadCategories() -> [Category] {
        guard let url = Bundle.main.url(forResource: "categories", withExtension: "json") else {
            print("⚠️ categories.json not found in bundle")
            return []
        }
        do {
            let data = try Data(contentsOf: url)
            let decoded = try JSONDecoder().decode(CategoriesWrapper.self, from: data)
            return decoded.categories
        } catch {
            print("⚠️ Failed to decode categories.json: \(error)")
            return []
        }
    }
}

private struct SaintsWrapper: Codable {
    let saints: [Saint]
}

private struct CategoriesWrapper: Codable {
    let categories: [Category]
}
