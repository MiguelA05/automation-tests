-- Script para insertar datos de prueba en la base de datos
-- Este script se ejecuta después de que el microservicio esté listo

-- Insertar usuario administrador (rol 0) si no existe
INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, rol)
VALUES ('admin', 'admin123', NULL, NULL, 'admin@example.com', 0)
ON CONFLICT (correo) DO NOTHING;

-- Insertar usuarios normales (rol 1) si no existen
INSERT INTO usuarios (usuario, clave, codigo_recuperacion, fecha_codigo, correo, rol) VALUES
('juan',   'juan123',   NULL, NULL, 'juan@example.com',   1),
('maria',  'maria123',  NULL, NULL, 'maria@example.com',  1),
('pedro',  'pedro123',  NULL, NULL, 'pedro@example.com',  1),
('laura',  'laura123',  NULL, NULL, 'laura@example.com',  1),
('carlos', 'carlos123', NULL, NULL, 'carlos@example.com', 1)
ON CONFLICT (correo) DO NOTHING;

-- Verificar que los datos se insertaron correctamente
SELECT 'Datos insertados correctamente' as status, COUNT(*) as total_usuarios FROM usuarios;

