using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;
using API_bibliotecnm.Models;

namespace API_bibliotecnm.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AuthController : ControllerBase
{
    private readonly ContextoBD _context;

    public AuthController(ContextoBD context)
    {
        _context = context;
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Correo) || string.IsNullOrWhiteSpace(request.Password))
        {
            return BadRequest(new MensajeDto { Mensaje = "Ingresa tu correo y contraseña." });
        }

        var correo = request.Correo.Trim().ToLowerInvariant();
        var usuario = await _context.Usuarios
            .AsNoTracking()
            .FirstOrDefaultAsync(u => u.Correo.ToLower() == correo);

        if (usuario == null || !PasswordHasher.Verify(request.Password, usuario.PasswordHash))
        {
            return Unauthorized(new MensajeDto { Mensaje = "Correo o contraseña incorrectos." });
        }

        if (!usuario.Activo)
        {
            return Unauthorized(new MensajeDto { Mensaje = "La cuenta está inactiva." });
        }

        return Ok(new LoginResponse
        {
            Id = usuario.Id,
            Nombre = usuario.Nombre,
            Correo = usuario.Correo,
            Rol = usuario.Rol
        });
    }

    [HttpPost("registro")]
    public async Task<ActionResult<LoginResponse>> Registro([FromBody] RegistroRequest request)
    {
        var nombre = request.Nombre?.Trim() ?? string.Empty;
        var correo = request.Correo?.Trim().ToLowerInvariant() ?? string.Empty;
        var password = request.Password ?? string.Empty;

        if (string.IsNullOrWhiteSpace(nombre) || string.IsNullOrWhiteSpace(correo) || password.Length < 6)
        {
            return BadRequest(new MensajeDto
            {
                Mensaje = "Ingresa tu nombre, un correo y una contraseña de al menos 6 caracteres."
            });
        }

        if (!correo.Contains('@') || !correo.Contains('.'))
        {
            return BadRequest(new MensajeDto { Mensaje = "Ingresa un correo electrónico válido." });
        }

        var existe = await _context.Usuarios
            .AsNoTracking()
            .AnyAsync(u => u.Correo.ToLower() == correo);

        if (existe)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "Ya existe una cuenta con ese correo.",
                Codigo = "CORREO_EXISTENTE"
            });
        }

        var usuario = new Usuario
        {
            Nombre = nombre,
            Correo = correo,
            PasswordHash = PasswordHasher.Hash(password),
            Rol = "lector",
            Activo = true
        };

        _context.Usuarios.Add(usuario);

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException)
        {
            return Conflict(new MensajeDto
            {
                Mensaje = "Ya existe una cuenta con ese correo.",
                Codigo = "CORREO_EXISTENTE"
            });
        }

        return Ok(new LoginResponse
        {
            Id = usuario.Id,
            Nombre = usuario.Nombre,
            Correo = usuario.Correo,
            Rol = usuario.Rol
        });
    }
}
