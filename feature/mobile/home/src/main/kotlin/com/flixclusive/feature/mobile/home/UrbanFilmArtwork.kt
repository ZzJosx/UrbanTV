package com.flixclusive.feature.mobile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Artwork for open films. This displays an attractive, readable fallback
 * immediately, even when the network is offline or an image server rejects
 * the request. Official film artwork is retrieved from Wikimedia Commons.
 */
@Composable
internal fun UrbanFilmArtwork(
    film: OpenFilm,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val colors = when (film.title) {
        "Sintel" -> listOf(Color(0xFF493758), Color(0xFF1B3250), Color(0xFF09111D))
        "Big Buck Bunny" -> listOf(Color(0xFF317C6D), Color(0xFF17536A), Color(0xFF0A1A2B))
        "Tears of Steel" -> listOf(Color(0xFF813E56), Color(0xFF314B65), Color(0xFF091320))
        else -> listOf(Color(0xFF765C95), Color(0xFF293B62), Color(0xFF0B1625))
    }

    Box(
        modifier = modifier.background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                "URBANTV",
                color = Color(0xFF6FE3DE),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                film.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "CINE ABIERTO · ${film.year}",
                color = Color(0xFFD0E1ED),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
        // A failed or unavailable image does not hide the fallback artwork.
        AsyncImage(
            model = film.poster,
            contentDescription = "Póster de ${film.title}",
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
