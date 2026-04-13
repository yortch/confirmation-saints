import Foundation

/// Reusable date formatting for saint dates.
/// Converts ISO date strings to display format: dd-Mon-yyyy (e.g., "02-Jan-1873").
/// For approximate ancient dates (month=01, day=01, year<800), shows year only.
enum SaintDateFormatter {
    /// Formats an ISO date string (e.g., "1873-01-02") for display.
    /// Uses the system DateFormatter for locale-aware month abbreviations.
    static func format(_ isoDate: String, language: String = "en") -> String {
        let parts = isoDate.split(separator: "-")
        guard parts.count == 3,
              let year = Int(parts[0]),
              let month = Int(parts[1]),
              let day = Int(parts[2]) else {
            return isoDate
        }

        // Approximate ancient dates: month=01, day=01, year<800
        if month == 1 && day == 1 && year < 800 {
            return "\(year)"
        }

        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: language)
        let monthStr = formatter.shortMonthSymbols[month - 1].capitalized

        let dayStr = String(format: "%02d", day)
        return "\(dayStr)-\(monthStr)-\(year)"
    }
}
