namespace API_bibliotecnm.Models;

public class Genero
{
    public int Id { get; set; }

    public string Nombre { get; set; } = null!;

    public ICollection<LibroGenero> LibrosGeneros { get; set; } = new List<LibroGenero>();
}
