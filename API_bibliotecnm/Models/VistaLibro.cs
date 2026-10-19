namespace API_bibliotecnm.Models;

/// <summary>
/// Proyeccion de solo lectura de la vista "vista_libros".
/// Incluye autores, generos y disponibilidad ya calculados.
/// </summary>
public class VistaLibro
{
    public int Id { get; set; }

    public string Titulo { get; set; } = null!;

    public short? Anio { get; set; }

    public string? Sinopsis { get; set; }

    public string? Isbn { get; set; }

    public string? Editorial { get; set; }

    public string? UrlPortada { get; set; }

    public string Autores { get; set; } = string.Empty;

    public string Generos { get; set; } = string.Empty;

    public int EjemplaresTotal { get; set; }

    public int EjemplaresDisponibles { get; set; }
}
