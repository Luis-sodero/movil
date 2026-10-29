namespace API_bibliotecnm.DTOs;

public class ResumenAdminDto
{
    public int Usuarios { get; set; }
    public int Libros { get; set; }
    public int Autores { get; set; }
    public int Generos { get; set; }
    public int Ejemplares { get; set; }
    public int EjemplaresDisponibles { get; set; }
    public int PrestamosActivos { get; set; }
    public int PrestamosVencidos { get; set; }
}

public class LibroAdminDto
{
    public int Id { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public short? Anio { get; set; }
    public string? Sinopsis { get; set; }
    public string? Isbn { get; set; }
    public string? Editorial { get; set; }
    public string? UrlPortada { get; set; }
    public List<string> Autores { get; set; } = new();
    public List<string> Generos { get; set; } = new();
    public int Ejemplares { get; set; }
    public int Disponibles { get; set; }
}

public class LibroAdminRequest
{
    public string Titulo { get; set; } = string.Empty;
    public short? Anio { get; set; }
    public string? Sinopsis { get; set; }
    public string? Isbn { get; set; }
    public string? Editorial { get; set; }
    public string? UrlPortada { get; set; }
    public List<string> Autores { get; set; } = new();
    public List<string> Generos { get; set; } = new();
}

public class PrestamoAdminDto
{
    public int Id { get; set; }
    public int UsuarioId { get; set; }
    public string Usuario { get; set; } = string.Empty;
    public int EjemplarId { get; set; }
    public string CodigoBarras { get; set; } = string.Empty;
    public int LibroId { get; set; }
    public string Libro { get; set; } = string.Empty;
    public DateTime FechaPrestamo { get; set; }
    public DateOnly FechaLimite { get; set; }
    public DateTime? FechaDevolucion { get; set; }
    public string Estado { get; set; } = string.Empty;
}

public class PrestamoAdminRequest
{
    public int UsuarioId { get; set; }
    public int EjemplarId { get; set; }
    public DateOnly? FechaLimite { get; set; }
}

public class EjemplarAdminDto
{
    public int Id { get; set; }
    public int LibroId { get; set; }
    public string Libro { get; set; } = string.Empty;
    public string CodigoBarras { get; set; } = string.Empty;
    public string Estado { get; set; } = string.Empty;
    public PrestamoActivoDto? PrestamoActivo { get; set; }
}

public class PrestamoActivoDto
{
    public int Id { get; set; }
    public string Usuario { get; set; } = string.Empty;
    public DateOnly FechaLimite { get; set; }
    public bool Vencido { get; set; }
}

public class UsuarioAdminDto
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Correo { get; set; } = string.Empty;
    public string Rol { get; set; } = string.Empty;
    public bool Activo { get; set; }
    public DateTime CreadoEn { get; set; }
    public int PrestamosActivos { get; set; }
}

public class UsuarioAdminRequest
{
    public string Nombre { get; set; } = string.Empty;
    public string Correo { get; set; } = string.Empty;
    public string Rol { get; set; } = "lector";
    public bool Activo { get; set; } = true;
    public string? Password { get; set; }
}
