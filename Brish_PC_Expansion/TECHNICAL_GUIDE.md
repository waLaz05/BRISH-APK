# 🛠️ Brish PC Expansion: Guía Técnica de Implementación

Este documento describe los pasos técnicos específicos requeridos para convertir la **App Android Brish** en un proyecto **Kotlin Multiplatform (KMP)** para soportar Windows Desktop.

---

## 🛑 Pre-requisitos
1.  **Backup**: Asegúrate de que la carpeta `Android` esté respaldada o guardada en Git.
2.  **IDE**: Usa **Android Studio (Ladybug o más nuevo)** o **IntelliJ IDEA Ultimate** con el plugin KMP.

---

## Paso 1: Migración de Inyección de Dependencias (Hilt -> Koin)
*Hilt está estrictamente atado al Ciclo de Vida de Android. Debemos cambiar a Koin, que es ligero y listo para KMP.*

### Acciones:
1.  **Eliminar Hilt**:
    *   Borrar `@HiltAndroidApp`, `@AndroidEntryPoint`, `@Inject`.
    *   Borrar dependencias `kapt` de Hilt en `build.gradle`.
2.  **Añadir Koin**:
    *   Implementar `io.insert-koin:koin-core` y dependencias de compose.
3.  **Crear Módulos**:
    *   `appModule`: Define Singletons para Repositorios y Base de Datos.
    *   `viewModelModule`: Define ViewModels.
4.  **Inicializar**:
    *   *Android*: En `FocusLiveApp.kt`, llamar a `startKoin { androidContext(this) ... }`.
    *   *Escritorio*: En `main.kt`, llamar a `startKoin { ... }`.

---

## Paso 2: Migración de Base de Datos (Room KMP)
*Room ahora soporta KMP (desde v2.7.0-alpha), así que no necesitamos reescribir SQL.*

### Acciones:
1.  **Actualizar Dependencias**: Actualizar Room a la última versión KMP.
2.  **Mover Esquema**: Mover Entidades (`Task`, `Habit`) y DAOs a `commonMain`.
3.  **Constructor de Base de Datos**:
    *   Crear un `getDatabaseBuilder()` genérico en `commonMain` (expect/actual).
    *   *Implementación Android*: Retorna `Room.databaseBuilder(context, ...)`
    *   *Implementación Escritorio*: Retorna `Room.databaseBuilder<AppDatabase>(name)` guardando en la carpeta de usuario.

---

## Paso 3: Lógica Compartida (La Carpeta `commonMain`)
*El núcleo de la migración.*

### Acciones:
1.  **Crear Módulo**: Crear un nuevo módulo Gradle llamado `shared`.
2.  **Mover Lógica**:
    *   Cortar `data/` (Repositorios, DTOs) del módulo `app`.
    *   Pegar en `shared/commonMain/kotlin/...`.
3.  **Mover UI**:
    *   Cortar `ui/` (Pantallas, Componentes) del módulo `app`.
    *   Pegar en `shared/commonMain/kotlin/...`.
4.  **Manejo de Recursos**:
    *   Migrar `R.string.x` y `R.drawable.x` a **Compose Multiplatform Resources** (`Res.string.x`).

---

## Paso 4: El Punto de Entrada en Escritorio (`desktopMain`)

### Crear `main.kt`:
```kotlin
fun main() = application {
    val windowState = rememberWindowState(width = 1200.dp, height = 800.dp)
    
    // Inicializar Koin
    startKoin {
        modules(appModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Ecosistema Brish",
        state = windowState
    ) {
        // Tema Compartido y Contenido
        BrishTheme {
            MainScreen()
        }
    }
}
```

---

## Paso 5: Notificaciones (Específico por Plataforma)

Como `AlarmManager` es solo de Android, manejamos esto vía `expect/actual`:

1.  **Interfaz**: Crear `interface NotificationManager { fun schedule(item: Task) }` en `commonMain`.
2.  **Android**: Implementar usando `AlarmManager` (lógica existente).
3.  **Escritorio**: Implementar usando un demonio en segundo plano o un simple bucle `Timer` que revise cada minuto si una tarea venció, y muestre una Notificación de Bandeja.

---

## ⚠️ Desafíos Conocidos y Soluciones

| Desafío | Solución |
| :--- | :--- |
| **Firebase Auth** | Usar `gitlive/firebase-kotlin-sdk` que soporta Auth en Escritorio. |
| **Context** | Eliminar `Context` de ViewModels. Pasar solo datos necesarios o usar `Koin` para inyectar ayudas de plataforma. |
| **Navegación** | Usar `JetBrains Navigation` (copia de Android Navigation para KMP) o `Decompose`. |
