package ar.edu.unlu.poo.canasta.modelo;
import ar.edu.unlu.rmimvc.observer.IObservableRemoto;
import java.rmi.RemoteException;
import java.util.List;

public interface IPartida extends IObservableRemoto {
    List<Jugador> getJugadores() throws RemoteException;

    void agregarJugador(String nombre) throws RemoteException;
    void iniciarPartida(int puntajeObjetivo) throws RemoteException;
    void deshacerJugadasTurno(String nombreJugador) throws RemoteException;

    // Acciones del turno
    void robarDelMazo(String nombreJugador) throws RemoteException;
    void tomarPozo(String nombreJugador, List<Integer> indicesMano, Integer indiceJuegoMesa) throws RemoteException;
    void bajarCombinacion(String nombreJugador, List<Integer> indicesCartas, boolean esOculta) throws RemoteException;
    void agregarACombinacion(String nombreJugador, int indiceJuegoMesa, List<Integer> indicesCartas) throws RemoteException;
    void descartar(String nombreJugador, int indiceCarta) throws RemoteException;
    void cerrarMano(String nombreJugador, int indiceCartaDescarte) throws RemoteException;

    // Consultas de estado para las vistas
    EstadoTurno getEstadoTurno() throws RemoteException;
    Jugador getJugadorActual() throws RemoteException;
    Jugador getJugadorPorNombre(String nombre) throws RemoteException;
    List<Equipo> getEquipos() throws RemoteException;
    Carta verTopePozo() throws RemoteException;
    int getCantidadPozo() throws RemoteException;
    boolean isPozoTapado() throws RemoteException;
    boolean isPozoCruzado() throws RemoteException;
    int getCantidadMazo() throws RemoteException;
    String getUltimoMensaje() throws RemoteException;
}