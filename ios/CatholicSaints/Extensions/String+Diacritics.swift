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

    /// Token-prefix match for deep search fields (tags / patronOf / affinities).
    /// `query` must be the prefix of at least one whitespace/hyphen/slash/comma-
    /// delimited word. Avoids "ter" → "writers"/"interpreters" false positives
    /// that plain `containsIgnoringDiacritics` produces.
    func matchesTokenPrefixIgnoringDiacritics(_ query: String) -> Bool {
        guard !query.isEmpty else { return false }
        let separators = CharacterSet(charactersIn: " -/,\t")
        return self.components(separatedBy: separators).contains { token in
            guard !token.isEmpty else { return false }
            return token.range(
                of: query,
                options: [.caseInsensitive, .diacriticInsensitive, .anchored]
            ) != nil
        }
    }
}
