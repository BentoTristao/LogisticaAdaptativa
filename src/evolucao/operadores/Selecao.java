package evolucao.operadores;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Operador de Seleção por Torneio para seleção de progenitores.
 */
public final class Selecao {
    private Selecao() {
        // Classe utilitária
    }

    /**
     * Sorteia k indivíduos ao acaso e escolhe aquele que apresenta maior fitness.
     *
     * param populacao Lista de indivíduos candidatas.
     * param fitnesses Vetor com as notas de aptidão correspondentes.
     * param k         Tamanho do torneio (padrão k = 3).
     * param rng       Gerador de números aleatórios determinístico.
     * return O indivíduo vencedor do torneio.
     */
    public static <T> T torneio(List<T> populacao, double[] fitnesses, int k, Random rng) {
        Objects.requireNonNull(populacao, "A população não pode ser nula.");
        Objects.requireNonNull(fitnesses, "O vetor de fitness não pode ser nulo.");
        Objects.requireNonNull(rng, "O gerador aleatório não pode ser nulo.");

        if (populacao.isEmpty() || populacao.size() != fitnesses.length) {
            throw new IllegalArgumentException("A população e o vetor de fitness devem ter tamanhos idênticos e não-vazios.");
        }
        if (k < 1) {
            throw new IllegalArgumentException("O tamanho do torneio k deve ser no mínimo 1.");
        }

        int melhorIndice = -1;
        double melhorFitness = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < k; i++) {
            int idx = rng.nextInt(populacao.size());
            if (melhorIndice == -1 || fitnesses[idx] > melhorFitness) {
                melhorIndice = idx;
                melhorFitness = fitnesses[idx];
            }
        }

        return populacao.get(melhorIndice);
    }
}
