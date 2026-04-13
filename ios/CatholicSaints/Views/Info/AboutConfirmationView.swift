import SwiftUI

struct AboutConfirmationView: View {
    @Bindable var viewModel: SaintListViewModel
    @Environment(\.appLanguage) private var language

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    if viewModel.confirmationSections.isEmpty {
                        ContentUnavailableView(
                            AppStrings.localized("Content Loading...", language: language),
                            systemImage: "book.closed.fill"
                        )
                    } else {
                        ForEach(viewModel.confirmationSections) { section in
                            sectionView(section)
                        }
                    }
                }
                .padding()
            }
            .navigationTitle(AppStrings.localized("About Confirmation", language: language))
        }
    }

    @ViewBuilder
    private func sectionView(_ section: ConfirmationSection) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            // Section title
            HStack {
                Image(systemName: iconForSection(section.id))
                    .font(.title2)
                    .foregroundStyle(.red)
                Text(section.title)
                    .font(.title2.bold())
            }

            // Content blocks
            ForEach(section.content, id: \.heading) { content in
                VStack(alignment: .leading, spacing: 8) {
                    Text(content.heading)
                        .font(.headline)
                        .foregroundStyle(.primary)
                    bodyView(content.body)
                }
            }

            // Sources
            if !section.sources.isEmpty {
                HStack(spacing: 4) {
                    Image(systemName: "doc.text.fill")
                        .font(.caption2)
                    Text(section.sources.joined(separator: ", "))
                        .font(.caption)
                }
                .foregroundStyle(.secondary)
            }

            Divider()
        }
    }

    /// Renders a body string as a vertical stack of paragraphs.
    /// Splits on double-newlines for paragraph spacing, and uses
    /// explicit newline characters within each paragraph for line breaks.
    @ViewBuilder
    private func bodyView(_ text: String) -> some View {
        let paragraphs = text.components(separatedBy: "\n\n")
        VStack(alignment: .leading, spacing: 12) {
            ForEach(Array(paragraphs.enumerated()), id: \.offset) { _, paragraph in
                let attributed = markdownAttributed(paragraph)
                Text(attributed)
                    .font(.body)
                    .foregroundStyle(.primary.opacity(0.85))
            }
        }
    }

    /// Parses markdown bold while preserving newlines as actual line breaks.
    private func markdownAttributed(_ text: String) -> AttributedString {
        // Split on single newlines, parse each line for markdown, rejoin with newlines
        let lines = text.components(separatedBy: "\n")
        var result = AttributedString()
        for (index, line) in lines.enumerated() {
            if index > 0 {
                result.append(AttributedString("\n"))
            }
            if let parsed = try? AttributedString(markdown: line) {
                result.append(parsed)
            } else {
                result.append(AttributedString(line))
            }
        }
        return result
    }

    private func iconForSection(_ id: String) -> String {
        switch id {
        case "what-is-confirmation": return "flame.fill"
        case "choosing-your-saint": return "person.crop.circle.badge.checkmark"
        case "tips-for-finding-your-match": return "lightbulb.fill"
        default: return "book.fill"
        }
    }
}

#Preview {
    AboutConfirmationView(viewModel: SaintListViewModel())
        .environment(\.appLanguage, "en")
}
