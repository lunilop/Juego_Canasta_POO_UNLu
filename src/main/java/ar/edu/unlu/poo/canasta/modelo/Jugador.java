package ar.edu.unlu.poo.canasta.modelo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Jugador implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final List<Carta> mano;
    private Equipo equipo;

    public Jugador(String nombre) {
        this.nombre = nombre; this.mano = new ArrayList<>();
    }

    public void recibirCarta(Carta carta) {
        if (carta != null) {
            mano.add(carta);
            ordenarMano();
        }
    }

    public void recibirCartas(List<Carta> nuevas) {
        mano.addAll(nuevas);
        ordenarMano();
    }

    public Carta quitarCartaPorIndice(int indice) {
        if (indice < 0 || indice >= mano.size()) {
            throw new IllegalArgumentException("Índice de carta fuera de rango.");
        }
        return mano.remove(indice);
    }

    public List<Carta> obtenerCartasPorIndices(List<Integer> indices) {
        List<Carta> seleccionadas = new ArrayList<>();
        for (int idx : indices) {
            if (idx < 0 || idx >= mano.size()) {
                throw new IllegalArgumentException("Índice inválido: " + idx);
            }
            seleccionadas.add(mano.get(idx));
        }
        return seleccionadas;
    }

    public void quitarCartas(List<Carta> aQuitar) {
        for (Carta c : aQuitar) {
            mano.remove(c);
        }
    }

    public void vaciarMano() {
        mano.clear();
    }

    public void ordenarMano() {
        mano.sort(Comparator.comparingInt(Carta::getNumero).thenComparing(Carta::getPalo));
    }

    public String getNombre() {
        return nombre;
    }

    public List<Carta> getMano() {
        return Collections.unmodifiableList(mano);
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }
}