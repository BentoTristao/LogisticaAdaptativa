package evolucao.operadores;

import modelo.genoma.GenomaEntregador;
import modelo.genoma.GenomaObstaculos;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.PlanoObstaculos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Operadores de Crossover (Recombinação) para Entregadores e Obstáculos.
 */
public final class Crossover {
    private Crossover() {
        // Classe utilitária
    }

    /**
     * Crossover BLX-alfa para o genoma de números reais do Entregador.
     * Para cada gene, o valor do filho é sorteado no intervalo [min - alfa*d, max + alfa*d],
     * onde d = |pai1 - pai2| e alfa ≈ 0.3.
     */
    public static GenomaEntregador blxAlfa(GenomaEntregador pai1, GenomaEntregador pai2, double alfa, Random rng) {
        Objects.requireNonNull(pai1, "Pai 1 não pode ser nulo.");
        Objects.requireNonNull(pai2, "Pai 2 não pode ser nulo.");
        Objects.requireNonNull(rng, "RNG não pode ser nulo.");

        double[] g1 = pai1.genes();
        double[] g2 = pai2.genes();
        double[] filho = new double[5];

        for (int i = 0; i < 5; i++) {
            double min = Math.min(g1[i], g2[i]);
            double max = Math.max(g1[i], g2[i]);
            double d = max - min;
            double minExt = min - alfa * d;
            double maxExt = max + alfa * d;
            filho[i] = minExt + rng.nextDouble() * (maxExt - minExt);
        }

        return new GenomaEntregador(filho);
    }

    /**
     * Crossover de ponto único para a lista de obstáculos do Planejador.
     * Troca trechos das listas de dois planos de obstáculos.
     */
    public static GenomaObstaculos pontoUnico(GenomaObstaculos pai1, GenomaObstaculos pai2, Random rng) {
        Objects.requireNonNull(pai1, "Pai 1 não pode ser nulo.");
        Objects.requireNonNull(pai2, "Pai 2 não pode ser nulo.");
        Objects.requireNonNull(rng, "RNG não pode ser nulo.");

        List<Obstaculo> l1 = pai1.plano().obstaculos();
        List<Obstaculo> l2 = pai2.plano().obstaculos();

        if (l1.isEmpty() && l2.isEmpty()) return pai1;
        if (l1.isEmpty()) return pai2;
        if (l2.isEmpty()) return pai1;

        int pontoCut1 = rng.nextInt(l1.size() + 1);
        int pontoCut2 = rng.nextInt(l2.size() + 1);

        List<Obstaculo> filho = new ArrayList<>();
        for (int i = 0; i < pontoCut1; i++) {
            filho.add(l1.get(i));
        }
        for (int i = pontoCut2; i < l2.size(); i++) {
            filho.add(l2.get(i));
        }

        return new GenomaObstaculos(new PlanoObstaculos(filho));
    }
}
