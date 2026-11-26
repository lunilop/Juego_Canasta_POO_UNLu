package ar.edu.unlu.poo.canasta.modelo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mazo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Carta> cartas;

    public Mazo(int cantidadJugadores) {
        this.cartas = new ArrayList<>();
        int cantidadBarajas = (cantidadJugadores == 6) ? 8 : 4;
        inicializar(cantidadBarajas); mezclar();
    }

    private void inicializar(int cantidadBarajas) {
        for (int b = 0; b < cantidadBarajas; b++) {
            for (Palo palo : Palo.values()) {
                if (palo == Palo.JOKER) {
                    cartas.add(new Carta(Palo.JOKER, 0));
                    cartas.add(new Carta(Palo.JOKER, 0)); // 2 jokers por baraja
                } else {
                    for (int num = 1; num <= 13; num++) {
                        cartas.add(new Carta(palo, num));
                    }
                }
            }
        }
    }

    public void mezclar() {
        Collections.shuffle(cartas);
    }

    public Carta robar() {
        if (cartas.isEmpty()) {return null;}
        return cartas.remove(cartas.size() - 1);
    }

    public boolean estaVacio() {
        return cartas.isEmpty();
    }

    public int cantidadRestante() {
        return cartas.size();}
}