const state = {
  view: "resumen",
  resumen: null,
  libros: [],
  autores: [],
  generos: [],
  ejemplares: [],
  prestamos: [],
  usuarios: []
};

const ICONS = {
  plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>',
  edit: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z"/></svg>',
  trash: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 6h18"/><path d="M8 6V4h8v2"/><path d="m19 6-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/></svg>',
  back: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M3 12a9 9 0 1 0 3-6.7L3 8"/><path d="M3 3v5h5"/></svg>',
  book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>',
  layers: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2 2 7l10 5 10-5-10-5z"/><path d="m2 17 10 5 10-5"/><path d="m2 12 10 5 10-5"/></svg>',
  check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6 9 17l-5-5"/></svg>',
  refresh: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 2 21 6l-4 4"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><path d="m7 22-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/></svg>',
  users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.9"/><path d="M16 3.1a4 4 0 0 1 0 7.8"/></svg>',
  alert: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/></svg>',
  search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>',
  tag: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20.6 13.4 12 22l-9-9V3h10l7.6 7.6a2 2 0 0 1 0 2.8z"/><circle cx="7.5" cy="7.5" r="1.5"/></svg>',
  user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>'
};

const $ = (id) => document.getElementById(id);

function esc(value) {
  return String(value ?? "").replace(/[&<>"']/g, (ch) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  }[ch]));
}

function blankToNull(value) {
  const text = String(value ?? "").trim();
  return text.length ? text : null;
}

function splitList(value) {
  return String(value ?? "")
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

async function request(url, options = {}) {
  const config = { ...options, headers: { ...(options.headers || {}) } };

  if (options.body && !config.headers["Content-Type"]) {
    config.headers["Content-Type"] = "application/json";
  }

  const response = await fetch(url, config);
  const text = await response.text();
  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      (data && (data.mensaje || data.message || data.detail || data.title)) ||
      `Error ${response.status} ${response.statusText}`;
    throw new Error(message);
  }

  return data;
}

function fmtDateOnly(value) {
  if (!value) return "—";
  const parts = String(value).slice(0, 10).split("-").map(Number);
  if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) return "—";
  return new Date(parts[0], parts[1] - 1, parts[2]).toLocaleDateString("es-MX", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  });
}

function fmtDateTime(value) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return date.toLocaleString("es-MX", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function isoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function defaultLimitDate() {
  const date = new Date();
  date.setDate(date.getDate() + 14);
  return isoDate(date);
}

function setStatus(online) {
  const dot = $("statusDot");
  const text = $("statusText");
  dot.classList.toggle("online", online === true);
  dot.classList.toggle("offline", online === false);
  text.textContent = online ? "API conectada" : "Sin conexión con la API";
}

async function checkConnection() {
  try {
    await request("/api/admin/resumen");
    setStatus(true);
  } catch {
    setStatus(false);
  }
}

let toastTimer = null;

function toast(message, type = "success") {
  const element = $("toast");
  element.textContent = message;
  element.classList.toggle("error", type === "error");
  element.classList.add("show");

  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => {
    element.classList.remove("show");
  }, 3600);
}

function loadingState(text = "Cargando información…") {
  return `<div class="loading"><span class="spinner"></span>${esc(text)}</div>`;
}

function emptyState(title, text, icon = "book") {
  return `<div class="empty-state">${ICONS[icon] || ICONS.book}<strong>${esc(title)}</strong><span>${esc(text)}</span></div>`;
}

function errorState(message) {
  return `<div class="error-state">${ICONS.alert}<strong>No se pudo cargar la información</strong><p>${esc(message)}</p><button class="btn btn-ghost" onclick="render(state.view)">Reintentar</button></div>`;
}

function statCard({ label, value, detail, soft, color, icon }) {
  return `<article class="stat-card" style="--stat-soft:${soft};--stat-color:${color}">
    <div class="stat-top">
      <span class="stat-label">${esc(label)}</span>
      <span class="stat-icon">${ICONS[icon]}</span>
    </div>
    <div class="stat-value">${esc(value)}</div>
    <div class="stat-detail">${esc(detail)}</div>
  </article>`;
}

function coverMarkup(libro) {
  const letter = esc((libro.titulo || "?").trim().charAt(0).toUpperCase() || "?");
  if (!libro.urlPortada) {
    return `<div class="book-cover"><span>${letter}</span></div>`;
  }

  return `<div class="book-cover"><span>${letter}</span><img src="${esc(libro.urlPortada)}" alt="" loading="lazy" onerror="this.remove()"></div>`;
}

function chips(items) {
  if (!items || !items.length) return '<span class="muted">—</span>';
  return `<div class="chips">${items.map((item) => `<span class="chip">${esc(item)}</span>`).join("")}</div>`;
}

function availabilityBadge(libro) {
  if (!libro.ejemplares) {
    return '<span class="badge badge-neutral">Sin ejemplares</span>';
  }
  if (!libro.disponibles) {
    return `<span class="badge badge-danger">0 de ${libro.ejemplares}</span>`;
  }
  return `<span class="badge badge-success">${libro.disponibles} de ${libro.ejemplares}</span>`;
}

