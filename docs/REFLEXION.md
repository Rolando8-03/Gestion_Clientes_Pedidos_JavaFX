# Decisiones de diseño

La aplicación separa la interfaz, las reglas y la persistencia para que cada clase tenga una función clara. El controlador recoge los datos y muestra los resultados; los servicios comprueban las entradas y los repositorios trabajan con Hibernate. Esto permite probar las operaciones sin tener que abrir la ventana en todos los casos.

Cliente y Pedido representan la relación uno a muchos. Pedido contiene la clave foránea y es el propietario de la asociación; `mappedBy = "cliente"` indica el lado inverso en Cliente. La colección no se expone como una lista modificable desde cualquier clase y, al agregar un pedido, se mantiene la referencia al cliente. Para cambiar el cliente de un pedido existente, el servicio actualiza el lado propietario dentro de una sesión nueva y luego recarga la vista.

Se mantuvo la eliminación en cascada del proyecto de referencia. La ventana pide confirmación porque borrar un cliente también elimina sus pedidos. Esta regla es adecuada para la práctica; en un sistema que deba conservar un historial de ventas convendría impedir esa eliminación o usar una baja lógica.

Cada operación abre su propia sesión y transacción. `persist()` registra objetos nuevos, `find()` recupera una entidad y `remove()` programa su eliminación. Al actualizar, la entidad recuperada está administrada y Hibernate detecta los cambios sin llamar a `merge()`. `commit()` confirma la operación y `rollback()` revierte sus cambios si ocurre un error. `merge()` sería útil para copiar el estado de un objeto separado a una instancia administrada, pero no es necesario en este flujo.

Las consultas reciben parámetros para no mezclar entradas del formulario con la estructura del HQL. Los pedidos se recuperan mediante `join fetch` junto con su cliente, de modo que la tabla y el reporte pueden leer el nombre después de cerrar la sesión. Se conserva la carga diferida en el mapeo para no traer colecciones completas en todas las consultas.

Los montos usan `BigDecimal` y `NUMERIC(12,2)` para conservar los centavos. Los correos se normalizan y se revisan en el servicio; el índice único de PostgreSQL respalda esa regla frente a escrituras simultáneas. La validación del formulario no sustituye la integridad de la base de datos.

JasperReports recibe una colección de DTO, sin abrir otra conexión JDBC. El PDF refleja el listado consultado y suma sus importes. La fuente se incluye como dependencia para conservar los acentos y la presentación del reporte. Las consultas y la exportación se ejecutan en tareas de JavaFX con una sesión independiente por operación.
