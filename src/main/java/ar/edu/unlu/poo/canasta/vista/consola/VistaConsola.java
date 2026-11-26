package ar.edu.unlu.poo.canasta.vista.consola;

import ar.edu.unlu.poo.canasta.controlador.Controlador;
import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Combinacion;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import ar.edu.unlu.poo.canasta.vista.IVista;
import java.util.ArrayList;
import java.util.List;

public class VistaConsola implements IVista {
    private static final String LINEA_DOBLE = "════════════════════════════════════════════════════";
    private static final String LINEA_SIMPLE = "────────────────────────────────────────────────────";

    private enum PasoEntrada {
        MENU_SALA,
        MENU_ROBO,
        POZO_INDICES,
        MENU_ACCION,
        BAJAR_INDICES,
        BAJAR_OCULTA,
        AGREGAR_JUEGO,
        AGREGAR_INDICES,
        DESCARTAR_INDICE,
        CERRAR_INDICE
    }

    private final VentanaConsola ventana;
    private Controlador controlador;
    private PasoEntrada pasoActual = PasoEntrada.MENU_SALA;
    private String ultimoMensajeMostrado = "";
    private String ultimaSalaMostrada = "";
    private List<Integer> indicesTemporales = new ArrayList<>();
    private int indiceJuegoTemporal = -1;

