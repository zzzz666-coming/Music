package com.example.musicbridge

import android.content.ComponentName
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.util.Log
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.json.JSONObject

class MusicBridgeListener : NotificationListenerService() {
    private val running = AtomicBoolean(false)
    private val worker = Executors.newSingleThreadExecutor()
    @Volatile private var server: ServerSocket? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (running.compareAndSet(false, true)) worker.execute { serveLoopback() }
    }

    override fun onListenerDisconnected() {
        running.set(false)
        try { server?.close() } catch (_: Exception) { }
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        running.set(false)
        try { server?.close() } catch (_: Exception) { }
        worker.shutdownNow()
        super.onDestroy()
    }

    private fun currentTrackJson(): String = try {
        val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions = manager.getActiveSessions(ComponentName(this, MusicBridgeListener::class.java))
        val selected = sessions.firstOrNull { it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING }
            ?: sessions.firstOrNull { it.metadata != null }
        val metadata = selected?.metadata
        val state = selected?.playbackState?.state
        JSONObject().apply {
            put("title", metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "")
            put("artist", metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: "")
            put("album", metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM) ?: "")
            put("playing", state == android.media.session.PlaybackState.STATE_PLAYING)
            put("source", selected?.packageName ?: "")
        }.toString()
    } catch (e: Exception) {
        Log.w("MusicBridge", "No se pudieron leer las sesiones multimedia", e)
        "{\"title\":\"\",\"artist\":\"\",\"album\":\"\",\"playing\":false,\"source\":\"\"}"
    }

    private fun serveLoopback() {
        try {
            ServerSocket(8765, 8, InetAddress.getByName("127.0.0.1")).use { socketServer ->
                server = socketServer
                socketServer.soTimeout = 1000
                while (running.get()) {
                    try { socketServer.accept().use { respond(it) } }
                    catch (_: java.net.SocketTimeoutException) { }
                }
            }
        } catch (e: Exception) {
            if (running.get()) Log.e("MusicBridge", "No se pudo iniciar el servidor local", e)
            running.set(false)
        } finally { server = null }
    }

    private fun respond(socket: Socket) {
        try {
            socket.soTimeout = 1500
            val body = currentTrackJson().toByteArray(StandardCharsets.UTF_8)
            val headers = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\n" +
                "Access-Control-Allow-Origin: *\r\nConnection: close\r\nContent-Length: ${body.size}\r\n\r\n"
            val output = socket.getOutputStream()
            output.write(headers.toByteArray(StandardCharsets.US_ASCII))
            output.write(body)
            output.flush()
        } catch (e: Exception) { Log.d("MusicBridge", "Cliente local desconectado", e) }
    }
}
