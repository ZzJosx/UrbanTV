# Auditoría inicial de seguridad — UrbanTV

Fecha: 2026-11-24

## Alcance
Revisión estática del APK debug generado por GitHub Actions #8 (commit c7b91e691c3f3ada654a01ca344519083cfc3401) y revisión dirigida de código Kotlin/Android. Esto NO es un certificado de ausencia de malware ni un escaneo antivirus. No se ha realizado análisis dinámico de tráfico ni pruebas en un dispositivo.

SHA-256 APK original (debug): `1741f9e993bb2f994ce13035a23da106bcc4b6368e42738334d1698147bdf246`

## Hallazgos

1. **CRÍTICO: TLS inseguro**. `core/network/.../OkHttpModule.kt` llamaba `ignoreAllSSLErrors()`. La implementación en `flixclusiveorg/core-stubs` confía en todos los certificados y todos los nombres de host. Corregido en esta rama: se usa la configuración estándar de OkHttp.
2. **ALTO: extensiones ejecutables**. `LoadProviderUseCaseImpl.kt` usa `PathClassLoader` para cargar complementos descargados. Es una función intencional, pero un proveedor no confiable puede ejecutar código en el contexto de la aplicación. Riesgo pendiente: no instalar proveedores desconocidos.
3. **PRIVACIDAD: informes de fallos**. `CrashActivity.kt` enviaba un informe a Google Forms al reiniciar la aplicación, sin un consentimiento explícito en esa pantalla. Corregido: el botón solo reinicia.
4. **PRIVACIDAD: ajustes iniciales**. Actualizaciones automáticas e informes automáticos estaban activos por defecto. Corregidos los valores por defecto y desactivada por defecto la actualización de proveedores.
5. **PERMISOS: instalación de APK y archivos**. El manifiesto original solicita `REQUEST_INSTALL_PACKAGES` y `MANAGE_EXTERNAL_STORAGE`, además de permisos de red/almacenamiento. Corregido el bloqueo del asistente para no exigir la instalación de APK desconocidas. Los permisos declarados continúan pendientes de reducir tras probar el acceso a archivos.
6. **BIBLIOTECAS**. El APK contiene 20 archivos DEX y 32 bibliotecas nativas para varias arquitecturas, incluidas bibliotecas multimedia. No se detectaron archivos APK ni extensiones .flx empaquetadas dentro del APK.
7. **INTEGRIDAD DEL ARCHIVO**. La estructura ZIP del artefacto y de la APK se comprobó sin errores. Esto no detecta malware.

## Recomendaciones para pruebas
- Probar la nueva APK solo en un emulador aislado y actualizado, sin cuentas personales.
- No conceder «Instalar aplicaciones desconocidas» ni «Acceso a todos los archivos».
- No instalar extensiones ni introducir listas IPTV o repositorios de terceros no verificados.
- Comprobar manualmente la APK con Microsoft Defender y Android Play Protect. Para mayor confianza, realizar análisis antivirus adicional y observación del tráfico de red antes de un uso cotidiano.
- Las modificaciones de esta rama requieren una nueva compilación; la APK generada en la ejecución #8 **NO** contiene estas correcciones.

## Pendiente
Análisis de dependencias, auditoría de cada proveedor, comprobación de manifiesto fusionado, comportamiento de tráfico en tiempo de ejecución, análisis antivirus externo y pruebas con Android real. No se puede afirmar «100% libre de virus».
