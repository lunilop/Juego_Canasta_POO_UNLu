package ar.edu.unlu.poo.canasta.controlador;

import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.Partida;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import ar.edu.unlu.poo.canasta.vista.IVista;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControladorTest {

    static class VistaEspia implements IVista {
        final List<String> llamadas = new ArrayList<>();

        @Override
        public void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo) {
            llamadas.add("mostrarSala:" + jugadores.size() + ":yo=" + (yo != null ? yo.getNombre() : ""));
        }

        @Override
        public void mostrarJuego() {
            llamadas.add("mostrarJuego");
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
            llamadas.add("mostrarMesa:turno=" + (deTurno != null ? deTurno.getNombre() : "")
                    + ":estado=" + estado);
        }

        @Override
        public void mostrarResultado(String mensajeFinal, List<RegistroRanking> ranking) {
            llamadas.add("mostrarResultado:" + mensajeFinal);
        }

        @Override
        public void mostrarMensaje(String mensaje) {
            llamadas.add("mostrarMensaje:" + mensaje);
        }

        @Override
        public void mostrarError(String mensaje) {
            llamadas.add("mostrarError:" + mensaje);
        }

        String ultima() {
            return llamadas.get(llamadas.size() - 1);
        }
    }

    private static Controlador conectar(Partida partida, IVista vista) throws RemoteException {
        Controlador controlador = new Controlador();
        controlador.setModeloRemoto(partida);
        partida.agregarObservador(controlador);
        controlador.setVista(vista);
        return controlador;
    }

    private Partida partida;
    private VistaEspia vista;
    private Controlador controlador;

    @BeforeEach
    void setUp() throws RemoteException {
        partida = new Partida();
        vista = new VistaEspia();
        controlador = conectar(partida, vista);
    }

    @Test
    void entrarMuestraSalaYNombreRepetidoMuestraError() throws RemoteException {
        assertTrue(controlador.entrar("Luna"));
        assertEquals("mostrarSala:1:yo=Luna", vista.ultima());

        VistaEspia vista2 = new VistaEspia();
        Controlador otro = conectar(partida, vista2);
        assertFalse(otro.entrar("Luna"));
        assertTrue(vista2.ultima().startsWith("mostrarError:"));
    }

    @Test
    void iniciarPartidaConDosJugadoresMuestraMesa() throws RemoteException {
        VistaEspia vista2 = new VistaEspia();
        Controlador segundo = conectar(partida, vista2);

        controlador.entrar("Luna");
        segundo.entrar("Nico");
        controlador.iniciarPartida(5000);

        assertTrue(vista.ultima().startsWith("mostrarMesa:turno=Luna:estado=ESPERANDO_ROBO"));
        assertTrue(vista2.ultima().startsWith("mostrarMesa:turno=Luna:estado=ESPERANDO_ROBO"));
    }

    @Test
    void jugarFueraDeTurnoMuestraError() throws RemoteException {
        VistaEspia vista2 = new VistaEspia();
        Controlador segundo = conectar(partida, vista2);

        controlador.entrar("Luna");
        segundo.entrar("Nico");
        controlador.iniciarPartida(5000);

        segundo.robarDelMazo();
        assertTrue(vista2.ultima().startsWith("mostrarError:"));
    }
}