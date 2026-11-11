package evolucao.operadores;

import grafo.core.Grafo;
import modelo.genoma.GenomaEntregador;
import modelo.genoma.GenomaObstaculos;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.PlanoObstaculos;
import modelo.obstaculo.TipoObstaculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Operadores de Mutação para as duas populações em co-evolução.
 */
public final class Mutacao {
    private Mutacao() {
        // Classe utilitária
    }

    /**
     * Mutação Gaussiana por gene para o Entregador.
     */
    public static GenomaEntregador gaussiana(GenomaEntregador original, double taxaMutacao, double sigmaPercentual, Random rng) {
        Objects.requireNonNull(original, "Genoma original não pode ser nulo.");
        Objects.requireNonNull(rng, "RNG não pode ser nulo.");

        double[] genes = original.genes().clone();
        double[] minLim = {GenomaEntregador.G1_MIN, GenomaEntregador.G2_MIN, GenomaEntregador.G3_MIN, GenomaEntregador.G4_MIN, GenomaEntregador.G5_MIN};
        double[] maxLim = {GenomaEntregador.G1_MAX, GenomaEntregador.G2_MAX, GenomaEntregador.G3_MAX, GenomaEntregador.G4_MAX, GenomaEntregador.G5_MAX};

        for (int i = 0; i < 5; i++) {
            if (rng.nextDouble() < taxaMutacao) {
                double faixa = maxLim[i] - minLim[i];
                double sigma = faixa * sigmaPercentual;
                genes[i] += rng.nextGaussian() * sigma;
            }
        }

        return new GenomaEntregador(genes);
    }

    /**
     * Mutação uniforme para o Planejador de Obstáculos.
     * Sorteia novos atributos (aresta, tick ou tipo) para cada obstáculo sob taxa de mutação.
     */
    public static GenomaObstaculos uniforme(GenomaObstaculos original, double taxaMutacao, Grafo grafo, int maxTick, Random rng) {
        Objects.requireNonNull(original, "Genoma original não pode ser nulo.");
        Objects.requireNonNull(grafo, "Grafo não pode ser nulo.");
        Objects.requireNonNull(rng, "RNG não pode ser nulo.");

        List<Obstaculo> atual = original.plano().obstaculos();
        List<Obstaculo> mutados = new ArrayList<>();
        TipoObstaculo[] tipos = TipoObstaculo.values();
        int numArestas = grafo.numArestas();

        for (Obstaculo obs : atual) {
            int arestaId = obs.arestaId();
            int tick = obs.tickAtivacao();
            TipoObstaculo tipo = obs.tipo();

            if (rng.nextDouble() < taxaMutacao && numArestas > 0) {
                arestaId = rng.nextInt(numArestas);
            }
            if (rng.nextDouble() < taxaMutacao && maxTick > 0) {
                tick = rng.nextInt(maxTick);
            }
            if (rng.nextDouble() < taxaMutacao) {
                tipo = tipos[rng.nextInt(tipos.length)];
            }

            mutados.add(new Obstaculo(arestaId, tick, tipo));
        }

        return new GenomaObstaculos(new PlanoObstaculos(mutados));
    }
}
