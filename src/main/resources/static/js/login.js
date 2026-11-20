document.getElementById('form-login').addEventListener('submit', async (e) => {
  e.preventDefault();
  const error = document.getElementById('error-login');
  error.textContent = '';
  const datos = new URLSearchParams({
    usuario: document.getElementById('usuario').value.trim(),
    clave: document.getElementById('clave').value
  });
  try {
    const resp = await fetch('/api/sesion/login', { method: 'POST', body: datos });
    if (!resp.ok) {
      const cuerpo = await resp.json().catch(() => ({}));
      error.textContent = cuerpo.error || 'No se pudo ingresar.';
      return;
    }
    window.location.href = 'seguimiento.html';
  } catch (err) {
    error.textContent = 'No se pudo conectar con el servidor.';
  }
});
