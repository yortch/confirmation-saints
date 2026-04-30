import Foundation

/// Dictionary-based localization that respects the in-app language setting
/// instead of the system locale. Keys match Localizable.xcstrings entries.
struct AppStrings {
    private static let translations: [String: [String: String]] = [
        "es": [
            "About": "Acerca de",
            "About Confirmation": "Sobre la Confirmación",
            "App Info": "Información de la App",
            "Biography": "Biografía",
            "Born": "Nacimiento",
            "Browse, search, and discover the saint who will walk with you on your Confirmation journey.": "Explora, busca y descubre al santo que caminará contigo en tu jornada de Confirmación.",
            "By Country": "Por País",
            "By Interest": "Por Interés",
            "By Life Stage": "Por Etapa de Vida",
            "By Name": "Por Nombre",
            "Canonized": "Canonización",
            "Changing the language updates all saint content and app text.": "Cambiar el idioma actualiza todo el contenido de los santos y el texto de la app.",
            "Choosing a saint for your Confirmation is a beautiful Catholic tradition. This app helps you discover the perfect patron saint for your journey.": "Elegir un santo para tu Confirmación es una hermosa tradición católica. Esta app te ayuda a descubrir el santo patrón perfecto para tu camino.",
            "Clear All": "Limpiar todo",
            "Content Loading...": "Cargando contenido...",
            "Content Sources": "Fuentes de Contenido",
            "Details": "Detalles",
            "Died": "Fallecimiento",
            "Done": "Listo",
            "English, Spanish": "Inglés, Español",
            "Explore": "Explorar",
            "Explore Saints Your Way": "Explora Santos a Tu Manera",
            "Female Saints": "Santas",
            "Find Your Confirmation Saint": "Encuentra Tu Santo de Confirmación",
            "Find Your Saint": "Encuentra tu Santo",
            "Interests & Tags": "Intereses y Etiquetas",
            "Language": "Idioma",
            "Languages": "Idiomas",
            "Learn about the Sacrament of Confirmation and why choosing a patron saint is such a meaningful part of your faith journey.": "Aprende sobre el Sacramento de la Confirmación y por qué elegir un santo patrón es una parte tan significativa de tu camino de fe.",
            "Let's Go!": "¡Vamos!",
            "Male Saints": "Santos Varones",
            "Modern Day Saints": "Santos de Hoy",
            "Name, interest, country...": "Nombre, interés, país...",
            "Next": "Siguiente",
            "No Saints Found": "No se encontraron santos",
            "No saints match this category.": "Ningún santo coincide con esta categoría.",
            "Onboarding": "Bienvenida",
            "Opens a larger view of this saint image.": "Abre una vista más grande de esta imagen del santo.",
            "Patron Of": "Patrón de",
            "Ready to Find Your Saint?": "¿Listo para Encontrar Tu Santo?",
            "Region": "Región",
            "Replay the welcome screen to revisit how the app works.": "Reproduce la pantalla de bienvenida para revisar cómo funciona la app.",
            "results": "resultados",
            "saint": "santo",
            "Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution.": "La información de los santos proviene de fuentes católicas confiables. Cada entrada incluye atribución específica.",
            "saints": "santos",
            "Saints": "Santos",
            "Saints data is loading...": "Los datos de los santos se están cargando...",
            "Saints Included": "Santos Incluidos",
            "Search": "Buscar",
            "Search saints...": "Buscar santos...",
            "Settings": "Ajustes",
            "Show Welcome Screen": "Mostrar Pantalla de Bienvenida",
            "Skip": "Omitir",
            "Sources": "Fuentes",
            "Understand the Tradition": "Comprende la Tradición",
            "Version": "Versión",
            "View larger image": "Ver imagen más grande",
            "View larger image of %@": "Ver imagen más grande de %@",
            "Why Choose This Saint?": "¿Por qué elegir este santo?",
            "Young Saint": "Santo Joven",
            "Young Saints": "Santos Jóvenes",
            // Filter chip labels
            "Married": "Casados",
            "Religious": "Religiosos",
            "Single": "Solteros",
            // Region labels
            "Europe": "Europa",
            "Americas": "Américas",
            "Africa": "África",
            "Asia": "Asia",
            "Middle East": "Medio Oriente",
            // Life state display values
            "married": "casado",
            "religious": "religioso",
            "single": "soltero",
            "layperson": "laico",
            "martyr": "mártir",
            // Support & Legal
            "Privacy Policy": "Política de Privacidad",
            "Support": "Soporte",
            "Contact Us": "Contáctanos",
            "Support & Legal": "Soporte y Legal",
            // Content Sources
            "Biographical information": "Información biográfica",
            "Public domain images": "Imágenes de dominio público",
            "Open": "Abrir",
            "in browser": "en el navegador",
        ]
    ]

    static func localized(_ key: String, language: String) -> String {
        if language == "en" { return key }
        return translations[language]?[key] ?? key
    }
}
