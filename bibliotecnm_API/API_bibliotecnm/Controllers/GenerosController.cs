using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
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

    // PUT: api/Generos/5
    [HttpPut("{id:int}")]
    public async Task<IActionResult> PutGenero(int id, Genero genero)
    {
        if (id != genero.Id)
        {
            return BadRequest();
        }

        if (string.IsNullOrWhiteSpace(genero.Nombre))
        {
            return BadRequest(new MensajeDto { Mensaje = "El nombre es obligatorio." });
        }

        var existente = await _context.Generos.FindAsync(id);
        if (existente == null)
        {
            return NotFound();
        }

        existente.Nombre = genero.Nombre.Trim();

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto { Mensaje = "Ya existe un género con ese nombre." });
        }

        return NoContent();
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

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "No se puede eliminar el género porque tiene libros asociados."
            });
        }

        return NoContent();
    }
}
