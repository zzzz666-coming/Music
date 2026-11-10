package com.example.musicbridge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 56, 40, 40)
        }
        layout.addView(TextView(this).apply {
            text = "MusicBridge\n\n" +
                "Conecta la música de Android con el HUD de Minecraft.\n\n" +
                "CONFIGURACIÓN\n" +
                "1. Tocá el botón de abajo.\n" +
                "2. Buscá MusicBridge y habilitá el acceso a notificaciones.\n" +
                "3. Volvé acá y reproducí música en Spotify o YouTube Music.\n" +
                "4. Dejá esta app instalada y abrí Minecraft desde Zalith con el mod.\n\n" +
                "La app lee metadatos multimedia y escucha solo en 127.0.0.1. No envía datos a Internet."
            textSize = 17f
        })
        layout.addView(Button(this).apply {
            text = "Configurar acceso a música"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        })
        setContentView(layout)
    }
}
