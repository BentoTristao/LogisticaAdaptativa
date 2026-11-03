package modelo.obstaculo;

/** Tipos mínimos de obstáculo definidos na seção 4.1 do guia. */
public enum TipoObstaculo {

    DESVIO(2.0, 1),
    CONGESTIONAMENTO(5.0, 2),
    BLOQUEIO(Double.POSITIVE_INFINITY, 3);

    /** Valor somado ao peso-base da aresta (infinito no bloqueio total). */
    public final double acrescimo;

    /** Pontos de desastre consumidos pelo obstáculo. */
    public final int custo;

    TipoObstaculo(double acrescimo, int custo) {
        this.acrescimo = acrescimo;
        this.custo = custo;
    }

    /** Calcula o peso final da aresta a partir de seu peso-base. */
    public double aplicarAoPeso(double pesoBase) {
        if (Double.isNaN(pesoBase) || pesoBase < 0.0 || pesoBase == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("O peso-base deve ser não negativo.");
        }
        if (this == BLOQUEIO) {
            return Double.POSITIVE_INFINITY;
        }
        return pesoBase + acrescimo;
    }
}
