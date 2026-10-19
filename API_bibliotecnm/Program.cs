using System.Text.Json.Serialization;
using Microsoft.EntityFrameworkCore;
using API_bibliotecnm.Data;

var builder = WebApplication.CreateBuilder(args);

// Conexion a PostgreSQL (base de datos "bibliotecnm")
builder.Services.AddDbContext<ContextoBD>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("ConexionPostgres")));

// Controllers + manejo de ciclos en el JSON
builder.Services.AddControllers()
    .AddJsonOptions(o =>
        o.JsonSerializerOptions.ReferenceHandler = ReferenceHandler.IgnoreCycles);

// CORS para consumir la API desde el front (Live Server)
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.WithOrigins("http://127.0.0.1:5500", "http://localhost:5500")
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();

    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseCors();

app.UseAuthorization();

app.MapControllers();

app.Run();
