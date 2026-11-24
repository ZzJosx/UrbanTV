package com.flixclusive.feature.mobile.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage

/**
 * Portada editorial de UrbanTV. No requiere plugins ni claves privadas.
 * El arte del héroe pertenece a un cortometraje abierto; los títulos de
 * servicios comerciales son fichas informativas, no reproducciones gratis.
 */
private val cyan = Color(0xFF39D5D1)
private val midnight = Color(0xFF080E18)
private val shadow = Color(0xFF121F31)

@Composable
internal fun UrbanTVExploreScreen() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Descubrir", "Estrenos", "Cine gratis", "Series y anime", "TV en vivo")

    Column(modifier = Modifier.fillMaxSize().background(midnight)) {
        Spacer(modifier = Modifier.height(76.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tabs.forEachIndexed { index, text ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(text) },
                )
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> UrbanTVDiscoverScreen(onNavigate = { selectedTab = it })
                1 -> TmdbLiveScreen()
                2 -> OpenCinemaScreen()
                3 -> TopPlatformsScreen()
                else -> PublicTVScreen()
            }
        }
    }
}

@Composable
private fun UrbanTVDiscoverScreen(onNavigate: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilm by remember { mutableStateOf<OpenFilm?>(null) }
    var selectedTitle by remember { mutableStateOf<FeaturedTitle?>(null) }

    val search = query.trim()
    val movieMatches = remember(search) {
        if (search.isBlank()) emptyList() else openFilms.filter {
            it.title.contains(search, ignoreCase = true) ||
                it.category.contains(search, ignoreCase = true)
        }
    }
    val titleMatches = remember(search) {
        if (search.isBlank()) emptyList() else featuredTitles.filter {
            it.name.contains(search, ignoreCase = true) ||
                it.platform.display.contains(search, ignoreCase = true) ||
                it.kind.contains(search, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "TU CINE, A TU MANERA",
                    color = cyan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Bienvenido a UrbanTV",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar películas, series y anime") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            )
        }

        if (search.isEmpty()) {
            item {
                Surface(
                    onClick = { onNavigate(1) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    color = Color(0xFF142C39),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("ESTRENOS Y TOP DEL MOMENTO", color = cyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold)
                            Text("Netflix · Prime · Max · Anime",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("Pósteres reales y tendencias de TMDB",
                                color = Color(0xFFC2D2DF),
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Text("›", color = cyan, style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
        if (search.isNotEmpty()) {
            item {
                SectionHeading(
                    title = "Resultados · ${movieMatches.size + titleMatches.size}",
                    subtitle = "Películas abiertas y fichas de plataformas",
                    onMore = { query = "" },
                    moreLabel = "Limpiar",
                )
            }
            if (movieMatches.isEmpty() && titleMatches.isEmpty()) {
                item {
                    Text(
                        "No encontramos títulos. Prueba otra búsqueda.",
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = Color(0xFFB6C6D7),
                    )
                }
            }
            movieMatches.forEach { film ->
                item(key = "free_${film.title}") {
                    SearchResultRow(
                        title = film.title,
                        subtitle = "GRATIS · ${film.year} · Reproducción en UrbanTV",
                        artwork = film.poster,
                        accent = cyan,
                        onClick = { selectedFilm = film },
                    )
                }
            }
            titleMatches.take(20).forEach { title ->
                item(key = "editorial_${title.platform.name}_${title.name}") {
                    SearchResultRow(
                        title = title.name,
                        subtitle = "${title.platform.display} · ${title.kind} · Ficha informativa",
                        artwork = null,
                        accent = title.platform.color,
                        onClick = { selectedTitle = title },
                    )
                }
            }
        } else {
            item {
                FeaturedFilmHero(
                    film = openFilms[1],
                    onWatch = { selectedFilm = openFilms[1] },
                    onBrowse = { onNavigate(2) },
                )
            }
            item {
                SectionHeading(
                    title = "Reproduce ahora",
                    subtitle = "Cortometrajes gratuitos y autorizados",
                    onMore = { onNavigate(2) },
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    openFilms.forEach { film ->
                        FilmPosterCard(film, onClick = { selectedFilm = film })
                    }
                }
            }
            item {
                SectionHeading(
                    title = "Series que dan de qué hablar",
                    subtitle = "Descubre títulos conocidos de varias plataformas",
                    onMore = { onNavigate(3) },
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    featuredTitles.filter { it.platform != Platform.CRUNCHYROLL }
                        .take(12).forEach { title ->
                            EditorialPosterCard(title, onClick = { selectedTitle = title })
                        }
                }
            }
            item {
                SectionHeading(
                    title = "Universo anime",
                    subtitle = "Aventuras, acción y fantasía",
                    onMore = { onNavigate(3) },
                    moreLabel = "Explorar",
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    featuredTitles.filter { it.platform == Platform.CRUNCHYROLL }
                        .forEach { title ->
                            EditorialPosterCard(title, onClick = { selectedTitle = title })
                        }
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    onClick = { onNavigate(4) },
                    shape = RoundedCornerShape(20.dp),
                    color = shadow,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("TV EN VIVO", color = cyan, style = MaterialTheme.typography.labelMedium)
                            Text("Canales abiertos de Perú", color = Color.White,
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Explorar transmisiones públicas compatibles",
                                color = Color(0xFFB9C9D7), style = MaterialTheme.typography.bodySmall)
                        }
                        Text("▶", color = cyan, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            item {
                Text(
                    "Los originales exclusivos de servicios de pago no se reproducen sin acceso autorizado. " +
                        "Las listas públicas no garantizan que cada emisión esté disponible o autorizada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF91A6BA),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                )
            }
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
    selectedTitle?.let { title ->
        EditorialDetailsDialog(title = title, onDismiss = { selectedTitle = null })
    }
}

@Composable
private fun FeaturedFilmHero(
    film: OpenFilm,
    onWatch: () -> Unit,
    onBrowse: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(shadow),
    ) {
        UrbanFilmArtwork(film = film, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x44080E18),
                        Color(0x99080E18),
                        Color(0xF8080E18),
                    )
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "ESTRENO DE CINE ABIERTO",
                color = cyan,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                film.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                "${film.year} · ${film.category} · Gratis",
                color = Color(0xFFDEEBEF),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                film.synopsis,
                color = Color(0xFFD1DCE6),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onWatch,
                    colors = ButtonDefaults.buttonColors(containerColor = cyan, contentColor = midnight),
                ) {
                    Text("▶  Reproducir", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onBrowse) {
                    Text("Ver catálogo")
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String,
    onMore: () -> Unit,
    moreLabel: String = "Ver todo",
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFFB4C4D6), style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onMore) {
            Text(moreLabel)
        }
    }
}

@Composable
private fun FilmPosterCard(film: OpenFilm, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(145.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(shadow),
        ) {
            UrbanFilmArtwork(film = film, modifier = Modifier.fillMaxSize())
            Surface(
                color = Color(0xDD113336),
                shape = RoundedCornerShape(topEnd = 12.dp, bottomStart = 12.dp),
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Text("GRATIS", color = cyan, style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    fontWeight = FontWeight.Black)
            }
            Text(
                "▶",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Text(film.title, color = Color.White, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${film.year} · Cortometraje", color = Color(0xFF9BAFC3),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EditorialPosterCard(title: FeaturedTitle, onClick: () -> Unit) {
    val accent = title.platform.color
    Column(
        modifier = Modifier.width(145.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.65f),
                            Color(0xFF122036),
                            Color(0xFF080E18),
                        )
                    )
                )
                .padding(12.dp),
        ) {
            Text(
                title.platform.display.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.TopStart),
            )
            Text(
                title.name,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
        Text(title.name, color = Color.White, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(title.kind + " · Ficha", color = Color(0xFF9BAFC3),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    artwork: String?,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = shadow,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.width(64.dp).height(72.dp).clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                if (artwork != null) {
                    val openFilm = openFilms.firstOrNull { it.poster == artwork }
                    if (openFilm != null) {
                        UrbanFilmArtwork(film = openFilm, modifier = Modifier.fillMaxSize())
                    } else {
                        AsyncImage(
                            model = artwork,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                } else {
                    Text("▶", color = accent, style = MaterialTheme.typography.titleLarge)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(subtitle, color = Color(0xFFAEBCD0),
                    style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Text("›", color = cyan, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun EditorialDetailsDialog(title: FeaturedTitle, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF142032),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(title.platform.display.uppercase(), color = title.platform.color,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                Text(title.name, color = Color.White, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Text(title.kind, color = Color(0xFFB5C5D6),
                    style = MaterialTheme.typography.labelMedium)
                Text(title.description, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Ficha informativa. Este título no se reproduce sin acceso autorizado. " +
                        "Comprueba su disponibilidad en Perú.",
                    color = Color(0xFFB5C5D6),
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = { uriHandler.openUri(title.platform.site) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Abrir " + title.platform.display) }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://www.justwatch.com/pe") },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Consultar dónde ver") }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    }
}
