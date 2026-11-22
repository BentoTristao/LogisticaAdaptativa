package grafo.analise;

import grafo.busca.BfsVisao;
import grafo.core.Aresta;
import grafo.core.Grafo;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.PlanoObstaculos;
import modelo.obstaculo.TipoObstaculo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Utilitários para detecção de pontes (Tarjan), validação de conectividade
 * e reparo de planos de obstáculos.
 */
public final class AnalisadorConectividade {
    private AnalisadorConectividade() {
        // Classe utilitária.
    }

    /**
     * Identifica os IDs de todas as pontes do grafo usando o algoritmo de Tarjan (DFS).
     */
    public static Set<Integer> pontes(Grafo grafo) {
        Objects.requireNonNull(grafo, "O grafo não pode ser nulo.");
        int n = grafo.numVertices();
        int[] disc = new int[n];
        int[] low = new int[n];
        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);

        Set<Integer> pontesIDs = new HashSet<>();
        int[] tempo = new int[]{0};

        for (int i = 0; i < n; i++) {
            if (disc[i] == -1) {
                dfsTarjan(i, -1, tempo, disc, low, pontesIDs, grafo);
            }
        }

        return Set.copyOf(pontesIDs);
    }

    private static void dfsTarjan(int u, int arestaPaiId, int[] tempo, int[] disc, int[] low, Set<Integer> pontes, Grafo g) {
        disc[u] = low[u] = ++tempo[0];

        for (Aresta a : g.arestasDe(u)) {
            if (a.id() == arestaPaiId) continue;
            int v = a.outro(u);

            if (disc[v] != -1) {
                low[u] = Math.min(low[u], disc[v]);
            } else {
                dfsTarjan(v, a.id(), tempo, disc, low, pontes, g);
                low[u] = Math.min(low[u], low[v]);
                if (low[v] > disc[u]) {
                    pontes.add(a.id());
                }
            }
        }
    }

    /**
     * Verifica se existe caminho entre o início e o destino ignorando arestas bloqueadas.
     */
    public static boolean conecta(Grafo grafo, Set<Integer> arestasBloqueadas) {
        Objects.requireNonNull(grafo, "O grafo não pode ser nulo.");
        if (arestasBloqueadas == null) arestasBloqueadas = Set.of();

        int origem = grafo.verticeInicio();
        int destino = grafo.verticeDestino();
        if (origem == destino) return true;

        boolean[] visitados = new boolean[grafo.numVertices()];
        Queue<Integer> fila = new ArrayDeque<>();
        visitados[origem] = true;
        fila.add(origem);

        while (!fila.isEmpty()) {
            int u = fila.poll();
            if (u == destino) return true;

            for (Aresta a : grafo.arestasDe(u)) {
                if (arestasBloqueadas.contains(a.id())) continue;
                int v = a.outro(u);
                if (!visitados[v]) {
                    visitados[v] = true;
                    fila.add(v);
                }
            }
        }

        return false;
    }

    /**
     * Repara um plano de obstáculos:
     * 1. Rebaixa bloqueios totais em pontes para Congestionamento.
     * 2. Remove ou rebaixa bloqueios adicionais que, em conjunto, desconectem o destino.
     * 3. Trunca obstáculos caso o custo ultrapasse o orçamento de desastre.
     */
    public static PlanoObstaculos reparar(Grafo g, PlanoObstaculos plano, int orcamentoDesastre) {
        Objects.requireNonNull(g, "O grafo não pode ser nulo.");
        Objects.requireNonNull(plano, "O plano não pode ser nulo.");

        Set<Integer> pontesDoGrafo = pontes(g);
        List<Obstaculo> obstaculosProcessados = new ArrayList<>();

        // Passo 1: Rebaixa bloqueios em pontes
        for (Obstaculo obs : plano.obstaculos()) {
            if (obs.tipo() == TipoObstaculo.BLOQUEIO && pontesDoGrafo.contains(obs.arestaId())) {
                obstaculosProcessados.add(new Obstaculo(obs.arestaId(), obs.tickAtivacao(), TipoObstaculo.CONGESTIONAMENTO));
            } else {
                obstaculosProcessados.add(obs);
            }
        }

        // Passo 2: Verifica desconexão combinada por BFS
        Set<Integer> bloqueadas = new HashSet<>();
        for (Obstaculo obs : obstaculosProcessados) {
            if (obs.tipo() == TipoObstaculo.BLOQUEIO) {
                bloqueadas.add(obs.arestaId());
            }
        }

        if (!conecta(g, bloqueadas)) {
            // Rebaixa bloqueios da direita para a esquerda até que o caminho seja restabelecido
            for (int i = obstaculosProcessados.size() - 1; i >= 0; i--) {
                Obstaculo obs = obstaculosProcessados.get(i);
                if (obs.tipo() == TipoObstaculo.BLOQUEIO) {
                    bloqueadas.remove(obs.arestaId());
                    obstaculosProcessados.set(i, new Obstaculo(obs.arestaId(), obs.tickAtivacao(), TipoObstaculo.CONGESTIONAMENTO));
                    if (conecta(g, bloqueadas)) break;
                }
            }
        }

        // Passo 3: Limita ao orçamento de desastre
        List<Obstaculo> planoFinal = new ArrayList<>();
        int custoAcumulado = 0;
        for (Obstaculo obs : obstaculosProcessados) {
            if (custoAcumulado + obs.tipo().custo <= orcamentoDesastre) {
                planoFinal.add(obs);
                custoAcumulado += obs.tipo().custo;
            }
        }

        return new PlanoObstaculos(planoFinal);
    }

    /**
     * Delega a busca por raio ao utilitário BfsVisao.
     */
    public static List<Integer> bfsAteRaio(Grafo g, int origem, int k) {
        return BfsVisao.bfsAteRaio(g, origem, k);
    }
}
