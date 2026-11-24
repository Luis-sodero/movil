using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
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

    // PUT: api/Autores/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> PutAutor(int id, Autor autor)
    {
        if (id != autor.Id)
        {
            return BadRequest();
        }

        if (string.IsNullOrWhiteSpace(autor.Nombre))
        {
            return BadRequest(new MensajeDto { Mensaje = "El nombre es obligatorio." });
        }

        var existente = await _context.Autores.FindAsync(id);
        if (existente == null)
        {
            return NotFound();
        }

        existente.Nombre = autor.Nombre.Trim();

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto { Mensaje = "Ya existe un autor con ese nombre." });
        }

        return NoContent();
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

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se puede eliminar el autor porque tiene libros asociados."
            });
        }

        return NoContent();
    }
}
