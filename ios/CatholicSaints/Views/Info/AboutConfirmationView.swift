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
                    .foregroundStyle(.purple)
                Text(section.title)
                    .font(.title2.bold())
            }

            // Content blocks
            ForEach(section.content, id: \.heading) { content in
                VStack(alignment: .leading, spacing: 8) {
                    Text(content.heading)
                        .font(.headline)
                        .foregroundStyle(.primary)
                    Text(markdownAttributedString(content.body))
                        .font(.body)
                        .foregroundStyle(.primary.opacity(0.85))
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

    private func markdownAttributedString(_ text: String) -> AttributedString {
        // Convert every newline to a Markdown hard line break (two trailing spaces)
        // so bullets, numbered lists, and bold section headings render with visible breaks.
        let normalized = text.replacingOccurrences(of: "\n", with: "  \n")
        return (try? AttributedString(markdown: normalized)) ?? AttributedString(text)
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
