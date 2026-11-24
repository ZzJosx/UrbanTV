package com.flixclusive.feature.mobile.home

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Directorio opt-in de streams públicos de IPTV-org para Perú.
 * Es un índice de enlaces; su presencia en una lista pública no demuestra licencia.
 * Se rechazan URL sin HTTPS y no se instala código de proveedores.
 */
private const val PUBLIC_PERU_M3U = "https://iptv-org.github.io/iptv/countries/pe.m3u"
private const val MAX_PLAYLIST_CHARS = 2_000_000
private const val MAX_PLAYLIST_LINES = 12_000
private const val MAX_CHANNELS = 500

private data class PublicChannel(
    val title: String,
    val group: String,
    val stream: String,
)

@Composable
internal fun PublicTVScreen() {
    var channels by remember { mutableStateOf<List<PublicChannel>>(emptyList()) }
    var search by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<PublicChannel?>(null) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val visible = remember(channels, search) {
        channels.filter {
            it.title.contains(search.trim(), ignoreCase = true) ||
                it.group.contains(search.trim(), ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "TV EN VIVO · PERÚ",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text("Canales públicos M3U", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Lista de enlaces de IPTV-org. No incluye Netflix, Prime Video ni Crunchyroll. " +
                        "La presencia de un canal en una lista pública no garantiza permisos de retransmisión; " +
                        "algunos canales pueden no cargar o estar bloqueados en tu región.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            channels = fetchPublicChannels()
                            if (channels.isEmpty()) error = "No se encontraron transmisiones HTTPS compatibles."
                        } catch (_: Exception) {
                            error = "No se pudo obtener la lista. Comprueba internet e inténtalo después."
                        } finally {
                            isLoading = false
                        }
                    }
                },
            ) { Text(if (channels.isEmpty()) "Cargar lista pública de Perú" else "Actualizar lista") }
        }
        if (isLoading) {
            item { Row { CircularProgressIndicator(); Text(" Buscando canales…", modifier = Modifier.padding(8.dp)) } }
        }
        if (error != null) {
            item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
        }
        item {
            TextButton(onClick = { uriHandler.openUri("https://github.com/iptv-org/iptv") }) {
                Text("Examinar origen de la lista en GitHub")
            }
        }
        if (channels.isNotEmpty()) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Buscar canal") },
                    singleLine = true,
                )
            }
            item {
                Text(
                    "Canales HTTPS encontrados: ${visible.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        items(visible, key = { it.stream }) { channel ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { selected = channel },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(channel.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        channel.group.ifBlank { "Canal público" },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("▶ Intentar reproducir transmisión HTTPS", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text(
                "Solo para enlaces que el titular distribuya libremente. No ingreses cuentas, " +
                    "contraseñas ni enlaces pirateados. UrbanTV no certifica el contenido de fuentes externas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    selected?.let { channel ->
        UrbanTVVideoPlayer(
            title = channel.title,
            video = channel.stream,
            description = channel.group.ifBlank { "Televisión en vivo" },
            credits = "Fuente: directorio público IPTV-org. Los derechos pertenecen a sus emisores.",
            onDismiss = { selected = null },
        )
    }
}

private suspend fun fetchPublicChannels(): List<PublicChannel> = withContext(Dispatchers.IO) {
    val connection = (URL(PUBLIC_PERU_M3U).openConnection() as HttpURLConnection).apply {
        connectTimeout = 12_000
        readTimeout = 12_000
        instanceFollowRedirects = false
        setRequestProperty("Accept", "application/x-mpegURL, text/plain, */*")
    }
    try {
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            throw IOException("HTTP ${connection.responseCode}")
        }
        val channels = mutableListOf<PublicChannel>()
        var lastTitle = ""
        var lastGroup = ""
        var chars = 0
        var lines = 0
        val groupPattern = Regex("group-title=\\\"([^\\\"]*)\\\"")
        connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            while (channels.size < MAX_CHANNELS && lines < MAX_PLAYLIST_LINES) {
                val line = reader.readLine()?.trim() ?: break
                lines++
                chars += line.length
                if (chars > MAX_PLAYLIST_CHARS) throw IOException("Lista demasiado grande")

                if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                    lastTitle = line.substringAfterLast(',', "Canal sin nombre").take(110)
                    lastGroup = groupPattern.find(line)?.groupValues?.getOrNull(1).orEmpty().take(70)
                } else if (line.startsWith("https://", ignoreCase = true)) {
                    val uri = Uri.parse(line)
                    if (uri.scheme == "https" && !uri.host.isNullOrBlank()) {
                        channels.add(
                            PublicChannel(
                                title = lastTitle.ifBlank { "Canal en vivo" },
                                group = lastGroup,
                                stream = line,
                            )
                        )
                    }
                    lastTitle = ""
                    lastGroup = ""
                } else if (line.isNotBlank() && !line.startsWith("#")) {
                    lastTitle = ""
                    lastGroup = ""
                }
            }
        }
        channels.distinctBy { it.stream }
    } finally {
        connection.disconnect()
    }
}
