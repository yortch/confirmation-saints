# Google Play Console Submission — Confirmation Saints (Android) — Español

Estas notas son solo para el lanzamiento de Android/Google Play. Las notas de iOS/App Store se gestionan por separado en `docs/appstore/submission-info-es.md` porque las correcciones específicas de cada plataforma pueden ser diferentes. La versión en inglés de estas notas está en `docs/android/submission-info.md`.

## Novedades en Android versión 1.1.1 (pendiente — aún no publicada)

Esta versión prepara el lanzamiento de la 1.1.1 en Google Play. Metadatos de versión: `versionName` 1.1.1 / `versionCode` 6 (ver la nota de código de versión más abajo).

## Novedades en la versión 1.1.1

- Se agregó la acción **Calificar y Reseñar** en Configuración (entre "Información de la app" e "Introducción"), usando la API de reseñas en la app de Google Play con un enlace de respaldo a Play Store, para calificar la app sin salir de ella.
- Se corrigió el perfil de santa María Troncatti: la atribución de su retrato ahora acredita correctamente el logo oficial de canonización de las FMA (Hermanas Salesianas) y su uso autorizado, corrigiendo un error de etiquetado de la versión anterior.
- Mejoras generales de estabilidad, precisión de traducción y calidad de contenido, incluyendo verificaciones automáticas reforzadas que mantienen sincronizados los datos de santos en inglés y español.

### Notas de la versión de Google Play (Español)

```
• Se agregó la opción Calificar y Reseñar en Configuración para dejar una reseña rápidamente.
• Se corrigió un problema de atribución/etiquetado en la imagen del perfil de santa María Troncatti.
• Mejoras generales de estabilidad y calidad de contenido.
```

### Nota sobre el código de versión

La versión en producción actual en Google Play es `versionName` 1.0.2 / `versionCode` 3. Un intento anterior (`versionCode` 4 / `versionName` 1.0.3) se revirtió porque la versión publicada en Google Play era 1.0.2 (versionCode 3); como Google Play retira permanentemente cualquier `versionCode` una vez subido a cualquier canal —incluso un borrador descartado—, no se puede asumir con seguridad que el 4 esté libre. `versionCode` 5 / `versionName` 1.1.0 se preparó localmente en un ciclo anterior (12 nuevos perfiles bilingües de santos, objetivo Android 16/API 36) pero **nunca se subió a Google Play**; esa versión fue reemplazada antes de su publicación. Esta versión usa `versionCode` 6 / `versionName` 1.1.1, que incorpora el contenido de la 1.1.0 más la acción Calificar y Reseñar y la corrección de atribución de Troncatti. **Antes de publicar, confirme en Play Console → Explorador de paquetes si el 4 o el 5 llegaron a consumirse**; si nunca se subieron, podría reutilizarse un código de versión menor y este archivo se ajustaría en consecuencia.

### Historial: versión 1.1.0 (preparada localmente, nunca publicada)

`versionCode` 5 / `versionName` 1.1.0 se preparó por completo en un ciclo anterior, pero **no se subió a Google Play** antes de que este ciclo 1.1.1 la reemplazara. Se conserva aquí como registro:

> Confirmation Saints ahora incluye 12 nuevos perfiles bilingües de santos y beatos —entre ellos san Estanislao Rother, san Felipe Neri, san Juan Nepomuceno, san Norberto de Xanten, san Pascual Bailón, san Ignacio Maloyan, san José Gregorio Hernández, santa Vincenza Maria Poloni, la beata María del Carmen Rendiles, el beato Bartolo Longo, santa María Troncatti y el beato Pedro To Rot— ampliando la biblioteca a 118 santos, todos disponibles sin conexión en inglés y español. Se agregaron once nuevas imágenes de retrato/logo con licencia. El perfil de santa María Troncatti usa el logo oficial de canonización de las FMA (Hermanas Salesianas), utilizado sin alteraciones bajo la autorización de uso de las FMA. Un perfil (el beato Pedro To Rot, canonizado recientemente) se publica intencionalmente sin imagen porque no se pudo verificar un retrato con licencia libre/CC. Esta versión también migró la app para apuntar a Android 16 (API nivel 36), sin cambios visibles para el usuario.
>
> En ese momento, la atribución en español de la imagen de santa María Troncatti quedó, por error, como una copia exacta sin traducir del texto en inglés — este es el problema de etiquetado corregido en la versión 1.1.1 mencionada arriba.

---

## Metadatos de la versión de Android

| Campo | Valor |
|---|---|
| Nombre de versión (pendiente) | `1.1.1` |
| Código de versión (pendiente) | `6` (ver la nota de código de versión en `submission-info.md`) |
| Nombre de versión (producción actual) | `1.0.2` |
| Código de versión (producción actual) | `3` |
| Paquete | `com.yortch.confirmationsaints` |
| SDK mínimo | API 26 |
| SDK objetivo | API 36 (Android 16) |
| Privacidad | No se recopilan datos |
| Estado de lanzamiento | Producción — 1.0.2 activa en Google Play; la 1.1.0 se preparó localmente pero nunca se subió; la 1.1.1 está preparada localmente, aún no subida |
