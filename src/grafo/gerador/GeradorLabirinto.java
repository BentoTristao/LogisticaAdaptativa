package grafo.gerador;

import grafo.core.Aresta;
import grafo.core.Grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Gerador de labirintos baseados em Árvore Geradora Mínima (Kruskal)
 * com reabertura de atalhos em grades M x N.
 */
public final class GeradorLabirinto {
    private GeradorLabirinto() {
        // Classe utilitária.
    }

    /**
     * Gera um labirinto navegável a partir de uma grade M x N.
     *
     * param linhas        Quantidade de linhas da grade (M >= 2).
     * param colunas       Quantidade de colunas da grade (N >= 2).
     * param fracaoAtalhos Porcentagem de paredes a serem removidas (0.0 a 1.0).
     * param seed          Semente determinística para reprodutibilidade.
     * return Instância imutável do Grafo gerado.
     */
    public static Grafo gerar(int linhas, int colunas, double fracaoAtalhos, long seed) {
        if (linhas < 2 || colunas < 2) {
            throw new IllegalArgumentException("Grade deve ter no mínimo 2 linhas e 2 colunas.");
        }
        if (!Double.isFinite(fracaoAtalhos) || fracaoAtalhos < 0.0 || fracaoAtalhos > 1.0) {
            throw new IllegalArgumentException("A fração de atalhos deve estar entre 0.0 e 1.0.");
        }

        Random rng = new Random(seed);
        int numVertices = linhas * colunas;

        // 1. Gera todas as conexões possíveis da grade (4 vizinhos)
        List<ArestaCandidata> candidatas = new ArrayList<>();
        for (int r = 0; r < linhas; r++) {
            for (int c = 0; c < colunas; c++) {
                int u = r * colunas + c;
                // Conexão à direita
                if (c + 1 < colunas) {
                    int v = r * colunas + (c + 1);
                    candidatas.add(new ArestaCandidata(u, v, rng.nextDouble()));
                }
                // Conexão abaixo
                if (r + 1 < linhas) {
                    int v = (r + 1) * colunas + c;
                    candidatas.add(new ArestaCandidata(u, v, rng.nextDouble()));
                }
            }
        }

        // 2. Kruskal: Ordena arestas pelo peso aleatório
        candidatas.sort(Comparator.comparingDouble(a -> a.pesoAleatorio));

        UnionFind uf = new UnionFind(numVertices);
        List<ArestaCandidata> arestasArvore = new ArrayList<>();
        List<ArestaCandidata> arestasForaArvore = new ArrayList<>();

        for (ArestaCandidata ac : candidatas) {
            if (uf.union(ac.u, ac.v)) {
                arestasArvore.add(ac);
            } else {
                arestasForaArvore.add(ac);
            }
        }

        // 3. Adiciona atalhos (reabre % de arestas fora da árvore)
        Collections.shuffle(arestasForaArvore, rng);
        int qtdAtalhos = (int) Math.round(arestasForaArvore.size() * fracaoAtalhos);

        List<ArestaCandidata> selecionadas = new ArrayList<>(arestasArvore);
        for (int i = 0; i < qtdAtalhos && i < arestasForaArvore.size(); i++) {
            selecionadas.add(arestasForaArvore.get(i));
        }

        // 4. Constrói a lista final de Arestas indexadas sequencialmente (0 .. E-1)
        List<Aresta> arestasFinais = new ArrayList<>(selecionadas.size());
        for (int id = 0; id < selecionadas.size(); id++) {
            ArestaCandidata ac = selecionadas.get(id);
            arestasFinais.add(new Aresta(id, ac.u, ac.v));
        }

        int vInicio = 0;
        int vDestino = numVertices - 1;

        return new Grafo(linhas, colunas, arestasFinais, vInicio, vDestino);
    }

    private record ArestaCandidata(int u, int v, double pesoAleatorio) {}

    private static class UnionFind {
        private final int[] parent;

        public UnionFind(int n) {
            parent = new int[n];
            for (int i = 0; i < n; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] == i) return i;
            return parent[i] = find(parent[i]);
        }

        public boolean union(int i, int j) {
            int rootI = find(i);
            int rootJ = find(j);
            if (rootI != rootJ) {
                parent[rootI] = rootJ;
                return true;
            }
            return false;
        }
    }
}
