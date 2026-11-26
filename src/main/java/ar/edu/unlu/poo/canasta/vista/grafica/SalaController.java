package ar.edu.unlu.poo.canasta.vista.grafica;

import ar.edu.unlu.poo.canasta.controlador.Controlador;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SalaController {
    private final VBox raiz;
    private final ListView<String> lstJugadores;
    private final Label lblInfo;
    private Controlador controlador;

    public SalaController() {
        Label titulo = new Label("CANASTA — Sala de Espera");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        lblInfo = new Label("Esperando jugadores (2, 4 o 6)...");
        lblInfo.setStyle("-fx-text-fill: #d5f5e3; -fx-font-size: 13px;");

        lstJugadores = new ListView<>();
        lstJugadores.setPrefHeight(180);

        Button btn5000 = new Button("Iniciar (5.000 pts)");
        btn5000.setOnAction(e -> {
            if (controlador != null) controlador.iniciarPartida(5000);
        });

        Button btn7000 = new Button("Iniciar (7.000 pts)");
        btn7000.setOnAction(e -> {
            if (controlador != null) controlador.iniciarPartida(7000);
        });

        Button btn15000 = new Button("Iniciar (15.000 pts)");
        btn15000.setOnAction(e -> {
            if (controlador != null) controlador.iniciarPartida(15000);
        });

        HBox botones = new HBox(10, btn5000, btn7000, btn15000);
        botones.setAlignment(Pos.CENTER);

        raiz = new VBox(14, titulo, lblInfo, lstJugadores, botones);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));
        raiz.setStyle("-fx-background-color: #143624;");
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public Parent getVista() {
        return raiz;
    }

    public void mostrar(List<Jugador> jugadores, Jugador admin, Jugador yo) {
        String nombreLocal = (yo != null) ? yo.getNombre() : "";
        lstJugadores.getItems().setAll(
                jugadores.stream()
                        .map(j -> j.getNombre()
                                + (!nombreLocal.isEmpty() && j.getNombre().equalsIgnoreCase(nombreLocal) ? " (vos)" : ""))
                        .toList()
        );
        lblInfo.setText("Conectados: " + jugadores.size() + " (se requieren 2, 4 o 6 para iniciar)");
    }
}