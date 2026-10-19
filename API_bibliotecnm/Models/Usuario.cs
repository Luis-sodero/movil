namespace API_bibliotecnm.Models;

public class Usuario
{
    public int Id { get; set; }

    public string Nombre { get; set; } = null!;

    public string Correo { get; set; } = null!;

    public string PasswordHash { get; set; } = null!;

    public string Rol { get; set; } = "lector";

    public bool Activo { get; set; } = true;

    public DateTime CreadoEn { get; set; }

    public ICollection<Prestamo> Prestamos { get; set; } = new List<Prestamo>();
}
