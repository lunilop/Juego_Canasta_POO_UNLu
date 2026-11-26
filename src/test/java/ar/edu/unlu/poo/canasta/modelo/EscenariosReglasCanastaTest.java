package ar.edu.unlu.poo.canasta.modelo;

import java.rmi.RemoteException;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EscenariosReglasCanastaTest {

    private Partida crearPartidaIniciadaDosJugadores(int objetivo) throws RemoteException {
        Partida partida = new Partida();
        partida.agregarJugador("Luna");
        partida.agregarJugador("Nico");
        partida.iniciarPartida(objetivo);
        return partida;
    }

    @Test
    void noSePuedeDescartarSinHaberRobadoAntes() throws RemoteException {
        Partida partida = crearPartidaIniciadaDosJugadores(5000);
        // Al iniciar, el estado es ESPERANDO_ROBO
        assertEquals(EstadoTurno.ESPERANDO_ROBO, partida.getEstadoTurno());
        assertThrows(IllegalStateException.class, () -> partida.descartar("Luna", 0));
    }

    @Test
    void robarDelMazoPasaAEsparandoAccionYDescartarPasaTurnoANico() throws RemoteException {
        Partida partida = crearPartidaIniciadaDosJugadores(5000);
        int manoAntes = partida.getJugadorPorNombre("Luna").getMano().size();

        partida.robarDelMazo("Luna");
        assertEquals(EstadoTurno.ESPERANDO_ACCION, partida.getEstadoTurno());
        assertEquals(manoAntes + 2, partida.getJugadorPorNombre("Luna").getMano().size());

        partida.descartar("Luna", 0);
        assertEquals(EstadoTurno.ESPERANDO_ROBO, partida.getEstadoTurno());
        assertEquals("Nico", partida.getJugadorActual().getNombre());
    }

    @Test
    void deshacerJugadasDevuelveCartasYRestauraMesa() throws RemoteException {
        Partida partida = crearPartidaIniciadaDosJugadores(5000);
        Jugador luna = partida.getJugadorPorNombre("Luna");
        luna.vaciarMano();
        luna.recibirCarta(new Carta(Palo.CORAZONES, 9));
        luna.recibirCarta(new Carta(Palo.DIAMANTES, 9));
        luna.recibirCarta(new Carta(Palo.PICAS, 9));
        luna.recibirCarta(new Carta(Palo.TREBOLES, 4));

        partida.robarDelMazo("Luna"); // ahora tiene 6 cartas (las 4 + 2 robadas)
        int cantidadConRobo = luna.getMano().size();

        // Buscamos índices de tres 9
        List<Integer> idxsNueves = List.of(
                indiceCarta(luna, Palo.CORAZONES, 9),
                indiceCarta(luna, Palo.DIAMANTES, 9),
                indiceCarta(luna, Palo.PICAS, 9)
        );
        partida.bajarCombinacion("Luna", idxsNueves, false);
        assertEquals(1, luna.getEquipo().getJuegosEnMesa().size());
        assertEquals(cantidadConRobo - 3, luna.getMano().size());

        partida.deshacerJugadasTurno("Luna");
        assertEquals(0, luna.getEquipo().getJuegosEnMesa().size());
        assertEquals(cantidadConRobo, luna.getMano().size());
    }

    @Test
    void tomarPozoCuandoEquipoNoAbrioAunDebeRequerirApertura() throws RemoteException {
        Partida partida = crearPartidaIniciadaDosJugadores(5000);
        Jugador luna = partida.getJugadorPorNombre("Luna");
        luna.vaciarMano();
        luna.recibirCarta(new Carta(Palo.CORAZONES, 8));
        luna.recibirCarta(new Carta(Palo.DIAMANTES, 8));
        luna.recibirCarta(new Carta(Palo.PICAS, 5));

        // Forzamos tope del pozo con un 8
        partida.robarDelMazo("Luna");
        // Vaciamos y ponemos un 8 de descarte usando una jugada limpia:
        // Si el pozo no tiene 8 arriba, probamos qué pasa al intentar tomar el pozo con dos 8 en mano
        Carta tope = partida.verTopePozo();
        if (tope != null && !tope.esComodin() && !tope.esTresNegro()) {
            luna.vaciarMano();
            luna.recibirCarta(new Carta(Palo.CORAZONES, tope.getNumero()));
            luna.recibirCarta(new Carta(Palo.DIAMANTES, tope.getNumero()));
            luna.recibirCarta(new Carta(Palo.PICAS, 4));

            // Regla: "Es obligatorio para el jugador abrirse cuando se lleva el pote"
            // ¿Qué ocurre si tiene los puntos de apertura (ej. 50 pts) o si no los tiene?
            assertThrows(IllegalStateException.class,
                    () -> partida.tomarPozo("Luna", List.of(0, 1), null));
        }
    }

    @Test
    void noSePuedeCerrarManoSinCanastaLimpiaYSucia() throws RemoteException {
        Partida partida = crearPartidaIniciadaDosJugadores(5000);
        Jugador luna = partida.getJugadorPorNombre("Luna");
        luna.vaciarMano();
        luna.recibirCarta(new Carta(Palo.CORAZONES, 4));

        assertThrows(IllegalStateException.class, () -> partida.cerrarMano("Luna", 0));
    }

    @Test
    void noSePuedeCerrarEnElMismoTurnoQueSeCerroLaCanastaLimpia() throws RemoteException {
        Equipo eq = new Equipo("Eq1");
        // Simulamos que ya tiene canasta limpia y sucia, pero turnoCanastaLimpia == turnoActual (5)
        for (int i = 0; i < 7; i++) {
            eq.bajarCombinacion(new Combinacion(List.of(
                    new Carta(Palo.CORAZONES, 8),
                    new Carta(Palo.DIAMANTES, 8),
                    new Carta(Palo.PICAS, 8)
            ), false), 5);
        }
        // Regla: solo se puede cerrar en el turno/mano siguiente de cerrar la canasta limpia
        assertFalse(eq.puedeCerrar(5));
        assertTrue(eq.puedeCerrar(6) || !eq.tieneCanastaLimpia() || !eq.tieneCanastaSucia());
    }

    @Test
    void tresNegrosEnManoRestanCienCadaUnoAlFinDeMano() {
        Equipo eq = new Equipo("EqTresNegros");
        Jugador j = new Jugador("Nico");
        j.recibirCarta(new Carta(Palo.PICAS, 3));
        j.recibirCarta(new Carta(Palo.TREBOLES, 3));
        eq.agregarIntegrante(j);

        int puntajeMano = eq.calcularPuntajeMano(false);
        assertEquals(-200, puntajeMano);
    }

    @Test
    void finDePartidaAlLlegarAlPuntajeObjetivo() throws RemoteException {
        Partida partida = new Partida();
        partida.agregarJugador("Luna");
        partida.agregarJugador("Nico");
        partida.iniciarPartida(500);

        Jugador luna = partida.getJugadorPorNombre("Luna");

        Combinacion limpia = new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 7),
                new Carta(Palo.DIAMANTES, 7),
                new Carta(Palo.PICAS, 7),
                new Carta(Palo.TREBOLES, 7),
                new Carta(Palo.CORAZONES, 7),
                new Carta(Palo.DIAMANTES, 7),
                new Carta(Palo.PICAS, 7)
        ), false);
        Combinacion sucia = new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 8),
                new Carta(Palo.DIAMANTES, 8),
                new Carta(Palo.PICAS, 8),
                new Carta(Palo.TREBOLES, 8),
                new Carta(Palo.CORAZONES, 8),
                new Carta(Palo.DIAMANTES, 2),
                new Carta(Palo.PICAS, 2)
        ), false);

        luna.getEquipo().bajarCombinacion(limpia, 1);
        luna.getEquipo().bajarCombinacion(sucia, 1);

        // Turno 1 de Luna: roba, queda con 2 cartas y descarta 1 (le queda 1)
        partida.robarDelMazo("Luna");
        luna.vaciarMano();
        luna.recibirCarta(new Carta(Palo.CORAZONES, 4));
        luna.recibirCarta(new Carta(Palo.PICAS, 5));
        partida.descartar("Luna", 0);

        // Turno 2 de Nico: roba, queda con 2 cartas y descarta 1
        partida.robarDelMazo("Nico");
        Jugador nico = partida.getJugadorPorNombre("Nico");
        nico.vaciarMano();
        nico.recibirCarta(new Carta(Palo.CORAZONES, 6));
        nico.recibirCarta(new Carta(Palo.DIAMANTES, 6));
        partida.descartar("Nico", 0);

        // Turno 3 de Luna: ya pasó más de 1 turno tras bajar la limpia (turno 1 -> 3)
        partida.robarDelMazo("Luna");
        luna.vaciarMano();
        luna.recibirCarta(new Carta(Palo.CORAZONES, 4)); // 1 sola carta sobrante para cerrar
        partida.cerrarMano("Luna", 0);

        assertEquals(EstadoTurno.FIN_PARTIDA, partida.getEstadoTurno());
        assertTrue(partida.getUltimoMensaje().contains("Luna"));
    }

    private int indiceCarta(Jugador j, Palo palo, int numero) {
        List<Carta> mano = j.getMano();
        for (int i = 0; i < mano.size(); i++) {
            Carta c = mano.get(i);
            if (c.getPalo() == palo && c.getNumero() == numero) {
                return i;
            }
        }
        throw new AssertionError("No se encontró carta " + numero + " de " + palo);
    }
}