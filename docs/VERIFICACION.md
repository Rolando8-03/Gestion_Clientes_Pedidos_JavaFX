# Comprobación de la práctica

**Fecha:** 4 de noviembre de 2026.

Se comprobó el caso de clientes y pedidos de la guía integradora. Las capturas provienen del formulario FXML ejecutado con JavaFX; el PDF se exportó desde el botón de la aplicación. Los datos son ficticios y se trabajó con una base exclusiva de pruebas.

## Resultados automatizados

| Ejecución | Casos | Resultado |
| --- | ---: | --- |
| Validaciones de datos | 21 | Sin fallos. |
| Prioridad de configuración local y entorno | 2 | Sin fallos. |
| PDF: acentos, importes, lista vacía y varias páginas | 3 | Sin fallos. |
| Persistencia con PostgreSQL | 11 | Sin fallos. |
| Recorrido integrado de la interfaz JavaFX | 1 | Sin fallos. |
| **Total de casos distintos** | **38** | **Todos aprobados.** |

Se ejecutaron `mvn -Ppostgres clean verify` y `mvn -Pinterfaz verify` con Java 21, PostgreSQL 16.6 y las versiones fijadas en `pom.xml`. El segundo comando vuelve a ejecutar las 26 pruebas unitarias antes del recorrido de interfaz; no se cuentan dos veces en la tabla.

Las pruebas de PostgreSQL comprobaron: CRUD del cliente; duplicados sin cambios parciales; rollback después de un error real de escritura; correo único sin distinguir mayúsculas; CRUD del pedido con cambio de cliente; filtros parametrizados; búsqueda literal de `%` y `_`; rechazo de datos inválidos; rechazo de pedidos huérfanos; eliminación en cascada y recuperación después de cerrar y abrir la SessionFactory.

## Evidencias de la interfaz

| Archivo | Comprobación |
| --- | --- |
| [01_clientes_registrados.png](evidencias/01_clientes_registrados.png) | Registro de Ana Pérez, Luis Ortega y María Díaz; tres filas recuperadas desde PostgreSQL. |
| [02_cliente_actualizado.png](evidencias/02_cliente_actualizado.png) | Correo de Luis actualizado a `luis.actualizado@example.com`. |
| [03_correo_duplicado.png](evidencias/03_correo_duplicado.png) | Rechazo de un segundo registro con el correo de Ana escrito en mayúsculas. |
| [04_cliente_eliminado.png](evidencias/04_cliente_eliminado.png) | Eliminación confirmada de Luis, que no tenía pedidos. Permanecen Ana y María. |
| [05_pedidos_asociados.png](evidencias/05_pedidos_asociados.png) | Dos pedidos de Ana y uno de María; total C$ 2,456.25. |
| [06_filtro_por_cliente.png](evidencias/06_filtro_por_cliente.png) | Consulta de Ana: dos pedidos por C$ 2,030.50. |
| [07_filtro_sin_resultados.png](evidencias/07_filtro_sin_resultados.png) | Búsqueda combinada sin coincidencias; tabla vacía y mensaje. |
| [08_reporte_generado.png](evidencias/08_reporte_generado.png) | Confirmación de exportación del PDF desde la ventana. |
| [09_verificacion_postgresql.txt](evidencias/09_verificacion_postgresql.txt) | Salida real de las consultas SQL: asociación, importes, restricciones y rollback. |
| [10_resultado_pruebas.txt](evidencias/10_resultado_pruebas.txt) | Resumen conservado de las ejecuciones Maven. |
| [pedidos_ejemplo.pdf](evidencias/pedidos_ejemplo.pdf) | Documento generado por JasperReports con los tres pedidos. |

En la salida SQL final hay dos clientes porque Luis se eliminó durante la demostración. La carga opcional `02_datos_ejemplo.sql` crea los tres clientes de la guía. No son estados de captura contradictorios: corresponden a momentos distintos del recorrido.

## Repetir la demostración en clase

1. Preparar una base vacía con `01_esquema.sql` y abrir la aplicación. Para repetir manualmente los registros, no cargar antes los ejemplos.
2. Registrar los tres clientes, actualizar el correo de Luis y comprobar su fila.
3. Intentar duplicar el correo de Ana. Comprobar que no aumenta el número de clientes.
4. Eliminar a Luis y verificar que Ana y María permanecen.
5. Registrar dos pedidos para Ana y uno para María. Consultar `cliente_id` en PostgreSQL.
6. Aplicar el filtro de Ana, combinarlo con una descripción y probar una búsqueda sin coincidencias.
7. Volver a mostrar todos los pedidos y generar el PDF. Comparar su total con la tabla y con `SUM(total)`.
8. Cerrar y volver a abrir la aplicación. Comprobar que los registros siguen disponibles.

## Alcance

El PDF de ejemplo y las capturas son evidencias de una ejecución de prueba. La conexión local del estudiante se configura al instalar el proyecto. Se utiliza `hbm2ddl.auto=update` y el pool básico de Hibernate para el laboratorio; no se incluye una configuración de despliegue de producción.
