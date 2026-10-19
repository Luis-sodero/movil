using API_bibliotecnm.Models;
using Microsoft.EntityFrameworkCore;

namespace API_bibliotecnm.Data;

/// <summary>
/// Contexto de Entity Framework Core para la base de datos PostgreSQL "bibliotecnm".
/// El esquema (tablas, indices y la vista vista_libros) se crea con el script
/// biblotecnm.sql; aqui solo se mapea dicho esquema.
/// </summary>
public class ContextoBD : DbContext
{
    public ContextoBD(DbContextOptions<ContextoBD> options)
        : base(options)
    {
    }

    public DbSet<Usuario> Usuarios => Set<Usuario>();

    public DbSet<Libro> Libros => Set<Libro>();

    public DbSet<Autor> Autores => Set<Autor>();

    public DbSet<Genero> Generos => Set<Genero>();

    public DbSet<LibroAutor> LibrosAutores => Set<LibroAutor>();

    public DbSet<LibroGenero> LibrosGeneros => Set<LibroGenero>();

    public DbSet<Ejemplar> Ejemplares => Set<Ejemplar>();

    public DbSet<Prestamo> Prestamos => Set<Prestamo>();

    public DbSet<VistaLibro> VistaLibros => Set<VistaLibro>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.ToTable("usuarios");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(120).IsRequired();
            entity.Property(e => e.Correo).HasColumnName("correo").HasMaxLength(160).IsRequired();
            entity.Property(e => e.PasswordHash).HasColumnName("password_hash").IsRequired();
            entity.Property(e => e.Rol).HasColumnName("rol").HasMaxLength(20).IsRequired();
            entity.Property(e => e.Activo).HasColumnName("activo");
            entity.Property(e => e.CreadoEn).HasColumnName("creado_en").HasColumnType("timestamptz").HasDefaultValueSql("now()");

