package modelo.genoma;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Vetor de 5 genes numéricos reais [g1, g2, g3, g4, g5] que definem
 * a estratégia de navegação do Entregador.
 */
public record GenomaEntregador(double[] genes) {
    public static final double G1_MIN = 0.0, G1_MAX = 3.0;
    public static final double G2_MIN = 0.0, G2_MAX = 3.0;
    public static final double G3_MIN = 0.0, G3_MAX = 5.0;
    public static final double G4_MIN = 0.0, G4_MAX = 2.0;
    public static final double G5_MIN = 0.0, G5_MAX = 0.5;

    public GenomaEntregador {
        Objects.requireNonNull(genes, "O vetor de genes não pode ser nulo.");
        if (genes.length != 5) {
            throw new IllegalArgumentException("O genoma do entregador deve conter exatamente 5 genes.");
        }
        genes = genes.clone();
        clampGenes(genes);
    }

    public double g1() { return genes[0]; }
    public double g2() { return genes[1]; }
    public double g3() { return genes[2]; }
    public double g4() { return genes[3]; }
    public double g5() { return genes[4]; }

    /**
     * Gera um genoma aleatório dentro dos limites operacionais.
     */
    public static GenomaEntregador aleatorio(Random rng) {
        Objects.requireNonNull(rng, "O gerador aleatório não pode ser nulo.");
        double[] g = new double[]{
            G1_MIN + rng.nextDouble() * (G1_MAX - G1_MIN),
            G2_MIN + rng.nextDouble() * (G2_MAX - G2_MIN),
            G3_MIN + rng.nextDouble() * (G3_MAX - G3_MIN),
            G4_MIN + rng.nextDouble() * (G4_MAX - G4_MIN),
            G5_MIN + rng.nextDouble() * (G5_MAX - G5_MIN)
        };
        return new GenomaEntregador(g);
    }

    private static void clampGenes(double[] g) {
        g[0] = Math.max(G1_MIN, Math.min(G1_MAX, g[0]));
        g[1] = Math.max(G2_MIN, Math.min(G2_MAX, g[1]));
        g[2] = Math.max(G3_MIN, Math.min(G3_MAX, g[2]));
        g[3] = Math.max(G4_MIN, Math.min(G4_MAX, g[3]));
        g[4] = Math.max(G5_MIN, Math.min(G5_MAX, g[4]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenomaEntregador that = (GenomaEntregador) o;
        return Arrays.equals(genes, that.genes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }

    @Override
    public String toString() {
        return String.format("GenomaEntregador[g1=%.2f, g2=%.2f, g3=%.2f, g4=%.2f, g5=%.2f]",
                g1(), g2(), g3(), g4(), g5());
    }
}
