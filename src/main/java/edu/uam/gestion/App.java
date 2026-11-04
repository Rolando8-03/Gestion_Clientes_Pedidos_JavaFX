package edu.uam.gestion;
import edu.uam.gestion.config.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class App extends Application {
 @Override public void start(Stage stage) throws Exception {
  var loader=new FXMLLoader(App.class.getResource("/fxml/principal.fxml"));
  stage.setTitle("UAM | Gestión de clientes y pedidos");stage.setScene(new Scene(loader.load(),1050,730));stage.show();
 }
 @Override public void stop(){HibernateUtil.cerrar();}
 public static void main(String[] args){launch(args);}
}
