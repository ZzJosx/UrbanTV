package com.flixclusive.feature.mobile.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Catálogo editorial de títulos conocidos. No contiene archivos de vídeo
 * comerciales, descifrado DRM ni enlaces a proveedores no autorizados.
 * Los títulos NO representan un ranking actualizado ni garantizan disponibilidad en Perú.
 * Para rankings dinámicos y pósteres oficiales, integrar TMDB con atribución y API propia.
 */
private enum class Platform(val display: String, val site: String, val color: Color) {
    NETFLIX("Netflix", "https://www.netflix.com/pe/", Color(0xFFE50914)),
    PRIME("Prime Video", "https://www.primevideo.com/", Color(0xFF00A8E1)),
    CRUNCHYROLL("Crunchyroll", "https://www.crunchyroll.com/", Color(0xFFFF9000)),
    DISNEY("Disney+", "https://www.disneyplus.com/", Color(0xFF6DAEFF)),
    MAX("Max", "https://www.max.com/", Color(0xFF9977FF)),
    APPLE("Apple TV+", "https://tv.apple.com/pe", Color(0xFFC3DBD7)),
}

private data class FeaturedTitle(
    val name: String,
    val platform: Platform,
    val kind: String,
    val description: String,
)

private val featuredTitles = listOf(
    FeaturedTitle("Stranger Things", Platform.NETFLIX, "Serie", "Misterio y ciencia ficción en Hawkins."),
    FeaturedTitle("Wednesday", Platform.NETFLIX, "Serie", "Una investigación sobrenatural en Nevermore."),
    FeaturedTitle("El juego del calamar", Platform.NETFLIX, "Serie", "Competencias extremas con secretos y tensión."),
    FeaturedTitle("La casa de papel", Platform.NETFLIX, "Serie", "Una banda planea un gran atraco."),
    FeaturedTitle("Dark", Platform.NETFLIX, "Serie", "Viajes en el tiempo y secretos familiares."),
    FeaturedTitle("El irlandés", Platform.NETFLIX, "Película", "Drama criminal dirigido por Martin Scorsese."),
    FeaturedTitle("Fallout", Platform.PRIME, "Serie", "Supervivencia en un mundo posapocalíptico."),
    FeaturedTitle("The Boys", Platform.PRIME, "Serie", "Superhéroes y corrupción en una sátira oscura."),
    FeaturedTitle("Reacher", Platform.PRIME, "Serie", "Acción, suspenso e investigación."),
    FeaturedTitle("Invincible", Platform.PRIME, "Serie animada", "Un joven héroe descubre grandes secretos."),
    FeaturedTitle("Los anillos de poder", Platform.PRIME, "Serie", "Fantasía ambientada en la Tierra Media."),
    FeaturedTitle("La guerra del mañana", Platform.PRIME, "Película", "Una batalla por el futuro de la humanidad."),
    FeaturedTitle("Jujutsu Kaisen", Platform.CRUNCHYROLL, "Anime", "Hechiceros y maldiciones enfrentan grandes amenazas."),
    FeaturedTitle("Demon Slayer", Platform.CRUNCHYROLL, "Anime", "Tanjiro combate demonios para ayudar a su hermana."),
    FeaturedTitle("Solo Leveling", Platform.CRUNCHYROLL, "Anime", "Un cazador débil obtiene un poder inesperado."),
    FeaturedTitle("Chainsaw Man", Platform.CRUNCHYROLL, "Anime", "Acción sobrenatural y demonios."),
    FeaturedTitle("One Piece", Platform.CRUNCHYROLL, "Anime", "La aventura pirata de Luffy y su tripulación."),
    FeaturedTitle("SPY x FAMILY", Platform.CRUNCHYROLL, "Anime", "Una familia singular esconde grandes secretos."),
    FeaturedTitle("Frieren", Platform.CRUNCHYROLL, "Anime", "Fantasía, memoria y el paso del tiempo."),
    FeaturedTitle("The Mandalorian", Platform.DISNEY, "Serie", "Un cazarrecompensas recorre la galaxia."),
    FeaturedTitle("Loki", Platform.DISNEY, "Serie", "El dios del engaño y una aventura multiversal."),
    FeaturedTitle("Andor", Platform.DISNEY, "Serie", "El inicio de una rebelión galáctica."),
    FeaturedTitle("Encanto", Platform.DISNEY, "Película", "Una familia extraordinaria y su hogar mágico."),
    FeaturedTitle("Coco", Platform.DISNEY, "Película", "Música y familia en una aventura inolvidable."),
    FeaturedTitle("The Last of Us", Platform.MAX, "Serie", "Supervivencia y humanidad en un mundo devastado."),
    FeaturedTitle("La casa del dragón", Platform.MAX, "Serie", "Poder y conflicto en la dinastía Targaryen."),
    FeaturedTitle("The White Lotus", Platform.MAX, "Serie", "Drama y misterio durante unas vacaciones."),
    FeaturedTitle("El pingüino", Platform.MAX, "Serie", "Crimen y poder en Gotham."),
    FeaturedTitle("Duna", Platform.MAX, "Película", "Intrigas de poder en el desierto de Arrakis."),
    FeaturedTitle("Severance", Platform.APPLE, "Serie", "Un trabajo divide misteriosamente la memoria."),
    FeaturedTitle("Silo", Platform.APPLE, "Serie", "Una comunidad vive bajo tierra y guarda secretos."),
    FeaturedTitle("Foundation", Platform.APPLE, "Serie", "Ciencia ficción inspirada en Isaac Asimov."),
    FeaturedTitle("Ted Lasso", Platform.APPLE, "Serie", "Comedia sobre fútbol, liderazgo y amistad."),
)

