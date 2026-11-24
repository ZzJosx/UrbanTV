package com.flixclusive.feature.mobile.home

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException

private const val TOKEN_PREFS = "urbantv_catalogo_tmdb"
private const val TOKEN_FIELD = "read_access_token"
private val cyan = Color(0xFF39D5D1)
private val deepBlue = Color(0xFF080E18)

/**
 * TMDB no permite obtener los vídeos de servicios de pago. Únicamente trae
 * metadatos, portadas y disponibilidad estimada de proveedores en Perú.
 *
 * Las credenciales nunca se incluyen en los commits ni se suben a GitHub.
 * El usuario agrega su token de lectura personal en su propio dispositivo.
 */
@Composable
internal fun TmdbLiveScreen() {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(TOKEN_PREFS, Context.MODE_PRIVATE)
    }
    var savedToken by remember { mutableStateOf(prefs.getString(TOKEN_FIELD, "").orEmpty()) }
    var tokenDraft by remember { mutableStateOf("") }
    var changingToken by remember { mutableStateOf(false) }
    var activeShelf by rememberSaveable { mutableStateOf(TmdbShelf.NEW) }
    var refresh by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var titles by remember { mutableStateOf<List<TmdbTitle>>(emptyList()) }
    var search by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<TmdbTitle?>(null) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(savedToken, activeShelf, refresh) {
        titles = emptyList()
        error = null
        if (savedToken.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        try {
            titles = TmdbCatalogClient.load(activeShelf, savedToken)
            if (titles.isEmpty()) {
                error = "No se encontraron resultados disponibles en este filtro para Perú."
            }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            error = ex.message ?: "No se pudieron cargar los datos de TMDB."
        } finally {
            isLoading = false
        }
    }

    val filtered = remember(titles, search) {
        if (search.isBlank()) titles else titles.filter {
            it.name.contains(search.trim(), ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(deepBlue),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "CATÁLOGO EN VIVO · PERÚ",
                color = cyan,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Estrenos y títulos top",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            Text(
                "Portadas y tendencias reales de TMDB, actualizadas al consultar. " +
                    "No incluye reproducción gratuita de exclusivos de pago.",
                color = Color(0xFFBCCBD9),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (savedToken.isBlank() || changingToken) {
            Surface(
                color = Color(0xFF162639),
                shape = RoundedCornerShape(17.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Conecta tu catálogo TMDB", color = Color.White,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Necesitas tu API Read Access Token gratuito de TMDB. " +
                            "Se guarda solo en el almacenamiento privado de esta app y no se publica en GitHub. " +
                            "No es una cuenta de Netflix ni desbloquea contenidos de pago.",
                        color = Color(0xFFD0DDEB), style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = tokenDraft,
                        onValueChange = { tokenDraft = it },
                        label = { Text("API Read Access Token") },
                        placeholder = { Text("Pega tu token de TMDB") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tokenDraft.isNotBlank(),
                        onClick = {
                            val token = tokenDraft.trim()
                            prefs.edit().putString(TOKEN_FIELD, token).apply()
                            savedToken = token
                            tokenDraft = ""
                            changingToken = false
                        },
                    ) { Text("Guardar y cargar estrenos") }
                    TextButton(
                        onClick = { uriHandler.openUri("https://www.themoviedb.org/settings/api") },
                    ) { Text("Obtener token en TMDB") }
                    if (savedToken.isNotBlank()) {
                        TextButton(onClick = { changingToken = false; tokenDraft = "" }) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Fuente: TMDB · Región PE", color = Color(0xFFB5C7D8),
                    style = MaterialTheme.typography.labelSmall)
                Row {
                    TextButton(onClick = { refresh++ }) { Text("Actualizar") }
                    TextButton(onClick = { changingToken = true }) { Text("Token") }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                TmdbShelf.entries.forEach { item ->
                    FilterChip(
                        selected = activeShelf == item,
                        onClick = { activeShelf = item; search = "" },
                        label = { Text(item.display) },
                    )
                }
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar entre los resultados") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator(color = cyan)
                        Text("Consultando estrenos y plataformas…", color = Color.White)
                    }
                } else if (error != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(error.orEmpty(), color = Color(0xFFFFB7B7))
                        Button(onClick = { refresh++ }) { Text("Reintentar") }
                        OutlinedButton(
                            onClick = {
                                prefs.edit().remove(TOKEN_FIELD).apply()
                                savedToken = ""
                                titles = emptyList()
                                changingToken = false
                            },
                        ) { Text("Olvidar token de TMDB") }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 112.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                "${filtered.size} resultados · ${activeShelf.display}",
                                color = Color(0xFFB6C6D6),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        items(filtered, key = { it.mediaType + it.id }) { item ->
                            TmdbPoster(item = item, onClick = { selected = item })
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(
                                    "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                                    color = Color(0xFF9CAEC0),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    "Información de proveedores suministrada por JustWatch vía TMDB. " +
                                        "La disponibilidad puede cambiar y requiere acceso autorizado.",
                                    color = Color(0xFF9CAEC0),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { TmdbTitleDialog(it, onDismiss = { selected = null }) }
}

@Composable
private fun TmdbPoster(item: TmdbTitle, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152236)),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(216.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color(0xFF203049)),
                contentAlignment = Alignment.Center,
            ) {
                if (item.posterUrl != null) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = "Póster de ${item.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("SIN PÓSTER", color = Color(0xFFD2DDEB))
                }
            }
            Text(
                item.name,
                modifier = Modifier.padding(horizontal = 9.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${if (item.mediaType == "movie") "Película" else "Serie"} · " +
                    "${item.date.take(4)} · ★ ${String.format(java.util.Locale.US, "%.1f", item.rating)}",
                modifier = Modifier.padding(start = 9.dp, end = 9.dp, bottom = 10.dp),
                color = cyan,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TmdbTitleDialog(item: TmdbTitle, onDismiss: () -> Unit) {
    val handler = LocalUriHandler.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF152338),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (item.backdropUrl != null) {
                    AsyncImage(
                        model = item.backdropUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxWidth().height(138.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                Text(item.name, color = Color.White, fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge)
                Text(
                    "${if (item.mediaType == "movie") "Película" else "Serie"} · ${item.date.take(4)}" +
                        " · ★ ${String.format(java.util.Locale.US, "%.1f", item.rating)}",
                    color = cyan, style = MaterialTheme.typography.bodySmall,
                )
                Text(item.synopsis.ifBlank { "Sin sinopsis disponible en español." },
                    color = Color(0xFFD4E0EB), style = MaterialTheme.typography.bodyMedium,
                    maxLines = 6, overflow = TextOverflow.Ellipsis)
                Text(
                    "Ficha informativa. UrbanTV no incluye enlaces gratuitos a vídeos " +
                        "protegidos por suscripción.",
                    color = Color(0xFFB0C3D6), style = MaterialTheme.typography.bodySmall,
                )
                Button(
                    onClick = { handler.openUri(item.detailUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Ver detalles en TMDB") }
                OutlinedButton(
                    onClick = {
                        handler.openUri("https://www.justwatch.com/pe/buscar?q=" +
                            Uri.encode(item.name))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Consultar dónde ver en Perú") }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    }
}
