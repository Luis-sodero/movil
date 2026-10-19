using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class GenerosController : ControllerBase
{
    private readonly ContextoBD _context;

    public GenerosController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/Generos
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Genero>>> GetGeneros()
    {
        return await _context.Generos.AsNoTracking().ToListAsync();
    }

    // GET: api/Generos/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Genero>> GetGenero(int id)
    {
        var genero = await _context.Generos.AsNoTracking().FirstOrDefaultAsync(g => g.Id == id);

        if (genero == null)
        {
            return NotFound();
        }

        return genero;
    }

    // POST: api/Generos
    [HttpPost]
    public async Task<ActionResult<Genero>> PostGenero(Genero genero)
    {
        _context.Generos.Add(genero);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetGenero), new { id = genero.Id }, genero);
    }

    // DELETE: api/Generos/5
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteGenero(int id)
    {
        var genero = await _context.Generos.FindAsync(id);
        if (genero == null)
        {
            return NotFound();
        }

        _context.Generos.Remove(genero);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
