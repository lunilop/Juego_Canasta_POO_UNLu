package ar.edu.unlu.poo.canasta.vista.grafica;

import ar.edu.unlu.poo.canasta.controlador.Controlador;
import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import ar.edu.unlu.poo.canasta.vista.IVista;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class VistaGrafica implements IVista {
    private final Stage stage;
    private final Controlador controlador;
    private final SalaController salaController;
    private final JuegoController juegoController;
    private final ResultadoController resultadoController;

    public VistaGrafica(Stage stage, Controlador controlador) {
        this.stage = stage;
        this.controlador = controlador;
        this.salaController = new SalaController();
        this.juegoController = new JuegoController();
        this.resultadoController = new ResultadoController();

        this.salaController.setControlador(controlador);
        this.juegoController.setControlador(controlador);

        stage.setTitle("Canasta UNLu");
        stage.setScene(new Scene(salaController.getVista(), 980, 650));
        stage.setOnCloseRequest(e -> controlador.salir());
        stage.show();
    }

    @Override
    public void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo) {
        Platform.runLater(() -> {
            String titulo = (yo != null) ? "Canasta — " + yo.getNombre() : "Canasta";
            stage.setTitle(titulo);
            stage.getScene().setRoot(salaController.getVista());
            salaController.mostrar(jugadores, admin, yo);
        });
    }

    @Override
    public void mostrarJuego() {
        Platform.runLater(() -> stage.getScene().setRoot(juegoController.getVista()));
    }

    @Override
    public void mostrarMesa(
            EstadoTurno estado,
            Jugador deTurno,
            Jugador yo,
            List<Equipo> equipos,
            int cartasMazo,
            int cartasPozo,
            Carta topePozo,
            boolean pozoTapado,
            boolean pozoCruzado,
            String ultimoMensaje
    ) {
        Platform.runLater(() -> {
            if (stage.getScene() != null && stage.getScene().getRoot() != juegoController.getVista()) {
                stage.getScene().setRoot(juegoController.getVista());
            }
            juegoController.mostrarMesa(
                    estado, deTurno, yo, equipos,
                    cartasMazo, cartasPozo, topePozo,
                    pozoTapado, pozoCruzado, ultimoMensaje
            );
        });
    }

    @Override
    public void mostrarResultado(String mensajeFinal, List<RegistroRanking> ranking) {
        Platform.runLater(() -> {
            stage.getScene().setRoot(resultadoController.getVista());
            resultadoController.mostrar(mensajeFinal, ranking);
        });
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        Platform.runLater(() -> juegoController.setMensaje(mensaje));
    }

    @Override
    public void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, mensaje);
            if (stage.getScene() != null) {
                alert.initOwner(stage);
            }
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}