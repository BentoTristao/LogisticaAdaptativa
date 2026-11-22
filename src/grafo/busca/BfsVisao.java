package grafo.busca;

import grafo.core.Aresta;
import grafo.core.Grafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

/** BFS usada para descobrir os vértices dentro do raio de visão do Entregador. */
public final class BfsVisao {
    private BfsVisao() {
        // Classe utilitária.
    }

    /**
     * Retorna os vértices alcançáveis em até k arestas a partir da origem,
     * incluindo a própria origem. A ordem é a ordem de visita da BFS.
     */
    public static List<Integer> bfsAteRaio(Grafo grafo, int origem, int k) {
        if (grafo == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }
        if (origem < 0 || origem >= grafo.numVertices()) {
            throw new IllegalArgumentException("Origem inválida: " + origem);
        }
        if (k < 0) {
            throw new IllegalArgumentException("O raio de visão não pode ser negativo.");
        }

        int[] distancia = new int[grafo.numVertices()];
        Arrays.fill(distancia, -1);

        Queue<Integer> fila = new ArrayDeque<>();
        List<Integer> visitados = new ArrayList<>();
        distancia[origem] = 0;
        fila.add(origem);

        while (!fila.isEmpty()) {
            int atual = fila.remove();
            visitados.add(atual);

            if (distancia[atual] == k) {
                continue;
            }

            for (Aresta aresta : grafo.arestasDe(atual)) {
                int vizinho = aresta.outro(atual);
                if (distancia[vizinho] == -1) {
                    distancia[vizinho] = distancia[atual] + 1;
                    fila.add(vizinho);
                }
            }
        }

        return List.copyOf(visitados);
    }

    /** Nome alternativo para deixar explícito o retorno do método. */
    public static List<Integer> verticesAteRaio(Grafo grafo, int origem, int k) {
        return bfsAteRaio(grafo, origem, k);
    }
}