    public VistaConsola(VentanaConsola ventana) {
        this.ventana = ventana;
        this.ventana.setManejadorEntrada(this::procesarLinea);
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    private void imprimir(String texto) {
        ventana.escribir(texto);
    }

    private void imprimirLinea(String texto) {
        ventana.escribir(texto + "\n");
    }

    @Override
    public void mostrarSala(List<Jugador> jugadores, Jugador admin, Jugador yo) {
        pasoActual = PasoEntrada.MENU_SALA;
        String resumenSala = jugadores.stream().map(Jugador::getNombre).toList().toString();
        if (resumenSala.equals(ultimaSalaMostrada)) {
            return;
        }
        ultimaSalaMostrada = resumenSala;

        imprimirLinea("\n" + LINEA_DOBLE);
        imprimirLinea("  SALA DE ESPERA");
        imprimirLinea(LINEA_SIMPLE);
        imprimirLinea("  Jugadores conectados: " + resumenSala);
        imprimirLinea("  1) Iniciar partida a 5.000 pts");
        imprimirLinea("  2) Iniciar partida a 7.000 pts");
        imprimirLinea("  3) Iniciar partida a 15.000 pts");
        imprimirLinea("  4) Ver ranking");
        imprimirLinea("  0) Salir");
        imprimirLinea(LINEA_DOBLE);
        imprimir("  Elegí una opción: ");
    }

    @Override
    public void mostrarJuego() {
        ultimaSalaMostrada = "";
        imprimirLinea("\n" + LINEA_DOBLE);
        imprimirLinea("  ¡La partida comenzó!");
        imprimirLinea(LINEA_DOBLE);
    }

    @Override
    public void mostrarMesa(
            EstadoTurno estado,
            Jugador deTurno,
            Jugador yo,
            List<Equipo> equipos,
            int cartasMazo,
            int cartasPozo,
            Carta topePozo,
            boolean pozoTapado,
            boolean pozoCruzado,
            String ultimoMensaje
    ) {
        if (ultimoMensaje != null && !ultimoMensaje.isBlank()
                && !ultimoMensaje.equals(ultimoMensajeMostrado)) {
            imprimirLinea("\n  [Acción] " + ultimoMensaje);
            ultimoMensajeMostrado = ultimoMensaje;
        }

        if (deTurno == null || yo == null) return;

        boolean esMiTurno = deTurno.getNombre().equalsIgnoreCase(yo.getNombre());
        if (!esMiTurno) {
            imprimirLinea("  Esperando a que juegue " + deTurno.getNombre() + "...");
            return;
        }

        imprimirMesa(deTurno, yo, equipos, cartasMazo, cartasPozo, topePozo, pozoTapado, pozoCruzado);

        if (estado == EstadoTurno.ESPERANDO_ROBO) {
            pasoActual = PasoEntrada.MENU_ROBO;
            //imprimirLinea("\n  -- ¿Cómo querés robar? --");
            imprimirLinea("  1) Robar 2 cartas del mazo");
            imprimirLinea("  2) Tomar el pozo");
            imprimir("  Opción: ");
        } else if (estado == EstadoTurno.ESPERANDO_ACCION) {
            pasoActual = PasoEntrada.MENU_ACCION;
            imprimirLinea("\n  -- ¿Qué querés hacer en tu turno? --");
            imprimirLinea("  1) Bajar combinación nueva");
            imprimirLinea("  2) Agregar cartas a un juego de la mesa");
            imprimirLinea("  3) Descartar una carta y pasar turno");
            imprimirLinea("  4) Cerrar mano");
            imprimirLinea("  5) Deshacer bajadas de este turno");
            imprimir("  Opción: ");
        }
    }

    private void procesarLinea(String raw) {
        if (controlador == null) return;
        String linea = raw == null ? "" : raw.trim();
        imprimirLinea("  > " + linea);

        EstadoTurno estado = controlador.getEstadoTurno();
        try {
            if (estado == EstadoTurno.ESPERANDO_JUGADORES) {
                switch (linea) {
                    case "1" -> controlador.iniciarPartida(5000);
                    case "2" -> controlador.iniciarPartida(7000);
                    case "3" -> controlador.iniciarPartida(15000);
                    case "4" -> {
                        imprimirRanking(controlador.getRanking());
                        imprimir("  Elegí una opción: ");
                    }
                    case "0" -> {
                        controlador.salir();
                        ventana.dispose();
                    }
                    default -> {
                        imprimirLinea("  Opción no válida.");
                        imprimir("  Elegí una opción: ");
                    }
                }
                return;
            }

            Jugador actual = controlador.getJugadorActual();
            String yo = controlador.getNombreLocal();
            if (actual == null || yo == null || !actual.getNombre().equalsIgnoreCase(yo)) {
                imprimirLinea("  Todavía no es tu turno.");
                return;
            }

            switch (pasoActual) {
                case MENU_ROBO -> {
                    if ("1".equals(linea)) {
                        controlador.robarDelMazo();
                    } else if ("2".equals(linea)) {
                        pasoActual = PasoEntrada.POZO_INDICES;
                        imprimir("  Índices de tu mano para combinar con el tope (o Enter si ya tenés el juego en mesa): ");
                    } else {
                        imprimirLinea("  Opción no válida.");
                        imprimir("  Opción: ");
                    }
                }
                case POZO_INDICES -> {
                    pasoActual = PasoEntrada.MENU_ROBO;
                    controlador.tomarPozo(parsearIndices(linea), null);
                }
                case MENU_ACCION -> {
                    switch (linea) {
                        case "1" -> {
                            pasoActual = PasoEntrada.BAJAR_INDICES;
                            imprimir("  Índices de cartas a bajar (ej: 0 1 2): ");
                        }
                        case "2" -> {
                            pasoActual = PasoEntrada.AGREGAR_JUEGO;
                            imprimir("  Número de juego en la mesa (0, 1, ...): ");
                        }
                        case "3" -> {
                            pasoActual = PasoEntrada.DESCARTAR_INDICE;
                            imprimir("  Índice de la carta a descartar: ");
                        }
                        case "4" -> {
                            pasoActual = PasoEntrada.CERRAR_INDICE;
                            imprimir("  Índice de la última carta para cerrar: ");
                        }
                        case "5" -> controlador.deshacerJugadasTurno();
                        default -> {
                            imprimirLinea("  Opción no válida.");
                            imprimir("  Opción: ");
                        }
                    }
                }
                case BAJAR_INDICES -> {
                    indicesTemporales = parsearIndices(linea);
                    pasoActual = PasoEntrada.BAJAR_OCULTA;
                    imprimir("  ¿Es canasta oculta? (s/n): ");
                }
                case BAJAR_OCULTA -> {
                    pasoActual = PasoEntrada.MENU_ACCION;
                    controlador.bajarCombinacion(indicesTemporales, linea.equalsIgnoreCase("s"));
                }
                case AGREGAR_JUEGO -> {
                    indiceJuegoTemporal = Integer.parseInt(linea);
                    pasoActual = PasoEntrada.AGREGAR_INDICES;
                    imprimir("  Índices de cartas de tu mano: ");
                }
                case AGREGAR_INDICES -> {
                    pasoActual = PasoEntrada.MENU_ACCION;
                    controlador.agregarACombinacion(indiceJuegoTemporal, parsearIndices(linea));
                }
                case DESCARTAR_INDICE -> {
                    pasoActual = PasoEntrada.MENU_ACCION;
                    controlador.descartar(Integer.parseInt(linea));
                }
                case CERRAR_INDICE -> {
                    pasoActual = PasoEntrada.MENU_ACCION;
                    controlador.cerrarMano(Integer.parseInt(linea));
                }
                default -> {
                }
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingresá números válidos.");
        }
    }

    private void imprimirMesa(
            Jugador actual,
            Jugador yo,
            List<Equipo> equipos,
            int cartasMazo,
            int cartasPozo,
            Carta topePozo,
            boolean pozoTapado,
            boolean pozoCruzado
    ) {
        imprimirLinea("\n" + LINEA_DOBLE);
        imprimirLinea("  TURNO DE: " + actual.getNombre());
        imprimirLinea("  Mazo: " + cartasMazo + " cartas  |  Pozo: " + cartasPozo + " cartas"
                + "  |  Tope: " + (topePozo != null ? topePozo : "Vacío")
                + (pozoTapado ? " [TAPADO]" : "")
                + (pozoCruzado ? " [CRUZADO]" : ""));
        imprimirLinea(LINEA_SIMPLE);

        for (int e = 0; e < equipos.size(); e++) {
            Equipo eq = equipos.get(e);
            if (e > 0) {
                imprimirLinea("");
            }
            imprimirLinea("  Equipo " + eq.getNombre()
                    + "  |  Total: " + eq.getPuntajeTotal()
                    + " pts  |  En mesa: " + eq.getPuntosParcialesEnMesa()
                    + " pts  |  Apertura mín: " + eq.getPuntosMinimosParaAbrir() + " pts");
            List<Combinacion> juegos = eq.getJuegosEnMesa();
            if (juegos.isEmpty()) {
                imprimirLinea("    (Sin juegos bajados aún)");
            } else {
                for (int i = 0; i < juegos.size(); i++) {
                    imprimirLinea("    Juego [" + i + "] -> " + juegos.get(i));
                }
            }
        }

        imprimirLinea(LINEA_SIMPLE);
        imprimirLinea("  Tu mano (" + yo.getNombre() + "):");
        imprimirManoProlija(yo.getMano());
        imprimirLinea(LINEA_DOBLE);
    }

    private void imprimirManoProlija(List<Carta> mano) {
        if (mano == null || mano.isEmpty()) {
            imprimirLinea("    (Sin cartas)");
            return;
        }
        int porLinea = 5;
        for (int i = 0; i < mano.size(); i += porLinea) {
            StringBuilder fila = new StringBuilder("    ");
            int fin = Math.min(i + porLinea, mano.size());
            for (int j = i; j < fin; j++) {
                fila.append(String.format("#%-2d %-10s", j, mano.get(j)));
            }
            imprimirLinea(fila.toString());
        }
    }

    private List<Integer> parsearIndices(String linea) {
        List<Integer> res = new ArrayList<>();
        if (linea.isEmpty()) return res;
        for (String parte : linea.split("\\s+")) {
            res.add(Integer.parseInt(parte));
        }
        return res;
    }

    private void imprimirRanking(List<RegistroRanking> ranking) {
        imprimirLinea("\n" + LINEA_SIMPLE);
        imprimirLinea("  RANKING");
        if (ranking == null || ranking.isEmpty()) {
            imprimirLinea("  Sin registros todavía.");
        } else {
            for (int i = 0; i < ranking.size(); i++) {
                imprimirLinea("  " + (i + 1) + ". " + ranking.get(i));
            }
        }
        imprimirLinea(LINEA_SIMPLE);
    }

    @Override
    public void mostrarResultado(String mensajeFinal, List<RegistroRanking> ranking) {
        imprimirLinea("\n" + LINEA_DOBLE);
        imprimirLinea("  FIN DE LA PARTIDA");
        imprimirLinea("  " + mensajeFinal);
        imprimirRanking(ranking);
        imprimirLinea(LINEA_DOBLE);
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        if (mensaje != null && !mensaje.isBlank() && !mensaje.equals(ultimoMensajeMostrado)) {
            imprimirLinea("  [Aviso] " + mensaje);
            ultimoMensajeMostrado = mensaje;
        }
    }

    @Override
    public void mostrarError(String mensaje) {
        imprimirLinea("  [!] Error: " + mensaje);
        imprimir("  Intentá de nuevo: ");
    }
}