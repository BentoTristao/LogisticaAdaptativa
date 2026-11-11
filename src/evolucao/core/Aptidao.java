package evolucao.core;

import modelo.resultado.ResultadoSimulacao;

/**
 * Calculadora das funções de aptidão (fitness) para Entregadores e Planejadores de Obstáculos
 * conforme o Guia do Projeto (seção 5.3).
 */
public final class Aptidao {
    private Aptidao() {
        // Classe utilitária
    }

    /**
     * Fitness do Entregador (quanto maior, melhor).
     * Se chegou: C_base + alpha * (T_max - T_chegada) - beta * recalculos - gamma * passosEmObstaculos
     * Se não chegou: crédito parcial proporcional ao progresso percorrido.
     */
    public static double calcularFitnessEntregador(ResultadoSimulacao res, double tIdeal, double cBase, double alpha) {
        if (res == null) return 0.0;

        double tMax = 4.0 * tIdeal;
        double beta = 2.0;  // Penalidade por recálculo
        double gamma = 3.0; // Penalidade por lentidão em obstáculo

        if (res.chegou()) {
            double ganhoTempo = alpha * (tMax - res.ticks());
            double penalidades = beta * res.recalculos() + gamma * res.passosEmObstaculos();
            return Math.max(1.0, cBase + ganhoTempo - penalidades);
        } else {
            // Crédito parcial por progresso em direção ao destino
            return Math.max(0.1, cBase * 0.1 * res.progresso());
        }
    }

    /**
     * Sobrecarga padrão para o fitness do Entregador.
     */
    public static double calcularFitnessEntregador(ResultadoSimulacao res, double tIdeal) {
        return calcularFitnessEntregador(res, tIdeal, 1000.0, 1.0);
    }

    /**
     * Fitness do Planejador de Obstáculos (quanto maior, melhor).
     * F = alpha * (T_chegada - T_ideal) - delta * CustoObstaculosUsados
     */
    public static double calcularFitnessObstaculo(ResultadoSimulacao res, double tIdeal, double alpha, double beta, double delta) {
        if (res == null) return 0.0;

        double tMax = 4.0 * tIdeal;
        double tChegada = res.chegou() ? res.ticks() : tMax;
        double atrasoCausado = Math.max(0.0, tChegada - tIdeal);

        // Penaliza orçamento de desastre desperdiçado em áreas que o entregador não visitou
        double penalidadeDesperdicio = delta * res.custoObstaculosUsados();

        double fitness = alpha * atrasoCausado - penalidadeDesperdicio;
        return Math.max(0.0, fitness);
    }

    /**
     * Sobrecarga padrão para o fitness do Obstáculo.
     */
    public static double calcularFitnessObstaculo(ResultadoSimulacao res, double tIdeal) {
        return calcularFitnessObstaculo(res, tIdeal, 1.0, 2.0, 3.0);
    }
}
