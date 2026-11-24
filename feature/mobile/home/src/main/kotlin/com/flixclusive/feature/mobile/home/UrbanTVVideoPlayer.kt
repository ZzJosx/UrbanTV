package com.flixclusive.feature.mobile.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Reproductor compartido para contenido abierto o medios que el usuario
 * está autorizado a reproducir. Solo admite direcciones HTTPS.
 * No descarga complementos, elude DRM ni administra cuentas externas.
 */
@Composable
internal fun UrbanTVVideoPlayer(
    title: String,
    video: String,
    description: String,
    credits: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember(video) {
        ExoPlayer.Builder(context).build().apply {
            val uri = Uri.parse(video)
            if (uri.scheme == "https" && !uri.host.isNullOrBlank()) {
                val item = MediaItem.Builder().setUri(uri).apply {
                    if (uri.path?.endsWith(".m3u8", ignoreCase = true) == true) {
                        setMimeType(MimeTypes.APPLICATION_M3U8)
                    }
                }.build()
                setMediaItem(item)
                prepare()
                playWhenReady = true
            }
        }
    }
    DisposableEffect(player) {
        onDispose {
            player.stop()
            player.release()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF080E18),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 28.dp, horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                }
                AndroidView(
                    factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                )
                Text(description, color = Color(0xFFD0DAE8), style = MaterialTheme.typography.bodyMedium)
                Text(credits, color = Color(0xFFB0BDCD), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Si el vídeo no carga, puede deberse al servidor, un bloqueo regional o al formato. " +
                        "No concedas permisos especiales para intentar reproducirlo.",
                    color = Color(0xFFB0BDCD),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
