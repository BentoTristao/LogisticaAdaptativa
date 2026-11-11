package modelo.resultado;

/**
 * Resultado estatístico consolidado de um confronto de simulação.
 */
public record ResultadoSimulacao(boolean chegou, int ticks, int recalculos, int passosEmObstaculos, double custoObstaculosUsados, double progresso) {
    public ResultadoSimulacao {
        if (ticks < 0) {
            throw new IllegalArgumentException("Ticks não podem ser negativos.");
        }
        if (recalculos < 0) {
            throw new IllegalArgumentException("Recálculos não podem ser negativos.");
        }
        if (passosEmObstaculos < 0) {
            throw new IllegalArgumentException("Passos em obstáculos não podem ser negativos.");
        }
        if (custoObstaculosUsados < 0.0) {
            throw new IllegalArgumentException("Custo de obstáculos não pode ser negativo.");
        }
        if (progresso < 0.0 || progresso > 1.0) {
            throw new IllegalArgumentException("Progresso deve estar entre 0.0 e 1.0.");
        }
    }
}
