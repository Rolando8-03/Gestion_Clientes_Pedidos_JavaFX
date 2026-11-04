-- Consultas de lectura para contrastar las tablas con la aplicación y el PDF.
SELECT id, nombre, correo FROM clientes ORDER BY id;
SELECT p.id, p.cliente_id, c.nombre AS cliente, p.descripcion, p.total, p.fecha
FROM pedidos p JOIN clientes c ON c.id = p.cliente_id
ORDER BY p.fecha DESC, p.id DESC;

SELECT c.id, c.nombre, COUNT(p.id) AS cantidad_pedidos,
       COALESCE(SUM(p.total), 0) AS total_pedidos
FROM clientes c LEFT JOIN pedidos p ON p.cliente_id = c.id
GROUP BY c.id, c.nombre ORDER BY c.id;

SELECT COUNT(*) AS pedidos, COALESCE(SUM(total), 0) AS total_general FROM pedidos;
-- Con la carga inicial: 3 clientes, 3 pedidos y C$ 2456.25.

SELECT COUNT(*) AS pedidos_sin_cliente
FROM pedidos p LEFT JOIN clientes c ON c.id = p.cliente_id WHERE c.id IS NULL;
-- Debe devolver 0.

SELECT conname, pg_get_constraintdef(oid) AS definicion
FROM pg_constraint WHERE conrelid IN ('clientes'::regclass, 'pedidos'::regclass)
ORDER BY conname;

-- Prueba voluntaria de rollback. El cambio NO debe permanecer.
BEGIN;
UPDATE clientes SET nombre = 'CAMBIO TEMPORAL' WHERE correo = 'ana.perez@example.com';
SELECT id, nombre FROM clientes WHERE correo = 'ana.perez@example.com';
ROLLBACK;
SELECT id, nombre FROM clientes WHERE correo = 'ana.perez@example.com';
