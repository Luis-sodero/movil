namespace API_bibliotecnm.DTOs;

public class SolicitudPrestamoResponse
{
    public int Id { get; set; }
    public int EjemplarId { get; set; }
    public DateOnly FechaLimite { get; set; }
}