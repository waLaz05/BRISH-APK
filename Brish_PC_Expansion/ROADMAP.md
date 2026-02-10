# 🗺️ Hoja de Ruta: Expansión de Brish a PC

## 🚀 Visión
Elevar **Brish** de una app móvil a un **Ecosistema de Productividad** completo. El objetivo es permitir a los usuarios gestionar sus hábitos, tareas y finanzas sin problemas desde su teléfono Android y su Computadora de Escritorio/Laptop (Windows), sincronizando todo en tiempo real.

## 🧠 Estrategia Principal: Kotlin Multiplatform (KMP)
En lugar de crear una aplicación separada para Windows (lo que duplicaría el trabajo de mantenimiento), transformaremos el proyecto Android existente en un proyecto **Kotlin Multiplatform**.

*   **Código Compartido**: Lograremos compartir ~95% del código.
*   **Rendimiento**: Rendimiento nativo en Windows (corriendo sobre JVM).
*   **UI**: 100% de la interfaz compartida usando **Compose Desktop**.

---

## 📅 Fases y Cronograma

### Fase 1: Cimientos (La Configuración) 🛠️
*Objetivo: Configurar el proyecto para soportar múltiples plataformas sin romper la App Android.*
1.  **Estructura del Proyecto**: Convertir el módulo `app` en una estructura KMP (`androidMain`, `desktopMain`, `commonMain`).
2.  **Configuración Gradle**: Añadir los objetivos de Escritorio (Windows/Linux/Mac).
3.  **Validación**: Asegurar que la app Android siga compilando y funcionando perfectamente.

### Fase 2: Migración de Lógica (El Cerebro) 🧠
*Objetivo: Mover la "lógica de negocio" a la capa compartida.*
1.  **Inyección de Dependencias**: Migrar de **Hilt** (solo Android) a **Koin** (Multiplataforma).
2.  **Base de Datos**: Migrar la configuración de **Room** a Room compatible con KMP (o SQLDelight).
3.  **Repositorios**: Mover `TaskRepository`, `HabitRepository`, etc., a `commonMain`.
4.  **ViewModels**: Mover ViewModels a una estructura compartida KMP.

### Fase 3: Migración de UI (La Cara) 🎨
*Objetivo: Correr exactamente las mismas pantallas en Windows.*
1.  **Componentes Compose**: Mover todas las pantallas `@Composable` a `commonMain`.
2.  **Recursos**: Mover textos, colores e imágenes a `composeResources` (compartido).
3.  **Ajustes de Escritorio**: Ajustar tamaños de ventana y adaptabilidad para pantallas grandes.

### Fase 4: Detalles de Plataforma ⚙️
1.  **Notificaciones**:
    *   *Android*: Mantener `AlarmManager`.
    *   *Windows*: Implementar notificaciones en la Bandeja del Sistema (System Tray) usando integraciones nativas.
2.  **Sincronización de Datos**: Asegurar que Firebase Auth/Firestore funcione sin problemas en Escritorio.
3.  **Distribución**: Crear el instalador `.msi` o `.exe` para Windows.

---

## 🛠️ Evolución Tecnológica

| Componente | Actual (Android) | Futuro (Ecosistema) | ¿Por qué? |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Kotlin | **Kotlin** | Cero curva de aprendizaje. |
| **UI** | Jetpack Compose | **Compose Multiplatform** | Comparte el 100% del código visual. |
| **Inyección** | Hilt (Dagger) | **Koin** | Hilt depende mucho de Android. Koin es puro Kotlin. |
| **Base de Datos** | Room | **Room KMP** | Room ahora soporta KMP oficialmente. |
| **Async** | Coroutines/Flow | **Coroutines/Flow** | El estándar de oro. |
| **Backend** | Firebase | **Firebase Kotlin SDK** | Soporte oficial Multiplataforma. |
