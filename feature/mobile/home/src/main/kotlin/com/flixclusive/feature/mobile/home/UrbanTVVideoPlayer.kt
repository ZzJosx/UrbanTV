package com.flixclusive.feature.mobile.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Reproductor seguro con errores visibles. Nunca instala plugins ni desactiva
 * HTTPS, y solo reproduce enlaces HTTPS de contenido autorizado.
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
    val uriHandler = LocalUriHandler.current
    var errorMessage by remember(video) { mutableStateOf<String?>(null) }
    var buffering by remember(video) { mutableStateOf(true) }
    var started by remember(video) { mutableStateOf(false) }
    val player = remember(video) { ExoPlayer.Builder(context).build() }

    DisposableEffect(player, video) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                buffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    started = true
                    errorMessage = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                buffering = false
                errorMessage = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "El servidor de vídeo rechazó la solicitud (403/404)."
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "No se pudo conectar con el servidor de vídeo."
                    PlaybackException.ERROR_CODE_DECODING_FAILED ->
                        "El formato de vídeo no es compatible con este dispositivo."
                    else -> "La reproducción falló (código ${error.errorCode})."
                }
            }
        }
        player.addListener(listener)

        val uri = Uri.parse(video)
        if (uri.scheme != "https" || uri.host.isNullOrBlank()) {
            errorMessage = "El enlace no es HTTPS o no tiene un servidor válido."
            buffering = false
        } else {
            try {
                val item = MediaItem.Builder().setUri(uri).apply {
                    if (uri.path?.endsWith(".m3u8", ignoreCase = true) == true) {
                        setMimeType(MimeTypes.APPLICATION_M3U8)
                    }
                }.build()
                player.setMediaItem(item)
                player.prepare()
                player.playWhenReady = true
            } catch (_: Exception) {
                errorMessage = "No se pudo preparar el vídeo."
                buffering = false
            }
        }

        onDispose {
            player.removeListener(listener)
            player.stop()
            player.release()
        }
    }

    // Avoid an endless blank player when a CDN never responds.
    LaunchedEffect(video, buffering, started, errorMessage) {
        if (buffering && !started && errorMessage == null) {
            delay(25_000L)
            if (buffering && !started && errorMessage == null) {
                errorMessage = "El servidor no respondió. Puedes reintentar o abrir el vídeo en el navegador."
                buffering = false
            }
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
                Box(
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = true
                            }
                        },
                        update = { it.player = player },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (buffering && errorMessage == null) {
                        CircularProgressIndicator(color = Color(0xFF39D5D1))
                    }
                    if (errorMessage != null) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                                .background(Color(0xEF080E18)).padding(12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                errorMessage.orEmpty(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                if (errorMessage != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                started = false
                                buffering = true
                                errorMessage = null
                                player.prepare()
                                player.playWhenReady = true
                            },
                        ) { Text("Reintentar") }
                        OutlinedButton(
                            onClick = { uriHandler.openUri(video) },
                        ) { Text("Abrir en navegador") }
                    }
                }
                Text(description, color = Color(0xFFD0DAE8),
                    style = MaterialTheme.typography.bodyMedium)
                Text(credits, color = Color(0xFFB0BDCD),
                    style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Si falla, comprueba la conexión o utiliza Abrir en navegador. " +
                        "No concedas permisos especiales ni instales extensiones desconocidas.",
                    color = Color(0xFFB0BDCD),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
