package edu.uam.gestion.controller;
import edu.uam.gestion.model.Cliente;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.report.ReporteService;
import edu.uam.gestion.service.ClienteService;
import edu.uam.gestion.service.PedidoService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
public class PrincipalController {
 @FXML private TextField nombreField,correoField,descripcionField,montoField;
 @FXML private TableView<Cliente> clientesTabla;
 @FXML private TableColumn<Cliente,Long> idClienteCol;
 @FXML private TableColumn<Cliente,String> nombreCol,correoCol;
 @FXML private ComboBox<Cliente> clienteCombo;
 @FXML private TableView<Pedido> pedidosTabla;
 @FXML private TableColumn<Pedido,Long> idPedidoCol;
 @FXML private TableColumn<Pedido,String> pedidoClienteCol,descripcionCol,totalCol,fechaCol;
 @FXML private Label estadoLabel;
 private final ClienteService clientes=new ClienteService();private final PedidoService pedidos=new PedidoService();
 private final ReporteService reportes=new ReporteService();
 @FXML public void initialize(){
  idClienteCol.setCellValueFactory(c->new ReadOnlyObjectWrapper<>(c.getValue().getId()));
  nombreCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getNombre()));
  correoCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getCorreo()));
  idPedidoCol.setCellValueFactory(c->new ReadOnlyObjectWrapper<>(c.getValue().getId()));
  pedidoClienteCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getCliente().getNombre()));
  descripcionCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getDescripcion()));
  totalCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getTotal().toPlainString()));
  fechaCol.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getFecha().toString()));
  clientesTabla.getSelectionModel().selectedItemProperty().addListener((obs,anterior,actual)->{
   if(actual!=null){nombreField.setText(actual.getNombre());correoField.setText(actual.getCorreo());}
  });
  ejecutar(this::refrescar);
 }
 @FXML private void guardarCliente(){ejecutar(()->{clientes.crear(nombreField.getText(),correoField.getText());limpiar();refrescar();mensaje("Cliente creado");});}
 @FXML private void editarCliente(){ejecutar(()->{
  Cliente seleccionado=clientesTabla.getSelectionModel().getSelectedItem();
  if(seleccionado==null)throw new IllegalArgumentException("Seleccione un cliente");
  clientes.actualizar(seleccionado.getId(),nombreField.getText(),correoField.getText());limpiar();refrescar();mensaje("Cliente actualizado");
 });}
 @FXML private void eliminarCliente(){ejecutar(()->{
  Cliente seleccionado=clientesTabla.getSelectionModel().getSelectedItem();
  if(seleccionado==null)throw new IllegalArgumentException("Seleccione un cliente");
  var confirmacion=new Alert(Alert.AlertType.CONFIRMATION,"Se eliminará el cliente y sus pedidos. ¿Continuar?",ButtonType.YES,ButtonType.NO);
  if(confirmacion.showAndWait().orElse(ButtonType.NO)!=ButtonType.YES)return;
  clientes.eliminar(seleccionado.getId());limpiar();refrescar();mensaje("Cliente eliminado");
 });}
 @FXML private void crearPedido(){ejecutar(()->{
  Cliente elegido=clienteCombo.getValue();
  pedidos.crear(elegido==null?null:elegido.getId(),descripcionField.getText(),montoField.getText());
  descripcionField.clear();montoField.clear();refrescar();mensaje("Pedido registrado");
 });}
 @FXML private void generarReporte(){ejecutar(()->{
  var ruta=reportes.exportar(pedidos.listar());mensaje("PDF generado: "+ruta);
  new Alert(Alert.AlertType.INFORMATION,"Reporte guardado en:
"+ruta).showAndWait();
 });}
 @FXML private void actualizarTablas(){ejecutar(this::refrescar);}
 private void refrescar(){
  Long actual=clienteCombo.getValue()==null?null:clienteCombo.getValue().getId();
  List<Cliente> lista=clientes.listar();clientesTabla.getItems().setAll(lista);clienteCombo.getItems().setAll(lista);
  if(actual!=null)lista.stream().filter(c->c.getId().equals(actual)).findFirst().ifPresent(clienteCombo::setValue);
  pedidosTabla.getItems().setAll(pedidos.listar());
 }
 private void limpiar(){nombreField.clear();correoField.clear();clientesTabla.getSelectionModel().clearSelection();}
 private void mensaje(String texto){estadoLabel.setText(texto);}
 private void ejecutar(Tarea tarea){try{tarea.ejecutar();}catch(Exception e){
  String detalle=e instanceof IllegalArgumentException?e.getMessage():"Operación fallida. Revise los registros y la conexión: "+e.getMessage();
  estadoLabel.setText(detalle);new Alert(Alert.AlertType.ERROR,detalle).showAndWait();
 }}
 @FunctionalInterface private interface Tarea{void ejecutar() throws Exception;}
}
