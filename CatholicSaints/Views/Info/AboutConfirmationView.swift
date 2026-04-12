import SwiftUI

struct AboutConfirmationView: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    infoSection(
                        title: String(localized: "What is Confirmation?"),
                        content: String(localized: "confirmation_explanation")
                    )

                    infoSection(
                        title: String(localized: "Choosing a Patron Saint"),
                        content: String(localized: "choosing_saint_explanation")
                    )

                    infoSection(
                        title: String(localized: "How to Use This App"),
                        content: String(localized: "app_usage_guide")
                    )

                    sourcesSection
                }
                .padding()
            }
            .navigationTitle(String(localized: "About"))
        }
    }

    @ViewBuilder
    private func infoSection(title: String, content: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.title2.bold())
            Text(content)
                .font(.body)
        }
    }

    @ViewBuilder
    private var sourcesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(String(localized: "Content Sources"))
                .font(.title2.bold())
            Text(String(localized: "sources_attribution"))
                .font(.body)
                .foregroundStyle(.secondary)
        }
    }
}

#Preview {
    AboutConfirmationView()
}
