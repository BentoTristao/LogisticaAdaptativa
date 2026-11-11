package modelo.resultado;

import modelo.obstaculo.Obstaculo;
import java.util.List;

/**
 * Estrutura de dados contendo o histórico tick a tick da simulação para animação/replay no front-end.
 */
public record ReplaySimulacao(
        ResultadoSimulacao resultado,
        int[] verticePorTick,
        List<Obstaculo> ativacoes,
        List<int[]> rotasPlanejadas,
        int[] ticksDosRecalculos
) {
    public ReplaySimulacao {
        if (resultado == null) {
            throw new IllegalArgumentException("ResultadoSimulacao não pode ser nulo.");
        }
        verticePorTick = verticePorTick != null ? verticePorTick.clone() : new int[0];
        ativacoes = ativacoes != null ? List.copyOf(ativacoes) : List.of();
        rotasPlanejadas = rotasPlanejadas != null ? List.copyOf(rotasPlanejadas) : List.of();
        ticksDosRecalculos = ticksDosRecalculos != null ? ticksDosRecalculos.clone() : new int[0];
    }
}