function estadoBadge(estado) {
  if (estado === "activo") return '<span class="badge badge-success">Activo</span>';
  if (estado === "mantenimiento") return '<span class="badge badge-warning">Mantenimiento</span>';
  if (estado === "baja") return '<span class="badge badge-neutral">Baja</span>';
  return `<span class="badge badge-neutral">${esc(estado)}</span>`;
}

function prestamoBadge(estado) {
  if (estado === "activo") return '<span class="badge badge-info">Activo</span>';
  if (estado === "vencido") return '<span class="badge badge-danger">Vencido</span>';
  if (estado === "devuelto") return '<span class="badge badge-success">Devuelto</span>';
  return `<span class="badge badge-neutral">${esc(estado)}</span>`;
}

function rolBadge(rol) {
  if (rol === "admin") return '<span class="badge badge-info">Admin</span>';
  if (rol === "bibliotecario") return '<span class="badge badge-warning">Bibliotecario</span>';
  return '<span class="badge badge-neutral">Lector</span>';
}

let modalHandler = null;

function openModal({ title, body, submit = "Guardar", successMessage = "Cambios guardados", onSubmit }) {
  $("modalTitle").textContent = title;
  $("modalBody").innerHTML = body;
  $("modalSubmit").textContent = submit;
  $("modalSubmit").disabled = false;
  $("modalBackdrop").hidden = false;
  document.body.classList.add("modal-open");
  modalHandler = { onSubmit, successMessage };

  setTimeout(() => {
    const first = $("modalBody").querySelector("input, select, textarea");
    if (first) first.focus();
  }, 60);
}

function closeModal() {
  $("modalBackdrop").hidden = true;
  document.body.classList.remove("modal-open");
  $("modalForm").reset();
  $("modalBody").innerHTML = "";
  modalHandler = null;
}

async function handleModalSubmit(event) {
  event.preventDefault();
  if (!modalHandler) return;

  const button = $("modalSubmit");
  const originalText = button.textContent;
  button.disabled = true;
  button.textContent = "Guardando…";

  try {
    await modalHandler.onSubmit(new FormData($("modalForm")), $("modalForm"));
    const message = modalHandler.successMessage;
    closeModal();
    toast(message);
    await render(state.view);
  } catch (error) {
    toast(error.message || "No se pudo completar la operación", "error");
    button.disabled = false;
    button.textContent = originalText;
  }
}

function navigate(view) {
  const section = $(`view-${view}`);
  if (!section) return;

  state.view = view;

  document.querySelectorAll(".nav-item").forEach((button) => {
    button.classList.toggle("active", button.dataset.view === view);
  });

  document.querySelectorAll(".view").forEach((element) => {
    element.classList.toggle("active", element === section);
  });

  $("pageTitle").textContent = section.dataset.title;
  $("pageSubtitle").textContent = section.dataset.subtitle;
  $("sidebar").classList.remove("open");
  window.scrollTo({ top: 0, behavior: "smooth" });

  render(view);
}

async function render(view) {
  const section = $(`view-${view}`);
  if (!section) return;

  try {
    if (view === "resumen") await renderResumen(section);
    if (view === "libros") await renderLibros(section);
    if (view === "autores") await renderAutores(section);
    if (view === "generos") await renderGeneros(section);
    if (view === "ejemplares") await renderEjemplares(section);
    if (view === "prestamos") await renderPrestamos(section);
    if (view === "usuarios") await renderUsuarios(section);
    setStatus(true);
  } catch (error) {
    setStatus(false);
    section.innerHTML = errorState(error.message || "Error inesperado");
  }
}

