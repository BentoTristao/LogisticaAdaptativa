package grafo.core;

/**
 * Identificador tipado de um vértice.
 *
 * No projeto, os vértices também são representados diretamente por int para
 * manter compatibilidade com os contratos dos algoritmos da seção 8.
 */
public record Vertice(int id) {

    public Vertice {
        if (id < 0) {
            throw new IllegalArgumentException("O id do vértice não pode ser negativo.");
        }
    }
}
