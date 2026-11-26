package ar.edu.unlu.poo.canasta.modelo;

import java.rmi.RemoteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EquipoYPartidaTest {

    @Test
    void cuadroDePuntosMinimosParaAbrir() {
        Equipo eq = new Equipo("Test");
        assertEquals(50, eq.getPuntosMinimosParaAbrir()); // 0..1495

        eq.calcularPuntajeMano(false); // 0 pts -> sigue 50
        assertEquals(50, eq.getPuntosMinimosParaAbrir());

        // Simulamos puntaje negativo
        Equipo eqNegativo = new Equipo("Neg");
        Jugador j = new Jugador("Ana");
        j.recibirCarta(new Carta(Palo.JOKER, 0)); // -50 en mano
        eqNegativo.agregarIntegrante(j);
        eqNegativo.calcularPuntajeMano(false);
        assertEquals(15, eqNegativo.getPuntosMinimosParaAbrir());
    }

    @Test
    void tresRojosSumanConCanastaYRestanSinCanastaAlCerrarMano() {
        Equipo eqSinCanasta = new Equipo("SinCanasta");
        eqSinCanasta.agregarTresRojo(new Carta(Palo.CORAZONES, 3));
        assertEquals(-100, eqSinCanasta.calcularPuntajeMano(false));
    }

    @Test
    void partidaRechazaNombreDuplicadoSinDistinguirMayusculas() throws RemoteException {
        Partida partida = new Partida();
        partida.agregarJugador("Luna");
        assertThrows(IllegalArgumentException.class, () -> partida.agregarJugador("LUNA"));
    }
}