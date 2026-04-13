import Foundation

/// Reusable date formatting for saint dates.
/// Converts ISO date strings to display format: dd-Mon-yyyy (e.g., "02-Jan-1873").
/// For approximate ancient dates (month=01, day=01, year<800), shows year only.
enum SaintDateFormatter {
    private static let monthAbbreviations = [
        1: "Jan", 2: "Feb", 3: "Mar", 4: "Apr",
        5: "May", 6: "Jun", 7: "Jul", 8: "Aug",
        9: "Sep", 10: "Oct", 11: "Nov", 12: "Dec"
    ]

    /// Formats an ISO date string (e.g., "1873-01-02") for display.
    /// Returns "02-Jan-1873" for precise dates, or just the year for approximate ancient dates.
    static func format(_ isoDate: String) -> String {
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

        let dayStr = String(format: "%02d", day)
        let monthStr = monthAbbreviations[month] ?? "\(month)"
        return "\(dayStr)-\(monthStr)-\(year)"
    }
}
