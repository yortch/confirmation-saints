import Foundation
import Observation

/// ViewModel for browsing and filtering saints.
@Observable
@MainActor
final class SaintListViewModel {
    var saints: [Saint] = []
    var categories: [CategoryGroup] = []
    var confirmationSections: [ConfirmationSection] = []
    var searchText: String = ""
    var selectedRegion: String?
    var selectedLifeState: String?
    var selectedAgeCategory: String?
    var selectedGender: String?
    var selectedAffinity: String?

    private let dataService = SaintDataService.shared

    var filteredSaints: [Saint] {
        var result = saints

        if !searchText.isEmpty {
            let query = searchText.lowercased()
            result = result.filter { saint in
                saint.name.lowercased().contains(query)
                || saint.biography.lowercased().contains(query)
                || saint.patronOf.contains { $0.lowercased().contains(query) }
                || saint.affinities.contains { $0.lowercased().contains(query) }
                || saint.tags.contains { $0.lowercased().contains(query) }
                || (saint.country?.lowercased().contains(query) ?? false)
            }
        }

        if let region = selectedRegion {
            result = result.filter { $0.region?.lowercased() == region.lowercased() }
        }

        if let lifeState = selectedLifeState {
            result = result.filter { $0.lifeState == lifeState }
        }

        if let age = selectedAgeCategory {
            result = result.filter { $0.ageCategory == age }
        }

        if let gender = selectedGender {
            result = result.filter { $0.gender == gender }
        }

        if let affinity = selectedAffinity {
            result = result.filter {
                $0.affinities.contains { $0.lowercased().contains(affinity.lowercased()) }
            }
        }

        return result
    }

    /// Saints matching a specific category value (for category browsing).
    func saints(forCategoryGroup groupId: String, valueId: String) -> [Saint] {
        saints.filter { saint in
            switch groupId {
            case "patronage":
                return saint.patronOf.contains { $0.lowercased().contains(valueId.replacingOccurrences(of: "-", with: " ")) }
            case "interests":
                return saint.affinities.contains { $0.lowercased().contains(valueId.replacingOccurrences(of: "-", with: " ")) }
                    || saint.tags.contains { $0.lowercased().contains(valueId.replacingOccurrences(of: "-", with: " ")) }
            case "age-category":
                return saint.ageCategory == valueId
            case "region":
                return saint.region?.lowercased() == valueId.replacingOccurrences(of: "-", with: " ")
            case "life-state":
                return saint.lifeState == valueId
            case "era":
                return matchesEra(saint: saint, era: valueId)
            case "gender":
                return saint.gender == valueId
            default:
                return false
            }
        }
    }

    private func matchesEra(saint: Saint, era: String) -> Bool {
        guard let birthDate = saint.birthDate,
              let year = Int(birthDate.prefix(4)) else { return false }
        switch era {
        case "early-church": return year < 500
        case "medieval": return year >= 500 && year < 1500
        case "early-modern": return year >= 1500 && year < 1800
        case "modern": return year >= 1800 && year < 1950
        case "contemporary": return year >= 1950
        default: return false
        }
    }

    func clearFilters() {
        searchText = ""
        selectedRegion = nil
        selectedLifeState = nil
        selectedAgeCategory = nil
        selectedGender = nil
        selectedAffinity = nil
    }

    var hasActiveFilters: Bool {
        selectedRegion != nil || selectedLifeState != nil || selectedAgeCategory != nil
        || selectedGender != nil || selectedAffinity != nil
    }

    func loadData(language: String) {
        saints = dataService.loadSaints(language: language)
        categories = dataService.loadCategories(language: language)
        confirmationSections = dataService.loadConfirmationInfo(language: language)
    }
}
