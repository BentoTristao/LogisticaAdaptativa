package evolucao.estatistica;

import modelo.genoma.GenomaEntregador;
import modelo.obstaculo.PlanoObstaculos;

import java.util.Objects;

/**
 * Registro de métricas e estados dos melhores indivíduos ao fim de cada geração.
 */
public record EstatisticasGeracao(int geracao, double melhorEntregador, double mediaEntregador, double melhorObstaculo, double mediaObstaculo, GenomaEntregador melhorGenomaEntregador, PlanoObstaculos melhorPlano, int[] contagemBloqueiosPorAresta) {
    public EstatisticasGeracao {
        if (geracao < 0) {
            throw new IllegalArgumentException("O número da geração não pode ser negativo.");
        }
        Objects.requireNonNull(melhorGenomaEntregador, "O melhor genoma do entregador não pode ser nulo.");
        Objects.requireNonNull(melhorPlano, "O melhor plano não pode ser nulo.");
        contagemBloqueiosPorAresta = contagemBloqueiosPorAresta != null ? contagemBloqueiosPorAresta.clone() : new int[0];
    }
}
