package com.flixclusive.feature.mobile.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage

/**
 * Pequeño catálogo independiente de plugins con cortometrajes abiertos.
 *
 * Los archivos de ejemplo son distribuidos como vídeos públicos de prueba,
 * con créditos explícitos al proyecto Blender y licencias Creative Commons.
 * Nunca se realizan búsquedas de servidores no verificados ni descargas de plugins.
 */
private data class OpenFilm(
    val title: String,
    val year: Int,
    val category: String,
    val synopsis: String,
    val poster: String,
    val video: String,
    val attribution: String,
)

private const val SAMPLE_ROOT = "https://storage.googleapis.com/gtv-videos-bucket/sample/"

private val openFilms = listOf(
    OpenFilm(
        title = "Big Buck Bunny",
        year = 2008,
        category = "Animación · Cortometraje",
        synopsis = "Un conejo gigante vive una inesperada aventura en el bosque.",
        poster = SAMPLE_ROOT + "images/BigBuckBunny.jpg",
        video = SAMPLE_ROOT + "BigBuckBunny.mp4",
        attribution = "© Blender Foundation / peach.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Sintel",
        year = 2010,
        category = "Fantasía · Cortometraje",
        synopsis = "Una joven emprende un viaje para encontrar a su pequeño dragón.",
        poster = SAMPLE_ROOT + "images/Sintel.jpg",
        video = SAMPLE_ROOT + "Sintel.mp4",
        attribution = "© Blender Foundation / durian.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Tears of Steel",
        year = 2012,
        category = "Ciencia ficción · Cortometraje",
        synopsis = "Una misión en una ciudad futurista explora la relación entre humanos y robots.",
        poster = SAMPLE_ROOT + "images/TearsOfSteel.jpg",
        video = SAMPLE_ROOT + "TearsOfSteel.mp4",
        attribution = "© Blender Foundation / mango.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Elephants Dream",
        year = 2006,
        category = "Animación · Cortometraje",
        synopsis = "Dos personajes exploran un misterioso mundo de máquinas.",
        poster = SAMPLE_ROOT + "images/ElephantsDream.jpg",
        video = SAMPLE_ROOT + "ElephantsDream.mp4",
        attribution = "© Blender Foundation / elephantsdream.org · CC BY 2.5",
    ),
)

@Composable
internal fun OpenCinemaScreen() {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFilm by remember { mutableStateOf<OpenFilm?>(null) }
    val visibleFilms = remember(search) {
        openFilms.filter {
            it.title.contains(search.trim(), ignoreCase = true) ||
                it.category.contains(search.trim(), ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 88.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "URBANTV ORIGINAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Cine abierto",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Películas de prueba gratuitas y autorizadas. Reprodúcelas aquí mismo, sin instalar proveedores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar películas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Text(
                text = "Películas disponibles · ${visibleFilms.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        items(visibleFilms, key = { it.title }) { film ->
            Card(
                onClick = { selectedFilm = film },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AsyncImage(
                        model = film.poster,
                        contentDescription = "Portada de ${film.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(118.dp)
                            .height(112.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            text = film.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${film.year} · ${film.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = film.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "▶ Ver gratis",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        if (visibleFilms.isEmpty()) {
            item {
                Text(
                    text = "No encontramos títulos con esa búsqueda.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Text(
                text = "Contenido de Blender Foundation bajo licencias Creative Commons. " +
                    "UrbanTV no incluye ni desbloquea servicios de pago. " +
                    "La disponibilidad de los vídeos depende del servidor público.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    selectedFilm?.let { film ->
        OpenCinemaPlayer(film = film, onDismiss = { selectedFilm = null })
    }
}

@Composable
private fun OpenCinemaPlayer(
    film: OpenFilm,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember(film.video) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(film.video))
            prepare()
            playWhenReady = true
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
                modifier = Modifier.fillMaxSize().padding(vertical = 28.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = film.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = true
                        }
                    },
                    update = { it.player = player },
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                )
                Text(
                    text = film.category,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )
                Text(
                    text = film.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD0DAE8),
                )
                Text(
                    text = film.attribution,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0BDCD),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Si el vídeo no carga, comprueba tu conexión o inténtalo después. " +
                        "No es necesario conceder permisos especiales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0BDCD),
                )
            }
        }
    }
}
