package ar.edu.unlu.poo.canasta.modelo;

import java.io.Serializable;
import java.util.Objects;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Palo palo;
    private final int numero; // 1 (As) al 13 (K), 0 para Joker

    public Carta(Palo palo, int numero) {
        if (palo == null) {
            throw new IllegalArgumentException("El palo no puede ser nulo.");
        }
        if (palo == Palo.JOKER && numero != 0) {
            throw new IllegalArgumentException("El Joker debe tener número 0.");
        }
        if (palo != Palo.JOKER && (numero < 1 || numero > 13)) {
            throw new IllegalArgumentException("Número de carta inválido: " + numero);
        }
        this.palo = palo;
        this.numero = numero;
    }

    public Palo getPalo() {
        return palo;
    }

    public int getNumero() {
        return numero;
    }

    public boolean esComodin() {
        return palo == Palo.JOKER || numero == 2;
    }

    public boolean esTresRojo() {
        return numero == 3 && palo.esRojo();
    }

    public boolean esTresNegro() {
        return numero == 3 && !palo.esRojo() && palo != Palo.JOKER;
    }

    public int getPuntaje() {
        if (palo == Palo.JOKER) {
            return 50;
        }
        if (numero == 1 || numero == 2) {
            return 20;
        }
        if (numero >= 8 && numero <= 13) {
            return 10;
        }
        if (numero >= 4 && numero <= 7) {
            return 5;
        }
        return 0;
    }

    public static String formatoNumero(int numero) {
        return switch (numero) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> String.valueOf(numero);
        };
    }

    @Override
    public String toString() {
        if (palo == Palo.JOKER) {
            return "[JOKER]";
        }
        String valor = formatoNumero(numero);
        String simbolo = switch (palo) {
            case CORAZONES -> "♥";
            case DIAMANTES -> "♦";
            case PICAS -> "♠";
            case TREBOLES -> "♣";
            default -> "";
        };
        return "[" + valor + simbolo + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Carta otra)) {
            return false;
        }
        return numero == otra.numero && palo == otra.palo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(palo, numero);
    }
}