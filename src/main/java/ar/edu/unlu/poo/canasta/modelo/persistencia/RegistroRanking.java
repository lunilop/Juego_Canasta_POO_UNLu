package ar.edu.unlu.poo.canasta.modelo.persistencia;

import java.io.Serializable;

public class RegistroRanking implements Serializable, Comparable<RegistroRanking> {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private int puntajeMaximo;
    private int partidasGanadas;

    public RegistroRanking(String nombre, int puntaje) {
        this.nombre = nombre;
        this.puntajeMaximo = puntaje;
        this.partidasGanadas = 1;
    }

    public void actualizarVictoria(int nuevoPuntaje) {
        this.partidasGanadas++;
        if (nuevoPuntaje > this.puntajeMaximo) {
            this.puntajeMaximo = nuevoPuntaje;
        }
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    @Override
    public int compareTo(RegistroRanking otro) {
        return Integer.compare(otro.puntajeMaximo, this.puntajeMaximo);
    }

    @Override
    public String toString() {
        return String.format("%-15s | Puntaje Máx: %6d | Ganadas: %d", nombre, puntajeMaximo, partidasGanadas);
    }
}