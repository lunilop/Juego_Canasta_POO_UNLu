package ar.edu.unlu.poo.canasta.cliente;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Ayuda para elegir el puerto donde escucha cada cliente RMI.
 */
public final class Puertos {
    private Puertos() {
    }

    public static int primerLibre(int desde) {
        for (int puerto = desde; puerto <= 65535; puerto++) {
            try (ServerSocket prueba = new ServerSocket(puerto)) {
                return puerto;
            } catch (IOException ocupado) {
                // Probamos con el siguiente puerto
            }
        }
        throw new IllegalStateException("No hay puertos libres desde el " + desde);
    }
}