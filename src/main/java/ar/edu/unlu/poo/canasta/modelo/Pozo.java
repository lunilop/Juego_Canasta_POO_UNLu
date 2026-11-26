package ar.edu.unlu.poo.canasta.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Pozo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Stack<Carta> cartas;
    private boolean cruzado;

    public Pozo() {
        this.cartas = new Stack<>();
        this.cruzado = false;
    }

    public void agregarCartaInicialOculta(Carta carta) {
        if (carta != null) {
            cartas.push(carta);
        }
    }

    public void descartar(Carta carta) {
        if (carta == null) {
            throw new IllegalArgumentException("No se puede descartar una carta nula.");
        }
        if (carta.esComodin()) {
            this.cruzado = true;
        }
        cartas.push(carta);
    }

    public Carta verTope() {
        return cartas.isEmpty() ? null : cartas.peek();
    }

    public boolean estaVacio() {
        return cartas.isEmpty();
    }

    public boolean estaTapado() {
        if (cartas.isEmpty()) {
            return false;
        }
        Carta tope = cartas.peek();
        return tope.esTresNegro();
    }

    public boolean estaCruzado() {
        return cruzado;
    }

    public int cantidadCartas() {
        return cartas.size();
    }

    public List<Carta> tomarTodo() {
        List<Carta> todas = new ArrayList<>(cartas);
        cartas.clear();
        this.cruzado = false;
        return todas;
    }
}