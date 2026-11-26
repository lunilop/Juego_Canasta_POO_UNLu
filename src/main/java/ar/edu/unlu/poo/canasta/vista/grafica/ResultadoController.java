package ar.edu.unlu.poo.canasta.vista.grafica;

import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class ResultadoController {
    private final VBox raiz;
    private final Label lblResultado;
    private final TextArea txtRanking;

    public ResultadoController() {
        Label titulo = new Label("FIN DE LA PARTIDA");
        titulo.setStyle("-fx-text-fill: #f5d76e; -fx-font-size: 20px; -fx-font-weight: bold;");

        lblResultado = new Label("");
        lblResultado.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

        txtRanking = new TextArea();
        txtRanking.setEditable(false);
        txtRanking.setPrefHeight(180);

        raiz = new VBox(14, titulo, lblResultado, txtRanking);
        raiz.setAlignment(Pos.CENTER);
        raiz.setPadding(new Insets(24));
        raiz.setStyle("-fx-background-color: #143624;");
    }

    public Parent getVista() {
        return raiz;
    }

    public void mostrar(String mensajeFinal, List<RegistroRanking> ranking) {
        lblResultado.setText(mensajeFinal != null ? mensajeFinal : "Partida finalizada.");
        if (ranking == null || ranking.isEmpty()) {
            txtRanking.setText("Sin registros en el ranking.");
        } else {
            StringBuilder sb = new StringBuilder("=== RANKING ===\n");
            for (int i = 0; i < ranking.size(); i++) {
                sb.append(i + 1).append(". ").append(ranking.get(i)).append("\n");
            }
            txtRanking.setText(sb.toString());
        }
    }
}