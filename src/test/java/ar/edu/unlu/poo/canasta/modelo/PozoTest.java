package ar.edu.unlu.poo.canasta.modelo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class PozoTest {

    @Test
    void deberiaTaparConTresNegroYCruzarConComodin() {
        Pozo pozo = new Pozo();
        assertTrue(pozo.estaVacio());
        assertFalse(pozo.estaTapado());
        assertFalse(pozo.estaCruzado());

        pozo.descartar(new Carta(Palo.PICAS, 3));
        assertTrue(pozo.estaTapado());
        assertFalse(pozo.estaCruzado());

        pozo.descartar(new Carta(Palo.JOKER, 0));
        assertTrue(pozo.estaCruzado());
        assertEquals(new Carta(Palo.JOKER, 0), pozo.verTope());

        List<Carta> tomadas = pozo.tomarTodo();
        assertEquals(2, tomadas.size());
        assertTrue(pozo.estaVacio());
        assertFalse(pozo.estaCruzado());
    }
}