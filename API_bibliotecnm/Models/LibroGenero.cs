namespace API_bibliotecnm.Models;

/// <summary>Tabla puente: relacion muchos-a-muchos entre libros y generos.</summary>
public class LibroGenero
{
    public int LibroId { get; set; }

    public int GeneroId { get; set; }

    public Libro Libro { get; set; } = null!;

    public Genero Genero { get; set; } = null!;
}
