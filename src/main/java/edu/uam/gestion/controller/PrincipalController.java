package edu.uam.gestion.controller;

import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.report.ReporteService;
import edu.uam.gestion.service.ClienteService;
import edu.uam.gestion.service.PedidoService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PrincipalController {
    private static final Logger LOG = LoggerFactory.getLogger(PrincipalController.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField nombreField, correoField, descripcionField, montoField;
    @FXML private TextField buscarClienteField, buscarPedidoField;
    @FXML private TableView<Cliente> clientesTabla;
    @FXML private TableColumn<Cliente, Long> idClienteCol;
    @FXML private TableColumn<Cliente, String> nombreCol, correoCol;
    @FXML private ComboBox<Cliente> clienteCombo, filtroClienteCombo;
    @FXML private TableView<Pedido> pedidosTabla;
    @FXML private TableColumn<Pedido, Long> idPedidoCol;
    @FXML private TableColumn<Pedido, String> pedidoClienteCol, descripcionCol, totalCol, fechaCol;
    @FXML private Label estadoLabel, clientesResumenLabel, pedidosResumenLabel;
    @FXML private Label clienteEdicionLabel, pedidoEdicionLabel;
    @FXML private VBox contenidoBox;
    @FXML private ProgressIndicator progreso;
    @FXML private Button actualizarClienteBtn, eliminarClienteBtn, actualizarPedidoBtn, eliminarPedidoBtn;

    private final ClienteService clientes = new ClienteService();
    private final PedidoService pedidos = new PedidoService();
    private final ReporteService reportes = new ReporteService();
    private final SimpleBooleanProperty ocupado = new SimpleBooleanProperty(false);
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "operaciones-bd");
        hilo.setDaemon(true);
        return hilo;
    });
    private final NumberFormat dinero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-NI"));
    private Long clienteEditado;
    private Long pedidoEditado;

    @FXML
    public void initialize() {
        dinero.setMinimumFractionDigits(2);
        dinero.setMaximumFractionDigits(2);
        contenidoBox.disableProperty().bind(ocupado);
        progreso.visibleProperty().bind(ocupado);
        progreso.managedProperty().bind(ocupado);
        actualizarClienteBtn.disableProperty().bind(clientesTabla.getSelectionModel().selectedItemProperty().isNull());
        eliminarClienteBtn.disableProperty().bind(actualizarClienteBtn.disableProperty());
        actualizarPedidoBtn.disableProperty().bind(pedidosTabla.getSelectionModel().selectedItemProperty().isNull());
        eliminarPedidoBtn.disableProperty().bind(actualizarPedidoBtn.disableProperty());
        idClienteCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        nombreCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNombre()));
        correoCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCorreo()));
        idPedidoCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        pedidoClienteCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getCliente().getNombre()));
        descripcionCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDescripcion()));
        totalCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(dinero.format(c.getValue().getTotal())));
        fechaCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(FECHA.format(c.getValue().getFecha())));
        configurarCombo(clienteCombo, "Seleccione un cliente");
        configurarCombo(filtroClienteCombo, "Todos los clientes");
        clientesTabla.getSelectionModel().selectedItemProperty().addListener((obs, previo, actual) -> {
            if (actual != null) {
                clienteEditado = actual.getId();
                nombreField.setText(actual.getNombre());
                correoField.setText(actual.getCorreo());
                clienteEdicionLabel.setText("Editando cliente #" + clienteEditado);
            }
        });
        pedidosTabla.getSelectionModel().selectedItemProperty().addListener((obs, previo, actual) -> {
            if (actual != null) {
                pedidoEditado = actual.getId();
                descripcionField.setText(actual.getDescripcion());
                montoField.setText(actual.getTotal().toPlainString());
                seleccionar(clienteCombo, actual.getCliente().getId());
                pedidoEdicionLabel.setText("Editando pedido #" + pedidoEditado);
            }
        });
        actualizarTablas();
    }

    private void configurarCombo(ComboBox<Cliente> combo, String vacio) {
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente cliente) { return cliente == null ? vacio : cliente.getNombre(); }
            @Override public Cliente fromString(String texto) { return null; }
        });
        combo.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty ? null : cliente == null ? vacio : cliente.getNombre());
            }
        });
    }

    @FXML
    private void guardarCliente() {
        String nombre = nombreField.getText(), correo = correoField.getText();
        ejecutar("Guardando cliente...", () -> clientes.crear(nombre, correo), c -> {
            limpiarCliente();
            recargar("Cliente #" + c.getId() + " registrado.");
        });
    }

    @FXML
    private void editarCliente() {
        Long id = clienteEditado;
        String nombre = nombreField.getText(), correo = correoField.getText();
        ejecutar("Actualizando cliente...", () -> {
            clientes.actualizar(id, nombre, correo);
            return null;
        }, nada -> { limpiarCliente(); recargar("Cliente actualizado."); });
    }

    @FXML
    private void eliminarCliente() {
        if (clienteEditado == null || !confirmar("Eliminar cliente",
                "Se eliminará el cliente seleccionado y todos sus pedidos asociados. ¿Desea continuar?")) {
            return;
        }
        Long id = clienteEditado;
        ejecutar("Eliminando cliente...", () -> { clientes.eliminar(id); return null; }, nada -> {
            limpiarCliente(); limpiarPedido(); recargar("Cliente y pedidos asociados eliminados.");
        });
    }

    @FXML
    private void crearPedido() { guardarPedido(false); }

    @FXML
    private void editarPedido() { guardarPedido(true); }

    private void guardarPedido(boolean editar) {
        Long id = pedidoEditado;
        Long clienteId = idSeleccionado(clienteCombo);
        String descripcion = descripcionField.getText(), monto = montoField.getText();
        ejecutar(editar ? "Actualizando pedido..." : "Registrando pedido...", () -> {
            if (editar) {
                pedidos.actualizar(id, clienteId, descripcion, monto);
            } else {
                pedidos.crear(clienteId, descripcion, monto);
            }
            return null;
        }, nada -> { limpiarPedido(); recargar(editar ? "Pedido actualizado." : "Pedido registrado."); });
    }

    @FXML
    private void eliminarPedido() {
        if (pedidoEditado == null || !confirmar("Eliminar pedido", "¿Desea eliminar el pedido seleccionado?")) { return; }
        Long id = pedidoEditado;
        ejecutar("Eliminando pedido...", () -> { pedidos.eliminar(id); return null; }, nada -> {
            limpiarPedido(); recargar("Pedido eliminado.");
        });
    }

    @FXML
    private void buscarClientes() {
        String texto = buscarClienteField.getText();
        ejecutar("Buscando clientes...", () -> clientes.buscar(texto), lista -> {
            limpiarCliente();
            clientesTabla.getItems().setAll(lista);
            clientesResumenLabel.setText(lista.size() + " cliente(s)");
            estadoLabel.setText(lista.isEmpty() ? "No se encontraron clientes." : "Búsqueda completada.");
        });
    }

    @FXML
    private void filtrarPedidos() {
        Long clienteId = idSeleccionado(filtroClienteCombo);
        String texto = buscarPedidoField.getText();
        ejecutar("Consultando pedidos...", () -> {
            if (clienteId != null && (texto == null || texto.isBlank())) {
                return pedidos.porCliente(clienteId);
            }
            return pedidos.buscar(clienteId, texto);
        }, lista -> {
            limpiarPedido();
            mostrarPedidos(lista);
            estadoLabel.setText(lista.isEmpty() ? "No hay pedidos para este filtro." : "Filtro aplicado.");
        });
    }

    @FXML
    private void quitarFiltroPedidos() {
        filtroClienteCombo.getSelectionModel().clearSelection();
        filtroClienteCombo.setValue(null);
        buscarPedidoField.clear();
        filtrarPedidos();
    }

    @FXML
    private void quitarFiltroClientes() {
        buscarClienteField.clear();
        buscarClientes();
    }

    @FXML
    private void actualizarTablas() { recargar("Datos actualizados."); }

    private void recargar(String mensaje) {
        String texto = buscarClienteField.getText(), descripcion = buscarPedidoField.getText();
        Long filtro = idSeleccionado(filtroClienteCombo), seleccionado = idSeleccionado(clienteCombo);
        ejecutar("Cargando información...", () -> new Datos(clientes.listar(), clientes.buscar(texto),
                pedidos.buscar(filtro, descripcion)), datos -> {
            limpiarCliente();
            limpiarPedido();
            clientesTabla.getItems().setAll(datos.visibles());
            clienteCombo.getItems().setAll(datos.todos());
            filtroClienteCombo.getItems().setAll(datos.todos());
            seleccionar(clienteCombo, seleccionado);
            seleccionar(filtroClienteCombo, filtro);
            clientesResumenLabel.setText(datos.visibles().size() + " cliente(s)");
            mostrarPedidos(datos.pedidos());
            estadoLabel.setText(mensaje);
        });
    }

    private void mostrarPedidos(List<Pedido> lista) {
        pedidosTabla.getItems().setAll(lista);
        BigDecimal total = lista.stream().map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        pedidosResumenLabel.setText(lista.size() + " pedido(s)   |   Total C$ " + dinero.format(total));
    }

    @FXML
    private void generarReporte() {
        // Se exporta exactamente el listado visible, incluido el filtro aplicado.
        List<Pedido> filas = List.copyOf(pedidosTabla.getItems());
        String alcance = filtroClienteCombo.getValue() == null ? "Todos los clientes"
                : "Cliente: " + filtroClienteCombo.getValue().getNombre();
        if (!buscarPedidoField.getText().isBlank()) {
            alcance += " | Descripción: " + buscarPedidoField.getText().strip();
        }
        String filtro = alcance;
        ejecutar("Generando reporte PDF...", () -> reportes.exportar(filas, Path.of("reportes", "pedidos.pdf"), filtro), ruta -> {
            estadoLabel.setText("Reporte generado: " + ruta);
            alerta(Alert.AlertType.INFORMATION, "Reporte generado", "El PDF se guardó en:\n" + ruta);
        });
    }

    @FXML
    private void limpiarCliente() {
        clientesTabla.getSelectionModel().clearSelection();
        clienteEditado = null;
        nombreField.clear(); correoField.clear();
        clienteEdicionLabel.setText("Nuevo cliente");
    }

    @FXML
    private void limpiarPedido() {
        pedidosTabla.getSelectionModel().clearSelection();
        pedidoEditado = null;
        descripcionField.clear(); montoField.clear();
        clienteCombo.getSelectionModel().clearSelection();
        clienteCombo.setValue(null);
        pedidoEdicionLabel.setText("Nuevo pedido");
    }

    private static Long idSeleccionado(ComboBox<Cliente> combo) {
        return combo.getValue() == null ? null : combo.getValue().getId();
    }

    private static void seleccionar(ComboBox<Cliente> combo, Long id) {
        combo.setValue(combo.getItems().stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null));
    }

    private <T> void ejecutar(String mensaje, Callable<T> operacion, Consumer<T> alTerminar) {
        if (ocupado.get()) { return; }
        ocupado.set(true);
        estadoLabel.setText(mensaje);
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return operacion.call(); }
        };
        tarea.setOnSucceeded(evento -> {
            ocupado.set(false);
            alTerminar.accept(tarea.getValue());
        });
        tarea.setOnFailed(evento -> {
            ocupado.set(false);
            Throwable error = tarea.getException();
            String detalle;
            if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
                detalle = error.getMessage();
            } else {
                detalle = "No se pudo completar la operación. Revise la conexión a PostgreSQL e intente de nuevo.";
            }
            // No se escribe la configuración ni el mensaje JDBC que pudiera contener datos sensibles.
            LOG.warn("Operación fallida: {}", error.getClass().getSimpleName());
            estadoLabel.setText(detalle);
            alerta(Alert.AlertType.ERROR, "No se pudo completar", detalle);
        });
        ejecutor.submit(tarea);
    }

    private boolean confirmar(String titulo, String mensaje) {
        Alert alerta = nuevaAlerta(Alert.AlertType.CONFIRMATION, titulo, mensaje);
        ButtonType eliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        alerta.getButtonTypes().setAll(eliminar, ButtonType.CANCEL);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == eliminar;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        nuevaAlerta(tipo, titulo, mensaje).showAndWait();
    }

    private Alert nuevaAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        if (estadoLabel.getScene() != null) {
            alerta.initOwner(estadoLabel.getScene().getWindow());
        }
        return alerta;
    }

    public void cerrar() {
        ejecutor.submit(HibernateUtil::cerrar);
        ejecutor.shutdown();
    }

    private record Datos(List<Cliente> todos, List<Cliente> visibles, List<Pedido> pedidos) { }
}
