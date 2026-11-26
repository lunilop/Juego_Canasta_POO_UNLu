package ar.edu.unlu.poo.canasta.servidor;

import ar.edu.unlu.poo.canasta.modelo.Partida;
import ar.edu.unlu.poo.canasta.modelo.persistencia.GestorPersistencia;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.servidor.Servidor;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Crea la única Partida y la publica con RMI para que se conecten los clientes
 */
public class AppServidor extends Application {

    @Override
    public void start(Stage stage) {
        List<String> ips = Util.getIpDisponibles();
        String ipDefault = (ips != null && !ips.isEmpty()) ? ips.get(0) : "127.0.0.1";
        List<String> opcionesIp = (ips != null && !ips.isEmpty()) ? ips : List.of("127.0.0.1");

        ChoiceDialog<String> dialogoIp = new ChoiceDialog<>(ipDefault, opcionesIp);
        dialogoIp.setTitle("Servidor");
        dialogoIp.setHeaderText("¿En qué IP escucha el servidor?");
        dialogoIp.setContentText("IP:");
        Optional<String> ip = dialogoIp.showAndWait();
        if (ip.isEmpty()) {
            Platform.exit();
            return;
        }

        TextInputDialog dialogoPuerto = new TextInputDialog("8888");
        dialogoPuerto.setTitle("Servidor");
        dialogoPuerto.setHeaderText("¿En qué puerto escucha el servidor?");
        dialogoPuerto.setContentText("Puerto:");
        Optional<String> puerto = dialogoPuerto.showAndWait();
        if (puerto.isEmpty()) {
            Platform.exit();
            return;
        }

        Partida modelo = null;
        if (GestorPersistencia.existePartidaGuardada()) {
            Alert confirmacion = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Se encontró una partida guardada. ¿Deseás restaurarla?",
                    ButtonType.YES,
                    ButtonType.NO
            );
            confirmacion.setTitle("Partida Guardada");
            confirmacion.setHeaderText(null);
            Optional<ButtonType> resp = confirmacion.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.YES) {
                modelo = GestorPersistencia.cargarPartida();
            }
        }
        if (modelo == null) {
            modelo = new Partida();
        }

        try {
            new Servidor(ip.get().trim(), Integer.parseInt(puerto.get().trim())).iniciar(modelo);
        } catch (RemoteException | RMIMVCException | NumberFormatException e) {
            Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage());
            error.setHeaderText("No se pudo iniciar el servidor");
            error.showAndWait();
            Platform.exit();
            return;
        }

        Label estado = new Label("Servidor corriendo");
        estado.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        Label datos = new Label("IP: " + ip.get().trim() + "     Puerto: " + puerto.get().trim());

        VBox raiz = new VBox(12, estado, datos);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));

        stage.setTitle("Canasta — Servidor");
        stage.setScene(new Scene(raiz, 340, 130));
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }
}