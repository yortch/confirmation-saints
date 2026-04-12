import SwiftUI

struct WelcomeView: View {
    @AppStorage("hasSeenWelcome") private var hasSeenWelcome = false
    @State private var currentPage = 0
    @State private var animateContent = false

    private let pageCount = 4

    var body: some View {
        ZStack {
            // Background gradient
            LinearGradient(
                colors: [
                    Color.purple.opacity(0.15),
                    Color.purple.opacity(0.05),
                    Color(red: 1.0, green: 0.95, blue: 0.8).opacity(0.3)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            VStack(spacing: 0) {
                TabView(selection: $currentPage) {
                    welcomePage.tag(0)
                    discoverPage.tag(1)
                    learnPage.tag(2)
                    getStartedPage.tag(3)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))
                .animation(.easeInOut(duration: 0.3), value: currentPage)

                // Custom page indicator + navigation
                VStack(spacing: 24) {
                    pageIndicator

                    if currentPage < pageCount - 1 {
                        HStack {
                            Button(String(localized: "Skip")) {
                                completeOnboarding()
                            }
                            .foregroundStyle(.secondary)

                            Spacer()

                            Button {
                                withAnimation { currentPage += 1 }
                            } label: {
                                HStack(spacing: 4) {
                                    Text(String(localized: "Next"))
                                    Image(systemName: "chevron.right")
                                }
                                .fontWeight(.semibold)
                                .foregroundStyle(.white)
                                .padding(.horizontal, 24)
                                .padding(.vertical, 12)
                                .background(Color.purple, in: Capsule())
                            }
                        }
                        .padding(.horizontal, 32)
                    }
                }
                .padding(.bottom, 40)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.8)) {
                animateContent = true
            }
        }
    }

    // MARK: - Page Indicator

    private var pageIndicator: some View {
        HStack(spacing: 8) {
            ForEach(0..<pageCount, id: \.self) { index in
                Circle()
                    .fill(index == currentPage ? Color.purple : Color.purple.opacity(0.3))
                    .frame(width: index == currentPage ? 10 : 8,
                           height: index == currentPage ? 10 : 8)
                    .animation(.spring(response: 0.3), value: currentPage)
            }
        }
    }

    // MARK: - Page 1: Welcome

    private var welcomePage: some View {
        OnboardingPageView(
            icon: "cross.fill",
            iconColor: .purple,
            title: String(localized: "Find Your Confirmation Saint"),
            subtitle: String(localized: "Choosing a saint for your Confirmation is a beautiful Catholic tradition. This app helps you discover the perfect patron saint for your journey."),
            animated: animateContent
        )
    }

    // MARK: - Page 2: Discover

    private var discoverPage: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "magnifyingglass.circle.fill")
                .font(.system(size: 70))
                .foregroundStyle(.purple)
                .symbolRenderingMode(.hierarchical)

            Text(String(localized: "Explore Saints Your Way"))
                .font(.title.bold())
                .multilineTextAlignment(.center)

            VStack(alignment: .leading, spacing: 12) {
                discoverRow(icon: "character.textbox", text: String(localized: "By Name"))
                discoverRow(icon: "heart.fill", text: String(localized: "By Interest"))
                discoverRow(icon: "globe.americas.fill", text: String(localized: "By Country"))
                discoverRow(icon: "calendar", text: String(localized: "By Feast Day"))
                discoverRow(icon: "person.fill", text: String(localized: "By Life Stage"))
            }
            .padding(.horizontal, 40)

            Spacer()
            Spacer()
        }
        .padding()
    }

    private func discoverRow(icon: String, text: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundStyle(.purple)
                .frame(width: 32)
            Text(text)
                .font(.body)
        }
    }

    // MARK: - Page 3: Learn

    private var learnPage: some View {
        OnboardingPageView(
            icon: "book.circle.fill",
            iconColor: Color(red: 0.8, green: 0.65, blue: 0.2),
            title: String(localized: "Understand the Tradition"),
            subtitle: String(localized: "Learn about the Sacrament of Confirmation and why choosing a patron saint is such a meaningful part of your faith journey."),
            animated: animateContent
        )
    }

    // MARK: - Page 4: Get Started

    private var getStartedPage: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "hands.sparkles.fill")
                .font(.system(size: 70))
                .foregroundStyle(.purple)
                .symbolRenderingMode(.hierarchical)

            Text(String(localized: "Ready to Find Your Saint?"))
                .font(.title.bold())
                .multilineTextAlignment(.center)

            Text(String(localized: "Browse, search, and discover the saint who will walk with you on your Confirmation journey."))
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button {
                completeOnboarding()
            } label: {
                Text(String(localized: "Let's Go!"))
                    .font(.title3.bold())
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 16)
                    .background(
                        LinearGradient(
                            colors: [.purple, Color(red: 0.5, green: 0.2, blue: 0.7)],
                            startPoint: .leading,
                            endPoint: .trailing
                        ),
                        in: RoundedRectangle(cornerRadius: 16)
                    )
            }
            .padding(.horizontal, 40)
            .padding(.top, 8)

            Spacer()
            Spacer()
        }
        .padding()
    }

    // MARK: - Actions

    private func completeOnboarding() {
        withAnimation(.easeInOut(duration: 0.3)) {
            hasSeenWelcome = true
        }
    }
}

// MARK: - Reusable Page Component

private struct OnboardingPageView: View {
    let icon: String
    let iconColor: Color
    let title: String
    let subtitle: String
    let animated: Bool

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: icon)
                .font(.system(size: 70))
                .foregroundStyle(iconColor)
                .symbolRenderingMode(.hierarchical)
                .scaleEffect(animated ? 1.0 : 0.5)
                .opacity(animated ? 1.0 : 0.0)

            Text(title)
                .font(.title.bold())
                .multilineTextAlignment(.center)

            Text(subtitle)
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Spacer()
            Spacer()
        }
        .padding()
    }
}

#Preview {
    WelcomeView()
        .environment(\.appLanguage, "en")
}
