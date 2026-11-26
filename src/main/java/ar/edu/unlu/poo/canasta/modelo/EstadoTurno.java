package ar.edu.unlu.poo.canasta.modelo;

public enum EstadoTurno {
    ESPERANDO_JUGADORES,
    ESPERANDO_ROBO,      // debe robar 2 del mazo o tomar el pozo
    ESPERANDO_ACCION,    // puede bajar combinaciones, agregar a mesa o descartar
    FIN_MANO,
    FIN_PARTIDA
}