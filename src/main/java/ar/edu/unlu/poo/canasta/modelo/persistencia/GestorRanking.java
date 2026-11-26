package ar.edu.unlu.poo.canasta.modelo.persistencia;

import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GestorRanking {
    private static final String ARCHIVO_RANKING = "ranking.dat";
    private static final int MAX_TOP = 5;

    public static void registrarGanadores(Equipo equipoGanador) {
        if (equipoGanador == null) {
            return;
        }
        List<RegistroRanking> ranking = obtenerRanking();
        for (Jugador j : equipoGanador.getIntegrantes()) {
            RegistroRanking existente = null;
            for (RegistroRanking r : ranking) {
                if (r.getNombre().equalsIgnoreCase(j.getNombre())) {
                    existente = r;
                    break;
                }
            }
            if (existente != null) {
                existente.actualizarVictoria(equipoGanador.getPuntajeTotal());
            } else {
                ranking.add(new RegistroRanking(j.getNombre(), equipoGanador.getPuntajeTotal()));
            }
        }
        Collections.sort(ranking);
        if (ranking.size() > MAX_TOP) {
            ranking = new ArrayList<>(ranking.subList(0, MAX_TOP));
        }
        guardarRanking(ranking);
    }

    @SuppressWarnings("unchecked")
    public static List<RegistroRanking> obtenerRanking() {
        File archivo = new File(ARCHIVO_RANKING);
        if (!archivo.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (List<RegistroRanking>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private static void guardarRanking(List<RegistroRanking> ranking) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_RANKING))) {
            oos.writeObject(ranking);
        } catch (IOException e) {
            System.err.println("Error al guardar ranking: " + e.getMessage());
        }
    }
}