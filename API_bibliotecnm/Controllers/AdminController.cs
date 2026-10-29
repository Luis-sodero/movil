using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/admin")]
[ApiController]
public class AdminController : ControllerBase
{
    private static readonly string[] RolesValidos = { "lector", "bibliotecario", "admin" };
    private static readonly string[] EstadosValidos = { "activo", "mantenimiento", "baja" };

    private readonly ContextoBD _context;

    public AdminController(ContextoBD context)
    {
        _context = context;
    }

    [HttpGet("resumen")]
    public async Task<ActionResult<ResumenAdminDto>> GetResumen()
    {
        var hoy = DateOnly.FromDateTime(DateTime.Today);

        var resumen = new ResumenAdminDto
        {
            Usuarios = await _context.Usuarios.CountAsync(),
            Libros = await _context.Libros.CountAsync(),
            Autores = await _context.Autores.CountAsync(),
            Generos = await _context.Generos.CountAsync(),
            Ejemplares = await _context.Ejemplares.CountAsync(),
            EjemplaresDisponibles = await _context.Ejemplares.CountAsync(e =>
                e.Estado == "activo" &&
                !_context.Prestamos.Any(p => p.EjemplarId == e.Id && p.FechaDevolucion == null)),
            PrestamosActivos = await _context.Prestamos.CountAsync(p => p.FechaDevolucion == null),
            PrestamosVencidos = await _context.Prestamos.CountAsync(p =>
                p.FechaDevolucion == null && p.FechaLimite < hoy)
        };

        return Ok(resumen);
    }

    [HttpGet("libros")]
    public async Task<ActionResult<IEnumerable<LibroAdminDto>>> GetLibros()
    {
        var libros = await LibrosConRelaciones().ToListAsync();
        var prestados = await EjemplaresPrestadosAsync();

        return Ok(libros.Select(l => MapearLibro(l, prestados)));
    }

    [HttpGet("libros/{id:int}")]
    public async Task<ActionResult<LibroAdminDto>> GetLibro(int id)
    {
        var libro = await LibrosConRelaciones().FirstOrDefaultAsync(l => l.Id == id);
        if (libro == null)
        {
            return NotFound();
        }

        var prestados = await EjemplaresPrestadosAsync();
        return Ok(MapearLibro(libro, prestados));
    }

    [HttpPost("libros")]
    public async Task<ActionResult<LibroAdminDto>> PostLibro([FromBody] LibroAdminRequest request)
    {
        var error = await ValidarLibroAsync(request, null);
        if (error != null)
        {
            return Conflict(new MensajeDto { Mensaje = error });
        }

        var libro = new Libro
        {
            Titulo = request.Titulo.Trim(),
            Anio = request.Anio,
            Sinopsis = NormalizarTexto(request.Sinopsis),
            Isbn = NormalizarTexto(request.Isbn),
            Editorial = NormalizarTexto(request.Editorial),
            UrlPortada = NormalizarTexto(request.UrlPortada)
        };

        await AplicarRelacionesAsync(libro, request);

        _context.Libros.Add(libro);
        await _context.SaveChangesAsync();

        var creado = await LibrosConRelaciones().FirstAsync(l => l.Id == libro.Id);
        return CreatedAtAction(nameof(GetLibro), new { id = libro.Id }, MapearLibro(creado, new HashSet<int>()));
    }

