import SwiftUI

/// Branded splash screen shown while the app loads data.
/// Displays the app icon and name on a red background matching the app theme.
struct SplashView: View {
    var body: some View {
        ZStack {
            Color.red.ignoresSafeArea()

            VStack(spacing: 20) {
                Image("SplashLogo")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 120, height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 24))
                    .shadow(color: .black.opacity(0.3), radius: 10, y: 5)

                Text("Confirmation Saints")
                    .font(.title.bold())
                    .foregroundStyle(.white)
            }
        }
    }
}

#Preview {
    SplashView()
}
