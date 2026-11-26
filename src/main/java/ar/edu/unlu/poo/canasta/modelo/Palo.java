package ar.edu.unlu.poo.canasta.modelo;

public enum Palo {
    CORAZONES(true),
    DIAMANTES(true),
    PICAS(false),
    TREBOLES(false),
    JOKER(false);

    private final boolean esRojo;

    Palo(boolean esRojo) {
        this.esRojo = esRojo;
    }
    public boolean esRojo() {
        return esRojo;
    }
}