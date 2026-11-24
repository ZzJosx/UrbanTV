package com.flixclusive.feature.mobile.home

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate

/**
 * Consulta únicamente la API oficial de TMDB.
 *
 * Nunca obtiene enlaces a vídeos de proveedores de pago ni ejecuta extensiones.
 * El token de solo lectura es proporcionado por el dueño del dispositivo y se
 * envía exclusivamente a api.themoviedb.org a través de HTTPS.
 */
internal enum class TmdbShelf(val display: String) {
    NEW("Estrenos"),
    TRENDING("Tendencias"),
    NETFLIX("Netflix"),
    PRIME("Prime Video"),
    CRUNCHYROLL("Crunchyroll"),
    MAX("Max"),
    ANIME("Anime"),
}

internal data class TmdbTitle(
    val id: Int,
    val mediaType: String,
    val name: String,
    val synopsis: String,
    val posterPath: String?,
    val backdropPath: String?,
    val date: String,
    val rating: Double,
    val popularity: Double,
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val detailUrl: String
        get() = "https://www.themoviedb.org/$mediaType/$id"
}

internal object TmdbCatalogClient {
    private const val API_ROOT = "https://api.themoviedb.org/3"

    suspend fun load(shelf: TmdbShelf, token: String): List<TmdbTitle> = withContext(Dispatchers.IO) {
        require(token.isNotBlank()) { "Falta el token de TMDB" }
        when (shelf) {
            TmdbShelf.TRENDING -> {
                fetchTitles("/trending/all/week", token, "movie",
                    mapOf("language" to "es-PE"))
            }
            TmdbShelf.NEW -> {
                val now = LocalDate.now()
                fetchTitles("/discover/movie", token, "movie",
                    mapOf(
                        "language" to "es-PE",
                        "region" to "PE",
                        "watch_region" to "PE",
                        "with_watch_monetization_types" to "flatrate",
                        "primary_release_date.gte" to now.minusMonths(5).toString(),
                        "primary_release_date.lte" to now.toString(),
                        "vote_count.gte" to "3",
                        "sort_by" to "primary_release_date.desc",
                        "include_adult" to "false",
                    ))
            }
            TmdbShelf.ANIME -> {
                fetchTitles("/discover/tv", token, "tv",
                    mapOf("language" to "es-PE", "sort_by" to "popularity.desc",
                        "with_genres" to "16", "with_original_language" to "ja",
                        "include_adult" to "false"))
            }
            else -> {
                val provider = when (shelf) {
                    TmdbShelf.NETFLIX -> setOf("Netflix")
                    TmdbShelf.PRIME -> setOf("Amazon Prime Video")
                    TmdbShelf.CRUNCHYROLL -> setOf("Crunchyroll")
                    TmdbShelf.MAX -> setOf("Max", "HBO Max")
                    else -> emptySet()
                }
                val types = if (shelf == TmdbShelf.CRUNCHYROLL) listOf("tv") else listOf("movie", "tv")
                val results = mutableListOf<TmdbTitle>()
                for (type in types) {
                    val providerIds = getProviderIds(type, token, provider)
                    if (providerIds.isEmpty()) continue
                    val params = mutableMapOf(
                        "language" to "es-PE",
                        "watch_region" to "PE",
                        "with_watch_providers" to providerIds.joinToString("|"),
                        "with_watch_monetization_types" to "flatrate",
                        "sort_by" to "popularity.desc",
                        "include_adult" to "false",
                    )
                    if (shelf == TmdbShelf.CRUNCHYROLL) {
                        params["with_genres"] = "16"
                    }
                    results += fetchTitles("/discover/$type", token, type, params)
                }
                results.distinctBy { it.mediaType to it.id }
                    .sortedByDescending { it.popularity }
                    .take(40)
            }
        }
    }

    private fun getProviderIds(type: String, token: String, providers: Set<String>): List<Int> {
        val response = getJson("/watch/providers/$type", token,
            mapOf("language" to "es-PE", "watch_region" to "PE"))
        val results = response.optJSONArray("results") ?: JSONArray()
        return buildList {
            for (i in 0 until results.length()) {
                val p = results.optJSONObject(i) ?: continue
                if (p.optString("provider_name") in providers) {
                    val id = p.optInt("provider_id", 0)
                    if (id > 0) add(id)
                }
            }
        }
    }

    private fun fetchTitles(
        path: String,
        token: String,
        defaultType: String,
        params: Map<String, String>,
    ): List<TmdbTitle> {
        val response = getJson(path, token, params)
        val arr = response.optJSONArray("results") ?: JSONArray()
        return buildList {
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val type = item.optString("media_type", defaultType).ifBlank { defaultType }
                if (type !in setOf("movie", "tv")) continue
                val name = (if (type == "movie") item.optString("title") else item.optString("name"))
                    .ifBlank { item.optString("original_name").ifBlank { item.optString("original_title") } }
                if (name.isBlank()) continue
                val id = item.optInt("id", 0)
                if (id <= 0) continue
                fun image(key: String): String? =
                    item.optString(key).takeIf { it.startsWith("/") && it.length < 200 }
                add(
                    TmdbTitle(
                        id = id,
                        mediaType = type,
                        name = name,
                        synopsis = item.optString("overview").take(850),
                        posterPath = image("poster_path"),
                        backdropPath = image("backdrop_path"),
                        date = if (type == "movie") item.optString("release_date")
                            else item.optString("first_air_date"),
                        rating = item.optDouble("vote_average", 0.0),
                        popularity = item.optDouble("popularity", 0.0),
                    )
                )
            }
        }
    }

    private fun getJson(path: String, token: String, params: Map<String, String>): JSONObject {
        require(path.startsWith("/") && !path.contains("://")) { "Ruta de API inválida" }
        val query = params.entries.joinToString("&") {
            "${encode(it.key)}=${encode(it.value)}"
        }
        val url = URL(API_ROOT + path + if (query.isBlank()) "" else "?$query")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = false
            setRequestProperty("Authorization", "Bearer " + token.trim())
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val status = conn.responseCode
            if (status == 401 || status == 403) {
                throw IOException("Token inválido o acceso no autorizado en TMDB")
            }
            if (status == 429) {
                throw IOException("TMDB limitó las solicitudes. Reintenta en unos minutos")
            }
            if (status != 200) {
                throw IOException("TMDB no respondió correctamente (HTTP $status)")
            }
            val raw = conn.inputStream.bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
            JSONObject(raw)
        } finally {
            conn.disconnect()
        }
    }

    private fun encode(s: String): String = URLEncoder.encode(s, "UTF-8")
}
