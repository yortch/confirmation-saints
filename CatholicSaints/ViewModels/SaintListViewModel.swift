import Foundation
import Observation

/// ViewModel for browsing and filtering saints.
@Observable
@MainActor
final class SaintListViewModel {
    var saints: [Saint] = []
    var categories: [Category] = []
    var searchText: String = ""
    var selectedCategory: String?
    var selectedAffinity: Affinity?
    var showYoungSaintsOnly: Bool = false
    var showMarriedSaintsOnly: Bool = false

    private let dataService = SaintDataService.shared

    var filteredSaints: [Saint] {
        var result = saints

        if !searchText.isEmpty {
            let query = searchText.lowercased()
            result = result.filter { saint in
                saint.name.localized.lowercased().contains(query)
                || saint.shortDescription.localized.lowercased().contains(query)
                || saint.patronOf.contains { $0.localized.lowercased().contains(query) }
            }
        }

        if let category = selectedCategory {
            result = result.filter { $0.categories.contains(category) }
        }

        if let affinity = selectedAffinity {
            result = result.filter { $0.affinities.contains(affinity.rawValue) }
        }

        if showYoungSaintsOnly {
            result = result.filter { $0.isYoungSaint }
        }

        if showMarriedSaintsOnly {
            result = result.filter { $0.wasMarried }
        }

        return result
    }

    func loadData() {
        saints = dataService.loadSaints()
        categories = dataService.loadCategories()
    }
}
