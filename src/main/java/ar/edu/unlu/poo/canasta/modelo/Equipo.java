package ar.edu.unlu.poo.canasta.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Equipo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final List<Jugador> integrantes;
    private final List<Combinacion> juegosEnMesa;
    private final List<Carta> tresRojos;
    private int puntajeTotal;
    private boolean abrioEnManoActual;
    private int turnoCanastaLimpia;
    private int puntosBajadosEnTurnoActual;

    public Equipo(String nombre) {
        this.nombre = nombre;
        this.integrantes = new ArrayList<>();
        this.juegosEnMesa = new ArrayList<>();
        this.tresRojos = new ArrayList<>();
        this.puntajeTotal = 0;
        this.abrioEnManoActual = false;
        this.turnoCanastaLimpia = -1;
        this.puntosBajadosEnTurnoActual = 0;
    }

    public void agregarIntegrante(Jugador j) {
        if (j != null) {
            integrantes.add(j);
        }
    }

    public void reiniciarParaNuevaMano() {
        juegosEnMesa.clear();
        tresRojos.clear();
        abrioEnManoActual = false;
        turnoCanastaLimpia = -1;
        puntosBajadosEnTurnoActual = 0;
    }

    public int getPuntosMinimosParaAbrir() {
        if (puntajeTotal < 0) {
            return 15;
        }
        if (puntajeTotal <= 1495) {
            return 50;
        }
        if (puntajeTotal <= 2995) {
            return 90;
        }
        if (puntajeTotal <= 4995) {
            return 120;
        }
        if (puntajeTotal <= 7495) {
            return 150;
        }
        return 180;
    }

    public void agregarTresRojo(Carta carta) {
        if (carta == null || !carta.esTresRojo()) {
            throw new IllegalArgumentException("Solo se admiten tres rojos.");
        }
        tresRojos.add(carta);
    }

    public void bajarCombinacion(Combinacion combinacion, int turnoActual) {
        juegosEnMesa.add(combinacion);
        if (!abrioEnManoActual) {
            registrarPuntosTurno(combinacion.calcularPuntaje());
        }
        actualizarEstadoCanastaLimpia(turnoActual);
    }

    public void agregarCartasACombinacion(int indiceJuego, List<Carta> cartas, int turnoActual) {
        if (indiceJuego < 0 || indiceJuego >= juegosEnMesa.size()) {
            throw new IllegalArgumentException("Índice de combinación inválido.");
        }
        juegosEnMesa.get(indiceJuego).agregarCartas(cartas);
        actualizarEstadoCanastaLimpia(turnoActual);
    }

    public List<Combinacion> clonarJuegosEnMesa() {
        List<Combinacion> copia = new ArrayList<>();
        for (Combinacion c : juegosEnMesa) {
            copia.add(new Combinacion(new ArrayList<>(c.getCartas()), c.isEsOculta()));
        }
        return copia;
    }

    public void restaurarMesa(List<Combinacion> backup, boolean abrioAntes, int turnoLimpiaAntes) {
        this.juegosEnMesa.clear();
        this.juegosEnMesa.addAll(backup);
        this.abrioEnManoActual = abrioAntes;
        this.turnoCanastaLimpia = turnoLimpiaAntes;
        this.puntosBajadosEnTurnoActual = 0;
    }

    private void actualizarEstadoCanastaLimpia(int turnoActual) {
        if (turnoCanastaLimpia == -1 && tieneCanastaLimpia()) {
            turnoCanastaLimpia = turnoActual;
        }
    }

    public int contarCanastas() {
        int total = 0;
        for (Combinacion c : juegosEnMesa) {
            if (c.esCanasta()) {
                total++;
            }
        }
        return total;
    }

    public boolean tieneCanasta() {
        return contarCanastas() > 0;
    }

    public boolean tieneCanastaLimpia() {
        for (Combinacion c : juegosEnMesa) {
            if (c.esLimpia()) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneCanastaSucia() {
        for (Combinacion c : juegosEnMesa) {
            if (c.esSucia()) {
                return true;
            }
        }
        return false;
    }

    public void registrarPuntosApertura(int puntos) {
        if (!abrioEnManoActual) {
            this.puntosBajadosEnTurnoActual += puntos;
        }
    }

    public boolean puedeCerrar(int turnoActual) {
        return tieneCanastaLimpia() && tieneCanastaSucia()
                && turnoCanastaLimpia != -1 && turnoActual > turnoCanastaLimpia;
    }

    private int calcularBonusCantidadCanastas(int totalCanastas) {
        if (totalCanastas >= 7) {
            return 2000;
        }
        if (totalCanastas >= 5) {
            return 500;
        }
        return 0;
    }

    private int calcularPuntosTresRojos(boolean tieneCanasta) {
        int cant = tresRojos.size();
        if (cant == 0) {
            return 0;
        }
        int puntos = (cant == 4) ? 800 : (cant * 100);
        return tieneCanasta ? puntos : -puntos;
    }

    public int getPuntosParcialesEnMesa() {
        int puntosMesa = 0;
        for (Combinacion c : juegosEnMesa) {
            puntosMesa += c.calcularPuntaje();
        }
        int bonusCantidad = calcularBonusCantidadCanastas(contarCanastas());
        return puntosMesa + bonusCantidad;
    }

    public int calcularPuntajeMano(boolean equipoCerro) {
        boolean hizoCanasta = tieneCanasta();
        int puntosMesa = 0;
        for (Combinacion c : juegosEnMesa) {
            puntosMesa += c.calcularPuntaje();
        }
        if (abrioEnManoActual && !hizoCanasta) {
            puntosMesa = -puntosMesa;
        }

        int bonusCantidad = calcularBonusCantidadCanastas(contarCanastas());
        int puntosRojos = calcularPuntosTresRojos(hizoCanasta);

        int puntosEnContraMano = 0;
        for (Jugador j : integrantes) {
            for (Carta c : j.getMano()) {
                if (c.esTresNegro()) {
                    puntosEnContraMano += 100;
                } else {
                    puntosEnContraMano += c.getPuntaje();
                }
            }
        }

        int bonusCierre = equipoCerro ? 200 : 0;
        int subtotal = puntosMesa + bonusCantidad + puntosRojos + bonusCierre - puntosEnContraMano;
        this.puntajeTotal += subtotal;
        return subtotal;
    }

    public void iniciarTurnoEquipo() {
        this.puntosBajadosEnTurnoActual = 0;
    }

    public int getPuntosBajadosEnTurnoActual() {
        return puntosBajadosEnTurnoActual;
    }

    public void registrarPuntosTurno(int puntos) {
        this.puntosBajadosEnTurnoActual += puntos;
    }

    public void confirmarAperturaSiCorresponde() {
        if (!abrioEnManoActual) {
            if (puntosBajadosEnTurnoActual > 0) {
                int minimo = getPuntosMinimosParaAbrir();
                if (puntosBajadosEnTurnoActual < minimo) {
                    throw new IllegalStateException("Bajaste " + puntosBajadosEnTurnoActual
                            + " pts, pero necesitás " + minimo
                            + " pts para abrir. Bajá más cartas o deshacé las jugadas del turno.");
                }
                this.abrioEnManoActual = true;
                this.puntosBajadosEnTurnoActual = 0;
            } else if (!juegosEnMesa.isEmpty()) {
                this.abrioEnManoActual = true;
            }
        }
    }

    public String getNombre() {
        return nombre;
    }

    public List<Jugador> getIntegrantes() {
        return Collections.unmodifiableList(integrantes);
    }

    public List<Combinacion> getJuegosEnMesa() {
        return Collections.unmodifiableList(juegosEnMesa);
    }

    public List<Carta> getTresRojos() {
        return Collections.unmodifiableList(tresRojos);
    }

    public int getPuntajeTotal() {
        return puntajeTotal;
    }

    public boolean isAbrioEnManoActual() {
        return abrioEnManoActual;
    }

    public int getTurnoCanastaLimpia() {
        return turnoCanastaLimpia;
    }
}