package grafo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Estrutura imutável de um grafo não-direcionado derivado de uma grade.
 *
 * Os pesos não fazem parte da estrutura do grafo: cada simulação mantém seu
 * próprio double[], indexado pelo id da aresta. Assim, simulações paralelas não
 * compartilham alterações de pesos.
 */
public final class Grafo {

    private final int linhas;
    private final int colunas;
    private final int verticeInicio;
    private final int verticeDestino;
    private final List<Aresta> arestas;
    private final List<List<Aresta>> adjacencias;

    /**
     * Cria um grafo a partir das arestas existentes na grade.
     * Os ids das arestas devem ser contíguos, começando em zero.
     */
    public Grafo(
            int linhas,
            int colunas,
            List<Aresta> arestas,
            int verticeInicio,
            int verticeDestino
    ) {
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Linhas e colunas devem ser positivas.");
        }

        final int quantidadeVertices;
        try {
            quantidadeVertices = Math.multiplyExact(linhas, colunas);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("A quantidade de vértices excede o limite suportado.", ex);
        }

        validarVertice(verticeInicio, quantidadeVertices, "vértice inicial");
        validarVertice(verticeDestino, quantidadeVertices, "vértice de destino");
        Objects.requireNonNull(arestas, "A lista de arestas não pode ser nula.");

        List<Aresta> arestasPorId = new ArrayList<>(arestas.size());
        for (int i = 0; i < arestas.size(); i++) {
            arestasPorId.add(null);
        }

        List<List<Aresta>> listasAdjacencia = new ArrayList<>(quantidadeVertices);
        for (int v = 0; v < quantidadeVertices; v++) {
            listasAdjacencia.add(new ArrayList<>());
        }

        for (Aresta aresta : arestas) {
            Objects.requireNonNull(aresta, "A lista não pode conter arestas nulas.");
            validarVertice(aresta.origem(), quantidadeVertices, "origem da aresta " + aresta.id());
            validarVertice(aresta.destino(), quantidadeVertices, "destino da aresta " + aresta.id());

            if (aresta.id() >= arestasPorId.size()) {
                throw new IllegalArgumentException("Os ids das arestas devem ser contíguos de 0 a E-1.");
            }
            if (arestasPorId.set(aresta.id(), aresta) != null) {
                throw new IllegalArgumentException("Id de aresta duplicado: " + aresta.id());
            }

            listasAdjacencia.get(aresta.origem()).add(aresta);
            listasAdjacencia.get(aresta.destino()).add(aresta);
        }

        if (arestasPorId.contains(null)) {
            throw new IllegalArgumentException("Os ids das arestas devem ser contíguos de 0 a E-1.");
        }

        this.linhas = linhas;
        this.colunas = colunas;
        this.verticeInicio = verticeInicio;
        this.verticeDestino = verticeDestino;
        this.arestas = List.copyOf(arestasPorId);
        this.adjacencias = listasAdjacencia.stream()
                .map(List::copyOf)
                .toList();
    }

    public int numVertices() {
        return linhas * colunas;
    }

    public int numArestas() {
        return arestas.size();
    }

    /** Retorna as arestas incidentes ao vértice, sem permitir alterações. */
    public List<Aresta> arestasDe(int vertice) {
        validarVertice(vertice, numVertices(), "vértice");
        return adjacencias.get(vertice);
    }

    /** Retorna todas as arestas em ordem crescente de id. */
    public List<Aresta> arestas() {
        return arestas;
    }

    public int linhas() {
        return linhas;
    }

    public int colunas() {
        return colunas;
    }

    /** Converte coordenadas da grade para o id: linha * colunas + coluna. */
    public int vertice(int linha, int coluna) {
        if (linha < 0 || linha >= linhas || coluna < 0 || coluna >= colunas) {
            throw new IndexOutOfBoundsException("Coordenada fora da grade: (" + linha + ", " + coluna + ").");
        }
        return linha * colunas + coluna;
    }

    public int linhaDe(int vertice) {
        validarVertice(vertice, numVertices(), "vértice");
        return vertice / colunas;
    }

    public int colunaDe(int vertice) {
        validarVertice(vertice, numVertices(), "vértice");
        return vertice % colunas;
    }

    public int verticeInicio() {
        return verticeInicio;
    }

    public int verticeDestino() {
        return verticeDestino;
    }

    /** Retorna uma cópia dos pesos-base; inicialmente, todas as arestas custam 1.0. */
    public double[] pesosBase() {
        double[] pesos = new double[numArestas()];
        java.util.Arrays.fill(pesos, 1.0);
        return pesos;
    }

    /** Valida um id de vértice para uso por outras classes do pacote. */
    static void validarVertice(int vertice, int quantidadeVertices, String nome) {
        if (vertice < 0 || vertice >= quantidadeVertices) {
            throw new IllegalArgumentException(nome + " inválido: " + vertice);
        }
    }
}
