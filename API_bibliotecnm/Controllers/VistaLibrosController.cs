using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;

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
    public async Task<ActionResult<IEnumerable<VistaLibroDto>>> GetVistaLibros()
    {
        var result = await _context.VistaLibros
            .AsNoTracking()
            .OrderBy(l => l.Id)
            .Select(l => new VistaLibroDto
            {
                Id = l.Id,
                Titulo = l.Titulo,
                Anio = l.Anio,
                Autores = l.Autores,
                Generos = l.Generos,
                EjemplaresTotal = l.EjemplaresTotal,
                EjemplaresDisponibles = l.EjemplaresDisponibles
            })
            .ToListAsync();

        return Ok(result);
    }

    // GET: api/VistaLibros/5
    [HttpGet("{id:int}")]
    public async Task<ActionResult<VistaLibroDto>> GetVistaLibro(int id)
    {
        var vistaLibro = await _context.VistaLibros
            .AsNoTracking()
            .Where(l => l.Id == id)
            .Select(l => new VistaLibroDto
            {
                Id = l.Id,
                Titulo = l.Titulo,
                Anio = l.Anio,
                Autores = l.Autores,
                Generos = l.Generos,
                EjemplaresTotal = l.EjemplaresTotal,
                EjemplaresDisponibles = l.EjemplaresDisponibles
            })
            .FirstOrDefaultAsync();

        if (vistaLibro == null)
        {
            return NotFound();
        }

        return Ok(vistaLibro);
    }
}