package grafo.busca;

import grafo.core.Aresta;
import grafo.core.Grafo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/** Busca A* usando a distância de Manhattan como heurística da grade. */
public final class AStar {

    private AStar() {
        // Classe utilitária.
    }

    /**
     * Calcula uma rota como ids de aresta.
     *
     * pesoHeuristica corresponde ao gene g2 do Entregador. Com valor zero,
     * o comportamento é equivalente ao Dijkstra. Valores maiores que 1 tornam
     * a busca mais direcionada ao destino, mas podem deixar de garantir a rota
     * globalmente ótima (A* ponderado).
     */
    public static List<Integer> caminho(
            Grafo grafo,
            double[] pesos,
            int origem,
            int destino,
            double pesoHeuristica
    ) {
        Dijkstra.validarEntrada(grafo, pesos, origem, destino);
        if (!Double.isFinite(pesoHeuristica) || pesoHeuristica < 0.0) {
            throw new IllegalArgumentException("O peso da heurística deve ser finito e não negativo.");
        }
        if (origem == destino) {
            return List.of();
        }

        int n = grafo.numVertices();
        double[] custo = new double[n];
        int[] anteriorVertice = new int[n];
        int[] anteriorAresta = new int[n];
        Arrays.fill(custo, Double.POSITIVE_INFINITY);
        Arrays.fill(anteriorVertice, -1);
        Arrays.fill(anteriorAresta, -1);
        custo[origem] = 0.0;

        PriorityQueue<Estado> fila = new PriorityQueue<>((a, b) -> Double.compare(a.f(), b.f()));
        fila.add(new Estado(origem, 0.0, heuristica(grafo, origem, destino, pesoHeuristica)));

        while (!fila.isEmpty()) {
            Estado atual = fila.poll();
            int u = atual.vertice();
            if (atual.g() > custo[u]) {
                continue;
            }
            if (u == destino) {
                return reconstruirCaminho(origem, destino, anteriorVertice, anteriorAresta);
            }

            for (Aresta aresta : grafo.arestasDe(u)) {
                double peso = pesos[aresta.id()];
                if (Double.isInfinite(peso)) {
                    continue;
                }

                int v = aresta.outro(u);
                double novoCusto = custo[u] + peso;
                if (novoCusto < custo[v]) {
                    custo[v] = novoCusto;
                    anteriorVertice[v] = u;
                    anteriorAresta[v] = aresta.id();
                    double f = novoCusto + heuristica(grafo, v, destino, pesoHeuristica);
                    fila.add(new Estado(v, novoCusto, f));
                }
            }
        }

        return List.of();
    }

    /** Sobrecarga com heurística padrão equivalente ao A* tradicional. */
    public static List<Integer> caminho(Grafo grafo, double[] pesos, int origem, int destino) {
        return caminho(grafo, pesos, origem, destino, 1.0);
    }

    private static double heuristica(Grafo grafo, int atual, int destino, double fator) {
        int distanciaManhattan = Math.abs(grafo.linhaDe(atual) - grafo.linhaDe(destino))
                + Math.abs(grafo.colunaDe(atual) - grafo.colunaDe(destino));
        return distanciaManhattan * fator;
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
            if (anteriorVertice[atual] < 0 || anteriorAresta[atual] < 0) {
                return List.of();
            }
            caminho.add(anteriorAresta[atual]);
            atual = anteriorVertice[atual];
        }
        Collections.reverse(caminho);
        return List.copyOf(caminho);
    }

    private record Estado(int vertice, double g, double f) {
    }
}
