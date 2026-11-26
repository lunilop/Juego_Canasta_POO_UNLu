package ar.edu.unlu.poo.canasta.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Combinacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int numeroBase;
    private final List<Carta> cartas;
    private final boolean esOculta;

    public Combinacion(List<Carta> cartasIniciales, boolean esOculta) {
        if (cartasIniciales == null || cartasIniciales.size() < 3) {
            throw new IllegalArgumentException("Una combinación debe tener al menos 3 cartas.");
        }
        this.numeroBase = extraerNumeroBase(cartasIniciales);
        validarReglasGrupo(cartasIniciales, this.numeroBase);
        this.cartas = new ArrayList<>(cartasIniciales);
        this.esOculta = esOculta;
    }

    private int extraerNumeroBase(List<Carta> lista) {
        for (Carta c : lista) {
            if (!c.esComodin()) {
                if (c.getNumero() == 3) {
                    throw new IllegalArgumentException("No se pueden bajar combinaciones de tres.");
                }
                return c.getNumero();
            }
        }
        throw new IllegalArgumentException("La combinación debe contener al menos una carta natural.");
    }

    private void validarReglasGrupo(List<Carta> lista, int numEsperado) {
        int naturales = 0;
        int comodines = 0;

        for (Carta c : lista) {
            if (c.esComodin()) {
                comodines++;
            } else {
                if (c.getNumero() != numEsperado) {
                    throw new IllegalArgumentException("Todas las cartas naturales deben ser del mismo número.");
                }
                naturales++;
            }
        }

        if (naturales < 2) {
            throw new IllegalArgumentException("Debe haber al menos 2 cartas naturales.");
        }
        if (comodines > naturales) {
            throw new IllegalArgumentException("No puede haber más comodines que cartas naturales.");
        }
    }

    public void agregarCartas(List<Carta> nuevas) {
        if (nuevas == null || nuevas.isEmpty()) {
            throw new IllegalArgumentException("Debés seleccionar al menos una carta para agregar.");
        }
        List<Carta> simulacion = new ArrayList<>(this.cartas);
        simulacion.addAll(nuevas);
        validarReglasGrupo(simulacion, this.numeroBase);
        this.cartas.addAll(nuevas);
    }

    public boolean esCanasta() {
        return cartas.size() >= 7;
    }

    public boolean esLimpia() {
        if (!esCanasta()) {
            return false;
        }
        for (Carta c : cartas) {
            if (c.esComodin()) {
                return false;
            }
        }
        return true;
    }

    public boolean esSucia() {
        return esCanasta() && !esLimpia();
    }

    public int sumarPuntosCartas() {
        int total = 0;
        for (Carta c : cartas) {
            total += c.getPuntaje();
        }
        return total;
    }

    public int calcularPuntaje() {
        int puntosCartas = sumarPuntosCartas();
        int bonusCanasta = 0;

        if (esCanasta()) {
            if (esOculta) {
                bonusCanasta = 1000;
            } else if (esLimpia()) {
                bonusCanasta = 500;
            } else {
                bonusCanasta = 300;
            }
        }
        return puntosCartas + bonusCanasta;
    }

    public int getNumeroBase() {
        return numeroBase;
    }

    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }

    public boolean isEsOculta() {
        return esOculta;
    }

    @Override
    public String toString() {
        String tipo = esCanasta()
                ? (esOculta ? "CANASTA OCULTA" : (esLimpia() ? "CANASTA LIMPIA" : "CANASTA SUCIA"))
                : "Juego";
        return tipo + " (" + Carta.formatoNumero(numeroBase) + "): " + cartas;
    }
}