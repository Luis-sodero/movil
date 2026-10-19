using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

/// <summary>Consultas de solo lectura sobre la vista "vista_libros".</summary>
[Route("api/[controller]")]
[ApiController]
public class VistaLibrosController : ControllerBase
{
    private readonly ContextoBD _context;

    public VistaLibrosController(ContextoBD context)
    {
        _context = context;
    }

    // GET: api/VistaLibros
    [HttpGet]
    public async Task<ActionResult<IEnumerable<VistaLibro>>> GetVistaLibros()
    {
        return await _context.VistaLibros
            .AsNoTracking()
            .OrderBy(l => l.Id)
            .ToListAsync();
    }

    // GET: api/VistaLibros/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<VistaLibro>> GetVistaLibro(int id)
    {
        var vistaLibro = await _context.VistaLibros
            .AsNoTracking()
            .FirstOrDefaultAsync(l => l.Id == id);

        if (vistaLibro == null)
        {
            return NotFound();
        }

        return vistaLibro;
    }
}
