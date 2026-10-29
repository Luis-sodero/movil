namespace API_bibliotecnm.DTOs;

public class SolicitudPrestamoRequest
{
    public int LibroId { get; set; }
    public int UsuarioId { get; set; }
    public long TimestampSolicitud { get; set; }
}