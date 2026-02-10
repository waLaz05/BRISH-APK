# 🕵️ Reporte de Análisis y Mejora - Brish

Este documento detalla el estado actual de la aplicación, bugs potenciales detectados y recomendaciones para mejorar la estabilidad y preparar el proyecto para su expansión (KMP).

## 📊 1. Resumen de Estado Actual
La aplicación es un proyecto nativo Android moderno que utiliza:
-   **UI**: Jetpack Compose (Moderno).
-   **Arquitectura**: MVVM con Hilt para inyección de dependencias.
-   **Base de Datos**: Room.
-   **Planificación**: AlarmManager y WorkManager.

**Estado**: La app tiene una base sólida en cuanto a UI, pero presenta **riesgos críticos de funcionalidad** en versiones recientes de Android (13 y 14) debido a la falta de gestión de permisos en tiempo de ejecución.

---

## 🐞 2. Bugs y Riesgos Detectados (Prioridad Alta)

### A. Falta de Permisos de Notificación (Android 13+)
**El Problema**: Desde Android 13 (API 33), las aplicaciones **deben pedir permiso explícito** al usuario para enviar notificaciones (`POST_NOTIFICATIONS`).
**Evidencia**: He revisado `MainActivity.kt` y `MainScreen.kt` y **no existe código que solicite este permiso**.
**Consecuencia**: En teléfonos nuevos, **las notificaciones (recordatorios, hábitos) estarán bloqueadas por defecto** y el usuario nunca las verá, haciendo que la app falle en su propósito principal.

### B. Riesgo de Alarmas Exactas (Android 12+)
**El Problema**: El permiso `SCHEDULE_EXACT_ALARM` se otorga automáticamente en algunas versiones, pero puede ser revocado por el sistema o no otorgado en ciertas condiciones de ahorro de batería.
**Evidencia**: `NotificationScheduler.kt` usa `canScheduleExactAlarms()` pero si devuelve `false`, degrada a alarmas inexactas sin avisar al usuario.
**Recomendación**: Implementar una pantalla de "Diagnóstico" o verificar esto al inicio para guiar al usuario a la configuración si es necesario.

### C. Limitación en Frecuencia de Hábitos
**El Problema**: El planificador de hábitos (`NotificationScheduler.kt`) asume que **todos los hábitos son diarios**.
```kotlin
// Código actual
if (before(Calendar.getInstance())) {
    add(Calendar.DAY_OF_YEAR, 1) // Simplemente lo mueve a mañana
}
```
**Consecuencia**: Si un usuario quiere un hábito solo para "Lunes y Viernes", la app lo molestará todos los días. Falta lógica de frecuencia en el modelo `Habit`.

### D. Fragilidad en Deep Linking
**El Problema**: La navegación por notificaciones usa una clase `NavigationBus` estática y cadenas de texto "a mano" (`"notes"`, `"planner"`) que se traducen en `MainActivity`.
**Riesgo**: Es propenso a errores tipográficos. Si cambias el nombre de una ruta en `Screen.kt`, las notificaciones dejarán de abrir la pantalla correcta.

---

## 🛠️ 3. Guía de Mejoras por Sección

### 📱 Android (App Actual)

#### 1. Gestión de Permisos (¡Urgente!)
Crear una **Pantalla de Solicitud de Permisos** (o un diálogo en el Onboarding) que pida:
1.  Notificaciones (`POST_NOTIFICATIONS`).
2.  Alarmas Exactas (redirigir a configuración si es necesario).
3.  Optimización de Batería (pedir excepción si las alarmas fallan mucho).

#### 2. Mejorar Modelo de Hábitos
Actualizar la entidad `Habit` en `Habit.kt` para incluir:
```kotlin
val frequency: List<DayOfWeek> = emptyList(), // Si está vacío, es diario
// O una clase más compleja:
val frequencyType: FrequencyType = FrequencyType.DAILY
```
Y actualizar `NotificationScheduler` para calcular la **próxima fecha válida** basada en estos días, no solo `+1 día`.

#### 3. Robustez de Alarmas (`BootReceiver`)
El `BootReceiver` actual simplemente llama a `rescheduleAllAlarms`.
**Mejora**: Añadir un `WorkManager` de respaldo que corra cada 12 horas para asegurar que las alarmas sigan vivas, ya que a veces el sistema las mata silenciosamente.

---

## 🚀 4. Recomendaciones para la Expansión (PC / KMP)

Según la guía técnica `TECHNICAL_GUIDE.md`, el plan es migrar a Kotlin Multiplatform.
**Consejo**: Antes de migrar, **¡arregla los bugs de Android primero!** Migrar código con bugs solo duplicará los problemas en Windows.

1.  **Fase 1 (Limpieza)**: Implementar los permisos y arreglar la lógica de hábitos en la app Android actual.
2.  **Fase 2 (Abstracción)**: Refactorizar `NotificationScheduler` detrás de una interfaz (`NotificationManager`) como sugiere la guía. Esto facilitará crear la versión de Windows después.

---

## 📝 Próximos Pasos Sugeridos
¿Por dónde te gustaría empezar?
1.  **[Prioridad Máxima]** Implementar la solicitud de permisos de notificación.
2.  **[Funcionalidad]** Mejorar el sistema de repetición de hábitos (días específicos).
3.  **[Arquitectura]** Comenzar la preparación para KMP (Koin, Room KMP).
