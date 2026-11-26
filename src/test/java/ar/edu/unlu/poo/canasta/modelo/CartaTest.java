package ar.edu.unlu.poo.canasta.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CartaTest {

    @Test
    void deberiaCrearCartasValidasYRechazarInvalidas() {
        Carta asPicas = new Carta(Palo.PICAS, 1);
        assertEquals(Palo.PICAS, asPicas.getPalo());
        assertEquals(1, asPicas.getNumero());

        Carta joker = new Carta(Palo.JOKER, 0);
        assertTrue(joker.esComodin());
        assertEquals(50, joker.getPuntaje());

        assertThrows(IllegalArgumentException.class, () -> new Carta(Palo.JOKER, 5));
        assertThrows(IllegalArgumentException.class, () -> new Carta(Palo.CORAZONES, 0));
        assertThrows(IllegalArgumentException.class, () -> new Carta(Palo.CORAZONES, 14));
    }

    @Test
    void deberiaDistinguirComodinesTresRojosYTresNegros() {
        Carta dosTrebol = new Carta(Palo.TREBOLES, 2);
        Carta joker = new Carta(Palo.JOKER, 0);
        Carta tresCorazones = new Carta(Palo.CORAZONES, 3);
        Carta tresDiamantes = new Carta(Palo.DIAMANTES, 3);
        Carta tresPicas = new Carta(Palo.PICAS, 3);
        Carta tresTreboles = new Carta(Palo.TREBOLES, 3);

        assertTrue(dosTrebol.esComodin());
        assertEquals(20, dosTrebol.getPuntaje());

        assertTrue(joker.esComodin());
        assertEquals(50, joker.getPuntaje());

        assertTrue(tresCorazones.esTresRojo());
        assertFalse(tresCorazones.esTresNegro());
        assertEquals(0, tresCorazones.getPuntaje());

        assertTrue(tresDiamantes.esTresRojo());
        assertTrue(tresPicas.esTresNegro());
        assertTrue(tresTreboles.esTresNegro());
    }

    @Test
    void deberiaRespetarPuntajesPorRangoDeCarta() {
        assertEquals(20, new Carta(Palo.DIAMANTES, 1).getPuntaje());
        assertEquals(10, new Carta(Palo.PICAS, 8).getPuntaje());
        assertEquals(10, new Carta(Palo.CORAZONES, 13).getPuntaje());
        assertEquals(5, new Carta(Palo.TREBOLES, 4).getPuntaje());
        assertEquals(5, new Carta(Palo.DIAMANTES, 7).getPuntaje());
    }

    @Test
    void deberiaCumplirIgualdadYHashCode() {
        Carta c1 = new Carta(Palo.PICAS, 12);
        Carta c2 = new Carta(Palo.PICAS, 12);
        Carta c3 = new Carta(Palo.CORAZONES, 12);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}