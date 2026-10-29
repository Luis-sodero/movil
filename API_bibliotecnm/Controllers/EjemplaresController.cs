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
    public async Task<IActionResult> PutEjemplar(int id, Ejemplar ejemplar)
    {
        if (id != ejemplar.Id)
        {
            return BadRequest();
        }

        if (string.IsNullOrWhiteSpace(ejemplar.CodigoBarras))
        {
            return BadRequest(new MensajeDto { Mensaje = "El código de barras es obligatorio." });
        }

        if (!EstadosValidos.Contains(ejemplar.Estado))
        {
            return BadRequest(new MensajeDto { Mensaje = "El estado debe ser activo, mantenimiento o baja." });
        }

        var existente = await _context.Ejemplares.FindAsync(id);
        if (existente == null)
        {
            return NotFound();
        }

        existente.LibroId = ejemplar.LibroId;
        existente.CodigoBarras = ejemplar.CodigoBarras.Trim();
        existente.Estado = ejemplar.Estado;

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
    public async Task<ActionResult<Ejemplar>> PostEjemplar(Ejemplar ejemplar)
    {
        _context.Ejemplares.Add(ejemplar);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetEjemplar), new { id = ejemplar.Id }, ejemplar);
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
