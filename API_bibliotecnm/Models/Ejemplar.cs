namespace API_bibliotecnm.Models;

public class Ejemplar
{
    public int Id { get; set; }

    public int LibroId { get; set; }

    public string CodigoBarras { get; set; } = null!;

    public string Estado { get; set; } = "activo";

    public DateTime CreadoEn { get; set; }

    public Libro Libro { get; set; } = null!;

    public ICollection<Prestamo> Prestamos { get; set; } = new List<Prestamo>();
}
