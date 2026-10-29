namespace API_bibliotecnm.Models;

public class Prestamo
{
    public int Id { get; set; }

    public int UsuarioId { get; set; }

    public int EjemplarId { get; set; }

    public DateTime FechaPrestamo { get; set; }

    public DateOnly FechaLimite { get; set; }

    public DateTime? FechaDevolucion { get; set; }

    public DateTime? FechaSolicitud { get; set; }

    public DateTime? FechaRecepcion { get; set; }

    public Usuario Usuario { get; set; } = null!;

    public Ejemplar Ejemplar { get; set; } = null!;
}
