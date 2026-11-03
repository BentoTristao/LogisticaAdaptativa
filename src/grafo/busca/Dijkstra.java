package grafo.busca;

import grafo.core.Aresta;
import grafo.core.Grafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/** Busca o caminho de menor custo usando pesos não negativos. */
public final class Dijkstra {

    private Dijkstra() {
        // Classe utilitária.
    }

    /**
     * Retorna os ids das arestas do menor caminho entre origem e destino.
     * Retorna uma lista vazia quando o destino não é alcançável.
     */
    public static List<Integer> caminho(
            Grafo grafo,
            double[] pesos,
            int origem,
            int destino
    ) {
        validarEntrada(grafo, pesos, origem, destino);

        int n = grafo.numVertices();
        double[] distancia = new double[n];
        int[] anteriorVertice = new int[n];
        int[] anteriorAresta = new int[n];
        Arrays.fill(distancia, Double.POSITIVE_INFINITY);
        Arrays.fill(anteriorVertice, -1);
        Arrays.fill(anteriorAresta, -1);
        distancia[origem] = 0.0;

        PriorityQueue<Estado> fila = new PriorityQueue<>((a, b) -> Double.compare(a.custo(), b.custo()));
        fila.add(new Estado(origem, 0.0));

        while (!fila.isEmpty()) {
            Estado atual = fila.poll();
            int u = atual.vertice();

            // Ignora entradas antigas da fila de prioridade.
            if (atual.custo() > distancia[u]) {
                continue;
            }
            if (u == destino) {
                break;
            }

            for (Aresta aresta : grafo.arestasDe(u)) {
                double peso = pesos[aresta.id()];
                if (Double.isInfinite(peso)) {
                    continue; // Aresta bloqueada.
                }

                int v = aresta.outro(u);
                double novaDistancia = distancia[u] + peso;
                if (novaDistancia < distancia[v]) {
                    distancia[v] = novaDistancia;
                    anteriorVertice[v] = u;
                    anteriorAresta[v] = aresta.id();
                    fila.add(new Estado(v, novaDistancia));
                }
            }
        }

        if (!Double.isFinite(distancia[destino])) {
            return List.of();
        }
        return reconstruirCaminho(origem, destino, anteriorVertice, anteriorAresta);
    }

    private static List<Integer> reconstruirCaminho(
            int origem,
            int destino,
            int[] anteriorVertice,
            int[] anteriorAresta
    ) {
        List<Integer> caminho = new ArrayList<>();
        int atual = destino;

        while (atual != origem) {
            int arestaId = anteriorAresta[atual];
            int proximoAnterior = anteriorVertice[atual];
            if (arestaId < 0 || proximoAnterior < 0) {
                return List.of();
            }
            caminho.add(arestaId);
            atual = proximoAnterior;
        }

        Collections.reverse(caminho);
        return List.copyOf(caminho);
    }

    static void validarEntrada(Grafo grafo, double[] pesos, int origem, int destino) {
        if (grafo == null) {
            throw new IllegalArgumentException("O grafo não pode ser nulo.");
        }
        if (pesos == null || pesos.length != grafo.numArestas()) {
            throw new IllegalArgumentException("O vetor de pesos deve ter um valor por aresta.");
        }
        validarVertice(origem, grafo.numVertices(), "origem");
        validarVertice(destino, grafo.numVertices(), "destino");

        for (int i = 0; i < pesos.length; i++) {
            if (Double.isNaN(pesos[i]) || pesos[i] < 0.0 || pesos[i] == Double.NEGATIVE_INFINITY) {
                throw new IllegalArgumentException("Peso inválido na aresta " + i + ": " + pesos[i]);
            }
        }
    }

    private static void validarVertice(int vertice, int quantidade, String nome) {
        if (vertice < 0 || vertice >= quantidade) {
            throw new IllegalArgumentException(nome + " inválida: " + vertice);
        }
    }

    private record Estado(int vertice, double custo) {
    }
}
