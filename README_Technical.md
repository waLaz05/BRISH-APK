# Brish - Gamified Life Assistant

Brish es una aplicación de productividad gamificada diseñada para ayudar a los usuarios a gestionar sus vidas mediante un sistema de tareas, finanzas, notas y hábitos, todo acompañado de una mascota virtual que evoluciona con el progreso.

## 🚀 Guía de Inicio Rápido

### Requisitos Previos
*   Android Studio Ladybug (o superior).
*   JDK 17.
*   Archivo `google-services.json` configurado en `app/`.

### Configuración de API Keys
Para que las funciones de IA (Gemini) funcionen, crea un archivo `local.properties` en la raíz del proyecto y añade:
```properties
GEMINI_API_KEY=tu_llave_aqui
```

## 🏗️ Arquitectura
El proyecto sigue el patrón **MVVM + Clean Architecture**:
*   **UI:** Jetpack Compose para componentes modernos y reactivos.
*   **Lógica:** ViewModels que mantienen el estado persistente durante cambios de configuración.
*   **Datos:** Repositorios que abstraen la persistencia en Firebase Firestore y SharedPreferences.
*   **DI:** Hilt para una gestión limpia de dependencias.

## 📂 Organización del Proyecto
*   `data/`: Modelos, Repositorios e interfaces de APIs.
*   `ui/`: Pantallas organizadas por dominios (Planner, Finance, Home).
*   `di/`: Módulos de inyección.
*   `scheduler/`: Workers para notificaciones en segundo plano.

## 🛠️ Tecnologías Principales
*   **Kotlin 2.0.21**
*   **Hilt** (Inyección de dependencias)
*   **Firebase** (Auth, Firestore)
*   **Gemini AI** (Generación de contenido/asistencia)
*   **Glance** (Android Widgets)

## ⚖️ Licencia
Este proyecto es propiedad privada de Katchy/FocusLive. Todos los derechos reservados.
