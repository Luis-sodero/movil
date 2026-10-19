using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class EjemplaresController : ControllerBase
{
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
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
