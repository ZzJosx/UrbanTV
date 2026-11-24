package com.flixclusive.feature.mobile.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Pequeño catálogo independiente de plugins con cortometrajes abiertos.
 *
 * Los archivos de ejemplo son distribuidos como vídeos públicos de prueba,
 * con créditos explícitos al proyecto Blender y licencias Creative Commons.
 * Nunca se realizan búsquedas de servidores no verificados ni descargas de plugins.
 */
internal data class OpenFilm(
    val title: String,
    val year: Int,
    val category: String,
    val synopsis: String,
    val poster: String,
    val video: String,
    val attribution: String,
)

internal val openFilms = listOf(
    OpenFilm(
        title = "Big Buck Bunny",
        year = 2008,
        category = "Animación · Cortometraje",
        synopsis = "Un conejo gigante vive una inesperada aventura en el bosque.",
        poster = "https://commons.wikimedia.org/wiki/Special:FilePath/Big_buck_bunny_poster_big.jpg?width=600",
        video = "https://video.blender.org/static/webseed/bf1f3fb5-b119-4f9f-9930-8e20e892b898-720.mp4",
        attribution = "© Blender Foundation / peach.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Sintel",
        year = 2010,
        category = "Fantasía · Cortometraje",
        synopsis = "Una joven emprende un viaje para encontrar a su pequeño dragón.",
        poster = "https://commons.wikimedia.org/wiki/Special:FilePath/Sintel_poster.jpg?width=600",
        video = "https://video.blender.org/static/webseed/0eb052d0-fd51-43e6-aa33-ecdbf77a5d40-720.mp4",
        attribution = "© Blender Foundation / durian.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Tears of Steel",
        year = 2012,
        category = "Ciencia ficción · Cortometraje",
        synopsis = "Una misión en una ciudad futurista explora la relación entre humanos y robots.",
        poster = "https://commons.wikimedia.org/wiki/Special:FilePath/Tos-poster.png?width=600",
        video = "https://video.blender.org/static/webseed/8533ea43-4271-4a57-9694-e9d0b35e1aa1-720.mp4",
        attribution = "© Blender Foundation / mango.blender.org · CC BY 3.0",
    ),
    OpenFilm(
        title = "Elephants Dream",
        year = 2006,
        category = "Animación · Cortometraje",
        synopsis = "Dos personajes exploran un misterioso mundo de máquinas.",
        poster = "https://commons.wikimedia.org/wiki/Special:FilePath/ElephantsDreamPoster.jpg?width=600",
        video = "https://video.blender.org/static/webseed/cccc3e60-0291-4ecc-aa56-39b2e2c7d0d5-720.mp4",
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
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CINE GRATUITO · BLENDER",
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
                    UrbanFilmArtwork(
                        film = film,
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
        UrbanTVVideoPlayer(
            title = film.title,
            video = film.video,
            description = film.synopsis,
            credits = film.attribution,
            onDismiss = { selectedFilm = null },
        )
    }
}

