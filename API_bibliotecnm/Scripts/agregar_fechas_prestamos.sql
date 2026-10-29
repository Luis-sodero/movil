-- Script para agregar columnas fecha_solicitud y fecha_recepcion a prestamos
-- Ajustar tipos si la base usa timestamp without time zone. Verificar tipos existentes.
ALTER TABLE prestamos ADD COLUMN IF NOT EXISTS fecha_solicitud TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE prestamos ADD COLUMN IF NOT EXISTS fecha_recepcion TIMESTAMPTZ NOT NULL DEFAULT now();
