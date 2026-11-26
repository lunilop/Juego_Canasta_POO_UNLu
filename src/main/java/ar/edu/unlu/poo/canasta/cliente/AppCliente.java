package ar.edu.unlu.poo.canasta.cliente;

import ar.edu.unlu.poo.canasta.controlador.Controlador;
import ar.edu.unlu.poo.canasta.vista.consola.VentanaConsola;
import ar.edu.unlu.poo.canasta.vista.consola.VistaConsola;
import ar.edu.unlu.poo.canasta.vista.grafica.VistaGrafica;
import ar.edu.unlu.rmimvc.RMIMVCException;
import ar.edu.unlu.rmimvc.Util;
import ar.edu.unlu.rmimvc.cliente.Cliente;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class AppCliente extends Application {

    @Override
    public void start(Stage stage) {
        List<String> ips = Util.getIpDisponibles();
        String ipDefault = (ips != null && !ips.isEmpty()) ? ips.get(0) : "127.0.0.1";
        List<String> opcionesIp = (ips != null && !ips.isEmpty()) ? ips : List.of("127.0.0.1");

        Optional<String> ipCliente = elegir("¿En qué IP escucha este cliente?", ipDefault, opcionesIp);
        if (ipCliente.isEmpty()) return;

        Optional<String> puertoCliente = preguntar(
                "¿En qué puerto escucha este cliente?",
                String.valueOf(Puertos.primerLibre(9999))
        );
        if (puertoCliente.isEmpty()) return;

        Optional<String> ipServidor = preguntar("¿En qué IP está el servidor?", "127.0.0.1");
        if (ipServidor.isEmpty()) return;

        Optional<String> puertoServidor = preguntar("¿En qué puerto está el servidor?", "8888");
        if (puertoServidor.isEmpty()) return;

        Optional<String> modo = elegir(
                "¿Cómo querés jugar?",
                "2. Interfaz Gráfica",
                List.of("1. Consola (ventana propia)", "2. Interfaz Gráfica")
        );
        if (modo.isEmpty()) return;

        boolean esConsola = modo.get().startsWith("1.");
        Controlador controlador = new Controlador();
        VistaConsola vistaConsola = null;
        VentanaConsola ventanaConsola = null;

        if (esConsola) {
            stage.hide();
            ventanaConsola = new VentanaConsola("Canasta Consola");
            vistaConsola = new VistaConsola(ventanaConsola);
            vistaConsola.setControlador(controlador);
            controlador.setVista(vistaConsola);
        } else {
            VistaGrafica vistaGrafica = new VistaGrafica(stage, controlador);
            controlador.setVista(vistaGrafica);
        }

        try {
            Cliente cliente = new Cliente(
                    ipCliente.get().trim(),
                    Integer.parseInt(puertoCliente.get().trim()),
                    ipServidor.get().trim(),
                    Integer.parseInt(puertoServidor.get().trim())
            );
            cliente.iniciar(controlador);
        } catch (RemoteException | RMIMVCException | NumberFormatException e) {
            if (ventanaConsola != null) {
                ventanaConsola.dispose();
            }
            Alert error = new Alert(
                    Alert.AlertType.ERROR,
                    "Verificá que el servidor esté corriendo en " + ipServidor.get().trim() + ":"
                            + puertoServidor.get().trim() + "\n\n" + e.getMessage()
            );
            error.setHeaderText("No se pudo conectar al servidor");
            error.showAndWait();
            System.exit(1);
        }

        boolean adentro = false;
        String nombreFinal = "";
        while (!adentro) {
            Optional<String> nombre = preguntar("¿Cómo te llamás?", "");
            if (nombre.isEmpty()) {
                controlador.salir();
                if (ventanaConsola != null) {
                    ventanaConsola.dispose();
                }
                return;
            }
            nombreFinal = nombre.get().trim();
            if (!esConsola) {
                stage.setTitle("Canasta — " + nombreFinal);
            }
            adentro = controlador.entrar(nombreFinal);
        }

        if (esConsola) {
            ventanaConsola.setTitle("Canasta Consola — " + nombreFinal);
            vistaConsola.mostrarMensaje("Conectado como " + nombreFinal + ".");
//            vistaConsola.mostrarSala(
//                    controlador.getJugadores(),
//                    null,
//                    controlador.getJugadorPorNombre(nombreFinal)
//            );
        }
    }

    private Optional<String> elegir(String pregunta, String porDefecto, List<String> opciones) {
        ChoiceDialog<String> dialogo = new ChoiceDialog<>(porDefecto, opciones);
        dialogo.setTitle("Conexión");
        dialogo.setHeaderText(pregunta);
        return salirSiCancela(dialogo.showAndWait());
    }

    private Optional<String> preguntar(String pregunta, String porDefecto) {
        TextInputDialog dialogo = new TextInputDialog(porDefecto);
        dialogo.setTitle("Conexión");
        dialogo.setHeaderText(pregunta);
        return salirSiCancela(dialogo.showAndWait());
    }

    private static Optional<String> salirSiCancela(Optional<String> respuesta) {
        if (respuesta.isEmpty()) {
            Platform.exit();
        }
        return respuesta;
    }
}