            entity.HasIndex(e => e.Correo).IsUnique().HasDatabaseName("usuarios_correo_key");
        });

        modelBuilder.Entity<Libro>(entity =>
        {
            entity.ToTable("libros");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.Titulo).HasColumnName("titulo").HasMaxLength(250).IsRequired();
            entity.Property(e => e.Anio).HasColumnName("anio").HasColumnType("smallint");
            entity.Property(e => e.Sinopsis).HasColumnName("sinopsis");
            entity.Property(e => e.Isbn).HasColumnName("isbn").HasMaxLength(20);
            entity.Property(e => e.Editorial).HasColumnName("editorial").HasMaxLength(150);
            entity.Property(e => e.UrlPortada).HasColumnName("url_portada");
            entity.Property(e => e.CreadoEn).HasColumnName("creado_en").HasColumnType("timestamptz").HasDefaultValueSql("now()");

            entity.HasIndex(e => e.Isbn).IsUnique().HasDatabaseName("libros_isbn_key");
        });

        modelBuilder.Entity<Autor>(entity =>
        {
            entity.ToTable("autores");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(150).IsRequired();

            entity.HasIndex(e => e.Nombre).IsUnique().HasDatabaseName("autores_nombre_key");
        });

        modelBuilder.Entity<Genero>(entity =>
        {
            entity.ToTable("generos");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.Nombre).HasColumnName("nombre").HasMaxLength(80).IsRequired();

            entity.HasIndex(e => e.Nombre).IsUnique().HasDatabaseName("generos_nombre_key");
        });

        modelBuilder.Entity<LibroAutor>(entity =>
        {
            entity.ToTable("libros_autores");
            entity.HasKey(e => new { e.LibroId, e.AutorId });

            entity.Property(e => e.LibroId).HasColumnName("libro_id");
            entity.Property(e => e.AutorId).HasColumnName("autor_id");

            entity.HasOne(e => e.Libro)
                .WithMany(l => l.LibrosAutores)
                .HasForeignKey(e => e.LibroId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(e => e.Autor)
                .WithMany(a => a.LibrosAutores)
                .HasForeignKey(e => e.AutorId)
                .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<LibroGenero>(entity =>
        {
            entity.ToTable("libros_generos");
            entity.HasKey(e => new { e.LibroId, e.GeneroId });

            entity.Property(e => e.LibroId).HasColumnName("libro_id");
            entity.Property(e => e.GeneroId).HasColumnName("genero_id");

            entity.HasOne(e => e.Libro)
                .WithMany(l => l.LibrosGeneros)
                .HasForeignKey(e => e.LibroId)
                .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(e => e.Genero)
                .WithMany(g => g.LibrosGeneros)
                .HasForeignKey(e => e.GeneroId)
                .OnDelete(DeleteBehavior.Restrict);
        });

        modelBuilder.Entity<Ejemplar>(entity =>
        {
            entity.ToTable("ejemplares");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.LibroId).HasColumnName("libro_id");
            entity.Property(e => e.CodigoBarras).HasColumnName("codigo_barras").HasMaxLength(40).IsRequired();
            entity.Property(e => e.Estado).HasColumnName("estado").HasMaxLength(20).IsRequired();
            entity.Property(e => e.CreadoEn).HasColumnName("creado_en").HasColumnType("timestamptz").HasDefaultValueSql("now()");

            entity.HasIndex(e => e.CodigoBarras).IsUnique().HasDatabaseName("ejemplares_codigo_barras_key");
            entity.HasIndex(e => e.LibroId).HasDatabaseName("ix_ejemplares_libro");

            entity.HasOne(e => e.Libro)
                .WithMany(l => l.Ejemplares)
                .HasForeignKey(e => e.LibroId)
                .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<Prestamo>(entity =>
        {
            entity.ToTable("prestamos");
            entity.HasKey(e => e.Id);

            entity.Property(e => e.Id).HasColumnName("id").UseIdentityAlwaysColumn();
            entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            entity.Property(e => e.EjemplarId).HasColumnName("ejemplar_id");
            entity.Property(e => e.FechaPrestamo).HasColumnName("fecha_prestamo").HasColumnType("timestamptz").HasDefaultValueSql("now()");
            entity.Property(e => e.FechaLimite).HasColumnName("fecha_limite").HasColumnType("date");
            entity.Property(e => e.FechaDevolucion).HasColumnName("fecha_devolucion").HasColumnType("timestamptz");

            entity.HasIndex(e => e.UsuarioId).HasDatabaseName("ix_prestamos_usuario");

            entity.HasOne(e => e.Usuario)
                .WithMany(u => u.Prestamos)
                .HasForeignKey(e => e.UsuarioId)
                .OnDelete(DeleteBehavior.Restrict);

            entity.HasOne(e => e.Ejemplar)
                .WithMany(ej => ej.Prestamos)
                .HasForeignKey(e => e.EjemplarId)
                .OnDelete(DeleteBehavior.Restrict);
        });

        // Vista de solo lectura: no tiene clave primaria.
        modelBuilder.Entity<VistaLibro>(entity =>
        {
            entity.HasNoKey();
            entity.ToView("vista_libros");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Titulo).HasColumnName("titulo");
            entity.Property(e => e.Anio).HasColumnName("anio");
            entity.Property(e => e.Sinopsis).HasColumnName("sinopsis");
            entity.Property(e => e.Isbn).HasColumnName("isbn");
            entity.Property(e => e.Editorial).HasColumnName("editorial");
            entity.Property(e => e.UrlPortada).HasColumnName("url_portada");
            entity.Property(e => e.Autores).HasColumnName("autores");
            entity.Property(e => e.Generos).HasColumnName("generos");
            entity.Property(e => e.EjemplaresTotal).HasColumnName("ejemplares_total");
            entity.Property(e => e.EjemplaresDisponibles).HasColumnName("ejemplares_disponibles");
        });
    }
}
