# MusicBridge — Android + Minecraft Fabric 1.21.11

MusicBridge conecta una app Android con un mod cliente Fabric para mostrar en el HUD la canción que se reproduce en el mismo teléfono.

Incluye:
- App Android que lee sesiones multimedia mediante NotificationListenerService.
- Servidor HTTP limitado a 127.0.0.1:8765 (solo loopback).
- Mod Fabric 1.21.11 que consulta fuera del hilo de renderizado y muestra título, artista y estado.
- GitHub Actions para compilar APK debug y JAR y publicarlos como artefactos.

## Instalación
1. Instalar el APK de MusicBridge.
2. Abrirlo y habilitar el acceso especial a notificaciones para MusicBridge.
3. Abrir Spotify, YouTube Music u otro reproductor compatible y reproducir una canción.
4. Iniciar Minecraft Java 1.21.11 con Fabric y Fabric API desde Zalith Launcher 2.
5. Copiar el JAR musicbridge a la carpeta mods.

La app no solicita credenciales de Spotify ni envía metadatos a Internet. El acceso a notificaciones es un permiso sensible: habilitalo solo si aceptás que lea título y artista.

## Compatibilidad
Preparado para Android 8+ y Minecraft 1.21.11 con Java 21. Aún debe validarse en Galaxy A34 5G y Zalith Launcher 2. Algunos reproductores pueden no publicar metadatos de sesión. El APK debug no está firmado para Play Store.

Los binarios aparecen como artefactos en la pestaña Actions después de ejecutar el workflow.
