/*
 * Vista web de PantallaUbicacionBolsines.
 * - Las acciones del EB se envían al servidor (POST /api/pantalla-seguimiento/...).
 * - Todo lo que el sistema muestra llega por eventos (Server-Sent Events): así, cuando los observadores
 *   del gestor reciben actualizar(...), la lista y el mapa se redibujan solos.
 */
const API = '/api/pantalla-seguimiento';
const $ = (id) => document.getElementById(id);

const estado = {
  bolsines: [],
  cmDestino: new Map(),   // código -> nombre (se arma con la primera lista completa)
  seleccionado: null,
  consultaActiva: false,
  timer: null,
  eventos: null
};

// ---------------------------------------------------------------- mapa (librería de mapas)
const mapa = L.map('mapa', { zoomControl: true }).setView([-34.6, -64.2], 5);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 18,
  attribution: '&copy; OpenStreetMap'
}).addTo(mapa);
// Fondo de respaldo (contorno de Sudamérica) por si no hay internet para los mapas de OpenStreetMap
if (window.SUDAMERICA) {
  L.geoJSON(window.SUDAMERICA, {
    style: (f) => ({ color: '#8aa3bd', weight: 1, fillColor: String(f.id) === '032' ? '#eef3f8' : '#f7f9fb', fillOpacity: 1 })
  }).addTo(mapa).bringToBack();
}
const capaMarcadores = L.layerGroup().addTo(mapa);

function dibujarMarcadores(marcadores) {
  mapa.invalidateSize();
  capaMarcadores.clearLayers();
  const puntos = [];
  marcadores.forEach((m) => {
    const marcador = L.marker([m.latitud, m.longitud], { title: m.titulo })
      .bindPopup(`<b>${m.titulo}</b><br>${m.detalle}<br><a href="#" data-sel="${m.numeroBolsin}">Informar ubicación…</a>`)
      .on('click', () => seleccionar(m.numeroBolsin));
    marcador.addTo(capaMarcadores);
    puntos.push([m.latitud, m.longitud]);
  });
  const encuadrar = () => {
    mapa.invalidateSize();
    if (puntos.length === 1) mapa.setView(puntos[0], 7);
    if (puntos.length > 1) mapa.fitBounds(puntos, { padding: [60, 60], maxZoom: 8 });
  };
  encuadrar();
  setTimeout(encuadrar, 120);
}

// ---------------------------------------------------------------- vistas
function mostrarVista(id) {
  ['vista-inicio', 'vista-seguimiento', 'vista-vacia', 'vista-fin'].forEach((v) => $(v).classList.toggle('oculto', v !== id));
  if (id === 'vista-seguimiento') setTimeout(() => mapa.invalidateSize(), 50);
}

function aviso(tipo, texto) {
  const div = document.createElement('div');
  div.className = `aviso ${tipo}`;
  div.textContent = texto;
  $('avisos').appendChild(div);
  setTimeout(() => div.remove(), tipo === 'error' || tipo === 'advertencia' ? 7000 : 4000);
}

function dibujarLista(bolsines) {
  const lista = $('lista');
  lista.innerHTML = '';
  $('contador').textContent = `${bolsines.length} ${bolsines.length === 1 ? 'bolsín' : 'bolsines'}`;
  bolsines.forEach((b) => {
    const item = document.createElement('article');
    item.className = 'item' + (b.ubicado ? '' : ' sin-ubicacion') + (estado.seleccionado === b.numeroBolsin ? ' seleccionado' : '');
    item.tabIndex = 0;
    item.innerHTML = `
      <div class="item-cabecera">
        <span class="item-numero">Bolsín N° ${b.numeroBolsin}</span>
        ${b.ubicado ? '<span class="insignia insignia-ok">En viaje</span>' : '<span class="insignia insignia-alerta">Sin señal GPS</span>'}
      </div>
      <dl class="item-datos">
        <dt>Destino</dt><dd>${b.nombreCMDestino}</dd>
        <dt>Precinto</dt><dd>${b.numeroPrecinto ?? '—'}</dd>
        <dt>GPS</dt><dd>${b.modeloGPSTracker}</dd>
        <dt>Últ. act.</dt><dd>${b.ubicado ? b.fechaHoraUltimaActualizacion : '—'}</dd>
      </dl>
      ${b.ubicado ? '' : `<div class="motivo">⚠ El GPS Tracker no pudo informar la ubicación.</div>`}`;
    item.addEventListener('click', () => seleccionar(b.numeroBolsin));
    item.addEventListener('keydown', (e) => { if (e.key === 'Enter') seleccionar(b.numeroBolsin); });
    lista.appendChild(item);
  });
}

