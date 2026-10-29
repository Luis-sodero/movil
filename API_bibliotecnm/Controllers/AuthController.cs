using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;
using API_bibliotecnm.DTOs;

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
}
