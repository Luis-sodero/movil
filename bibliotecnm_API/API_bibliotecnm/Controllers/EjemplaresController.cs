using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class EjemplaresController : ControllerBase
{
    private static readonly string[] EstadosValidos = { "activo", "mantenimiento", "baja" };

    private readonly ContextoBD _context;

    public EjemplaresController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/Ejemplares
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Ejemplar>>> GetEjemplares()
    {
        return await _context.Ejemplares
            .Include(e => e.Libro)
            .AsNoTracking()
            .ToListAsync();
    }

    // GET: api/Ejemplares/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Ejemplar>> GetEjemplar(int id)
    {
        var ejemplar = await _context.Ejemplares
            .Include(e => e.Libro)
            .AsNoTracking()
            .FirstOrDefaultAsync(e => e.Id == id);

        if (ejemplar == null)
        {
            return NotFound();
        }

        return ejemplar;
    }

    // PUT: api/Ejemplares/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> PutEjemplar(int id, [FromBody] EjemplarRequest request)
    {
        var existente = await _context.Ejemplares.FindAsync(id);
        if (existente == null)
        {
            return NotFound();
        }

        var error = Validar(request, id);
        if (error != null)
        {
            return BadRequest(new MensajeDto { Mensaje = error });
        }

        if (!await _context.Libros.AnyAsync(l => l.Id == request.LibroId))
        {
            return BadRequest(new MensajeDto { Mensaje = "El libro seleccionado no existe." });
        }

        var codigo = request.CodigoBarras.Trim();
        var duplicado = await _context.Ejemplares
            .AnyAsync(e => e.CodigoBarras == codigo && e.Id != id);

        if (duplicado)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "Ya existe un ejemplar con ese código de barras.",
                Codigo = "CODIGO_DUPLICADO"
            });
        }

        existente.LibroId = request.LibroId;
        existente.CodigoBarras = codigo;
        existente.Estado = request.Estado;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se pudo actualizar: código de barras duplicado o libro inexistente."
            });
        }

        return NoContent();
    }

    // POST: api/Ejemplares
    [HttpPost]
    public async Task<ActionResult<Ejemplar>> PostEjemplar([FromBody] EjemplarRequest request)
    {
        var error = Validar(request, null);
        if (error != null)
        {
            return BadRequest(new MensajeDto { Mensaje = error });
        }

        if (!await _context.Libros.AnyAsync(l => l.Id == request.LibroId))
        {
            return BadRequest(new MensajeDto { Mensaje = "El libro seleccionado no existe." });
        }

        var codigo = request.CodigoBarras.Trim();
        var duplicado = await _context.Ejemplares
            .AnyAsync(e => e.CodigoBarras == codigo);

        if (duplicado)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "Ya existe un ejemplar con ese código de barras.",
                Codigo = "CODIGO_DUPLICADO"
            });
        }

        var ejemplar = new Ejemplar
        {
            LibroId = request.LibroId,
            CodigoBarras = codigo,
            Estado = request.Estado
        };

        _context.Ejemplares.Add(ejemplar);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "Ya existe un ejemplar con ese código de barras.",
                Codigo = "CODIGO_DUPLICADO"
            });
        }

        return CreatedAtAction(nameof(GetEjemplar), new { id = ejemplar.Id }, ejemplar);
    }

    private static string? Validar(EjemplarRequest request, int? id)
    {
        if (string.IsNullOrWhiteSpace(request.CodigoBarras))
        {
            return "El código de barras es obligatorio.";
        }

        if (request.CodigoBarras.Trim().Length > 40)
        {
            return "El código de barras no puede tener más de 40 caracteres.";
        }

        if (!EstadosValidos.Contains(request.Estado))
        {
            return "El estado debe ser activo, mantenimiento o baja.";
        }

        return null;
    }

    // DELETE: api/Ejemplares/5
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteEjemplar(int id)
    {
        var ejemplar = await _context.Ejemplares.FindAsync(id);
        if (ejemplar == null)
        {
            return NotFound();
        }

        _context.Ejemplares.Remove(ejemplar);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se puede eliminar el ejemplar porque tiene préstamos registrados."
            });
        }

        return NoContent();
    }
}
