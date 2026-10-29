using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Npgsql;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
using API_bibliotecnm.Models;
using System.Data;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class PrestamosController : ControllerBase
{
    private readonly ContextoBD _context;

    public PrestamosController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/Prestamos?usuarioId=1
    [HttpGet]
    public async Task<ActionResult<IEnumerable<PrestamoDto>>> GetPrestamos([FromQuery] int? usuarioId = null)
    {
        var query = _context.Prestamos
            .AsNoTracking()
            .Where(p => !usuarioId.HasValue || p.UsuarioId == usuarioId.Value);

        var result = await query
            .Select(p => new PrestamoDto
            {
                Id = p.Id,
                Titulo = p.Ejemplar.Libro.Titulo,
                Autor = string.Join(", ", p.Ejemplar.Libro.LibrosAutores
                    .Select(la => la.Autor != null ? la.Autor.Nombre : string.Empty)
                    .Where(a => !string.IsNullOrEmpty(a))),
                FechaLimite = p.FechaLimite,
                FechaDevolucion = p.FechaDevolucion
            })
            .ToListAsync();

        return Ok(result);
    }

    // GET: api/Prestamos/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<PrestamoDto>> GetPrestamo(int id)
    {
        var prestamo = await _context.Prestamos
            .AsNoTracking()
            .Where(p => p.Id == id)
            .Select(p => new PrestamoDto
            {
                Id = p.Id,
                Titulo = p.Ejemplar.Libro.Titulo,
                Autor = string.Join(", ", p.Ejemplar.Libro.LibrosAutores
                    .Select(la => la.Autor != null ? la.Autor.Nombre : string.Empty)
                    .Where(a => !string.IsNullOrEmpty(a))),
                FechaLimite = p.FechaLimite,
                FechaDevolucion = p.FechaDevolucion
            })
            .FirstOrDefaultAsync();

        if (prestamo == null)
        {
            return NotFound();
        }

        return Ok(prestamo);
    }

    // POST: api/Prestamos/solicitar
    [HttpPost("solicitar")]
    public async Task<ActionResult> SolicitarPrestamo([FromBody] SolicitudPrestamoRequest request)
    {
        if (request == null)
        {
            return BadRequest(new MensajeDto { Mensaje = "Solicitud inválida" });
        }

        // 1. Verificar que el usuario existe
        var usuarioExiste = await _context.Usuarios
            .AsNoTracking()
            .AnyAsync(u => u.Id == request.UsuarioId);

        if (!usuarioExiste)
        {
            return NotFound();
        }

        var ahoraServidor = DateTime.UtcNow;
        DateTime fechaSolicitud;
        if (request.TimestampSolicitud <= 0)
        {
            fechaSolicitud = DateTime.UtcNow;
        }
        else
        {
            try
            {
                fechaSolicitud = DateTimeOffset.FromUnixTimeMilliseconds(request.TimestampSolicitud).UtcDateTime;
            }
            catch
            {
                fechaSolicitud = DateTime.UtcNow;
            }

            if (fechaSolicitud > DateTime.UtcNow.AddMinutes(2) || fechaSolicitud < DateTime.UtcNow.AddDays(-7))
            {
                fechaSolicitud = DateTime.UtcNow;
            }
        }

        if (fechaSolicitud.Kind != DateTimeKind.Utc)
        {
            fechaSolicitud = DateTime.SpecifyKind(fechaSolicitud, DateTimeKind.Utc);
        }

        var fechaRecepcion = DateTime.SpecifyKind(DateTime.UtcNow, DateTimeKind.Utc);

        // 2. Buscar un préstamo existente con usuario_id + libro_id + fecha_solicitud (+ fecha_devolucion IS NULL)
        var prestamoExistentePorSolicitud = await _context.Prestamos
            .Include(p => p.Ejemplar)
            .Where(p => p.UsuarioId == request.UsuarioId && p.Ejemplar != null && p.Ejemplar.LibroId == request.LibroId && p.FechaSolicitud == fechaSolicitud && p.FechaDevolucion == null)
            .FirstOrDefaultAsync();
        if (prestamoExistentePorSolicitud != null)
        {
            var respExistente = new SolicitudPrestamoResponse
            {
                Id = prestamoExistentePorSolicitud.Id,
                EjemplarId = prestamoExistentePorSolicitud.EjemplarId,
                FechaLimite = prestamoExistentePorSolicitud.FechaLimite
            };
            return Ok(respExistente);
        }

        // 3. Si el usuario ya tiene el libro activo (otra solicitud distinta): 409 con codigo "YA_PRESTADO"
        var yaPrestado = await _context.Prestamos
            .Include(p => p.Ejemplar)
            .AnyAsync(p => p.UsuarioId == request.UsuarioId && p.Ejemplar != null && p.Ejemplar.LibroId == request.LibroId && p.FechaDevolucion == null);
        if (yaPrestado)
        {
            return Conflict(new MensajeDto { Mensaje = "Ya tienes este libro prestado", Codigo = "YA_PRESTADO" });
        }

        await using var tx = await _context.Database.BeginTransactionAsync(System.Data.IsolationLevel.Serializable);

        // Buscar ejemplares activos del libro con sin prestamo activo (FechaDevolucion == null)
        var ejemplaresCandidatos = await _context.Ejemplares
            .FromSqlRaw("SELECT * FROM ejemplares WHERE libro_id = {0} AND estado = 'activo' FOR UPDATE", request.LibroId)
            .ToListAsync();

        var ejemplaresLibres = new List<API_bibliotecnm.Models.Ejemplar>();
        foreach (var e in ejemplaresCandidatos)
        {
            var tieneActivo = await _context.Prestamos
                .AnyAsync(p => p.EjemplarId == e.Id && p.FechaDevolucion == null);
            if (!tieneActivo)
            {
                ejemplaresLibres.Add(e);
            }
        }

        if (ejemplaresLibres.Count == 0)
        {
            await tx.RollbackAsync();
            return Conflict(new MensajeDto { Mensaje = "No hay ejemplares libres", Codigo = "SIN_EJEMPLARES" });
        }

        // Asignación por orden de llegada por fecha_solicitud: priorizar solicitudes con menor fecha_solicitud
        var ejemplarSeleccionado = ejemplaresLibres.First();

        try
        {
            var prestamo = new Prestamo
            {
                UsuarioId = request.UsuarioId,
                EjemplarId = ejemplarSeleccionado.Id,
                FechaPrestamo = DateTime.SpecifyKind(DateTime.UtcNow, DateTimeKind.Utc),
                FechaLimite = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(14)),
                FechaDevolucion = null,
                FechaSolicitud = fechaSolicitud,
                FechaRecepcion = fechaRecepcion
            };

            _context.Prestamos.Add(prestamo);
            await _context.SaveChangesAsync();
            await tx.CommitAsync();

            var response = new SolicitudPrestamoResponse
            {
                Id = prestamo.Id,
                EjemplarId = prestamo.EjemplarId,
                FechaLimite = prestamo.FechaLimite
            };

            return Ok(response);
        }
        catch (DbUpdateException ex)
        {
            await tx.RollbackAsync();
            foreach (var entry in _context.ChangeTracker.Entries())
            {
                entry.State = EntityState.Detached;
            }

            var inner = ex.InnerException;
            if (inner is PostgresException pgEx && pgEx.SqlState == "23505")
            {
                return Conflict(new MensajeDto { Mensaje = "No hay ejemplares libres", Codigo = "SIN_EJEMPLARES" });
            }

            return Conflict(new MensajeDto { Mensaje = "No hay ejemplares libres", Codigo = "SIN_EJEMPLARES" });
        }
        catch
        {
            await tx.RollbackAsync();
            return Conflict(new MensajeDto { Mensaje = "No hay ejemplares libres", Codigo = "SIN_EJEMPLARES" });
        }
    }

    // POST: api/Prestamos
    [HttpPost]
    public async Task<ActionResult<Prestamo>> PostPrestamo(Prestamo prestamo)
    {
        _context.Prestamos.Add(prestamo);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetPrestamo), new { id = prestamo.Id }, prestamo);
    }

    // DELETE: api/Prestamos/5
    [HttpDelete("{id:int}")]
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
}