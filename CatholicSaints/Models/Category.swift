import Foundation

/// Categories for filtering and browsing saints.
struct Category: Codable, Identifiable, Hashable, Sendable {
    let id: String
    let name: LocalizedText
    let systemImage: String
    let description: LocalizedText
}

/// Predefined affinity tags that map to teen-friendly interests.
enum Affinity: String, CaseIterable, Sendable {
    case sports
    case music
    case art
    case science
    case education
    case military
    case writing
    case nature
    case healing
    case leadership

    var localizedName: String {
        String(localized: String.LocalizationValue(rawValue))
    }

    var systemImage: String {
        switch self {
        case .sports: return "figure.run"
        case .music: return "music.note"
        case .art: return "paintbrush.fill"
        case .science: return "atom"
        case .education: return "book.fill"
        case .military: return "shield.fill"
        case .writing: return "pencil"
        case .nature: return "leaf.fill"
        case .healing: return "cross.case.fill"
        case .leadership: return "crown.fill"
        }
    }
}