async function renderResumen(section) {
  section.innerHTML = loadingState();

  const [resumen, prestamos, libros] = await Promise.all([
    request("/api/admin/resumen"),
    request("/api/admin/prestamos"),
    request("/api/admin/libros")
  ]);

  state.resumen = resumen;
  state.prestamos = prestamos;
  state.libros = libros;

  const activos = prestamos.filter((prestamo) => prestamo.estado !== "devuelto");
  const vencidos = prestamos.filter((prestamo) => prestamo.estado === "vencido");
  const sinDisponibles = libros.filter((libro) => libro.ejemplares > 0 && libro.disponibles === 0).slice(0, 6);

  const activosMarkup = activos.length
    ? `<div class="table-wrap">
        <table>
          <thead><tr><th>Libro</th><th>Usuario</th><th>Límite</th><th>Estado</th></tr></thead>
          <tbody>
            ${activos.slice(0, 7).map((prestamo) => `
              <tr>
                <td><div class="stack"><span class="book-title">${esc(prestamo.libro)}</span><span class="book-meta">${esc(prestamo.codigoBarras)}</span></div></td>
                <td>${esc(prestamo.usuario)}</td>
                <td>${fmtDateOnly(prestamo.fechaLimite)}</td>
                <td>${prestamoBadge(prestamo.estado)}</td>
              </tr>`).join("")}
          </tbody>
        </table>
      </div>`
    : emptyState("Sin préstamos activos", "Todos los ejemplares están disponibles.", "check");

  const sinDisponiblesMarkup = sinDisponibles.length
    ? `<div class="mini-list">
        ${sinDisponibles.map((libro) => `
          <div class="mini-item">
            <div class="mini-icon">${ICONS.book}</div>
            <div class="mini-content">
              <div class="mini-title">${esc(libro.titulo)}</div>
              <div class="mini-sub">${esc((libro.autores || []).join(", ") || "Sin autor")} · ${libro.ejemplares} ejemplar(es)</div>
            </div>
            <span class="badge badge-danger">Prestado</span>
          </div>`).join("")}
      </div>`
    : emptyState("Todo disponible", "No hay títulos con todos sus ejemplares prestados.", "check");

  section.innerHTML = `
    <div class="stats">
      ${statCard({ label: "Libros", value: resumen.libros, detail: "Títulos en catálogo", soft: "#ccfbf1", color: "#0f766e", icon: "book" })}
      ${statCard({ label: "Ejemplares", value: resumen.ejemplares, detail: "Copias físicas", soft: "#dbeafe", color: "#1d4ed8", icon: "layers" })}
      ${statCard({ label: "Disponibles", value: resumen.ejemplaresDisponibles, detail: "Listos para préstamo", soft: "#d1fae5", color: "#047857", icon: "check" })}
      ${statCard({ label: "Préstamos", value: resumen.prestamosActivos, detail: "Activos en circulación", soft: "#fef3c7", color: "#a16207", icon: "refresh" })}
      ${statCard({ label: "Vencidos", value: resumen.prestamosVencidos, detail: "Requieren seguimiento", soft: "#fee2e2", color: "#c02626", icon: "alert" })}
      ${statCard({ label: "Usuarios", value: resumen.usuarios, detail: "Lectores y personal", soft: "#ede9fe", color: "#6d28d9", icon: "users" })}
    </div>

    <div class="grid-2">
      <div class="panel">
        <div class="panel-head">
          <div>
            <h2>Préstamos activos</h2>
            <p>${activos.length} en circulación${vencidos.length ? ` · ${vencidos.length} vencidos` : ""}</p>
          </div>
          <button class="btn btn-primary btn-sm" onclick="openPrestamoModal()">${ICONS.plus}Nuevo préstamo</button>
        </div>
        ${activosMarkup}
      </div>

      <div class="panel">
        <div class="panel-head">
          <div>
            <h2>Títulos sin disponibilidad</h2>
            <p>Libros con todos sus ejemplares prestados</p>
          </div>
          <button class="btn btn-ghost btn-sm" onclick="navigate('libros')">Ver catálogo</button>
        </div>
        ${sinDisponiblesMarkup}
      </div>
    </div>
  `;
}

