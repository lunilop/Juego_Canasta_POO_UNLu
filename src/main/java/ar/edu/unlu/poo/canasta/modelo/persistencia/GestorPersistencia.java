package ar.edu.unlu.poo.canasta.modelo.persistencia;

import ar.edu.unlu.poo.canasta.modelo.Partida;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GestorPersistencia {
    private static final String ARCHIVO_PARTIDA = "partida_guardada.dat";

    public static void guardarPartida(Partida partida) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_PARTIDA))) {
            oos.writeObject(partida);
        } catch (IOException e) {
            System.err.println("Error al guardar la partida: " + e.getMessage());
        }
    }

    public static Partida cargarPartida() {
        File archivo = new File(ARCHIVO_PARTIDA);
        if (!archivo.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (Partida) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la partida: " + e.getMessage());
            return null;
        }
    }

    public static void eliminarPartidaGuardada() {
        File archivo = new File(ARCHIVO_PARTIDA);
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    public static boolean existePartidaGuardada() {
        return new File(ARCHIVO_PARTIDA).exists();
    }
}