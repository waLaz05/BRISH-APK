# 🚀 Guía de Despliegue a GitHub para Wlaz Studios

Hola, como conoces Linux, esto será pan comido para ti.
Aquí tienes los comandos exactos para subir tu proyecto.

## 1. Preparación Previa
Asegúrate de haber creado un **Nuevo Repositorio** vacío en tu cuenta de GitHub (sin README, sin .gitignore).
Copia la URL del repositorio (termina en `.git`, ej: `https://github.com/waLaz05/wlaz-studios.git`).

## 2. Comandos de la Terminal
Abre tu terminal en la carpeta `WlazStudios` y ejecuta lo siguiente en orden:

```bash
# 1. Asegurar que estamos en la rama principal correcta
git branch -M main

# 2. Conectar tu repositorio local con el remoto (REEMPLAZA LA URL)
git remote add origin https://github.com/ TU_USUARIO_AQUI / TU_REPO_AQUI.git

# 3. Verificar la conexión
git remote -v

# 4. Subir el código por primera vez
git push -u origin main
```

## 3. Comandos Útiles para el Futuro

Cada vez que hagas cambios y quieras guardarlos en la nube:

```bash
git add .
git commit -m "Descripción de lo que cambiaste"
git push
```

## 4. Notas Adicionales
*   Si te pide credenciales, usa tu usuario de GitHub y tu **Personal Access Token** (no tu contraseña de login).
*   Si cometiste un error en el `git init` anterior, puedes borrar la carpeta oculta `.git` y empezar de cero con `git init`.

¡Listo para despegar! 🌌
