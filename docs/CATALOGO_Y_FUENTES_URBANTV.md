# UrbanTV — Catálogo y fuentes multimedia (fase de exploración)

## Alcance
UrbanTV ofrece tres áreas de exploración cuando no hay catálogos de extensiones instalados:

1. **Cine gratuito**: 4 cortometrajes abiertos de Blender Foundation reproducibles con Media3. Los enlaces de ejemplo son del almacenamiento público de Google para vídeos de muestra; disponibilidad y condiciones sujetas a los titulares.
2. **Plataformas**: selección editorial local de 33 títulos de series, anime y películas asociados a Netflix, Prime Video, Crunchyroll, Disney+, Max y Apple TV+. **No reproduce esos títulos** ni garantiza su disponibilidad actual en Perú. Cada tarjeta permite abrir el sitio oficial del servicio y consultar JustWatch Perú.
3. **TV en vivo**: tras tocar "Cargar lista pública de Perú", descarga un directorio M3U desde `https://iptv-org.github.io/iptv/countries/pe.m3u`. No se descarga ni ejecuta código. Solo se aceptan URLs de streaming HTTPS; se limita el tamaño de la lista (2 MB), líneas (12.000) y canales (500). Las emisiones pueden fallar, estar geobloqueadas o carecer de derechos de retransmisión.

## Lo que no hace
- No desbloquea películas, series ni anime de pago.
- No importa repositorios de proveedores ni instala APK externas.
- No intenta eludir DRM, credenciales, restricciones de suscripción o límites geográficos.
- No incorpora playlists de credenciales privadas o enlaces pirateados.
- No rastrea los títulos ni obtiene posters en tiempo real todavía.

## Riesgos y pruebas pendientes
- **Revisión jurídica por stream**: una URL pública no garantiza permiso para retransmitirla. Evitar canales no autorizados.
- **Reproducción real**: verificar cada película y un conjunto de canales HTTPS/HLS en Android 13/14.
- **Disponibilidad**: títulos por región cambian continuamente. La selección editorial no es una clasificación actualizada.
- **Imágenes**: para portadas y datos dinámicos, integrar TMDB con atribución adecuada y cumplir su licencia; **no** guardar un token secreto en el repositorio.
- **Seguridad**: observar tráfico de red, comprobar dependencias y no instalar complementos desconocidos.

## Referencias
- https://github.com/iptv-org/iptv
- https://github.com/streamline-iptv/iptv-playlists
- https://github.com/nexonic-technologies/nexonic-iptv
- https://www.justwatch.com/pe
- https://developer.themoviedb.org/docs/faq

No publicar una APK como servicio de streaming comercial sin revisar los derechos correspondientes.
