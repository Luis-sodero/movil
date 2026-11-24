namespace API_bibliotecnm.Models;

public class Autor
{
    public int Id { get; set; }

    public string Nombre { get; set; } = null!;

    public ICollection<LibroAutor> LibrosAutores { get; set; } = new List<LibroAutor>();
}
