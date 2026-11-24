# UrbanTV: estrenos reales, tendencias y plataformas (TMDB)

## Qué cambia
La nueva sección **Inicio > Estrenos** consulta The Movie Database (TMDB) para mostrar películas y series con pósteres reales, sinopsis, valoración y fecha. Los filtros son:
- **Estrenos**: películas con fecha de estreno reciente y opción de streaming de suscripción indicada en TMDB para Perú. Es una selección basada en fecha de lanzamiento y datos de disponibilidad, no prueba de que se hayan incorporado hoy.
- **Tendencias**: películas y series con mayor actividad de la semana en TMDB, no un ranking particular de Netflix.
- **Netflix**, **Prime Video**, **Max**: catálogo de películas y series que TMDB atribuye a los proveedores en Perú.
- **Crunchyroll**: series de animación con proveedor Crunchyroll en Perú.
- **Anime**: series de animación de origen japonés populares en TMDB, independientemente del proveedor.

La disponibilidad depende de región (PE), fechas y fuentes TMDB/JustWatch; puede estar incompleta o cambiar. La app no elude suscripciones, licencias ni DRM.

## Activación (en el teléfono, sin enviar tokens por chat)
1. Abre https://www.themoviedb.org/ y crea una cuenta gratuita.
2. En https://www.themoviedb.org/settings/api solicita acceso a la API conforme a sus condiciones.
3. Copia tu **API Read Access Token** (token de lectura, no contraseña).
4. Dentro de UrbanTV entra a **Inicio > Estrenos**, pega el token en la pantalla y pulsa **Guardar y cargar estrenos**.
5. El token se guarda en SharedPreferences privadas de la aplicación Android (MODE_PRIVATE). La aplicación ya desactiva backup mediante el manifiesto. No se publica en GitHub, pero no está protegido ante un dispositivo comprometido o rooteado.
6. Filtra por Netflix, Prime Video, Crunchyroll, Max, Anime, etc. Pulsa **Actualizar** para renovar resultados. Pulsa **Token** para sustituirlo, u **Olvidar token** tras un error si quieres eliminarlo.

No es necesario configurar hosting ni entregar el token al desarrollador. Los datos se descargan a demanda, no continuamente.

## Seguridad y límites
- El cliente solo se conecta a `https://api.themoviedb.org/3` y las imágenes se cargan desde `https://image.tmdb.org`.
- El token se envía como `Authorization: Bearer`; no se incluye en la URL ni en el código del repositorio.
- Se bloquean redirecciones HTTP al consultar la API.
- El catálogo es informativo: los exclusivos comerciales no se reproducen sin acceso autorizado.
- Las fichas tienen enlace a TMDB y permiten consultar dónde ver en Perú en JustWatch.
- TMDB exige atribución: “This product uses the TMDB API but is not endorsed or certified by TMDB.” La pantalla de estrenos muestra créditos y mención a los datos de disponibilidad de JustWatch.
- Revisa los términos de TMDB antes de distribuir o monetizar la app.
- Si el filtro de un proveedor no muestra títulos, puede deberse a la cobertura de datos para Perú.

## Próximos pasos
- Pruebas en dispositivo Android con un token TMDB real.
- Revisión de errores de red, tasas y caché.
- Incorporar sección «Créditos / Acerca de» en Ajustes para la atribución permanente de TMDB.
- Mejora de fichas con temporadas, episodios y tráilers oficiales.
- Ranking separado para estrenos en salas versus estrenos digitales, sin mezclarlos.
