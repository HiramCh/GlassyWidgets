# Glassy Widgets 🫧

App Android de widgets de **reloj**, **clima** y **combinados**, con un **creador dinámico** de widgets propios. Interfaz en estilo **Liquid Glass** o **Material You** (se cambia en Ajustes).

Kotlin · Jetpack Compose · Material 3 · Jetpack Glance · WorkManager · DataStore · Open-Meteo

---

## Qué incluye

**Widgets listos (pantalla de inicio)**
- **Reloj Glass**: hora grande + fecha, en vivo (usa `TextClock`, no gasta batería).
- **Clima Glass**: ciudad, icono, temperatura, condición y máx/mín.
- **Reloj + Clima**: todo en uno.
- 5 estilos de fondo: Cristal, Cristal oscuro, Material You (colores de tu fondo de pantalla), Sólido y Degradado. Tintes, opacidad y esquinas ajustables.

**Creador de widgets (pestaña "Crear")**
- Vista previa en vivo idéntica al widget real, en tamaños 2×2, 4×2 y 4×3.
- 13 elementos: hora, fecha, icono del clima, temperatura, condición, máx/mín, sensación, humedad, viento, ciudad, texto libre, separador y espacio.
- Por elemento: tamaño, 8 tipografías, color (texto / acento / suave / propio), formato de hora o fecha, mayúsculas, y **"misma fila"** para poner elementos lado a lado.
- Ordenar (subir/bajar), duplicar y eliminar elementos.
- Fondo: estilo, 2 colores, opacidad, borde de luz, esquinas, margen y alineación.
- 6 plantillas para empezar: Cristal clásico, Pixel apilado, Clima líquido, Noche minimal, Atardecer y Neón mono.
- Botón **"Guardar y añadir"** que ancla el widget directamente en tu pantalla de inicio.
- El widget "Mi diseño" se puede reconfigurar (mantener presionado → Configurar) para cambiar de diseño.

**Clima**
- Open-Meteo: gratis y **sin API key**.
- Busca tu ciudad o usa tu ubicación. °C / °F.
- Se actualiza cada 30 min en segundo plano.

---

## Cómo compilar

1. Instala **Android Studio** (Ladybug o más reciente).
2. `File → Open` y elige la carpeta `GlassyWidgets`.
3. Espera la sincronización de Gradle (descarga dependencias la primera vez).
4. Conecta tu teléfono (depuración USB) o abre un emulador y pulsa ▶ **Run**.

Por terminal: `./gradlew assembleDebug` → el APK queda en `app/build/outputs/apk/debug/`.

Requisitos: Android 8.0+ (API 26). Material You dinámico y esquinas redondeadas reales en widgets requieren Android 12+.

---

## Estructura

```
app/src/main/java/com/glassywidgets/
├── data/            Modelos, DataStore, API de Open-Meteo, plantillas
├── work/            WeatherWorker (actualización periódica)
├── widget/
│   ├── GlassPainter.kt     Dibuja el fondo liquid glass (bitmap)
│   ├── DesignLogic.kt      Filas, escala automática, textos (compartido)
│   ├── GlanceDesign.kt     Renderiza cualquier diseño como widget
│   ├── Widgets.kt          Los 4 widgets + receivers
│   └── ConfigureActivity   Elegir diseño para "Mi diseño"
└── ui/              Compose: tema, componentes de cristal, pantallas
```

**Idea clave:** todo widget es un `WidgetDesign` (lista de elementos + estilo). El mismo diseño se dibuja con Glance en la pantalla de inicio y con Compose en la vista previa, usando el mismo bitmap de fondo y la misma lógica de escala. Por eso agregar un nuevo tipo de elemento solo requiere tocar `Models.kt`, `DesignLogic.kt`, `GlanceDesign.kt` y `DesignPreview.kt`.

---

## Sobre el efecto Liquid Glass

Los widgets de Android (RemoteViews) **no pueden desenfocar** lo que está detrás. El cristal se simula con capas en un bitmap: tinte translúcido en degradado, brillo especular, cáustica inferior, sombra interna y borde de luz. Dentro de la app, las tarjetas son translúcidas sobre un fondo de "gotas" de color animadas.

Si quieres refracción real dentro de la app (Android 13+), puedes añadir una de estas librerías y aplicarla a `GlassCard`:
- [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) — Compose, AGSL.
- [styropyr0/PrismalAGSL](https://github.com/styropyr0/PrismalAGSL) — Compose, con fallback para versiones viejas.
- [FletchMcKee/liquid](https://github.com/FletchMcKee/liquid) — Compose Multiplatform.

## Repositorios de referencia

- [android/platform-samples – appwidgets](https://github.com/android/platform-samples/tree/main/samples/user-interface/appwidgets): ejemplos oficiales de widgets con Glance (layouts canónicos, clima).
- [sadellie/sukko](https://github.com/sadellie/sukko): alternativa libre a KWGT en Compose; buena referencia para un editor más avanzado.
- [seojacknet-ops/clockjacked](https://github.com/seojacknet-ops/clockjacked): app de reloj con Compose + Glance + DataStore.
- [Open-Meteo](https://open-meteo.com/en/docs): API del clima.

## Ideas para seguir

- Más elementos: batería, próximo evento del calendario, pronóstico por horas, fases de la luna.
- Imágenes/íconos propios y fuentes descargables.
- Exportar/importar diseños como JSON para compartirlos.
- Arrastrar para reordenar elementos.
