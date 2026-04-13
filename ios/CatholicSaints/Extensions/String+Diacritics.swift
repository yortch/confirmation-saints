import Foundation

extension String {
    /// Returns true if the receiver contains `other`, ignoring both case and diacritics.
    /// e.g. "Thérèse".containsIgnoringDiacritics("therese") == true
    func containsIgnoringDiacritics(_ other: String) -> Bool {
        self.range(of: other, options: [.caseInsensitive, .diacriticInsensitive]) != nil
    }

    /// Diacritic-insensitive equality check (case-insensitive).
    func equalsIgnoringDiacritics(_ other: String) -> Bool {
        self.compare(other, options: [.caseInsensitive, .diacriticInsensitive]) == .orderedSame
    }
}
