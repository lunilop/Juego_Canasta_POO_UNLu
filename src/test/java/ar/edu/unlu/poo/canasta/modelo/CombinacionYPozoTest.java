package ar.edu.unlu.poo.canasta.modelo;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinacionYPozoTest {

    @Test
    void combinacionValidaYNoPermiteMasComodinesQueNaturales() {
        Combinacion juegoValido = new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 7),
                new Carta(Palo.PICAS, 7),
                new Carta(Palo.DIAMANTES, 2)
        ), false);
        assertEquals(7, juegoValido.getNumeroBase());
        assertFalse(juegoValido.esCanasta());

        assertThrows(IllegalArgumentException.class, () -> new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 7),
                new Carta(Palo.DIAMANTES, 2),
                new Carta(Palo.TREBOLES, 2)
        ), false));
    }

    @Test
    void canastaLimpiaYSuciaSumanBonos() {
        Combinacion limpia = new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 9),
                new Carta(Palo.DIAMANTES, 9),
                new Carta(Palo.PICAS, 9),
                new Carta(Palo.TREBOLES, 9),
                new Carta(Palo.CORAZONES, 9),
                new Carta(Palo.DIAMANTES, 9),
                new Carta(Palo.PICAS, 9)
        ), false);
        assertTrue(limpia.esCanasta());
        assertTrue(limpia.esLimpia());
        assertEquals(7 * 10 + 500, limpia.calcularPuntaje());

        Combinacion sucia = new Combinacion(List.of(
                new Carta(Palo.CORAZONES, 9),
                new Carta(Palo.DIAMANTES, 9),
                new Carta(Palo.PICAS, 9),
                new Carta(Palo.TREBOLES, 9),
                new Carta(Palo.CORAZONES, 9),
                new Carta(Palo.DIAMANTES, 2),
                new Carta(Palo.PICAS, 2)
        ), false);
        assertTrue(sucia.esCanasta());
        assertTrue(sucia.esSucia());
        assertEquals(5 * 10 + 2 * 20 + 300, sucia.calcularPuntaje());
    }

    @Test
    void pozoConTresNegroTaponaYConComodinCruza() {
        Pozo pozo = new Pozo();
        pozo.descartar(new Carta(Palo.CORAZONES, 6));
        assertFalse(pozo.estaTapado());
        assertFalse(pozo.estaCruzado());

        pozo.descartar(new Carta(Palo.TREBOLES, 3));
        assertTrue(pozo.estaTapado());

        pozo.descartar(new Carta(Palo.JOKER, 0));
        assertTrue(pozo.estaCruzado());

        List<Carta> tomadas = pozo.tomarTodo();
        assertEquals(3, tomadas.size());
        assertTrue(pozo.estaVacio());
        assertFalse(pozo.estaCruzado());
    }
}