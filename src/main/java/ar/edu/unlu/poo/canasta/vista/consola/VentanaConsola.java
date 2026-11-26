package ar.edu.unlu.poo.canasta.vista.consola;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class VentanaConsola extends JFrame {
    private final JTextArea areaSalida;
    private final JTextField campoEntrada;
    private Consumer<String> manejadorEntrada;

    public VentanaConsola(String titulo) {
        super(titulo);

        areaSalida = new JTextArea();
        areaSalida.setEditable(false);
        areaSalida.setBackground(new Color(20, 54, 36));
        areaSalida.setForeground(new Color(235, 245, 238));
        areaSalida.setFont(new Font("Consolas", Font.PLAIN, 13));
        areaSalida.setLineWrap(true);
        areaSalida.setWrapStyleWord(true);

        campoEntrada = new JTextField();
        campoEntrada.setFont(new Font("Consolas", Font.PLAIN, 13));
        JButton btnEnviar = new JButton("Enviar");

        ActionListener enviar = e -> {
            String texto = campoEntrada.getText();
            campoEntrada.setText("");
            if (manejadorEntrada != null) {
                manejadorEntrada.accept(texto);
            }
        };

        campoEntrada.addActionListener(enviar);
        btnEnviar.addActionListener(enviar);

        JPanel panelInferior = new JPanel(new BorderLayout(6, 0));
        panelInferior.add(campoEntrada, BorderLayout.CENTER);
        panelInferior.add(btnEnviar, BorderLayout.EAST);

        setLayout(new BorderLayout(6, 6));
        add(new JScrollPane(areaSalida), BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);

        setSize(720, 540);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setManejadorEntrada(Consumer<String> manejadorEntrada) {
        this.manejadorEntrada = manejadorEntrada;
    }

    public void escribir(String texto) {
        SwingUtilities.invokeLater(() -> {
            areaSalida.append(texto);
            areaSalida.setCaretPosition(areaSalida.getDocument().getLength());
        });
    }
}