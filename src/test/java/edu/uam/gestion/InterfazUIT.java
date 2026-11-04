package edu.uam.gestion;

import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.config.Transacciones;
import edu.uam.gestion.controller.PrincipalController;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.service.ClienteService;
import edu.uam.gestion.service.PedidoService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba opcional: necesita escritorio gráfico y una base exclusiva gestion_uam_test. */
class InterfazUIT {
    private Parent raiz;
    private Stage stage;
    private PrincipalController controller;
    private final Path evidencias = Path.of("target", "evidencias");

    @Test
    void flujoDeLaGuiaDesdeLosBotonesReales() throws Exception {
        String url = System.getenv("DB_URL");
        assertNotNull(url, "Configure DB_URL y TEST_DB_URL para una base de pruebas.");
        assertEquals(System.getenv("TEST_DB_URL"), url);
        PersistenciaIT.preparar(); PersistenciaIT.cerrar();
        new Transacciones(HibernateUtil::factory).ejecutar(s -> {
            s.createNativeMutationQuery("TRUNCATE pedidos, clientes RESTART IDENTITY CASCADE").executeUpdate();
            return null;
        });
        Files.createDirectories(evidencias);
        CountDownLatch inicio = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); inicio.countDown(); });
        assertTrue(inicio.await(20, TimeUnit.SECONDS));
        try {
            fx(() -> {
                FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/principal.fxml"));
                raiz = loader.load(); controller = loader.getController();
                Scene scene = new Scene(raiz, 1180, 780);
                scene.getStylesheets().add(App.class.getResource("/css/estilos.css").toExternalForm());
                stage = new Stage(); stage.setScene(scene); stage.show();
                return null;
            });
            esperarListo();
            crearCliente("Ana Pérez", "ana.perez@example.com");
            crearCliente("Luis Ortega", "luis.ortega@example.com");
            crearCliente("María Díaz", "maria.diaz@example.com");
            assertEquals(3, new ClienteService().listar().size());
            captura("01_clientes_registrados.png");

            fx(() -> {
                tablaClientes().getSelectionModel().select(1);
                campo("correoField").setText("luis.actualizado@example.com");
                boton("actualizarClienteBtn").fire(); return null;
            });
            esperarListo();
            assertEquals("luis.actualizado@example.com", new ClienteService().buscar("Luis").getFirst().getCorreo());
            captura("02_cliente_actualizado.png");

            fx(() -> {
                campo("nombreField").setText("Ana duplicada");
                campo("correoField").setText("ANA.PEREZ@EXAMPLE.COM");
                boton("guardarClienteBtn").fire(); return null;
            });
            esperarDialogo();
            capturarDialogo("03_correo_duplicado.png");
            cerrarDialogo(false);
            esperarListo();
            assertEquals(3, new ClienteService().listar().size());

            fx(() -> { tablaClientes().getSelectionModel().select(1); boton("eliminarClienteBtn").fire(); return null; }, false);
            esperarDialogo(); cerrarDialogo(true); esperarListo();
            assertEquals(2, new ClienteService().listar().size());
            captura("04_cliente_eliminado.png");

            fx(() -> { ((TabPane) raiz.lookup("#tabs")).getSelectionModel().select(1); return null; });
            crearPedido(0, "Materiales para práctica de redes", "1250.50");
            crearPedido(0, "Accesorios para laboratorio", "780.00");
            crearPedido(1, "Papelería para exposición", "425.75");
            assertEquals(3, new PedidoService().listar().size());
            captura("05_pedidos_asociados.png");

            fx(() -> { combo("filtroClienteCombo").getSelectionModel().select(0); boton("filtrarPedidosBtn").fire(); return null; });
            esperarListo();
            assertEquals(2, fx(() -> tablaPedidos().getItems().size()));
            captura("06_filtro_por_cliente.png");

            fx(() -> { campo("buscarPedidoField").setText("sin coincidencia"); boton("filtrarPedidosBtn").fire(); return null; });
            esperarListo();
            assertEquals(0, fx(() -> tablaPedidos().getItems().size()));
            captura("07_filtro_sin_resultados.png");
            fx(() -> { boton("quitarFiltroBtn").fire(); return null; });
            esperarListo();
            fx(() -> { boton("reporteBtn").fire(); return null; });
            esperarDialogo(); capturarDialogo("08_reporte_generado.png"); cerrarDialogo(false);
            esperarListo();
            Files.copy(Path.of("reportes", "pedidos.pdf"), evidencias.resolve("pedidos_ejemplo.pdf"),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            assertTrue(Files.size(evidencias.resolve("pedidos_ejemplo.pdf")) > 1000);
        } finally {
            fx(() -> { if (controller != null) { controller.cerrar(); } if (stage != null) { stage.close(); } return null; });
            Platform.exit();
            HibernateUtil.cerrar();
        }
    }

    private void crearCliente(String nombre, String correo) throws Exception {
        fx(() -> {
            campo("nombreField").setText(nombre); campo("correoField").setText(correo);
            boton("guardarClienteBtn").fire(); return null;
        });
        esperarListo();
    }

    private void crearPedido(int indice, String descripcion, String monto) throws Exception {
        fx(() -> {
            combo("clienteCombo").getSelectionModel().select(indice);
            campo("descripcionField").setText(descripcion); campo("montoField").setText(monto);
            boton("crearPedidoBtn").fire(); return null;
        });
        esperarListo();
    }

    private void esperarListo() throws Exception {
        esperar(() -> {
            try { return fx(() -> !raiz.lookup("#contenidoBox").isDisabled()); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        // Deja que termine el callback que puede encadenar una recarga.
        fx(() -> null);
    }

    private void esperarDialogo() throws Exception {
        esperar(() -> {
            try { return fx(() -> Window.getWindows().stream().anyMatch(w -> w != stage && w.isShowing())); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    private void cerrarDialogo(boolean aceptar) throws Exception {
        fx(() -> {
            Window ventana = Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).findFirst().orElseThrow();
            DialogPane pane = (DialogPane) ventana.getScene().getRoot();
            ButtonType tipo = aceptar ? pane.getButtonTypes().stream()
                    .filter(t -> t.getButtonData() == ButtonBar.ButtonData.OK_DONE).findFirst().orElseThrow()
                    : pane.getButtonTypes().getLast();
            ((Button) pane.lookupButton(tipo)).fire();
            return null;
        });
    }

    private void captura(String nombre) throws Exception {
        fx(() -> { guardarImagen(raiz, nombre); return null; });
    }

    private void capturarDialogo(String nombre) throws Exception {
        fx(() -> {
            Window ventana = Window.getWindows().stream().filter(w -> w != stage && w.isShowing()).findFirst().orElseThrow();
            guardarImagen(ventana.getScene().getRoot(), nombre); return null;
        });
    }

    private void guardarImagen(Node nodo, String nombre) throws Exception {
        var imagen = nodo.snapshot(null, null);
        BufferedImage salida = new BufferedImage((int) imagen.getWidth(), (int) imagen.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var pixeles = imagen.getPixelReader();
        for (int y = 0; y < salida.getHeight(); y++) {
            for (int x = 0; x < salida.getWidth(); x++) { salida.setRGB(x, y, pixeles.getArgb(x, y)); }
        }
        ImageIO.write(salida, "png", evidencias.resolve(nombre).toFile());
    }

    private void esperar(BooleanSupplier condicion) throws Exception {
        long limite = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        while (!condicion.getAsBoolean()) {
            if (System.nanoTime() > limite) { fail("Se agotó el tiempo de espera de JavaFX."); }
            Thread.sleep(50);
        }
    }

    private TextField campo(String id) { return (TextField) raiz.lookup("#" + id); }
    private Button boton(String id) { return (Button) raiz.lookup("#" + id); }
    @SuppressWarnings("unchecked")
    private ComboBox<Cliente> combo(String id) { return (ComboBox<Cliente>) raiz.lookup("#" + id); }
    @SuppressWarnings("unchecked")
    private TableView<Cliente> tablaClientes() { return (TableView<Cliente>) raiz.lookup("#clientesTabla"); }
    @SuppressWarnings("unchecked")
    private TableView<Pedido> tablaPedidos() { return (TableView<Pedido>) raiz.lookup("#pedidosTabla"); }

    private <T> T fx(Callable<T> accion) throws Exception { return fx(accion, true); }

    private <T> T fx(Callable<T> accion, boolean esperar) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return esperar ? tarea.get(30, TimeUnit.SECONDS) : null;
    }
}