async function renderLibros(section) {
  section.innerHTML = loadingState();
  const [libros, autores, generos] = await Promise.all([
    request("/api/admin/libros"),
    request("/api/Autores"),
    request("/api/Generos")
  ]);

  state.libros = libros;
  state.autores = autores;
  state.generos = generos;

  const totalEjemplares = libros.reduce((total, libro) => total + libro.ejemplares, 0);

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Catálogo de libros</h2>
          <p>${libros.length} títulos · ${totalEjemplares} ejemplares activos</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="libroSearch" type="search" placeholder="Buscar por título, autor o ISBN" oninput="renderLibrosTable()">
          </label>
          <button class="btn btn-primary" onclick="openLibroModal()">${ICONS.plus}Nuevo libro</button>
        </div>
      </div>
      <div id="librosTable"></div>
    </div>
  `;

  renderLibrosTable();
}

function renderLibrosTable() {
  const container = $("librosTable");
  if (!container) return;

  const term = ($("libroSearch")?.value || "").trim().toLowerCase();
  const libros = state.libros.filter((libro) => {
    if (!term) return true;
    const text = [
      libro.titulo,
      libro.isbn,
      libro.editorial,
      ...(libro.autores || []),
      ...(libro.generos || [])
    ].join(" ").toLowerCase();
    return text.includes(term);
  });

  if (!libros.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay libros que coincidan con la búsqueda.");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th></th>
            <th>Libro</th>
            <th>Año</th>
            <th>Autores</th>
            <th>Géneros</th>
            <th>Disponibilidad</th>
            <th class="actions-col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${libros.map((libro) => `
            <tr>
              <td>${coverMarkup(libro)}</td>
              <td>
                <div class="stack">
                  <span class="book-title">${esc(libro.titulo)}</span>
                  <span class="book-meta">${esc(libro.editorial || "Sin editorial")}${libro.isbn ? ` · ISBN ${esc(libro.isbn)}` : ""}</span>
                </div>
              </td>
              <td>${libro.anio ?? "—"}</td>
              <td>${chips(libro.autores)}</td>
              <td>${chips(libro.generos)}</td>
              <td>${availabilityBadge(libro)}</td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="action-button" title="Editar libro" onclick="openLibroModal(${libro.id})">${ICONS.edit}</button>
                  <button class="action-button danger" title="Eliminar libro" onclick="deleteLibro(${libro.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${libros.length} de ${state.libros.length} libros</div>
  `;
}

function openLibroModal(id) {
  const libro = id ? state.libros.find((item) => item.id === id) : null;

  const body = `
    <div class="form-grid">
      <div class="field full">
        <label for="libroTitulo">Título *</label>
        <input id="libroTitulo" name="titulo" required maxlength="250" value="${esc(libro?.titulo)}" placeholder="Ej. Cien años de soledad">
      </div>
      <div class="field">
        <label for="libroAnio">Año</label>
        <input id="libroAnio" name="anio" type="number" min="1" max="2100" value="${libro?.anio ?? ""}">
      </div>
      <div class="field">
        <label for="libroIsbn">ISBN</label>
        <input id="libroIsbn" name="isbn" maxlength="20" value="${esc(libro?.isbn)}">
      </div>
      <div class="field">
        <label for="libroEditorial">Editorial</label>
        <input id="libroEditorial" name="editorial" maxlength="150" value="${esc(libro?.editorial)}">
      </div>
      <div class="field">
        <label for="libroPortada">URL de portada</label>
        <input id="libroPortada" name="urlPortada" type="url" value="${esc(libro?.urlPortada)}" placeholder="https://…">
      </div>
      <div class="field full">
        <label for="libroAutores">Autores</label>
        <input id="libroAutores" name="autores" value="${esc((libro?.autores || []).join(", "))}" placeholder="Separados por coma">
        <small>Si el autor no existe, se crea automáticamente.</small>
      </div>
      <div class="field full">
        <label for="libroGeneros">Géneros</label>
        <input id="libroGeneros" name="generos" value="${esc((libro?.generos || []).join(", "))}" placeholder="Separados por coma">
        <small>Si el género no existe, se crea automáticamente.</small>
      </div>
      <div class="field full">
        <label for="libroSinopsis">Sinopsis</label>
        <textarea id="libroSinopsis" name="sinopsis">${esc(libro?.sinopsis)}</textarea>
      </div>
    </div>
  `;

  openModal({
    title: libro ? "Editar libro" : "Nuevo libro",
    submit: libro ? "Guardar cambios" : "Crear libro",
    successMessage: libro ? "Libro actualizado" : "Libro creado",
    body,
    onSubmit: async (formData) => {
      const payload = {
        titulo: String(formData.get("titulo") || "").trim(),
        anio: formData.get("anio") ? Number(formData.get("anio")) : null,
        isbn: blankToNull(formData.get("isbn")),
        editorial: blankToNull(formData.get("editorial")),
        urlPortada: blankToNull(formData.get("urlPortada")),
        sinopsis: blankToNull(formData.get("sinopsis")),
        autores: splitList(formData.get("autores")),
        generos: splitList(formData.get("generos"))
      };

      await request(libro ? `/api/admin/libros/${libro.id}` : "/api/admin/libros", {
        method: libro ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });
    }
  });
}

async function deleteLibro(id) {
  const libro = state.libros.find((item) => item.id === id);
  if (!window.confirm(`¿Eliminar el libro "${libro?.titulo || id}"? Esta acción no se puede deshacer.`)) return;

  try {
    await request(`/api/admin/libros/${id}`, { method: "DELETE" });
    toast("Libro eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function renderAutores(section) {
  section.innerHTML = loadingState();
  const [autores, libros] = await Promise.all([
    request("/api/Autores"),
    request("/api/admin/libros")
  ]);

  state.autores = autores;
  state.libros = libros;

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Autores</h2>
          <p>${autores.length} autores registrados</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="autorSearch" type="search" placeholder="Buscar autor" oninput="renderAutoresTable()">
          </label>
          <button class="btn btn-primary" onclick="openAutorModal()">${ICONS.plus}Nuevo autor</button>
        </div>
      </div>
      <div id="autoresTable"></div>
    </div>
  `;

  renderAutoresTable();
}

