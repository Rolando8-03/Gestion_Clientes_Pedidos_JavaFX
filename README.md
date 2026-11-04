# Práctica integrada JavaFX, Hibernate ORM y JasperReports

**Asignatura:** Programación de Aplicaciones de Escritorio — UAM. Sesiones 34, 35 y 36.

## Requisitos
- JDK 21, Maven, IntelliJ IDEA, PostgreSQL (servicio en ejecución).
- Conexión a internet para la primera descarga de dependencias Maven.

## Instrucciones paso a paso
1. Extraiga ZIP y abra `pom.xml` como proyecto Maven en IntelliJ IDEA.
2. Cree la base de datos vacía `gestion_uam` en PostgreSQL (por ejemplo, `CREATE DATABASE gestion_uam;`).
3. Configure usuario, contraseña y URL en `src/main/resources/hibernate.properties`.
4. Haga Reload All Maven Projects (Maven en IntelliJ).
5. Ejecute desde la terminal `mvn clean javafx:run`, en la carpeta con `pom.xml`.
6. En **Clientes**, cree tres clientes, edite uno y elimine otro (la eliminación también elimina pedidos asociados).
7. En **Pedidos**, registre pedidos para clientes diferentes. Observe la relación uno a muchos.
8. Pulse **Generar reporte PDF**, el documento aparecerá en `reportes/pedidos.pdf`.
9. Abra `src/main/resources/reports/pedidos.jrxml` en Jaspersoft Studio compatible con JasperReports 6.x para personalizar el diseño.

## Estructura y responsabilidades
- `model`: entidades JPA Cliente y Pedido (encapsulación, asociaciones).
- `repository`: persistencia y consultas HQL, sin reglas de interfaz.
- `service`: reglas, validaciones y transacciones con rollback.
- `controller`: eventos JavaFX y actualización de TableView.
- `report`: transformación de pedidos a DTO (FilaPedido), compilación de JRXML y PDF.
- `config`: inicialización única de SessionFactory.

## Actividades por sesión
- **34:** CRUD, revisar `@Entity`, `@Id`, `persist`, `remove` y dirty checking.
- **35:** relaciones `@OneToMany` / `@ManyToOne`, `join fetch`, añadir búsqueda parametrizada por cliente.
- **36:** editar JRXML en Studio, exportar PDF y añadir fecha/totales al reporte.

## Retos de código limpio
- Añada buscar por correo sin concatenar SQL/HQL.
- Evite consultas y acceso a Hibernate dentro de controladores.
- Valide datos en `service` y muestre errores amigables.
- Reto avanzado: mover operaciones costosas a `Task` de JavaFX para no bloquear la interfaz.

## Nota sobre seguridad
`hibernate.hbm2ddl.auto=update` es solo para laboratorio. No coloque contraseñas reales en repositorios públicos. En producción use variables de entorno, migraciones de esquema y un pool de conexiones.

## Referencias
- https://openjfx.io/openjfx-docs/
- https://hibernate.org/orm/documentation/6.6/
- https://community.jaspersoft.com/documentation/
- https://jdbc.postgresql.org/documentation/
