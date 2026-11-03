package grafo.core;

/**
 * Ligação não-direcionada entre dois vértices do grafo.
 *
 * O identificador é único dentro do grafo e é usado para indexar o vetor de pesos
 * de cada simulação. A mesma instância da aresta aparece na adjacência dos dois
 * vértices extremos.
 */
public record Aresta(int id, int origem, int destino) {

    public Aresta {
        if (id < 0) {
            throw new IllegalArgumentException("O id da aresta não pode ser negativo.");
        }
        if (origem < 0 || destino < 0) {
            throw new IllegalArgumentException("Os vértices da aresta não podem ser negativos.");
        }
        if (origem == destino) {
            throw new IllegalArgumentException("Uma aresta não pode ligar um vértice a ele mesmo.");
        }
    }

    /** Retorna a outra extremidade da aresta a partir de um de seus vértices. */
    public int outro(int vertice) {
        if (vertice == origem) {
            return destino;
        }
        if (vertice == destino) {
            return origem;
        }
        throw new IllegalArgumentException("O vértice informado não pertence a esta aresta.");
    }
}
