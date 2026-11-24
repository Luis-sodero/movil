using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class LibrosController : ControllerBase
{
    private readonly ContextoBD _context;

    public LibrosController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/Libros
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Libro>>> GetLibros()
    {
        return await _context.Libros
            .Include(l => l.LibrosAutores).ThenInclude(la => la.Autor)
            .Include(l => l.LibrosGeneros).ThenInclude(lg => lg.Genero)
            .Include(l => l.Ejemplares)
            .AsSplitQuery()
            .AsNoTracking()
            .ToListAsync();
    }

    // GET: api/Libros/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Libro>> GetLibro(int id)
    {
        var libro = await _context.Libros
            .Include(l => l.LibrosAutores).ThenInclude(la => la.Autor)
            .Include(l => l.LibrosGeneros).ThenInclude(lg => lg.Genero)
            .Include(l => l.Ejemplares)
            .AsSplitQuery()
            .AsNoTracking()
            .FirstOrDefaultAsync(l => l.Id == id);

        if (libro == null)
        {
            return NotFound();
        }

        return libro;
    }

    // PUT: api/Libros/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> PutLibro(int id, Libro libro)
    {
        if (id != libro.Id)
        {
            return BadRequest();
        }

        _context.Entry(libro).State = EntityState.Modified;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException)
        {
            if (!await LibroExists(id))
            {
                return NotFound();
            }

            throw;
        }

        return NoContent();
    }

    // POST: api/Libros
    [HttpPost]
    public async Task<ActionResult<Libro>> PostLibro(Libro libro)
    {
        _context.Libros.Add(libro);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetLibro), new { id = libro.Id }, libro);
    }

    // DELETE: api/Libros/5
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> DeleteLibro(int id)
    {
        var libro = await _context.Libros.FindAsync(id);
        if (libro == null)
        {
            return NotFound();
        }

        _context.Libros.Remove(libro);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    private Task<bool> LibroExists(int id) => _context.Libros.AnyAsync(e => e.Id == id);
}
