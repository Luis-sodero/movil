namespace API_bibliotecnm.Models;

public class Libro
{
    public int Id { get; set; }

    public string Titulo { get; set; } = null!;

    public short? Anio { get; set; }

    public string? Sinopsis { get; set; }

    public string? Isbn { get; set; }

    public string? Editorial { get; set; }

    public string? UrlPortada { get; set; }

    public DateTime CreadoEn { get; set; }

    public ICollection<LibroAutor> LibrosAutores { get; set; } = new List<LibroAutor>();

    public ICollection<LibroGenero> LibrosGeneros { get; set; } = new List<LibroGenero>();

    public ICollection<Ejemplar> Ejemplares { get; set; } = new List<Ejemplar>();
}
