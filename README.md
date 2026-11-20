# PPAI 2026 · DSI · 3K4 · Grupo 9 — CU 36 Consultar seguimiento de bolsines

Implementación del CU 36 con los patrones **Adapter** (tres modelos de GPS Tracker) y **Observer**
(la pantalla y el mapa se actualizan cuando el gestor obtiene, refresca o filtra las ubicaciones).

| | |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3.5 (Spring Web + Spring Data JPA) |
| Tecnología | Web (HTML5 + CSS3 + JavaScript; Server-Sent Events; Leaflet para el mapa) |
| Base de datos | H2 embebida en modo archivo (`./data/bolsines.mv.db`) |
| Persistencia | JPA / Hibernate (Spring Data JPA) |
| API externa | API mock de GPS Tracker de la cátedra: https://github.com/LiberaeMentis/dsi-api-bolsines |

## Cómo correrlo (2 terminales)

**1. Levantar la API mock de GPS de la cátedra (puerto 8080)**

```bash
git clone https://github.com/LiberaeMentis/dsi-api-bolsines.git
cd dsi-api-bolsines
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

**2. Levantar esta aplicación (puerto 8081)**

```bash
cd bolsines-cu36
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

También se puede abrir la carpeta en IntelliJ y ejecutar `BolsinesApplication`.

**3. Abrir** http://localhost:8081 e ingresar con:

| Usuario | Clave | Para mostrar |
|---|---|---|
| `eb.cordoba` | `1234` | Flujo principal, A2, A3, A4, A5 |
| `eb.jujuy` | `1234` | A1 (no hay bolsines enviados) |

La base se crea y se carga sola la primera vez. Para volver a los datos iniciales, borrar la carpeta `data/`.
Consola de la base: http://localhost:8081/h2-console (JDBC URL `jdbc:h2:file:./data/bolsines`, usuario `sa`, sin clave).

## Guion para la defensa

| Flujo | Qué hacer | Qué se ve |
|---|---|---|
| Principal (XTR-4500L) | Ingresar como `eb.cordoba` → *Consultar ubicación de bolsines* → clic en el bolsín **101** → *Enviar correo* | CM Córdoba, 5 bolsines en la lista, 4 en el mapa; correo al GCM de Rosario (se ve en la consola de la app) |
| Los tres adaptadores | Mirar la columna *GPS* de la lista | 101 XTR-4500L (JSON), 110 NavTrack QX-7A (texto con comas), 103 y 107 GeoPulse MTR-900 (matriz) |
| Observer | *Actualizar ubicaciones* o tildar *Actualizar cada 30 s*; usar los filtros | La lista **y** el mapa se redibujan solos con una sola notificación del gestor |
| A1 | Ingresar como `eb.jujuy` → *Consultar* | "No hay bolsines en viaje" |
| A2 | Buscar el precinto `99999` | Aviso: no se encontró bolsín con ese precinto. (Con `50110` filtra el bolsín 110) |
| A3 | Ver el bolsín **111** | "Sin señal GPS": la API respondió 404. Si se apaga la API mock, todos quedan sin señal y el CU sigue funcionando |
| A4 | *Cancelar* | Vuelve al inicio |
| A5 | Seleccionar un bolsín → *No enviar* | "Consulta finalizada", no se envía correo |

## Estructura

```
src/main/java/ar/edu/utn/frc/dsi/bolsines
├── entidades/        Bolsin, CambioEstadoBolsin, Estado, ComisionMedica, Empleado, Rol, Usuario, Sesion
├── repositorios/     Spring Data JPA
├── gestores/         GestorSeguimientoBolsines   («control» · Cliente del Adapter · Sujeto del Observer)
├── pantallas/        PantallaSeguimientoBolsines («boundary» · Observador) + CanalEventosPantalla (SSE)
├── interfaces/
│   ├── gps/          IAdaptadorGPSTracker (Target), Adaptador* (Adapters), GPSTracker* (Adaptees), UbicacionBolsin
│   ├── observer/     ISujetoSeguimiento, IObservadorSeguimiento
│   ├── mapa/         InterfazGoogleMaps («boundary» · Observador)
│   └── mail/         InterfazNotificacionMail («boundary» · CU 31 simulado)
├── dto/              DatosSeguimientoBolsin, MarcadorMapa, MensajePantalla
├── sesion/           Login mínimo y ContextoSesion
└── config/           CargaDatosIniciales (datos de prueba)
```

## Datos de prueba (usuario eb.cordoba · CM-CBA-01)

| Bolsín | Precinto | GPS | Estado | Destino | Resultado |
|---|---|---|---|---|---|
| 101 | 50101 | XTR-4500L | Enviado | CM Rosario | Ubicado |
| 110 | 50110 | NavTrack QX-7A | Enviado | CM Rosario | Ubicado |
| 103 | 50103 | GeoPulse MTR-900 | Enviado | CMC (CABA) | Ubicado |
| 107 | 50107 | GeoPulse MTR-900 | Enviado | CM Mar del Plata | Ubicado |
| 111 | 50111 | XTR-4500L | Enviado | CM Tucumán | A3: la API no lo conoce |
| 120 | 50120 | XTR-4500L | Cerrado | CM Rosario | No aparece (no está Enviado) |

## Notas

* El mapa usa Leaflet con mapas de OpenStreetMap porque Google Maps necesita una API Key paga. La clase
  `InterfazGoogleMaps` es la boundary que arma los marcadores; para usar Google Maps sólo cambia el script del navegador.
  Sin internet el mapa se ve sin fondo, pero los marcadores igual aparecen.
* El envío de correo (CU 31, Microsoft Graph) está **simulado**: el correo se arma y se escribe en la consola.
* Las claves se guardan en texto plano porque el login está fuera del alcance del CU.
