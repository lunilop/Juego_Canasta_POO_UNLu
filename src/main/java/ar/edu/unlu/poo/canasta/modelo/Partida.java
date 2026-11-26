package ar.edu.unlu.poo.canasta.modelo;

import ar.edu.unlu.poo.canasta.modelo.persistencia.GestorPersistencia;
import ar.edu.unlu.poo.canasta.modelo.persistencia.GestorRanking;
import ar.edu.unlu.rmimvc.observer.ObservableRemoto;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Partida extends ObservableRemoto implements IPartida, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Jugador> jugadores;
    private final List<Equipo> equipos;
    private Mazo mazo;
    private Pozo pozo;
    private int indiceTurnoActual;
    private int numeroTurnoGlobal;
    private int puntajeObjetivo;
    private EstadoTurno estadoTurno;
    private String ultimoMensaje;

    private int puntosBajadosEnTurnoActual;
    private List<Carta> backupManoTurno;
    private List<Combinacion> backupMesaTurno;
    private boolean backupAbrioAntes;
    private int backupTurnoLimpiaAntes;

    public Partida() {
        this.jugadores = new ArrayList<>();
        this.equipos = new ArrayList<>();
        this.estadoTurno = EstadoTurno.ESPERANDO_JUGADORES;
        this.ultimoMensaje = "Esperando jugadores...";
    }

    @Override
    public List<Jugador> getJugadores() {
        return jugadores;
    }

    @Override
    public void agregarJugador(String nombre) throws RemoteException {
        if (estadoTurno != EstadoTurno.ESPERANDO_JUGADORES) {
            throw new IllegalStateException("La partida ya comenzó.");
        }
        if (jugadores.size() >= 6) {
            throw new IllegalStateException("Máximo 6 jugadores.");
        }
        String limpio = (nombre == null) ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(limpio)) {
                throw new IllegalArgumentException("Ya existe un jugador con ese nombre.");
            }
        }
        jugadores.add(new Jugador(limpio));
        ultimoMensaje = limpio + " se unió a la sala.";
        notificarObservadores(Eventos.JUGADOR_UNIDO);
    }

    @Override
    public void iniciarPartida(int puntajeObjetivo) throws RemoteException {
        int cant = jugadores.size();
        if (cant != 2 && cant != 4 && cant != 6) {
            throw new IllegalStateException("Se requieren 2, 4 o 6 jugadores para iniciar.");
        }
        this.puntajeObjetivo = puntajeObjetivo;
        configurarEquipos();
        iniciarNuevaMano();
        ultimoMensaje = "¡Partida iniciada! Turno de " + getJugadorActual().getNombre();
        notificarObservadores(Eventos.PARTIDA_INICIADA);
    }

    private void configurarEquipos() {
        equipos.clear();
        if (jugadores.size() == 2) {
            Equipo eq1 = new Equipo(jugadores.get(0).getNombre());
            Equipo eq2 = new Equipo(jugadores.get(1).getNombre());
            eq1.agregarIntegrante(jugadores.get(0));
            jugadores.get(0).setEquipo(eq1);
            eq2.agregarIntegrante(jugadores.get(1));
            jugadores.get(1).setEquipo(eq2);
            equipos.add(eq1);
            equipos.add(eq2);
        } else {
            Equipo eqA = new Equipo("Equipo A");
            Equipo eqB = new Equipo("Equipo B");
            for (int i = 0; i < jugadores.size(); i++) {
                Equipo destino = (i % 2 == 0) ? eqA : eqB;
                destino.agregarIntegrante(jugadores.get(i));
                jugadores.get(i).setEquipo(destino);
            }
            equipos.add(eqA);
            equipos.add(eqB);
        }
    }

    private void iniciarNuevaMano() {
        this.mazo = new Mazo(jugadores.size());
        this.pozo = new Pozo();
        this.numeroTurnoGlobal = 1;
        this.indiceTurnoActual = 0;

        for (Equipo eq : equipos) {
            eq.reiniciarParaNuevaMano();
        }
        for (Jugador j : jugadores) {
            j.vaciarMano();
            for (int i = 0; i < 15; i++) {
                entregarCartaProcesandoTresRojo(j);
            }
        }
        for (int i = 0; i < 6; i++) {
            pozo.agregarCartaInicialOculta(mazo.robar());
        }
        Carta topeInicial = mazo.robar();
        while (topeInicial != null && (topeInicial.esTresRojo() || topeInicial.esComodin())) {
            pozo.agregarCartaInicialOculta(topeInicial);
            topeInicial = mazo.robar();
        }
        if (topeInicial != null) {
            pozo.descartar(topeInicial);
        }
        this.estadoTurno = EstadoTurno.ESPERANDO_ROBO;
    }

    private void entregarCartaProcesandoTresRojo(Jugador jugador) {
        Carta c = mazo.robar();
        while (c != null && c.esTresRojo()) {
            jugador.getEquipo().agregarTresRojo(c);
            c = mazo.robar();
        }
        if (c != null) {
            jugador.recibirCarta(c);
        }
    }

    private void guardarRespaldoTurno(Jugador actual) {
        Equipo eq = actual.getEquipo();
        this.puntosBajadosEnTurnoActual = 0;
        this.backupManoTurno = new ArrayList<>(actual.getMano());
        this.backupMesaTurno = eq.clonarJuegosEnMesa();
        this.backupAbrioAntes = eq.isAbrioEnManoActual();
        this.backupTurnoLimpiaAntes = eq.getTurnoCanastaLimpia();
    }

    private int sumarPuntosCartas(List<Carta> lista) {
        int suma = 0;
        for (Carta c : lista) {
            suma += c.getPuntaje();
        }
        return suma;
    }

    @Override
    public void robarDelMazo(String nombreJugador) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ROBO);
        Jugador actual = getJugadorActual();
        if (mazo.cantidadRestante() < 2) {
            finalizarManoPorMazoAgotado();
            return;
        }
        entregarCartaProcesandoTresRojo(actual);
        entregarCartaProcesandoTresRojo(actual);
        guardarRespaldoTurno(actual);
        this.estadoTurno = EstadoTurno.ESPERANDO_ACCION;
        this.ultimoMensaje = actual.getNombre() + " robó 2 cartas del mazo.";
        notificarObservadores(Eventos.CARTAS_ROBADAS);
    }

    @Override
    public void tomarPozo(String nombreJugador, List<Integer> indicesMano, Integer indiceJuegoMesa) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ROBO);
        if (pozo.estaVacio() || pozo.estaTapado()) {
            throw new IllegalStateException("El pozo está vacío o tapado con 3 negro; debés robar del mazo.");
        }
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();
        Carta tope = pozo.verTope();

        List<Carta> seleccionadas = (indicesMano == null || indicesMano.isEmpty())
                ? new ArrayList<>()
                : actual.obtenerCartasPorIndices(indicesMano);

        int naturalesEnMano = 0;
        int comodinesSeleccionados = 0;
        for (Carta c : seleccionadas) {
            if (c.esComodin()) {
                comodinesSeleccionados++;
            } else if (c.getNumero() == tope.getNumero()) {
                naturalesEnMano++;
            }
        }

        List<Combinacion> juegosMesa = equipo.getJuegosEnMesa();
        boolean yaEstabaAbierto = equipo.isAbrioEnManoActual() || !juegosMesa.isEmpty();
        if (!yaEstabaAbierto) {
            throw new IllegalStateException("Tu equipo primero debe abrirse en esta mano antes de poder levantar el pozo.");
        }

        Integer juegoDestino = indiceJuegoMesa;
        if (juegoDestino == null && !pozo.estaCruzado() && naturalesEnMano < 2) {
            for (int i = 0; i < juegosMesa.size(); i++) {
                if (juegosMesa.get(i).getNumeroBase() == tope.getNumero()) {
                    juegoDestino = i;
                    break;
                }
            }
        }

        String etiquetaTope = Carta.formatoNumero(tope.getNumero());
        if (pozo.estaCruzado()) {
            if (naturalesEnMano < 3) {
                throw new IllegalArgumentException(
                        "Pozo CRUZADO: necesitás 3 cartas naturales del número " + etiquetaTope + " en mano.");
            }
        } else if (juegoDestino == null) {
            boolean cumpleConComodin = naturalesEnMano >= 1 && comodinesSeleccionados >= 1;
            if (naturalesEnMano < 2 && !cumpleConComodin) {
                throw new IllegalArgumentException(
                        "Necesitás 2+ cartas del mismo número que " + etiquetaTope
                                + " en mano, o tener ya bajado un juego de " + etiquetaTope + " en tu mesa.");
            }
        }

        List<Carta> cartasPozo = pozo.tomarTodo();
        if (juegoDestino != null) {
            Carta topeExtraido = cartasPozo.remove(cartasPozo.size() - 1);
            List<Carta> grupoParaMesa = new ArrayList<>(seleccionadas);
            grupoParaMesa.add(topeExtraido);
            equipo.agregarCartasACombinacion(juegoDestino, grupoParaMesa, numeroTurnoGlobal);
            actual.quitarCartas(seleccionadas);
        }
        for (Carta c : cartasPozo) {
            if (c.esTresRojo()) {
                equipo.agregarTresRojo(c);
            } else {
                actual.recibirCarta(c);
            }
        }
        equipo.confirmarAperturaSiCorresponde();
        guardarRespaldoTurno(actual);
        this.estadoTurno = EstadoTurno.ESPERANDO_ACCION;
        this.ultimoMensaje = actual.getNombre() + " tomó el pozo completo (" + (cartasPozo.size() + 1) + " cartas).";
        notificarObservadores(Eventos.POZO_TOMADO);
    }

    @Override
    public void bajarCombinacion(String nombreJugador, List<Integer> indicesCartas, boolean esOculta) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ACCION);
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();

        List<Carta> seleccionadas = actual.obtenerCartasPorIndices(indicesCartas);
        Combinacion nueva = new Combinacion(seleccionadas, esOculta);

        int cartasRestantes = actual.getMano().size() - seleccionadas.size();
        if (cartasRestantes == 0) {
            throw new IllegalStateException("Siempre debés conservar 1 carta para descartar al finalizar el turno.");
        }
        boolean cumpliraCierre = equipo.tieneCanastaSucia()
                && (equipo.tieneCanastaLimpia() || nueva.esLimpia())
                && equipo.getTurnoCanastaLimpia() != -1
                && numeroTurnoGlobal > equipo.getTurnoCanastaLimpia();
        if (cartasRestantes == 1 && !cumpliraCierre) {
            throw new IllegalStateException(
                    "No podés quedarte con 1 sola carta porque aún no cumplís los requisitos para cerrar.");
        }
        boolean estabaAbiertoAntes = equipo.isAbrioEnManoActual();
        actual.quitarCartas(seleccionadas);
        equipo.bajarCombinacion(nueva, numeroTurnoGlobal);

        if (!estabaAbiertoAntes) {
            puntosBajadosEnTurnoActual += sumarPuntosCartas(seleccionadas);
        }
        this.ultimoMensaje = actual.getNombre() + " bajó: " + nueva;
        notificarObservadores(Eventos.JUGADA_BAJADA);
    }

    @Override
    public void agregarACombinacion(String nombreJugador, int indiceJuegoMesa, List<Integer> indicesCartas) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ACCION);
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();

        if (!equipo.isAbrioEnManoActual() && equipo.getJuegosEnMesa().isEmpty()) {
            throw new IllegalStateException("Tu equipo primero debe abrirse.");
        }

        List<Carta> seleccionadas = actual.obtenerCartasPorIndices(indicesCartas);
        int cartasRestantes = actual.getMano().size() - seleccionadas.size();
        if (cartasRestantes == 0) {
            throw new IllegalStateException("Siempre debés conservar 1 carta para descartar al finalizar el turno.");
        }
        if (cartasRestantes == 1 && !equipo.puedeCerrar(numeroTurnoGlobal)) {
            throw new IllegalStateException(
                    "No podés quedarte con 1 sola carta porque aún no cumplís los requisitos para cerrar.");
        }
        boolean estabaAbiertoAntes = equipo.isAbrioEnManoActual();
        equipo.agregarCartasACombinacion(indiceJuegoMesa, seleccionadas, numeroTurnoGlobal);
        actual.quitarCartas(seleccionadas);

        if (!estabaAbiertoAntes) {
            int puntosAgregados = sumarPuntosCartas(seleccionadas);
            puntosBajadosEnTurnoActual += puntosAgregados;
            equipo.registrarPuntosApertura(puntosAgregados);
        }
        this.ultimoMensaje = actual.getNombre() + " agregó " + seleccionadas.size()
                + " carta(s) al juego [" + indiceJuegoMesa + "].";
        notificarObservadores(Eventos.JUGADA_BAJADA);
    }

    @Override
    public void deshacerJugadasTurno(String nombreJugador) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ACCION);
        if (backupManoTurno == null) {
            throw new IllegalStateException("No hay acciones en este turno para deshacer.");
        }
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();
        actual.vaciarMano();
        actual.recibirCartas(backupManoTurno);
        equipo.restaurarMesa(backupMesaTurno, backupAbrioAntes, backupTurnoLimpiaAntes);
        this.puntosBajadosEnTurnoActual = 0;
        this.ultimoMensaje = actual.getNombre() + " deshizo las jugadas bajadas en este turno.";
        notificarObservadores(Eventos.JUGADA_BAJADA);
    }

    @Override
    public void descartar(String nombreJugador, int indiceCarta) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ACCION);
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();
        equipo.confirmarAperturaSiCorresponde();

        if (actual.getMano().size() == 1) {
            throw new IllegalStateException("Solo te queda 1 carta: debés usar 'Cerrar Mano' si cumplís los requisitos.");
        }
        Carta descarte = actual.quitarCartaPorIndice(indiceCarta);
        pozo.descartar(descarte);
        this.puntosBajadosEnTurnoActual = 0;
        this.backupManoTurno = null;
        avanzarTurno();
        GestorPersistencia.guardarPartida(this);
        this.ultimoMensaje = actual.getNombre() + " descartó " + descarte + ". Turno de " + getJugadorActual().getNombre();
        notificarObservadores(Eventos.CARTA_DESCARTADA);
    }

    @Override
    public void cerrarMano(String nombreJugador, int indiceCartaDescarte) throws RemoteException {
        validarTurnoYEstado(nombreJugador, EstadoTurno.ESPERANDO_ACCION);
        Jugador actual = getJugadorActual();
        Equipo equipo = actual.getEquipo();
        equipo.confirmarAperturaSiCorresponde();

        if (actual.getMano().size() != 1) {
            throw new IllegalStateException("Para cerrar debés tener exactamente 1 carta sobrante en la mano.");
        }
        if (!equipo.puedeCerrar(numeroTurnoGlobal)) {
            throw new IllegalStateException(
                    "No cumplís los requisitos de cierre (canasta limpia + sucia, y esperar 1 turno tras la limpia).");
        }
        Carta cierre = actual.quitarCartaPorIndice(indiceCartaDescarte);
        pozo.descartar(cierre);
        procesarFinDeMano(equipo);
    }

    private void finalizarManoPorMazoAgotado() throws RemoteException {
        procesarFinDeMano(null);
    }

    private void procesarFinDeMano(Equipo equipoQueCerro) throws RemoteException {
        for (Equipo eq : equipos) {
            eq.calcularPuntajeMano(eq == equipoQueCerro);
        }
        boolean hayGanador = false;
        for (Equipo eq : equipos) {
            if (eq.getPuntajeTotal() >= puntajeObjetivo) {
                hayGanador = true;
                break;
            }
        }

        if (hayGanador) {
            this.estadoTurno = EstadoTurno.FIN_PARTIDA;
            Equipo ganador = obtenerEquipoGanador();
            GestorRanking.registrarGanadores(ganador);
            GestorPersistencia.eliminarPartidaGuardada();
            this.ultimoMensaje = "¡Fin de la partida! Ganador: "
                    + (ganador != null ? ganador.getNombre() : "");
            notificarObservadores(Eventos.PARTIDA_FINALIZADA);
        } else {
            this.estadoTurno = EstadoTurno.FIN_MANO;
            this.ultimoMensaje = "Mano finalizada. Se repartirá una nueva mano.";
            notificarObservadores(Eventos.MANO_FINALIZADA);
            iniciarNuevaMano();
            GestorPersistencia.guardarPartida(this);
            notificarObservadores(Eventos.PARTIDA_INICIADA);
        }
    }

    private Equipo obtenerEquipoGanador() {
        Equipo mejor = null;
        for (Equipo eq : equipos) {
            if (mejor == null || eq.getPuntajeTotal() > mejor.getPuntajeTotal()) {
                mejor = eq;
            }
        }
        return mejor;
    }

    private void avanzarTurno() {
        indiceTurnoActual = (indiceTurnoActual + 1) % jugadores.size();
        numeroTurnoGlobal++;
        estadoTurno = EstadoTurno.ESPERANDO_ROBO;
    }

    private void validarTurnoYEstado(String nombreJugador, EstadoTurno estadoEsperado) {
        if (this.estadoTurno != estadoEsperado) {
            throw new IllegalStateException("Acción no permitida en el estado actual: " + this.estadoTurno);
        }
        if (!getJugadorActual().getNombre().equalsIgnoreCase(nombreJugador)) {
            throw new IllegalStateException("No es tu turno. Turno actual: " + getJugadorActual().getNombre());
        }
    }

    @Override
    public EstadoTurno getEstadoTurno() {
        return estadoTurno;
    }

    @Override
    public Jugador getJugadorActual() {
        return jugadores.get(indiceTurnoActual);
    }

    @Override
    public Jugador getJugadorPorNombre(String nombre) {
        if (nombre == null) {return null;}
        for (Jugador j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombre.trim())) {return j;}
        }
        return null;
    }

    @Override
    public List<Equipo> getEquipos() {return equipos;}

    @Override
    public Carta verTopePozo() {return pozo != null ? pozo.verTope() : null;}

    @Override
    public int getCantidadPozo() {return pozo != null ? pozo.cantidadCartas() : 0;}

    @Override
    public boolean isPozoTapado() {return pozo != null && pozo.estaTapado();}

    @Override
    public boolean isPozoCruzado() {return pozo != null && pozo.estaCruzado();}

    @Override
    public int getCantidadMazo() {return mazo != null ? mazo.cantidadRestante() : 0;}

    @Override
    public String getUltimoMensaje() {return ultimoMensaje;}
}