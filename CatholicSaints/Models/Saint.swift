import Foundation

/// Represents a Catholic saint with localized content.
/// Data is loaded from bundled JSON (SharedContent/Data/) for cross-platform reuse.
struct Saint: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let name: LocalizedText
    let feastDay: String
    let birthYear: Int?
    let deathYear: Int?
    let canonizationYear: Int?
    let countryOfOrigin: String?
    let biography: LocalizedText
    let shortDescription: LocalizedText
    let patronOf: [LocalizedText]
    let affinities: [String]
    let categories: [String]
    let isYoungSaint: Bool
    let wasMarried: Bool
    let imageName: String?
    let imageAttribution: String?
    let sources: [ContentSource]
}

/// Localized text container supporting English and Spanish.
/// Extensible to additional languages by adding fields.
struct LocalizedText: Codable, Hashable, Sendable {
    let en: String
    let es: String

    /// Returns the text for the current device locale, falling back to English.
    var localized: String {
        let lang = Locale.current.language.languageCode?.identifier ?? "en"
        switch lang {
        case "es": return es
        default: return en
        }
    }
}

/// Attribution for saint information sources.
struct ContentSource: Codable, Hashable, Sendable {
    let name: String
    let url: String?
}
