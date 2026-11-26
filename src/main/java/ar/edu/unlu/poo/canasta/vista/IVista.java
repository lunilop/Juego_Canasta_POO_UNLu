package ar.edu.unlu.poo.canasta.vista;

import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import java.util.List;

public interface IVista {
    void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo);

    void mostrarJuego();

    void mostrarMesa(
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
    );

    //void mostrarResultado(List<Equipo> equipos, Equipo ganador, Jugador yo);
    void mostrarResultado(String mensajeFinal, List<RegistroRanking> ranking);
    void mostrarMensaje(String mensaje);
    void mostrarError(String mensaje);
}