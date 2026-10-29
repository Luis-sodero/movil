namespace API_bibliotecnm.DTOs;

public class VistaLibroDto
{
    public int Id { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public short? Anio { get; set; }
    public string? Autores { get; set; }
    public string? Generos { get; set; }
    public int EjemplaresTotal { get; set; }
    public int EjemplaresDisponibles { get; set; }
}