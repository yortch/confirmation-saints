import Foundation

/// Represents a Catholic saint loaded from per-language JSON files.
/// Each language has its own file (saints-en.json, saints-es.json).
struct Saint: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let name: String
    let feastDay: String
    let birthDate: String?
    let deathDate: String?
    let canonizationDate: String?
    let country: String?
    let region: String?
    let gender: String?
    let lifeState: String?
    let ageCategory: String?
    let patronOf: [String]
    let tags: [String]
    let affinities: [String]
    let quote: String?
    let biography: String
    let whyConfirmationSaint: String?
    let image: SaintImage?
    let sources: [String]
    let sourceURLs: [String: String]?

    /// Formatted feast day for display (e.g. "October 1")
    var formattedFeastDay: String {
        let parts = feastDay.split(separator: "-")
        guard parts.count == 2,
              let month = Int(parts[0]),
              let day = Int(parts[1]) else { return feastDay }
        let months = [
            1: "January", 2: "February", 3: "March", 4: "April",
            5: "May", 6: "June", 7: "July", 8: "August",
            9: "September", 10: "October", 11: "November", 12: "December"
        ]
        return "\(months[month] ?? "") \(day)"
    }

    var isYoung: Bool { ageCategory == "young" }
}

struct SaintImage: Codable, Hashable, Sendable {
    let filename: String
    let attribution: String
}

/// Wrapper for decoding the saints JSON file.
struct SaintsFile: Codable, Sendable {
    let version: String
    let language: String
    let lastUpdated: String
    let saints: [Saint]
}

// MARK: - Confirmation Info

struct ConfirmationInfoFile: Codable, Sendable {
    let version: String
    let language: String
    let lastUpdated: String
    let sections: [ConfirmationSection]
}

struct ConfirmationSection: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let title: String
    let content: [ConfirmationContent]
    let sources: [String]
}

struct ConfirmationContent: Codable, Hashable, Sendable {
    let heading: String
    let body: String
}

// MARK: - Categories

struct CategoriesFile: Codable, Sendable {
    let version: String
    let language: String
    let lastUpdated: String
    let categories: [CategoryGroup]
}

struct CategoryGroup: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let name: String
    let description: String
    let icon: String
    let values: [CategoryValue]
}

struct CategoryValue: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let label: String
}
