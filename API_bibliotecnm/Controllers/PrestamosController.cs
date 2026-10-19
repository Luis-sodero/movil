using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

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

    // GET: api/Prestamos
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Prestamo>>> GetPrestamos()
    {
        return await _context.Prestamos
            .Include(p => p.Usuario)
            .Include(p => p.Ejemplar)
            .AsNoTracking()
            .ToListAsync();
    }

    // GET: api/Prestamos/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Prestamo>> GetPrestamo(int id)
    {
        var prestamo = await _context.Prestamos
            .Include(p => p.Usuario)
            .Include(p => p.Ejemplar)
            .AsNoTracking()
            .FirstOrDefaultAsync(p => p.Id == id);

        if (prestamo == null)
        {
            return NotFound();
        }

        return prestamo;
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
