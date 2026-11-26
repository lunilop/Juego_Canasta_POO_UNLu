package ar.edu.unlu.poo.canasta.vista.grafica;

import ar.edu.unlu.poo.canasta.controlador.Controlador;
import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Combinacion;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.Palo;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class JuegoController {
    private final BorderPane raiz;
    private final Label lblEstadoSuperior;
    private final Label lblPozoMazo;
    private final Label lblMensajes;
    private final VBox panelMesas;
    private final HBox panelMano;
    private final HBox panelAcciones;
    private final Set<Integer> seleccionados = new HashSet<>();

    private Controlador controlador;

    public JuegoController() {
        raiz = new BorderPane();
        raiz.setStyle("-fx-background-color: #143624;");
        raiz.setPadding(new Insets(12));

        lblEstadoSuperior = new Label("Cargando partida...");
        lblEstadoSuperior.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnRanking = new Button("Ranking");
        btnRanking.setOnAction(e -> mostrarRanking());

        HBox barraSuperior = new HBox(15, lblEstadoSuperior, btnRanking);
        barraSuperior.setAlignment(Pos.CENTER_LEFT);
        barraSuperior.setPadding(new Insets(0, 0, 10, 0));
        raiz.setTop(barraSuperior);

        lblPozoMazo = new Label();
        lblPozoMazo.setStyle("-fx-text-fill: #f5d76e; -fx-font-size: 14px; -fx-font-weight: bold;");

        panelMesas = new VBox(8);
        panelMesas.setPadding(new Insets(10));
        panelMesas.setStyle("-fx-background-color: #1c4730; -fx-background-radius: 8;");

        ScrollPane scrollMesas = new ScrollPane(panelMesas);
        scrollMesas.setFitToWidth(true);
        scrollMesas.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox centro = new VBox(8, lblPozoMazo, scrollMesas);
        raiz.setCenter(centro);

        lblMensajes = new Label("");
        lblMensajes.setStyle("-fx-text-fill: #a3e4d7; -fx-font-size: 13px;");

        panelMano = new HBox(6);
        panelMano.setAlignment(Pos.CENTER);
        panelMano.setPadding(new Insets(8));

        ScrollPane scrollMano = new ScrollPane(panelMano);
        scrollMano.setFitToHeight(true);
        scrollMano.setPrefHeight(110);
        scrollMano.setStyle("-fx-background: #0e2619; -fx-background-color: #0e2619;");

        panelAcciones = new HBox(8);
        panelAcciones.setAlignment(Pos.CENTER);
        panelAcciones.setPadding(new Insets(8, 0, 0, 0));

        VBox inferior = new VBox(6, lblMensajes, scrollMano, panelAcciones);
        raiz.setBottom(inferior);
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    public Parent getVista() {
        return raiz;
    }

    public void setMensaje(String mensaje) {
        if (mensaje != null && !mensaje.isBlank()) {
            lblMensajes.setText(">> " + mensaje);
        }
    }

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
        seleccionados.clear();
        if (deTurno == null || yo == null) return;

        boolean esMiTurno = deTurno.getNombre().equalsIgnoreCase(yo.getNombre());
        Equipo miEquipo = yo.getEquipo();

        lblEstadoSuperior.setText(String.format(
                "Turno: %s %s | Equipo: %s | Total: %d pts | En mesa: %d pts | Apertura mín: %d pts",
                deTurno.getNombre(),
                esMiTurno ? "(TU TURNO)" : "",
                miEquipo != null ? miEquipo.getNombre() : "-",
                miEquipo != null ? miEquipo.getPuntajeTotal() : 0,
                miEquipo != null ? miEquipo.getPuntosParcialesEnMesa() : 0,
                miEquipo != null ? miEquipo.getPuntosMinimosParaAbrir() : 0
        ));

        lblPozoMazo.setText(String.format(
                "Mazo: %d cartas   |   Pozo (%d cartas): %s %s %s",
                cartasMazo,
                cartasPozo,
                topePozo != null ? topePozo.toString() : "Vacío",
                pozoTapado ? "[TAPADO]" : "",
                pozoCruzado ? "[CRUZADO]" : ""
        ));

        setMensaje(ultimoMensaje);

        panelMesas.getChildren().clear();
        for (Equipo eq : equipos) {
            Label lblEq = new Label(eq.getNombre()
                    + " | Total: " + eq.getPuntajeTotal()
                    + " | En mesa: " + eq.getPuntosParcialesEnMesa()
                    + " | 3 Rojos: " + eq.getTresRojos().size());
            lblEq.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            panelMesas.getChildren().add(lblEq);

            List<Combinacion> juegos = eq.getJuegosEnMesa();
            if (juegos.isEmpty()) {
                Label sinJuegos = new Label("   Sin juegos bajados aún");
                sinJuegos.setStyle("-fx-text-fill: #bdc3c7;");
                panelMesas.getChildren().add(sinJuegos);
            } else {
                for (int i = 0; i < juegos.size(); i++) {
                    Label lblJuego = new Label("   [" + i + "] " + juegos.get(i));
                    lblJuego.setStyle("-fx-text-fill: #d5f5e3; -fx-font-family: 'Consolas';");
                    panelMesas.getChildren().add(lblJuego);
                }
            }
        }

        panelMano.getChildren().clear();
        List<Carta> mano = yo.getMano();
        for (int i = 0; i < mano.size(); i++) {
            final int idx = i;
            Carta c = mano.get(i);
            ToggleButton btn = new ToggleButton(c.toString());
            btn.setPrefSize(66, 86);
            String color = (c.getPalo() == Palo.CORAZONES || c.getPalo() == Palo.DIAMANTES) ? "#c0392b" : "#1c2833";
            btn.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-background-radius: 6;");
            btn.setOnAction(e -> {
                if (btn.isSelected()) {
                    seleccionados.add(idx);
                    btn.setTranslateY(-8);
                } else {
                    seleccionados.remove(idx);
                    btn.setTranslateY(0);
                }
            });
            panelMano.getChildren().add(btn);
        }

        panelAcciones.getChildren().clear();
        if (!esMiTurno || controlador == null) {
            Label espera = new Label("Esperando que " + deTurno.getNombre() + " juegue...");
            espera.setStyle("-fx-text-fill: #f9e79f;");
            panelAcciones.getChildren().add(espera);
            return;
        }

        if (estado == EstadoTurno.ESPERANDO_ROBO) {
            Button btnRobar = new Button("Robar 2 del Mazo");
            btnRobar.setOnAction(e -> controlador.robarDelMazo());

            Button btnPozo = new Button("Tomar Pozo");
            btnPozo.setOnAction(e -> controlador.tomarPozo(new ArrayList<>(seleccionados), null));

            panelAcciones.getChildren().addAll(btnRobar, btnPozo);
        } else if (estado == EstadoTurno.ESPERANDO_ACCION) {
            CheckBox chkOculta = new CheckBox("Oculta");
            chkOculta.setStyle("-fx-text-fill: white;");

            Button btnBajar = new Button("Bajar Combinación");
            btnBajar.setOnAction(e -> controlador.bajarCombinacion(new ArrayList<>(seleccionados), chkOculta.isSelected()));

            ComboBox<Integer> comboMesa = new ComboBox<>();
            if (miEquipo != null) {
                for (int i = 0; i < miEquipo.getJuegosEnMesa().size(); i++) {
                    comboMesa.getItems().add(i);
                }
            }
            comboMesa.setPromptText("Juego #");

            Button btnAgregar = new Button("Agregar a Juego");
            btnAgregar.setOnAction(e -> {
                if (comboMesa.getValue() == null) {
                    controlador.mostrarError("Seleccioná un número de juego en mesa.");
                    return;
                }
                controlador.agregarACombinacion(comboMesa.getValue(), new ArrayList<>(seleccionados));
            });

            Button btnDescartar = new Button("Descartar");
            btnDescartar.setOnAction(e -> {
                if (seleccionados.size() != 1) {
                    controlador.mostrarError("Seleccioná exactamente 1 carta para descartar.");
                    return;
                }
                controlador.descartar(seleccionados.iterator().next());
            });

            Button btnCerrar = new Button("Cerrar Mano");
            btnCerrar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");
            btnCerrar.setOnAction(e -> {
                if (seleccionados.size() != 1) {
                    controlador.mostrarError("Seleccioná exactamente la última carta para cerrar.");
                    return;
                }
                controlador.cerrarMano(seleccionados.iterator().next());
            });

            Button btnDeshacer = new Button("Deshacer Bajadas");
            btnDeshacer.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
            btnDeshacer.setOnAction(e -> controlador.deshacerJugadasTurno());

            panelAcciones.getChildren().addAll(chkOculta, btnBajar, comboMesa, btnAgregar, btnDescartar, btnCerrar, btnDeshacer);
        }
    }

    private void mostrarRanking() {
        if (controlador == null) return;
        List<RegistroRanking> ranking = controlador.getRanking();
        String texto = ranking.isEmpty()
                ? "Aún no hay partidas registradas."
                : String.join("\n", ranking.stream().map(RegistroRanking::toString).toList());

        Alert alert = new Alert(Alert.AlertType.INFORMATION, texto);
        alert.setTitle("Ranking");
        alert.setHeaderText("Top 5 - Canasta");
        alert.showAndWait();
    }
}
