package ar.edu.unlu.poo.canasta.modelo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MazoTest {

    @Test
    void deberiaUsarCuatroBarajasParaDosYCuatroJugadores() {
        // 4 barajas * 54 cartas (52 + 2 jokers) = 216 cartas
        Mazo mazo2 = new Mazo(2);
        Mazo mazo4 = new Mazo(4);

        assertEquals(216, mazo2.cantidadRestante());
        assertEquals(216, mazo4.cantidadRestante());
    }

    @Test
    void deberiaUsarOchoBarajasParaSeisJugadores() {
        // 8 barajas * 54 cartas = 432 cartas
        Mazo mazo6 = new Mazo(6);
        assertEquals(432, mazo6.cantidadRestante());
    }

    @Test
    void deberiaRobarCartasYDetectarVacio() {
        Mazo mazo = new Mazo(2);
        int inicial = mazo.cantidadRestante();

        Carta robada = mazo.robar();
        assertNotNull(robada);
        assertEquals(inicial - 1, mazo.cantidadRestante());

        while (!mazo.estaVacio()) {
            mazo.robar();
        }
        assertNull(mazo.robar());
        assertEquals(0, mazo.cantidadRestante());
    }
}