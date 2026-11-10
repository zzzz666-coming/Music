package com.example.musicbridge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class MusicBridgeClient implements ClientModInitializer {
    private static final String ENDPOINT = "http://127.0.0.1:8765/now-playing";
    private static final AtomicReference<Track> TRACK = new AtomicReference<>(Track.DISCONNECTED);
    private static final ScheduledExecutorService POLLER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "MusicBridge-poller");
        thread.setDaemon(true);
        return thread;
    });

    @Override public void onInitializeClient() {
        POLLER.scheduleWithFixedDelay(MusicBridgeClient::pollAndroid, 0, 2, TimeUnit.SECONDS);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("musicbridge", "track_hud"), MusicBridgeClient::renderHud);
    }

    private static void pollAndroid() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(ENDPOINT).toURL().openConnection();
            connection.setConnectTimeout(400);
            connection.setReadTimeout(400);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String title = stringValue(json, "title");
                String artist = stringValue(json, "artist");
                boolean playing = json.has("playing") && !json.get("playing").isJsonNull() && json.get("playing").getAsBoolean();
                TRACK.set(new Track(title, artist, playing, true));
            }
        } catch (Exception ignored) { TRACK.set(Track.DISCONNECTED); }
        finally { if (connection != null) connection.disconnect(); }
    }

    private static String stringValue(JsonObject json, String key) {
        return !json.has(key) || json.get(key).isJsonNull() ? "" : json.get(key).getAsString();
    }

    private static void renderHud(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
        Track track = TRACK.get();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) return;
        String title, artist, status;
        if (!track.connected()) {
            title = "MusicBridge desconectado"; artist = "Abrí la app MusicBridge en Android"; status = "!";
        } else if (track.title().isBlank()) {
            title = "No hay música detectada"; artist = "Reproducí algo en Spotify o YouTube Music"; status = "♪";
        } else {
            title = track.title(); artist = track.artist(); status = track.playing() ? "▶" : "Ⅱ";
        }
        int x = 8, y = 8;
        int maxTextWidth = Math.max(80, minecraft.getWindow().getGuiScaledWidth() - 40);
        title = fit(title, minecraft, maxTextWidth - 22);
        artist = fit(artist, minecraft, maxTextWidth - 22);
        int width = Math.min(Math.max(minecraft.font.width(title), minecraft.font.width(artist)) + 28, maxTextWidth);
        graphics.fill(x - 4, y - 4, x + width, y + 29, 0x99000000);
        graphics.drawString(minecraft.font, status + "  " + title, x, y, 0xFFFFFFFF, false);
        graphics.drawString(minecraft.font, artist, x + 18, y + 12, 0xFFCCCCCC, false);
    }

    private static String fit(String value, Minecraft minecraft, int maxWidth) {
        String result = value == null ? "" : value;
        while (!result.isEmpty() && minecraft.font.width(result) > maxWidth) result = result.substring(0, result.length() - 1);
        if (!result.equals(value) && result.length() > 1) {
            while (!result.isEmpty() && minecraft.font.width(result + "…") > maxWidth) result = result.substring(0, result.length() - 1);
            result += "…";
        }
        return result;
    }

    private record Track(String title, String artist, boolean playing, boolean connected) {
        private static final Track DISCONNECTED = new Track("", "", false, false);
    }
}
