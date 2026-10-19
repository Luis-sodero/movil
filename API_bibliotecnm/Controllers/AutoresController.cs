using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AutoresController : ControllerBase
{
    private readonly ContextoBD _context;

    public AutoresController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/Autores
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Autor>>> GetAutores()
    {
        return await _context.Autores.AsNoTracking().ToListAsync();
    }

    // GET: api/Autores/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Autor>> GetAutor(int id)
    {
        var autor = await _context.Autores.AsNoTracking().FirstOrDefaultAsync(a => a.Id == id);

        if (autor == null)
        {
            return NotFound();
        }

        return autor;
    }

    // POST: api/Autores
    [HttpPost]
    public async Task<ActionResult<Autor>> PostAutor(Autor autor)
    {
        _context.Autores.Add(autor);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetAutor), new { id = autor.Id }, autor);
    }

    // DELETE: api/Autores/5
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteAutor(int id)
    {
        var autor = await _context.Autores.FindAsync(id);
        if (autor == null)
        {
            return NotFound();
        }

        _context.Autores.Remove(autor);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