    [HttpPut("libros/{id:int}")]
    public async Task<IActionResult> PutLibro(int id, [FromBody] LibroAdminRequest request)
    {
        var libro = await _context.Libros
            .Include(l => l.LibrosAutores)
            .Include(l => l.LibrosGeneros)
            .FirstOrDefaultAsync(l => l.Id == id);

        if (libro == null)
        {
            return NotFound();
        }

        var error = await ValidarLibroAsync(request, id);
        if (error != null)
        {
            return Conflict(new MensajeDto { Mensaje = error });
        }

        libro.Titulo = request.Titulo.Trim();
        libro.Anio = request.Anio;
        libro.Sinopsis = NormalizarTexto(request.Sinopsis);
        libro.Isbn = NormalizarTexto(request.Isbn);
        libro.Editorial = NormalizarTexto(request.Editorial);
        libro.UrlPortada = NormalizarTexto(request.UrlPortada);

        _context.LibrosAutores.RemoveRange(libro.LibrosAutores);
        _context.LibrosGeneros.RemoveRange(libro.LibrosGeneros);
        libro.LibrosAutores.Clear();
        libro.LibrosGeneros.Clear();

        await AplicarRelacionesAsync(libro, request);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpDelete("libros/{id:int}")]
    public async Task<IActionResult> DeleteLibro(int id)
    {
        var libro = await _context.Libros.FindAsync(id);
        if (libro == null)
        {
            return NotFound();
        }

        _context.Libros.Remove(libro);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se puede eliminar el libro porque tiene préstamos asociados."
            });
        }

        return NoContent();
    }

    [HttpGet("prestamos")]
    public async Task<ActionResult<IEnumerable<PrestamoAdminDto>>> GetPrestamos()
    {
        var hoy = DateOnly.FromDateTime(DateTime.Today);

        var prestamos = await _context.Prestamos
            .AsNoTracking()
            .OrderByDescending(p => p.FechaPrestamo)
            .Select(p => new PrestamoAdminDto
            {
                Id = p.Id,
                UsuarioId = p.UsuarioId,
                Usuario = p.Usuario.Nombre,
                EjemplarId = p.EjemplarId,
                CodigoBarras = p.Ejemplar.CodigoBarras,
                LibroId = p.Ejemplar.LibroId,
                Libro = p.Ejemplar.Libro.Titulo,
                FechaPrestamo = p.FechaPrestamo,
                FechaLimite = p.FechaLimite,
                FechaDevolucion = p.FechaDevolucion
            })
            .ToListAsync();

        foreach (var prestamo in prestamos)
        {
            prestamo.Estado = prestamo.FechaDevolucion != null
                ? "devuelto"
                : prestamo.FechaLimite < hoy ? "vencido" : "activo";
        }

        return Ok(prestamos);
    }

    [HttpPost("prestamos")]
    public async Task<ActionResult> PostPrestamo([FromBody] PrestamoAdminRequest request)
    {
        var usuario = await _context.Usuarios.FindAsync(request.UsuarioId);
        if (usuario == null || !usuario.Activo)
        {
            return BadRequest(new MensajeDto { Mensaje = "El usuario no existe o está inactivo." });
        }

        var ejemplar = await _context.Ejemplares.FindAsync(request.EjemplarId);
        if (ejemplar == null || ejemplar.Estado != "activo")
        {
            return BadRequest(new MensajeDto { Mensaje = "El ejemplar no existe o no está disponible." });
        }

        var tienePrestamo = await _context.Prestamos
            .AnyAsync(p => p.EjemplarId == request.EjemplarId && p.FechaDevolucion == null);

        if (tienePrestamo)
        {
            return Conflict(new MensajeDto { Mensaje = "El ejemplar ya tiene un préstamo activo." });
        }

        var hoy = DateOnly.FromDateTime(DateTime.Today);
        var fechaLimite = request.FechaLimite ?? hoy.AddDays(14);

        if (fechaLimite < hoy)
        {
            return BadRequest(new MensajeDto { Mensaje = "La fecha límite no puede ser anterior a hoy." });
        }

        var prestamo = new Prestamo
        {
            UsuarioId = request.UsuarioId,
            EjemplarId = request.EjemplarId,
            FechaPrestamo = DateTime.Now,
            FechaLimite = fechaLimite
        };

        _context.Prestamos.Add(prestamo);
        await _context.SaveChangesAsync();

        return Created($"/api/admin/prestamos/{prestamo.Id}", new { id = prestamo.Id });
    }

    [HttpPut("prestamos/{id:int}/devolver")]
    public async Task<IActionResult> DevolverPrestamo(int id)
    {
        var prestamo = await _context.Prestamos.FindAsync(id);
        if (prestamo == null)
        {
            return NotFound();
        }

        if (prestamo.FechaDevolucion != null)
        {
            return BadRequest(new MensajeDto { Mensaje = "El préstamo ya fue devuelto." });
        }

        prestamo.FechaDevolucion = DateTime.Now;
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpDelete("prestamos/{id:int}")]
    public async Task<IActionResult> DeletePrestamo(int id)
    {
        var prestamo = await _context.Prestamos.FindAsync(id);
        if (prestamo == null)
        {
            return NotFound();
        }

        _context.Prestamos.Remove(prestamo);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpGet("usuarios")]
    public async Task<ActionResult<IEnumerable<UsuarioAdminDto>>> GetUsuarios()
    {
        var usuarios = await _context.Usuarios
            .AsNoTracking()
            .OrderBy(u => u.Nombre)
            .Select(u => new UsuarioAdminDto
            {
                Id = u.Id,
                Nombre = u.Nombre,
                Correo = u.Correo,
                Rol = u.Rol,
                Activo = u.Activo,
                CreadoEn = u.CreadoEn,
                PrestamosActivos = u.Prestamos.Count(p => p.FechaDevolucion == null)
            })
            .ToListAsync();

        return Ok(usuarios);
    }

    [HttpPost("usuarios")]
    public async Task<ActionResult<UsuarioAdminDto>> PostUsuario([FromBody] UsuarioAdminRequest request)
    {
        var error = await ValidarUsuarioAsync(request, null);
        if (error != null)
        {
            return Conflict(new MensajeDto { Mensaje = error });
        }

        var usuario = new Usuario
        {
            Nombre = request.Nombre.Trim(),
            Correo = request.Correo.Trim().ToLowerInvariant(),
            PasswordHash = PasswordHasher.Hash(request.Password!),
            Rol = request.Rol,
            Activo = request.Activo
        };

        _context.Usuarios.Add(usuario);
        await _context.SaveChangesAsync();

        var creado = await _context.Usuarios
            .AsNoTracking()
            .Where(u => u.Id == usuario.Id)
            .Select(u => new UsuarioAdminDto
            {
                Id = u.Id,
                Nombre = u.Nombre,
                Correo = u.Correo,
                Rol = u.Rol,
                Activo = u.Activo,
                CreadoEn = u.CreadoEn,
                PrestamosActivos = 0
            })
            .FirstAsync();

        return CreatedAtAction(nameof(GetUsuarios), new { id = usuario.Id }, creado);
    }

    [HttpPut("usuarios/{id:int}")]
    public async Task<IActionResult> PutUsuario(int id, [FromBody] UsuarioAdminRequest request)
    {
        var usuario = await _context.Usuarios.FindAsync(id);
        if (usuario == null)
        {
            return NotFound();
        }

        var error = await ValidarUsuarioAsync(request, id);
        if (error != null)
        {
            return Conflict(new MensajeDto { Mensaje = error });
        }

        usuario.Nombre = request.Nombre.Trim();
        usuario.Correo = request.Correo.Trim().ToLowerInvariant();
        usuario.Rol = request.Rol;
        usuario.Activo = request.Activo;

        if (!string.IsNullOrWhiteSpace(request.Password))
        {
            usuario.PasswordHash = PasswordHasher.Hash(request.Password);
        }

        await _context.SaveChangesAsync();

        return NoContent();
    }

    [HttpDelete("usuarios/{id:int}")]
    public async Task<IActionResult> DeleteUsuario(int id)
    {
        var usuario = await _context.Usuarios.FindAsync(id);
        if (usuario == null)
        {
            return NotFound();
        }

        _context.Usuarios.Remove(usuario);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se puede eliminar el usuario porque tiene préstamos registrados."
            });
        }

        return NoContent();
    }

    [HttpGet("ejemplares")]
    public async Task<ActionResult<IEnumerable<EjemplarAdminDto>>> GetEjemplares()
    {
        var hoy = DateOnly.FromDateTime(DateTime.Today);

        var ejemplares = await _context.Ejemplares
            .AsNoTracking()
            .OrderBy(e => e.CodigoBarras)
            .Select(e => new EjemplarAdminDto
            {
                Id = e.Id,
                LibroId = e.LibroId,
                Libro = e.Libro.Titulo,
                CodigoBarras = e.CodigoBarras,
                Estado = e.Estado,
                PrestamoActivo = e.Prestamos
                    .Where(p => p.FechaDevolucion == null)
                    .Select(p => new PrestamoActivoDto
                    {
                        Id = p.Id,
                        Usuario = p.Usuario.Nombre,
                        FechaLimite = p.FechaLimite,
                        Vencido = p.FechaLimite < hoy
                    })
                    .FirstOrDefault()
            })
            .ToListAsync();

        return Ok(ejemplares);
    }

    private IQueryable<Libro> LibrosConRelaciones()
    {
        return _context.Libros
            .AsNoTracking()
            .Include(l => l.LibrosAutores).ThenInclude(la => la.Autor)
            .Include(l => l.LibrosGeneros).ThenInclude(lg => lg.Genero)
            .Include(l => l.Ejemplares)
            .AsSplitQuery()
            .OrderBy(l => l.Titulo);
    }

    private async Task<HashSet<int>> EjemplaresPrestadosAsync()
    {
        var ids = await _context.Prestamos
            .AsNoTracking()
            .Where(p => p.FechaDevolucion == null)
            .Select(p => p.EjemplarId)
            .ToListAsync();

        return ids.ToHashSet();
    }

    private static LibroAdminDto MapearLibro(Libro libro, HashSet<int> prestados)
    {
        return new LibroAdminDto
        {
            Id = libro.Id,
            Titulo = libro.Titulo,
            Anio = libro.Anio,
            Sinopsis = libro.Sinopsis,
            Isbn = libro.Isbn,
            Editorial = libro.Editorial,
            UrlPortada = libro.UrlPortada,
            Autores = libro.LibrosAutores
                .Where(la => la.Autor != null)
                .Select(la => la.Autor.Nombre)
                .OrderBy(n => n)
                .ToList(),
            Generos = libro.LibrosGeneros
                .Where(lg => lg.Genero != null)
                .Select(lg => lg.Genero.Nombre)
                .OrderBy(n => n)
                .ToList(),
            Ejemplares = libro.Ejemplares.Count(e => e.Estado == "activo"),
            Disponibles = libro.Ejemplares.Count(e => e.Estado == "activo" && !prestados.Contains(e.Id))
        };
    }

    private async Task AplicarRelacionesAsync(Libro libro, LibroAdminRequest request)
    {
        foreach (var nombre in LimpiarNombres(request.Autores))
        {
            var autor = await ObtenerOCrearAutorAsync(nombre);
            libro.LibrosAutores.Add(new LibroAutor { Libro = libro, Autor = autor });
        }

        foreach (var nombre in LimpiarNombres(request.Generos))
        {
            var genero = await ObtenerOCrearGeneroAsync(nombre);
            libro.LibrosGeneros.Add(new LibroGenero { Libro = libro, Genero = genero });
        }
    }

    private async Task<Autor> ObtenerOCrearAutorAsync(string nombre)
    {
        var normalizado = nombre.ToLower();
        var autor = await _context.Autores
            .FirstOrDefaultAsync(a => a.Nombre.ToLower() == normalizado);

        if (autor == null)
        {
            autor = new Autor { Nombre = nombre };
            _context.Autores.Add(autor);
        }

        return autor;
    }

    private async Task<Genero> ObtenerOCrearGeneroAsync(string nombre)
    {
        var normalizado = nombre.ToLower();
        var genero = await _context.Generos
            .FirstOrDefaultAsync(g => g.Nombre.ToLower() == normalizado);

        if (genero == null)
        {
            genero = new Genero { Nombre = nombre };
            _context.Generos.Add(genero);
        }

        return genero;
    }

    private async Task<string?> ValidarLibroAsync(LibroAdminRequest request, int? id)
    {
        if (string.IsNullOrWhiteSpace(request.Titulo))
        {
            return "El título es obligatorio.";
        }

        if (request.Anio is < 1 or > 2100)
        {
            return "El año debe estar entre 1 y 2100.";
        }

        var isbn = NormalizarTexto(request.Isbn);
        if (isbn != null)
        {
            var existe = await _context.Libros
                .AnyAsync(l => l.Isbn == isbn && (!id.HasValue || l.Id != id.Value));

            if (existe)
            {
                return "Ya existe un libro con ese ISBN.";
            }
        }

        return null;
    }

    private async Task<string?> ValidarUsuarioAsync(UsuarioAdminRequest request, int? id)
    {
        if (string.IsNullOrWhiteSpace(request.Nombre))
        {
            return "El nombre es obligatorio.";
        }

        if (string.IsNullOrWhiteSpace(request.Correo))
        {
            return "El correo es obligatorio.";
        }

        if (!RolesValidos.Contains(request.Rol))
        {
            return "El rol debe ser lector, bibliotecario o admin.";
        }

        if (!id.HasValue && string.IsNullOrWhiteSpace(request.Password))
        {
            return "La contraseña es obligatoria para un usuario nuevo.";
        }

        var correo = request.Correo.Trim().ToLowerInvariant();
        var existe = await _context.Usuarios
            .AnyAsync(u => u.Correo == correo && (!id.HasValue || u.Id != id.Value));

        return existe ? "Ya existe un usuario con ese correo." : null;
    }

    private static IEnumerable<string> LimpiarNombres(IEnumerable<string>? nombres)
    {
        if (nombres == null)
        {
            return Enumerable.Empty<string>();
        }

        return nombres
            .Select(n => n?.Trim() ?? string.Empty)
            .Where(n => n.Length > 0)
            .Distinct(StringComparer.OrdinalIgnoreCase);
    }

    private static string? NormalizarTexto(string? valor)
    {
        return string.IsNullOrWhiteSpace(valor) ? null : valor.Trim();
    }

}