function cargarCMDestino(bolsines) {
  bolsines.forEach((b) => estado.cmDestino.set(b.codigoCMDestino, b.nombreCMDestino));
  const select = $('filtro-cm');
  const valor = select.value;
  select.innerHTML = '<option value="">Todas</option>';
  [...estado.cmDestino.entries()].sort((a, b) => a[1].localeCompare(b[1])).forEach(([codigo, nombre]) => {
    const op = document.createElement('option');
    op.value = codigo;
    op.textContent = nombre;
    select.appendChild(op);
  });
  select.value = valor;
}

function abrirConfirmacion(datos) {
  $('resumen-bolsin').innerHTML = `
    <dt>Bolsín</dt><dd>N° ${datos.numeroBolsin}</dd>
    <dt>CM destino</dt><dd>${datos.nombreCMDestino}</dd>
    <dt>Última actualización</dt><dd>${datos.fechaHoraUltimaActualizacion}</dd>
    <dt>Coordenadas</dt><dd>${datos.latitud.toFixed(4)}, ${datos.longitud.toFixed(4)}</dd>`;
  $('modal-confirmacion').classList.add('abierto');
  $('btn-enviar').focus();
}
const cerrarConfirmacion = () => $('modal-confirmacion').classList.remove('abierto');

function detenerAutoActualizacion() {
  if (estado.timer) clearInterval(estado.timer);
  estado.timer = null;
  $('auto-actualizar').checked = false;
}

// ---------------------------------------------------------------- eventos que llegan del servidor
function conectarEventos() {
  return new Promise((resolver) => {
    if (estado.eventos && estado.eventos.readyState === 1) { resolver(); return; }
    const es = new EventSource(`${API}/eventos`);
    estado.eventos = es;
    es.onopen = () => resolver();
    es.onerror = () => { /* EventSource reintenta solo */ };

    es.addEventListener('pantalla', () => {
      estado.consultaActiva = true;
      estado.cmDestino.clear();
      estado.seleccionado = null;
      mostrarVista('vista-seguimiento');
    });
    es.addEventListener('cm', (e) => { $('cm-origen').textContent = 'CM origen: ' + JSON.parse(e.data).nombre; });
    // Observador PantallaUbicacionBolsines.actualizar(...)
    es.addEventListener('bolsines', (e) => {
      estado.bolsines = JSON.parse(e.data);
      cargarCMDestino(estado.bolsines);
      dibujarLista(estado.bolsines);
      $('ultima-act').textContent = 'Consultado a las ' + new Date().toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    });
    // Observador InterfazGoogleMaps.actualizar(...)
    es.addEventListener('mapa', (e) => dibujarMarcadores(JSON.parse(e.data)));
    es.addEventListener('solicitarSeleccion', (e) => { $('leyenda').textContent = JSON.parse(e.data).texto; });
    es.addEventListener('confirmacion', (e) => abrirConfirmacion(JSON.parse(e.data)));
    es.addEventListener('mensaje', (e) => { const m = JSON.parse(e.data); aviso(m.tipo, m.texto); });
    es.addEventListener('sinBolsines', () => mostrarVista('vista-vacia'));
    es.addEventListener('notificacionExitosa', (e) => {
      const d = JSON.parse(e.data);
      cerrarConfirmacion();
      $('icono-fin').textContent = '✅';
      $('titulo-fin').textContent = 'Notificación enviada';
      $('texto-fin').textContent = `Se informó la ubicación del bolsín N° ${d.numeroBolsin} a ${d.email}.`;
      mostrarVista('vista-fin');
    });
    es.addEventListener('finCU', () => {
      estado.consultaActiva = false;
      detenerAutoActualizacion();
      if (!$('vista-seguimiento').classList.contains('oculto') && estado.ultimaAccion === 'no-enviar') {
        $('icono-fin').textContent = 'ℹ️';
        $('titulo-fin').textContent = 'Consulta finalizada';
        $('texto-fin').textContent = 'No se envió ningún correo electrónico.';
        mostrarVista('vista-fin');
      } else if (estado.ultimaAccion === 'cancelar') {
        mostrarVista('vista-inicio');
      }
    });
  });
}

