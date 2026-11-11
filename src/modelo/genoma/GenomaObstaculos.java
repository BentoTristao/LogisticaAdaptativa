package modelo.genoma;

import grafo.core.Grafo;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.PlanoObstaculos;
import modelo.obstaculo.TipoObstaculo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Genoma do Planejador de Obstáculos. Envolve um PlanoObstaculos e fornece
 * métodos para inicialização e mutação aleatória de planos.
 */
public record GenomaObstaculos(PlanoObstaculos plano) {
    public GenomaObstaculos {
        Objects.requireNonNull(plano, "O plano de obstáculos não pode ser nulo.");
    }

    /**
     * Gera um genoma com lista aleatória de obstáculos dentro do orçamento e limite máximo.
     */
    public static GenomaObstaculos aleatorio(Grafo grafo, int maxObstaculos, int maxTick, Random rng) {
        Objects.requireNonNull(grafo, "O grafo não pode ser nulo.");
        Objects.requireNonNull(rng, "O gerador aleatório não pode ser nulo.");

        int numArestas = grafo.numArestas();
        if (numArestas == 0 || maxObstaculos <= 0) {
            return new GenomaObstaculos(PlanoObstaculos.vazio());
        }

        int qtd = rng.nextInt(maxObstaculos) + 1;
        TipoObstaculo[] tipos = TipoObstaculo.values();
        List<Obstaculo> lista = new ArrayList<>();

        for (int i = 0; i < qtd; i++) {
            int arestaId = rng.nextInt(numArestas);
            int tick = maxTick > 0 ? rng.nextInt(maxTick) : 0;
            TipoObstaculo tipo = tipos[rng.nextInt(tipos.length)];
            lista.add(new Obstaculo(arestaId, tick, tipo));
        }

        return new GenomaObstaculos(new PlanoObstaculos(lista));
    }
}
