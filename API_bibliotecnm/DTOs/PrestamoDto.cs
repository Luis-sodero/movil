namespace API_bibliotecnm.DTOs;

public class PrestamoDto
{
    public int Id { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string Autor { get; set; } = string.Empty;
    public DateOnly FechaLimite { get; set; }
    public DateTime? FechaDevolucion { get; set; }
}