function renderAutoresTable() {
  const container = $("autoresTable");
  if (!container) return;

  const term = ($("autorSearch")?.value || "").trim().toLowerCase();
  const autores = state.autores.filter((autor) => autor.nombre.toLowerCase().includes(term));

  if (!autores.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay autores que coincidan con la búsqueda.", "user");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead><tr><th>Autor</th><th>Libros en catálogo</th><th class="actions-col">Acciones</th></tr></thead>
        <tbody>
          ${autores.map((autor) => {
            const count = state.libros.filter((libro) =>
              (libro.autores || []).some((nombre) => nombre.toLowerCase() === autor.nombre.toLowerCase())
            ).length;

            return `<tr>
              <td><span class="book-title">${esc(autor.nombre)}</span></td>
              <td><span class="badge badge-neutral">${count} libro(s)</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="action-button" title="Editar autor" onclick="openAutorModal(${autor.id})">${ICONS.edit}</button>
                  <button class="action-button danger" title="Eliminar autor" onclick="deleteAutor(${autor.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${autores.length} de ${state.autores.length} autores</div>
  `;
}

function openAutorModal(id) {
  const autor = id ? state.autores.find((item) => item.id === id) : null;

  openModal({
    title: autor ? "Editar autor" : "Nuevo autor",
    submit: autor ? "Guardar cambios" : "Crear autor",
    successMessage: autor ? "Autor actualizado" : "Autor creado",
    body: `
      <div class="form-grid">
        <div class="field full">
          <label for="autorNombre">Nombre *</label>
          <input id="autorNombre" name="nombre" required maxlength="150" value="${esc(autor?.nombre)}" placeholder="Nombre completo">
        </div>
      </div>
    `,
    onSubmit: async (formData) => {
      const payload = {
        id: autor?.id || 0,
        nombre: String(formData.get("nombre") || "").trim()
      };

      await request(autor ? `/api/Autores/${autor.id}` : "/api/Autores", {
        method: autor ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });
    }
  });
}

async function deleteAutor(id) {
  const autor = state.autores.find((item) => item.id === id);
  if (!window.confirm(`¿Eliminar el autor "${autor?.nombre || id}"?`)) return;

  try {
    await request(`/api/Autores/${id}`, { method: "DELETE" });
    toast("Autor eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function renderGeneros(section) {
  section.innerHTML = loadingState();
  const [generos, libros] = await Promise.all([
    request("/api/Generos"),
    request("/api/admin/libros")
  ]);

  state.generos = generos;
  state.libros = libros;

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Géneros</h2>
          <p>${generos.length} géneros registrados</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="generoSearch" type="search" placeholder="Buscar género" oninput="renderGenerosTable()">
          </label>
          <button class="btn btn-primary" onclick="openGeneroModal()">${ICONS.plus}Nuevo género</button>
        </div>
      </div>
      <div id="generosTable"></div>
    </div>
  `;

  renderGenerosTable();
}

function renderGenerosTable() {
  const container = $("generosTable");
  if (!container) return;

  const term = ($("generoSearch")?.value || "").trim().toLowerCase();
  const generos = state.generos.filter((genero) => genero.nombre.toLowerCase().includes(term));

  if (!generos.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay géneros que coincidan con la búsqueda.", "tag");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead><tr><th>Género</th><th>Libros en catálogo</th><th class="actions-col">Acciones</th></tr></thead>
        <tbody>
          ${generos.map((genero) => {
            const count = state.libros.filter((libro) =>
              (libro.generos || []).some((nombre) => nombre.toLowerCase() === genero.nombre.toLowerCase())
            ).length;

            return `<tr>
              <td><span class="book-title">${esc(genero.nombre)}</span></td>
              <td><span class="badge badge-neutral">${count} libro(s)</span></td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="action-button" title="Editar género" onclick="openGeneroModal(${genero.id})">${ICONS.edit}</button>
                  <button class="action-button danger" title="Eliminar género" onclick="deleteGenero(${genero.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${generos.length} de ${state.generos.length} géneros</div>
  `;
}

function openGeneroModal(id) {
  const genero = id ? state.generos.find((item) => item.id === id) : null;

  openModal({
    title: genero ? "Editar género" : "Nuevo género",
    submit: genero ? "Guardar cambios" : "Crear género",
    successMessage: genero ? "Género actualizado" : "Género creado",
    body: `
      <div class="form-grid">
        <div class="field full">
          <label for="generoNombre">Nombre *</label>
          <input id="generoNombre" name="nombre" required maxlength="80" value="${esc(genero?.nombre)}" placeholder="Ej. Novela histórica">
        </div>
      </div>
    `,
    onSubmit: async (formData) => {
      const payload = {
        id: genero?.id || 0,
        nombre: String(formData.get("nombre") || "").trim()
      };

      await request(genero ? `/api/Generos/${genero.id}` : "/api/Generos", {
        method: genero ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });
    }
  });
}

async function deleteGenero(id) {
  const genero = state.generos.find((item) => item.id === id);
  if (!window.confirm(`¿Eliminar el género "${genero?.nombre || id}"?`)) return;

  try {
    await request(`/api/Generos/${id}`, { method: "DELETE" });
    toast("Género eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function renderEjemplares(section) {
  section.innerHTML = loadingState();
  const [ejemplares, libros] = await Promise.all([
    request("/api/admin/ejemplares"),
    request("/api/admin/libros")
  ]);

  state.ejemplares = ejemplares;
  state.libros = libros;

  const disponibles = ejemplares.filter((ejemplar) => ejemplar.estado === "activo" && !ejemplar.prestamoActivo).length;

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Ejemplares</h2>
          <p>${ejemplares.length} copias · ${disponibles} disponibles</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="ejemplarSearch" type="search" placeholder="Buscar por código o libro" oninput="renderEjemplaresTable()">
          </label>
          <button class="btn btn-primary" onclick="openEjemplarModal()">${ICONS.plus}Nuevo ejemplar</button>
        </div>
      </div>
      <div id="ejemplaresTable"></div>
    </div>
  `;

  renderEjemplaresTable();
}

function renderEjemplaresTable() {
  const container = $("ejemplaresTable");
  if (!container) return;

  const term = ($("ejemplarSearch")?.value || "").trim().toLowerCase();
  const ejemplares = state.ejemplares.filter((ejemplar) =>
    `${ejemplar.codigoBarras} ${ejemplar.libro}`.toLowerCase().includes(term)
  );

  if (!ejemplares.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay ejemplares que coincidan con la búsqueda.", "layers");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Código</th>
            <th>Libro</th>
            <th>Estado</th>
            <th>Préstamo actual</th>
            <th class="actions-col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${ejemplares.map((ejemplar) => {
            const prestamo = ejemplar.prestamoActivo;
            const info = prestamo
              ? `<div class="stack"><span>${esc(prestamo.usuario)}</span><span class="book-meta">Límite: ${fmtDateOnly(prestamo.fechaLimite)}</span></div>`
              : '<span class="muted">Disponible</span>';

            return `<tr>
              <td><span class="book-title">${esc(ejemplar.codigoBarras)}</span></td>
              <td>${esc(ejemplar.libro)}</td>
              <td>${estadoBadge(ejemplar.estado)}</td>
              <td>${info}${prestamo?.vencido ? ' <span class="badge badge-danger">Vencido</span>' : ""}</td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="action-button" title="Editar ejemplar" onclick="openEjemplarModal(${ejemplar.id})">${ICONS.edit}</button>
                  <button class="action-button danger" title="Eliminar ejemplar" onclick="deleteEjemplar(${ejemplar.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`;
          }).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${ejemplares.length} de ${state.ejemplares.length} ejemplares</div>
  `;
}

function openEjemplarModal(id) {
  const ejemplar = id ? state.ejemplares.find((item) => item.id === id) : null;
  const libroId = ejemplar?.libroId || state.libros[0]?.id;

  if (!state.libros.length) {
    toast("Primero registra al menos un libro.", "error");
    return;
  }

  const options = state.libros.map((libro) =>
    `<option value="${libro.id}" ${libro.id === libroId ? "selected" : ""}>${esc(libro.titulo)}</option>`
  ).join("");

  openModal({
    title: ejemplar ? "Editar ejemplar" : "Nuevo ejemplar",
    submit: ejemplar ? "Guardar cambios" : "Crear ejemplar",
    successMessage: ejemplar ? "Ejemplar actualizado" : "Ejemplar creado",
    body: `
      <div class="form-grid">
        <div class="field full">
          <label for="ejemplarLibro">Libro *</label>
          <select id="ejemplarLibro" name="libroId" required>${options}</select>
        </div>
        <div class="field">
          <label for="ejemplarCodigo">Código de barras *</label>
          <input id="ejemplarCodigo" name="codigoBarras" required maxlength="40" value="${esc(ejemplar?.codigoBarras)}" placeholder="Ej. BIB-0006">
        </div>
        <div class="field">
          <label for="ejemplarEstado">Estado *</label>
          <select id="ejemplarEstado" name="estado" required>
            <option value="activo" ${ejemplar?.estado !== "mantenimiento" && ejemplar?.estado !== "baja" ? "selected" : ""}>Activo</option>
            <option value="mantenimiento" ${ejemplar?.estado === "mantenimiento" ? "selected" : ""}>Mantenimiento</option>
            <option value="baja" ${ejemplar?.estado === "baja" ? "selected" : ""}>Baja</option>
          </select>
        </div>
      </div>
    `,
    onSubmit: async (formData) => {
      const payload = {
        id: ejemplar?.id || 0,
        libroId: Number(formData.get("libroId")),
        codigoBarras: String(formData.get("codigoBarras") || "").trim(),
        estado: String(formData.get("estado") || "activo")
      };

      await request(ejemplar ? `/api/Ejemplares/${ejemplar.id}` : "/api/Ejemplares", {
        method: ejemplar ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });
    }
  });
}

async function deleteEjemplar(id) {
  const ejemplar = state.ejemplares.find((item) => item.id === id);
  if (!window.confirm(`¿Eliminar el ejemplar "${ejemplar?.codigoBarras || id}"?`)) return;

  try {
    await request(`/api/Ejemplares/${id}`, { method: "DELETE" });
    toast("Ejemplar eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function renderPrestamos(section) {
  section.innerHTML = loadingState();
  const [prestamos, usuarios, ejemplares] = await Promise.all([
    request("/api/admin/prestamos"),
    request("/api/admin/usuarios"),
    request("/api/admin/ejemplares")
  ]);

  state.prestamos = prestamos;
  state.usuarios = usuarios;
  state.ejemplares = ejemplares;

  const activos = prestamos.filter((prestamo) => prestamo.estado === "activo").length;
  const vencidos = prestamos.filter((prestamo) => prestamo.estado === "vencido").length;

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Préstamos</h2>
          <p>${activos} activos · ${vencidos} vencidos · ${prestamos.length} registros</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="prestamoSearch" type="search" placeholder="Buscar por libro, usuario o código" oninput="renderPrestamosTable()">
          </label>
          <select id="prestamoFilter" class="filter-select" onchange="renderPrestamosTable()">
            <option value="todos">Todos los estados</option>
            <option value="activo">Activos</option>
            <option value="vencido">Vencidos</option>
            <option value="devuelto">Devueltos</option>
          </select>
          <button class="btn btn-primary" onclick="openPrestamoModal()">${ICONS.plus}Nuevo préstamo</button>
        </div>
      </div>
      <div id="prestamosTable"></div>
    </div>
  `;

  renderPrestamosTable();
}

function renderPrestamosTable() {
  const container = $("prestamosTable");
  if (!container) return;

  const term = ($("prestamoSearch")?.value || "").trim().toLowerCase();
  const filter = $("prestamoFilter")?.value || "todos";

  const prestamos = state.prestamos.filter((prestamo) => {
    const matchesTerm = `${prestamo.libro} ${prestamo.usuario} ${prestamo.codigoBarras}`.toLowerCase().includes(term);
    const matchesFilter = filter === "todos" || prestamo.estado === filter;
    return matchesTerm && matchesFilter;
  });

  if (!prestamos.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay préstamos que coincidan con el filtro.", "refresh");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Libro</th>
            <th>Ejemplar</th>
            <th>Usuario</th>
            <th>Prestado</th>
            <th>Límite</th>
            <th>Estado</th>
            <th class="actions-col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${prestamos.map((prestamo) => `
            <tr>
              <td><span class="book-title">${esc(prestamo.libro)}</span></td>
              <td>${esc(prestamo.codigoBarras)}</td>
              <td>${esc(prestamo.usuario)}</td>
              <td>${fmtDateTime(prestamo.fechaPrestamo)}</td>
              <td>${fmtDateOnly(prestamo.fechaLimite)}</td>
              <td>${prestamoBadge(prestamo.estado)}</td>
              <td class="actions-col">
                <div class="row-actions">
                  ${prestamo.estado !== "devuelto"
                    ? `<button class="action-button" title="Registrar devolución" onclick="devolverPrestamo(${prestamo.id})">${ICONS.back}</button>`
                    : ""}
                  <button class="action-button danger" title="Eliminar préstamo" onclick="deletePrestamo(${prestamo.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${prestamos.length} de ${state.prestamos.length} préstamos</div>
  `;
}

async function openPrestamoModal() {
  try {
    if (!state.usuarios.length || !state.ejemplares.length) {
      const [usuarios, ejemplares] = await Promise.all([
        request("/api/admin/usuarios"),
        request("/api/admin/ejemplares")
      ]);
      state.usuarios = usuarios;
      state.ejemplares = ejemplares;
    }

    const usuarios = state.usuarios.filter((usuario) => usuario.activo);
    const ejemplares = state.ejemplares.filter((ejemplar) => ejemplar.estado === "activo" && !ejemplar.prestamoActivo);

    if (!usuarios.length) {
      toast("No hay usuarios activos para registrar un préstamo.", "error");
      return;
    }

    if (!ejemplares.length) {
      toast("No hay ejemplares disponibles para prestar.", "error");
      return;
    }

    const usuarioOptions = usuarios.map((usuario) =>
      `<option value="${usuario.id}">${esc(usuario.nombre)} · ${esc(usuario.correo)}</option>`
    ).join("");

    const ejemplarOptions = ejemplares.map((ejemplar) =>
      `<option value="${ejemplar.id}">${esc(ejemplar.codigoBarras)} · ${esc(ejemplar.libro)}</option>`
    ).join("");

    openModal({
      title: "Nuevo préstamo",
      submit: "Registrar préstamo",
      successMessage: "Préstamo registrado",
      body: `
        <div class="form-grid">
          <div class="field full">
            <label for="prestamoUsuario">Usuario *</label>
            <select id="prestamoUsuario" name="usuarioId" required>${usuarioOptions}</select>
          </div>
          <div class="field full">
            <label for="prestamoEjemplar">Ejemplar disponible *</label>
            <select id="prestamoEjemplar" name="ejemplarId" required>${ejemplarOptions}</select>
          </div>
          <div class="field full">
            <label for="prestamoLimite">Fecha límite *</label>
            <input id="prestamoLimite" name="fechaLimite" type="date" required value="${defaultLimitDate()}">
            <small>Por defecto se proponen 14 días.</small>
          </div>
        </div>
      `,
      onSubmit: async (formData) => {
        await request("/api/admin/prestamos", {
          method: "POST",
          body: JSON.stringify({
            usuarioId: Number(formData.get("usuarioId")),
            ejemplarId: Number(formData.get("ejemplarId")),
            fechaLimite: formData.get("fechaLimite") || null
          })
        });
      }
    });
  } catch (error) {
    toast(error.message, "error");
  }
}

async function devolverPrestamo(id) {
  const prestamo = state.prestamos.find((item) => item.id === id);
  if (!window.confirm(`¿Registrar la devolución de "${prestamo?.libro || id}"?`)) return;

  try {
    await request(`/api/admin/prestamos/${id}/devolver`, { method: "PUT" });
    toast("Devolución registrada");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function deletePrestamo(id) {
  if (!window.confirm("¿Eliminar este registro de préstamo? El ejemplar quedará disponible.")) return;

  try {
    await request(`/api/admin/prestamos/${id}`, { method: "DELETE" });
    toast("Préstamo eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

async function renderUsuarios(section) {
  section.innerHTML = loadingState();
  const usuarios = await request("/api/admin/usuarios");
  state.usuarios = usuarios;

  const activos = usuarios.filter((usuario) => usuario.activo).length;
  const administradores = usuarios.filter((usuario) => usuario.rol === "admin").length;

  section.innerHTML = `
    <div class="panel">
      <div class="panel-head">
        <div>
          <h2>Usuarios</h2>
          <p>${usuarios.length} registrados · ${activos} activos · ${administradores} administradores</p>
        </div>
        <div class="toolbar">
          <label class="search">
            ${ICONS.search}
            <input id="usuarioSearch" type="search" placeholder="Buscar por nombre o correo" oninput="renderUsuariosTable()">
          </label>
          <button class="btn btn-primary" onclick="openUsuarioModal()">${ICONS.plus}Nuevo usuario</button>
        </div>
      </div>
      <div id="usuariosTable"></div>
    </div>
  `;

  renderUsuariosTable();
}

function renderUsuariosTable() {
  const container = $("usuariosTable");
  if (!container) return;

  const term = ($("usuarioSearch")?.value || "").trim().toLowerCase();
  const usuarios = state.usuarios.filter((usuario) =>
    `${usuario.nombre} ${usuario.correo}`.toLowerCase().includes(term)
  );

  if (!usuarios.length) {
    container.innerHTML = emptyState("Sin resultados", "No hay usuarios que coincidan con la búsqueda.", "users");
    return;
  }

  container.innerHTML = `
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Correo</th>
            <th>Rol</th>
            <th>Estado</th>
            <th>Préstamos activos</th>
            <th>Alta</th>
            <th class="actions-col">Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${usuarios.map((usuario) => `
            <tr>
              <td><span class="book-title">${esc(usuario.nombre)}</span></td>
              <td>${esc(usuario.correo)}</td>
              <td>${rolBadge(usuario.rol)}</td>
              <td>${usuario.activo ? '<span class="badge badge-success">Activo</span>' : '<span class="badge badge-neutral">Inactivo</span>'}</td>
              <td>${usuario.prestamosActivos ? `<span class="badge badge-info">${usuario.prestamosActivos}</span>` : '<span class="muted">0</span>'}</td>
              <td>${fmtDateTime(usuario.creadoEn)}</td>
              <td class="actions-col">
                <div class="row-actions">
                  <button class="action-button" title="Editar usuario" onclick="openUsuarioModal(${usuario.id})">${ICONS.edit}</button>
                  <button class="action-button danger" title="Eliminar usuario" onclick="deleteUsuario(${usuario.id})">${ICONS.trash}</button>
                </div>
              </td>
            </tr>`).join("")}
        </tbody>
      </table>
    </div>
    <div class="pagination-note">Mostrando ${usuarios.length} de ${state.usuarios.length} usuarios</div>
  `;
}

function openUsuarioModal(id) {
  const usuario = id ? state.usuarios.find((item) => item.id === id) : null;

  openModal({
    title: usuario ? "Editar usuario" : "Nuevo usuario",
    submit: usuario ? "Guardar cambios" : "Crear usuario",
    successMessage: usuario ? "Usuario actualizado" : "Usuario creado",
    body: `
      <div class="form-grid">
        <div class="field full">
          <label for="usuarioNombre">Nombre *</label>
          <input id="usuarioNombre" name="nombre" required maxlength="120" value="${esc(usuario?.nombre)}" placeholder="Nombre completo">
        </div>
        <div class="field full">
          <label for="usuarioCorreo">Correo *</label>
          <input id="usuarioCorreo" name="correo" type="email" required maxlength="160" value="${esc(usuario?.correo)}" placeholder="usuario@correo.com">
        </div>
        <div class="field">
          <label for="usuarioRol">Rol *</label>
          <select id="usuarioRol" name="rol" required>
            <option value="lector" ${usuario?.rol === "lector" ? "selected" : ""}>Lector</option>
            <option value="bibliotecario" ${usuario?.rol === "bibliotecario" ? "selected" : ""}>Bibliotecario</option>
            <option value="admin" ${usuario?.rol === "admin" ? "selected" : ""}>Admin</option>
          </select>
        </div>
        <div class="check-field">
          <input id="usuarioActivo" name="activo" type="checkbox" ${usuario?.activo === false ? "" : "checked"}>
          <label for="usuarioActivo">Usuario activo</label>
        </div>
        <div class="field full">
          <label for="usuarioPassword">Contraseña ${usuario ? "" : "*"}</label>
          <input id="usuarioPassword" name="password" type="password" minlength="4" ${usuario ? "" : "required"} autocomplete="new-password" placeholder="${usuario ? "Dejar vacío para conservar la actual" : "Contraseña del usuario"}">
          <small>Se almacena como hash SHA-256 dentro de la base de datos.</small>
        </div>
      </div>
    `,
    onSubmit: async (formData, form) => {
      const payload = {
        nombre: String(formData.get("nombre") || "").trim(),
        correo: String(formData.get("correo") || "").trim(),
        rol: String(formData.get("rol") || "lector"),
        activo: form.elements.activo.checked,
        password: blankToNull(formData.get("password"))
      };

      await request(usuario ? `/api/admin/usuarios/${usuario.id}` : "/api/admin/usuarios", {
        method: usuario ? "PUT" : "POST",
        body: JSON.stringify(payload)
      });
    }
  });
}

async function deleteUsuario(id) {
  const usuario = state.usuarios.find((item) => item.id === id);
  if (!window.confirm(`¿Eliminar al usuario "${usuario?.nombre || id}"?`)) return;

  try {
    await request(`/api/admin/usuarios/${id}`, { method: "DELETE" });
    toast("Usuario eliminado");
    await render(state.view);
  } catch (error) {
    toast(error.message, "error");
  }
}

function init() {
  const today = new Date().toLocaleDateString("es-MX", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric"
  });
  $("datePill").textContent = today.charAt(0).toUpperCase() + today.slice(1);

  document.querySelectorAll(".nav-item").forEach((button) => {
    button.addEventListener("click", () => navigate(button.dataset.view));
  });

  $("menuButton").addEventListener("click", () => $("sidebar").classList.toggle("open"));
  $("modalClose").addEventListener("click", closeModal);
  $("modalCancel").addEventListener("click", closeModal);
  $("modalBackdrop").addEventListener("click", (event) => {
    if (event.target === $("modalBackdrop")) closeModal();
  });
  $("modalForm").addEventListener("submit", handleModalSubmit);
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !$("modalBackdrop").hidden) closeModal();
  });

  checkConnection();
  render("resumen");
}

document.addEventListener("DOMContentLoaded", init);