@Composable
internal fun TopPlatformsScreen() {
    var search by rememberSaveable { mutableStateOf("") }
    var platform by rememberSaveable { mutableStateOf("Todas") }
    var selected by remember { mutableStateOf<FeaturedTitle?>(null) }
    val filters = listOf("Todas") + Platform.entries.map { it.display }
    val visible = featuredTitles.filter {
        (platform == "Todas" || it.platform.display == platform) &&
            (search.isBlank() ||
                it.name.contains(search.trim(), ignoreCase = true) ||
                it.kind.contains(search.trim(), ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DESTACADOS PARA EXPLORAR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Películas, series y anime",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Una selección de ${featuredTitles.size} títulos conocidos. " +
                        "Consulta dónde verlos legalmente en Perú. " +
                        "Su disponibilidad puede cambiar según la plataforma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar título o categoría") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                filters.forEach { item ->
                    FilterChip(
                        selected = platform == item,
                        onClick = { platform = item },
                        label = { Text(item) },
                    )
                }
            }
        }
        items(visible, key = { it.platform.display + it.name }) { item ->
            Card(
                onClick = { selected = item },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(color = item.platform.color.copy(alpha = 0.18f), shape = MaterialTheme.shapes.medium) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.kind.take(1).uppercase(),
                                fontWeight = FontWeight.Black,
                                color = item.platform.color,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${item.platform.display} · ${item.kind}",
                            color = item.platform.color,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            item.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        if (visible.isEmpty()) {
            item { Text("No hay títulos para este filtro.") }
        }
        item {
            Text(
                "Catálogo informativo, no una lista de archivos de reproducción. " +
                    "Los títulos requieren las condiciones de acceso de cada servicio. " +
                    "Esta selección es editorial, no un ranking en tiempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    selected?.let { title ->
        val handler = LocalUriHandler.current
        Dialog(onDismissRequest = { selected = null }) {
            Surface(shape = MaterialTheme.shapes.large) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(title.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${title.platform.display} · ${title.kind}", color = title.platform.color)
                    Text(title.description)
                    Text(
                        "Esta ficha no incluye el vídeo. Para verlo en la plataforma " +
                            "se necesita el acceso correspondiente. Comprueba su disponibilidad en Perú.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { handler.openUri(title.platform.site) },
                    ) { Text("Abrir ${title.platform.display}") }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { handler.openUri("https://www.justwatch.com/pe") },
                    ) { Text("Consultar dónde ver en Perú") }
                    TextButton(onClick = { selected = null }) { Text("Cerrar") }
                }
            }
        }
    }
}
