# Plan de Implementación: AntiProcast Web App

Este plan detalla la creación de la versión web de la aplicación "AntiProcast" (FocusLive), enfocada en organización, hábitos y motivación con IA.

## Tecnologías
- **Frontend**: React + Vite
- **Lenguaje**: JavaScript (JSX)
- **Estilos**: Vanilla CSS (Diseño Premium con CSS Variables, Glassmorphism, Animaciones)
- **Base de Datos/Auth**: Firebase (Firestore, Auth)
- **PWA**: vite-plugin-pwa
- **Gráficos**: Recharts
- **Animaciones**: Framer Motion

## Estructura del Proyecto
El proyecto web residirá en la carpeta `webapp/`.

## Pasos de Implementación

### Fase 1: Configuración Inicial (Manual)
*Nota: Se requiere instalar Node.js para ejecutar este proyecto.*
- [x] Crear estructura de carpetas manual en `webapp/`.
- [ ] Crear `package.json` con dependencias.
- [ ] Crear `vite.config.js` config PWA.
- [ ] Crear `index.html` y punto de entrada React (`src/main.jsx`).
- [ ] Configurar Firebase (`src/firebase.js`).

### Fase 2: Diseño y Estilos Base (Premium UI)
- [ ] Crear `src/index.css` con variables CSS y estilos base.
- [ ] Crear Layout principal en `App.jsx`.
- [ ] Componentes UI: Navbar/Sidebar, Card, Button.

### Fase 3: Módulos Funcionales (Notas, Horario, Metas)
- [ ] Implementar Store (Context API o Zustand) para estado local.
- [ ] Crear vistas: `Notes.jsx`, `Schedule.jsx`, `Goals.jsx`.
- [ ] Integrar Firestore para persistencia.

### Fase 4: Chat IA y Gamificación
- [ ] Implementar `Chat.jsx` con interfaz tipo mensajería.
- [ ] Integrar lógica de "IA" (simulada o API).
- [ ] Añadir sistema de progreso visual (barras, badges).

### Fase 5: Finalización
- [ ] Instrucciones de instalación y ejecución.

