package ar.edu.unlu.poo.canasta.controlador;

import ar.edu.unlu.poo.canasta.modelo.Carta;
import ar.edu.unlu.poo.canasta.modelo.Equipo;
import ar.edu.unlu.poo.canasta.modelo.EstadoTurno;
import ar.edu.unlu.poo.canasta.modelo.Eventos;
import ar.edu.unlu.poo.canasta.modelo.IPartida;
import ar.edu.unlu.poo.canasta.modelo.Jugador;
import ar.edu.unlu.poo.canasta.modelo.persistencia.GestorRanking;
import ar.edu.unlu.poo.canasta.modelo.persistencia.RegistroRanking;
import ar.edu.unlu.poo.canasta.vista.IVista;
import ar.edu.unlu.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import java.rmi.RemoteException;
import java.util.List;

public class Controlador implements IControladorRemoto {
    private IPartida modelo;
    private IVista vista;
    private String nombreLocal;

    public Controlador() {
    }

    public Controlador(IVista vista) {
        this.vista = vista;
    }

    public void setVista(IVista vista) {
        this.vista = vista;
    }

    public String getNombreLocal() {
        return nombreLocal;
    }

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto(T modeloRemoto) throws RemoteException {
        this.modelo = (IPartida) modeloRemoto;
    }

    public boolean entrar(String nombre) {
        try {
            String limpio = (nombre == null) ? "" : nombre.trim();
            this.nombreLocal = limpio;
            if (modelo.getEstadoTurno() == EstadoTurno.ESPERANDO_JUGADORES) {
                modelo.agregarJugador(limpio);
            } else if (modelo.getJugadorPorNombre(limpio) == null) {
                throw new IllegalStateException("La partida ya comenzó y no figurás en ella.");
            }
            mostrarSegunEstado();
            return true;
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
            return false;
        }
    }

    public void iniciarPartida(int puntajeObjetivo) {
        try {
            modelo.iniciarPartida(puntajeObjetivo);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void robarDelMazo() {
        try {
            modelo.robarDelMazo(nombreLocal);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void tomarPozo(List<Integer> indicesMano, Integer indiceJuegoMesa) {
        try {
            modelo.tomarPozo(nombreLocal, indicesMano, indiceJuegoMesa);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void bajarCombinacion(List<Integer> indicesCartas, boolean esOculta) {
        try {
            modelo.bajarCombinacion(nombreLocal, indicesCartas, esOculta);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void agregarACombinacion(int indiceJuegoMesa, List<Integer> indicesCartas) {
        try {
            modelo.agregarACombinacion(nombreLocal, indiceJuegoMesa, indicesCartas);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void deshacerJugadasTurno() {
        try {
            modelo.deshacerJugadasTurno(nombreLocal);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void descartar(int indiceCarta) {
        try {
            modelo.descartar(nombreLocal, indiceCarta);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void cerrarMano(int indiceCartaDescarte) {
        try {
            modelo.cerrarMano(nombreLocal, indiceCartaDescarte);
        } catch (RemoteException | IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(extraerMensaje(e));
        }
    }

    public void salir() {
        try {
            if (modelo != null) {
                modelo.removerObservador(this);
            }
        } catch (RemoteException | RuntimeException ignored) {
        }
    }

    @Override
    public void actualizar(IObservableRemoto observable, Object evento) throws RemoteException {
        if (nombreLocal == null || nombreLocal.isBlank() || modelo == null || vista == null) {
            return;
        }
        if (evento == Eventos.JUGADOR_UNIDO) {
            mostrarSegunEstado();
        } else if (evento == Eventos.PARTIDA_INICIADA) {
            vista.mostrarJuego();
            mostrarMesa();
        } else if (evento == Eventos.CARTAS_ROBADAS
                || evento == Eventos.POZO_TOMADO
                || evento == Eventos.JUGADA_BAJADA
                || evento == Eventos.CARTA_DESCARTADA
                || evento == Eventos.MANO_FINALIZADA) {
            mostrarMesa();
        } else if (evento == Eventos.PARTIDA_FINALIZADA) {
            vista.mostrarResultado(modelo.getUltimoMensaje(), getRanking());
        }
    }

    private void mostrarSegunEstado() throws RemoteException {
        EstadoTurno estado = modelo.getEstadoTurno();
        if (estado == EstadoTurno.ESPERANDO_JUGADORES) {
            Jugador yo = modelo.getJugadorPorNombre(nombreLocal);
            vista.mostrarSala(modelo.getJugadores(), null, yo);
        } else if (estado == EstadoTurno.FIN_PARTIDA) {
            vista.mostrarResultado(modelo.getUltimoMensaje(), getRanking());
        } else {
            vista.mostrarJuego();
            mostrarMesa();
        }
    }

    private void mostrarMesa() throws RemoteException {
        Jugador yo = modelo.getJugadorPorNombre(nombreLocal);
        vista.mostrarMesa(
                modelo.getEstadoTurno(),
                modelo.getJugadorActual(),
                yo,
                modelo.getEquipos(),
                modelo.getCantidadMazo(),
                modelo.getCantidadPozo(),
                modelo.verTopePozo(),
                modelo.isPozoTapado(),
                modelo.isPozoCruzado(),
                modelo.getUltimoMensaje()
        );
    }

    public void mostrarError(String mensaje) {
        if (vista != null) {
            vista.mostrarError(mensaje);
        }
    }

    public List<Jugador> getJugadores() {
        try {
            return modelo != null ? modelo.getJugadores() : List.of();
        } catch (RemoteException e) {
            return List.of();
        }
    }

    public EstadoTurno getEstadoTurno() {
        try {
            return modelo != null ? modelo.getEstadoTurno() : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public Jugador getJugadorActual() {
        try {
            return modelo != null ? modelo.getJugadorActual() : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public Jugador getJugadorPorNombre(String nombre) {
        try {
            return modelo != null ? modelo.getJugadorPorNombre(nombre) : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public List<Equipo> getEquipos() {
        try {
            return modelo != null ? modelo.getEquipos() : List.of();
        } catch (RemoteException e) {
            return List.of();
        }
    }

    public Carta verTopePozo() {
        try {
            return modelo != null ? modelo.verTopePozo() : null;
        } catch (RemoteException e) {
            return null;
        }
    }

    public int getCantidadPozo() {
        try {
            return modelo != null ? modelo.getCantidadPozo() : 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public boolean isPozoTapado() {
        try {
            return modelo != null && modelo.isPozoTapado();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isPozoCruzado() {
        try {
            return modelo != null && modelo.isPozoCruzado();
        } catch (RemoteException e) {
            return false;
        }
    }

    public int getCantidadMazo() {
        try {
            return modelo != null ? modelo.getCantidadMazo() : 0;
        } catch (RemoteException e) {
            return 0;
        }
    }

    public List<RegistroRanking> getRanking() {
        return GestorRanking.obtenerRanking();
    }

    private String extraerMensaje(Throwable e) {
        Throwable actual = e;
        while (actual.getCause() != null && actual.getCause() != actual) {
            if (actual.getMessage() != null && !actual.getMessage().isBlank()
                    && !(actual instanceof RemoteException)) {
                return actual.getMessage();
            }
            actual = actual.getCause();
        }
        return actual.getMessage() != null ? actual.getMessage() : "Error en la operación.";
    }
}