// ---------------------------------------------------------------- acciones del EB
async function enviar(ruta, params) {
  const url = params ? `${API}/${ruta}?${new URLSearchParams(params)}` : `${API}/${ruta}`;
  const resp = await fetch(url, { method: 'POST' });
  if (resp.status === 401) window.location.href = 'index.html';
  if (!resp.ok && resp.status !== 202) aviso('error', 'Error al comunicarse con el servidor (' + resp.status + ').');
}

function seleccionar(numeroBolsin) {
  if (!estado.consultaActiva) return;
  estado.seleccionado = numeroBolsin;
  dibujarLista(estado.bolsines);
  enviar('seleccionar', { numeroBolsin });
}

$('btn-consultar').addEventListener('click', async () => {
  estado.ultimaAccion = 'consultar';
  await conectarEventos();
  enviar('consultar');
});
$('btn-actualizar').addEventListener('click', () => enviar('actualizar'));
$('auto-actualizar').addEventListener('change', (e) => {
  if (e.target.checked) estado.timer = setInterval(() => enviar('actualizar'), 30000);
  else detenerAutoActualizacion();
});
$('btn-buscar-precinto').addEventListener('click', () => {
  const v = $('filtro-precinto').value.trim();
  $('filtro-cm').value = '';
  enviar('filtrar-precinto', v ? { numeroPrecinto: v } : {});
});
$('filtro-precinto').addEventListener('keydown', (e) => { if (e.key === 'Enter') $('btn-buscar-precinto').click(); });
$('filtro-cm').addEventListener('change', (e) => {
  $('filtro-precinto').value = '';
  enviar('filtrar-cm-destino', { codigoCMDestino: e.target.value });
});
$('btn-limpiar').addEventListener('click', () => {
  $('filtro-precinto').value = '';
  $('filtro-cm').value = '';
  enviar('filtrar-cm-destino', { codigoCMDestino: '' });
});
$('btn-cancelar').addEventListener('click', () => {
  if (!confirm('¿Cancelar la consulta de seguimiento?')) return;
  estado.ultimaAccion = 'cancelar';
  enviar('cancelar');
});
$('btn-enviar').addEventListener('click', () => { estado.ultimaAccion = 'enviar'; enviar('confirmar-envio', { confirma: true }); });
$('btn-no-enviar').addEventListener('click', () => {
  estado.ultimaAccion = 'no-enviar';
  cerrarConfirmacion();
  enviar('confirmar-envio', { confirma: false });
});
document.querySelectorAll('[data-accion="volver"]').forEach((b) => b.addEventListener('click', () => mostrarVista('vista-inicio')));
document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && $('modal-confirmacion').classList.contains('abierto')) $('btn-no-enviar').click(); });
mapa.on('popupopen', (e) => {
  const enlace = e.popup.getElement().querySelector('[data-sel]');
  if (enlace) enlace.addEventListener('click', (ev) => { ev.preventDefault(); seleccionar(Number(enlace.dataset.sel)); });
});

// ---------------------------------------------------------------- sesión
(async () => {
  const resp = await fetch('/api/sesion/actual');
  if (!resp.ok) { window.location.href = 'index.html'; return; }
  const u = await resp.json();
  $('nombre-usuario').textContent = u.empleado || u.usuario;
  $('cm-usuario').textContent = 'Encargado de Bolsines · ' + (u.comisionMedica || '');
  $('avatar').textContent = (u.empleado || u.usuario).split(' ').map((p) => p[0]).join('').slice(0, 2).toUpperCase();
})();
$('btn-salir').addEventListener('click', async () => {
  await fetch('/api/sesion/logout', { method: 'POST' });
  window.location.href = 'index.html';
});
