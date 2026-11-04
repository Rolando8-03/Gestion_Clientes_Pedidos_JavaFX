package edu.uam.gestion;

import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.controller.PrincipalController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private PrincipalController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/principal.fxml"));
        Scene scene = new Scene(loader.load(), 1180, 780);
        scene.getStylesheets().add(App.class.getResource("/css/estilos.css").toExternalForm());
        controller = loader.getController();
        stage.setTitle("Gestión de clientes y pedidos | UAM");
        stage.setMinWidth(1050);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // La conexión se cierra cuando termina la última tarea, sin bloquear JavaFX.
        if (controller != null) {
            controller.cerrar();
        } else {
            HibernateUtil.cerrar();
        }
    }

    public static void main(String[] args) { launch(args); }
}
