package evolucao.estatistica;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Repositório histórico dos melhores indivíduos de gerações anteriores.
 */
public class HallOfFame<T> {
    private final int capacidadeMaxima;
    private final List<EntradaHoF<T>> membros;

    public HallOfFame(int capacidadeMaxima) {
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("A capacidade do Hall of Fame deve ser positiva.");
        }
        this.capacidadeMaxima = capacidadeMaxima;
        this.membros = new ArrayList<>();
    }

    /**
     * Adiciona um campeão ao Hall of Fame mantendo a ordenação decrescente de fitness.
     */
    public synchronized void adicionar(T individuo, double fitness) {
        Objects.requireNonNull(individuo, "Indivíduo não pode ser nulo.");

        membros.add(new EntradaHoF<>(individuo, fitness));
        membros.sort((a, b) -> Double.compare(b.fitness(), a.fitness()));

        if (membros.size() > capacidadeMaxima) {
            membros.remove(membros.size() - 1);
        }
    }

    /**
     * Sorteia aleatoriamente um campeão do histórico.
     */
    public synchronized T sortear(Random rng) {
        Objects.requireNonNull(rng, "RNG não pode ser nulo.");
        if (membros.isEmpty()) {
            throw new IllegalStateException("O Hall of Fame está vazio.");
        }
        return membros.get(rng.nextInt(membros.size())).individuo();
    }

    public synchronized boolean estaVazio() {
        return membros.isEmpty();
    }

    public synchronized T getMelhor() {
        if (membros.isEmpty()) return null;
        return membros.get(0).individuo();
    }

    public synchronized int tamanho() {
        return membros.size();
    }

    public synchronized List<T> todos() {
        return membros.stream().map(EntradaHoF::individuo).toList();
    }

    public record EntradaHoF<T>(T individuo, double fitness) {}
}
