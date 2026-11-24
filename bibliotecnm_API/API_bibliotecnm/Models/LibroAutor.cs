namespace API_bibliotecnm.Models;

/// <summary>Tabla puente: relacion muchos-a-muchos entre libros y autores.</summary>
public class LibroAutor
{
    public int LibroId { get; set; }

    public int AutorId { get; set; }

    public Libro Libro { get; set; } = null!;

    public Autor Autor { get; set; } = null!;
}
