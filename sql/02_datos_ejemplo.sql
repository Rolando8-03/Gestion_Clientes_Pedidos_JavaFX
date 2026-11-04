-- Datos ficticios de la guía. Ejecutar después de 01_esquema.sql.
-- Se puede repetir: no vuelve a insertar los mismos clientes o pedidos.
BEGIN;
INSERT INTO clientes (nombre, correo) VALUES
    ('Ana Pérez', 'ana.perez@example.com'),
    ('Luis Ortega', 'luis.ortega@example.com'),
    ('María Díaz', 'maria.diaz@example.com')
ON CONFLICT DO NOTHING;

INSERT INTO pedidos (descripcion, total, fecha, cliente_id)
SELECT d.descripcion, d.total, DATE '2026-11-04', c.id
FROM (VALUES
    ('ana.perez@example.com', 'Materiales para práctica de redes', 1250.50::NUMERIC),
    ('ana.perez@example.com', 'Accesorios para laboratorio', 780.00::NUMERIC),
    ('maria.diaz@example.com', 'Papelería para exposición', 425.75::NUMERIC)
) AS d(correo, descripcion, total)
JOIN clientes c ON lower(c.correo) = d.correo
WHERE NOT EXISTS (
    SELECT 1 FROM pedidos p
    WHERE p.cliente_id = c.id AND p.descripcion = d.descripcion
);
COMMIT;
