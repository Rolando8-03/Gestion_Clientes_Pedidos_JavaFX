package edu.uam.gestion;

import edu.uam.gestion.config.Transacciones;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.service.ClienteService;
import edu.uam.gestion.service.PedidoService;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaIT {
    static SessionFactory factory;
    static Properties propiedades;
    ClienteService clientes;
    PedidoService pedidos;

    @BeforeAll
    static void preparar() throws Exception {
        String url = System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/gestion_uam_test");
        if (!url.matches("jdbc:postgresql://[^/]+/gestion_uam_test(?:[?].*)?")) {
            throw new IllegalArgumentException("TEST_DB_URL debe apuntar a gestion_uam_test; no se borrarán otras bases.");
        }
        String usuario = System.getenv().getOrDefault("TEST_DB_USER", "postgres");
        String clave = System.getenv().getOrDefault("TEST_DB_PASSWORD", "");
        propiedades = new Properties();
        propiedades.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        propiedades.setProperty("hibernate.connection.url", url);
        propiedades.setProperty("hibernate.connection.username", usuario);
        propiedades.setProperty("hibernate.connection.password", clave);
        propiedades.setProperty("hibernate.hbm2ddl.auto", "validate");
        propiedades.setProperty("hibernate.show_sql", "false");
        try (Connection conexion = DriverManager.getConnection(url, usuario, clave);
             var sentencia = conexion.createStatement()) {
            sentencia.execute("DROP TABLE IF EXISTS pedidos, clientes CASCADE");
            for (String sql : Files.readString(Path.of("sql/01_esquema.sql")).split(";")) {
                if (!sql.isBlank()) { sentencia.execute(sql); }
            }
        }
        factory = abrirFactory();
    }

    static SessionFactory abrirFactory() {
        return new Configuration().addProperties(propiedades).addAnnotatedClass(Cliente.class)
                .addAnnotatedClass(Pedido.class).buildSessionFactory();
    }

    @BeforeEach
    void limpiar() {
        new Transacciones(() -> factory).ejecutar(s -> {
            s.createNativeMutationQuery("TRUNCATE pedidos, clientes RESTART IDENTITY CASCADE").executeUpdate();
            return null;
        });
        clientes = new ClienteService(factory); pedidos = new PedidoService(factory);
    }

    @AfterAll
    static void cerrar() { if (factory != null) { factory.close(); } }

    @Test
    void crudClientePersisteEnSesionesDiferentes() {
        Cliente ana = clientes.crear(" Ana Pérez ", "ANA@EXAMPLE.COM");
        assertEquals("ana@example.com", clientes.listar().getFirst().getCorreo());
        clientes.actualizar(ana.getId(), "Ana María Pérez", "ana.maria@example.com");
        assertEquals("Ana María Pérez", clientes.buscar("MARIA@").getFirst().getNombre());
        clientes.eliminar(ana.getId());
        assertTrue(clientes.listar().isEmpty());
    }

    @Test
    void duplicadoNoDejaCambiosParcialesYPermiteSeguirTrabajando() {
        clientes.crear("Ana", "ana@example.com");
        Cliente luis = clientes.crear("Luis", "luis@example.com");
        assertThrows(IllegalArgumentException.class, () -> clientes.crear("Otra Ana", "ANA@EXAMPLE.COM"));
        assertThrows(IllegalArgumentException.class, () -> clientes.actualizar(luis.getId(), "No guardar", "ana@example.com"));
        assertEquals("Luis", clientes.buscar("luis@").getFirst().getNombre());
        clientes.actualizar(luis.getId(), "Luis Ortega", "luis.nuevo@example.com");
        assertEquals(2, clientes.listar().size());
    }

    @Test
    void errorRealDeEscrituraRevierteLaTransaccionCompleta() {
        Transacciones tx = new Transacciones(() -> factory);
        assertThrows(RuntimeException.class, () -> tx.ejecutar(s -> {
            s.persist(new Cliente("Temporal", "duplicado@example.com"));
            s.flush();
            s.persist(new Cliente("Duplicado", "duplicado@example.com"));
            s.flush();
            return null;
        }));
        assertTrue(clientes.listar().isEmpty(), "El primer INSERT también debe revertirse.");
        assertDoesNotThrow(() -> clientes.crear("Después del rollback", "valido@example.com"));
    }

    @Test
    void indiceNormalizadoProtegeInclusoSinPasarPorElServicio() {
        clientes.crear("Ana", "ana@example.com");
        assertThrows(RuntimeException.class, () -> new Transacciones(() -> factory).ejecutar(s -> {
            s.persist(new Cliente("Otra", "ANA@EXAMPLE.COM")); return null;
        }));
        assertEquals(1, clientes.listar().size());
    }

    @Test
    void crudPedidoConservaFechaYCambiaCliente() {
        Cliente ana = clientes.crear("Ana", "ana@example.com");
        Cliente maria = clientes.crear("María", "maria@example.com");
        Pedido pedido = pedidos.crear(ana.getId(), "Materiales", "1250.50");
        var fecha = pedido.getFecha();
        pedidos.actualizar(pedido.getId(), maria.getId(), "Materiales revisados", "780.00");
        Pedido actualizado = pedidos.listar().getFirst();
        assertEquals(maria.getId(), actualizado.getCliente().getId());
        assertEquals("780.00", actualizado.getTotal().toPlainString());
        assertEquals(fecha, actualizado.getFecha());
        pedidos.eliminar(pedido.getId());
        assertTrue(pedidos.listar().isEmpty());
        assertEquals(2, clientes.listar().size());
    }

    @Test
    void consultasPorClienteYDescripcionDevuelvenLaRelacionFueraDeSesion() {
        Cliente ana = clientes.crear("Ana", "ana@example.com");
        Cliente maria = clientes.crear("María", "maria@example.com");
        pedidos.crear(ana.getId(), "Materiales de redes", "1250.50");
        pedidos.crear(ana.getId(), "Accesorios", "780.00");
        pedidos.crear(maria.getId(), "Materiales varios", "425.75");
        assertEquals(2, pedidos.porCliente(ana.getId()).size());
        var resultado = pedidos.buscar(ana.getId(), "REDES");
        assertEquals(1, resultado.size());
        assertEquals("Ana", resultado.getFirst().getCliente().getNombre());
        assertTrue(pedidos.buscar(ana.getId(), "inexistente").isEmpty());
        assertTrue(pedidos.buscar(null, "' or 1=1 --").isEmpty());
    }

    @Test
    void busquedaTrataPorcentajeYGuionBajoComoTextoLiteral() {
        clientes.crear("Cliente 100%", "porcentaje@example.com");
        clientes.crear("Cliente normal", "normal@example.com");
        assertEquals(1, clientes.buscar("%").size());
        assertTrue(clientes.buscar("_").isEmpty());
    }

    @Test
    void clienteInexistenteYMontoInvalidoNoCreanPedidos() {
        Cliente ana = clientes.crear("Ana", "ana@example.com");
        assertThrows(IllegalArgumentException.class, () -> pedidos.crear(999L, "Inválido", "10"));
        assertThrows(IllegalArgumentException.class, () -> pedidos.crear(ana.getId(), "Inválido", "0"));
        assertThrows(IllegalArgumentException.class, () -> pedidos.crear(ana.getId(), "Inválido", "99999999999"));
        assertTrue(pedidos.listar().isEmpty());
    }

    @Test
    void claveForaneaRechazaHuerfanosEnPostgreSQL() {
        assertThrows(RuntimeException.class, () -> new Transacciones(() -> factory).ejecutar(s -> {
            s.createNativeMutationQuery("INSERT INTO pedidos(descripcion,total,fecha,cliente_id) VALUES ('Huérfano',10,CURRENT_DATE,999)")
                    .executeUpdate();
            return null;
        }));
        assertTrue(pedidos.listar().isEmpty());
    }

    @Test
    void eliminarClienteBorraSoloSusPedidos() {
        Cliente ana = clientes.crear("Ana", "ana@example.com");
        Cliente maria = clientes.crear("María", "maria@example.com");
        pedidos.crear(ana.getId(), "Uno", "10");
        pedidos.crear(ana.getId(), "Dos", "20");
        pedidos.crear(maria.getId(), "Otro", "30");
        clientes.eliminar(ana.getId());
        assertEquals(1, clientes.listar().size());
        assertEquals(1, pedidos.listar().size());
        assertEquals("María", pedidos.listar().getFirst().getCliente().getNombre());
    }

    @Test
    void losRegistrosSobrevivenAlCierreDeLaFactory() {
        Cliente ana = clientes.crear("Ana", "ana@example.com");
        pedidos.crear(ana.getId(), "Persistente", "25.50");
        factory.close(); factory = abrirFactory();
        var recuperados = new PedidoService(factory).listar();
        assertEquals(1, recuperados.size());
        assertEquals("Ana", recuperados.getFirst().getCliente().getNombre());
        assertEquals("25.50", recuperados.getFirst().getTotal().toPlainString());
    }
}
