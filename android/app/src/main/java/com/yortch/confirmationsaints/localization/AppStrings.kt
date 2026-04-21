package com.yortch.confirmationsaints.localization

/**
 * In-memory UI-string translation map — direct port of iOS `AppStrings`
 * (see ios/CatholicSaints/Services/LocalizationService.swift).
 *
 * Keys match the English display text; [localized] returns the key unchanged
 * for [AppLanguage.EN] and looks up Spanish (etc.) translations by key.
 *
 * Why not `strings.xml`? Android's resource system switches language on
 * `Configuration` / `Locale`, which requires Activity recreation or
 * `attachBaseContext` hacks. iOS switches live via a state flag; matching
 * that behaviour requires keeping the map in Kotlin and recomposing on the
 * [LocalAppLanguage] CompositionLocal.
 */
object AppStrings {

    private val spanish: Map<String, String> = mapOf(
        // Screen titles / tabs
        "About" to "Acerca de",
        "About Confirmation" to "Sobre la Confirmación",
        "Explore" to "Explorar",
        "Saints" to "Santos",
        "Settings" to "Ajustes",

        // Info / metadata
        "App Info" to "Información de la App",
        "Version" to "Versión",
        "Saints Included" to "Santos Incluidos",
        "Language" to "Idioma",
        "Languages" to "Idiomas",
        "English, Spanish" to "Inglés, Español",

        // Saint detail labels
        "Biography" to "Biografía",
        "Born" to "Nacimiento",
        "Died" to "Fallecimiento",
        "Canonized" to "Canonización",
        "Details" to "Detalles",
        "Patron Of" to "Patrón de",
        "Interests & Tags" to "Intereses y Etiquetas",
        "Region" to "Región",
        "Sources" to "Fuentes",
        "Why Choose This Saint?" to "¿Por qué elegir este santo?",

        // Search / list
        "Search" to "Buscar",
        "Search saints..." to "Buscar santos...",
        "Name, interest, country..." to "Nombre, interés, país...",
        "No Saints Found" to "No se encontraron santos",
        "No saints match this category." to "Ningún santo coincide con esta categoría.",
        "Saints data is loading..." to "Los datos de los santos se están cargando...",
        "Content Loading..." to "Cargando contenido...",
        "Clear All" to "Limpiar todo",
        "results" to "resultados",
        "saint" to "santo",
        "saints" to "santos",
        "Find Your Saint" to "Encuentra tu Santo",

        // Filter chips / categories
        "Young Saints" to "Santos Jóvenes",
        "Young Saint" to "Santo Joven",
        "Female Saints" to "Santas",
        "Male Saints" to "Santos Varones",
        "By Name" to "Por Nombre",
        "By Interest" to "Por Interés",
        "By Country" to "Por País",
        "By Feast Day" to "Por Día de Fiesta",
        "By Life Stage" to "Por Etapa de Vida",

        // Life state display values (lowercase keys, match saint data)
        "married" to "casado",
        "religious" to "religioso",
        "single" to "soltero",
        "layperson" to "laico",
        "martyr" to "mártir",
        // Capitalized forms for filter chips
        "Married" to "Casados",
        "Religious" to "Religiosos",
        "Single" to "Solteros",

        // Regions
        "Europe" to "Europa",
        "Americas" to "Américas",
        "Africa" to "África",
        "Asia" to "Asia",
        "Middle East" to "Medio Oriente",

        // Onboarding
        "Onboarding" to "Bienvenida",
        "Find Your Confirmation Saint" to "Encuentra Tu Santo de Confirmación",
        "Choosing a saint for your Confirmation is a beautiful Catholic tradition. This app helps you discover the perfect patron saint for your journey."
            to "Elegir un santo para tu Confirmación es una hermosa tradición católica. Esta app te ayuda a descubrir el santo patrón perfecto para tu camino.",
        "Explore Saints Your Way" to "Explora Santos a Tu Manera",
        "Understand the Tradition" to "Comprende la Tradición",
        "Learn about the Sacrament of Confirmation and why choosing a patron saint is such a meaningful part of your faith journey."
            to "Aprende sobre el Sacramento de la Confirmación y por qué elegir un santo patrón es una parte tan significativa de tu camino de fe.",
        "Ready to Find Your Saint?" to "¿Listo para Encontrar Tu Santo?",
        "Browse, search, and discover the saint who will walk with you on your Confirmation journey."
            to "Explora, busca y descubre al santo que caminará contigo en tu jornada de Confirmación.",
        "Skip" to "Omitir",
        "Next" to "Siguiente",
        "Let's Go!" to "¡Vamos!",

        // Settings
        "Show Welcome Screen" to "Mostrar Pantalla de Bienvenida",
        "Replay the welcome screen to revisit how the app works." to
            "Reproduce la pantalla de bienvenida para revisar cómo funciona la app.",
        "Changing the language updates all saint content and app text." to
            "Cambiar el idioma actualiza todo el contenido de los santos y el texto de la app.",
        "Content Sources" to "Fuentes de Contenido",
        "Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution." to
            "La información de los santos proviene de fuentes católicas confiables. Cada entrada incluye atribución específica.",
        "Privacy Policy" to "Política de Privacidad",
        "Support" to "Soporte",
        "Contact Us" to "Contáctanos",
        "Support & Legal" to "Soporte y Legal",
        "Open link" to "Abrir enlace",
    )

    private val translations: Map<String, Map<String, String>> = mapOf(
        AppLanguage.ES.code to spanish,
    )

    /**
     * Look up [key] for [language]. English returns the key verbatim
     * (English IS the key); other languages fall back to the key if the
     * translation is missing — matches iOS behaviour.
     */
    fun localized(key: String, language: AppLanguage): String {
        if (language == AppLanguage.EN) return key
        return translations[language.code]?.get(key) ?: key
    }
}
