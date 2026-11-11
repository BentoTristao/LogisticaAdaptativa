package simulacao.engine;

import grafo.analise.AnalisadorConectividade;
import grafo.busca.Dijkstra;
import grafo.core.Grafo;
import modelo.config.Configuracao;
import modelo.genoma.GenomaEntregador;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.PlanoObstaculos;
import modelo.resultado.ReplaySimulacao;
import modelo.resultado.ResultadoSimulacao;
import simulacao.agente.Entregador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Motor de execução discreto tick-a-tick da simulação entre Entregador e PlanoObstaculos.
 */
public class MotorSimulacao {
    public MotorSimulacao() {
        // Motor reutilizável sem estado persistente
    }

    /**
     * Executa a simulação sem gerar histórico detalhado de replay.
     */
    public ResultadoSimulacao executar(Grafo grafo, GenomaEntregador entregadorGenoma, PlanoObstaculos planoObstaculos, Configuracao cfg, long seed) {
        return executarInterno(grafo, entregadorGenoma, planoObstaculos, cfg, null);
    }

    /**
     * Executa a simulação registrando todos os passos para replay visual.
     */
    public ReplaySimulacao executarComReplay(Grafo grafo, GenomaEntregador entregadorGenoma, PlanoObstaculos planoObstaculos, Configuracao cfg, long seed) {
        GravadorReplay gravador = new GravadorReplay();
        ResultadoSimulacao res = executarInterno(grafo, entregadorGenoma, planoObstaculos, cfg, gravador);
        return gravador.finalizar(res);
    }

    private ResultadoSimulacao executarInterno(Grafo g, GenomaEntregador entregadorGenoma, PlanoObstaculos plano, Configuracao cfg, GravadorReplay gravador) {
        Objects.requireNonNull(g, "O grafo não pode ser nulo.");
        Objects.requireNonNull(entregadorGenoma, "O genoma do entregador não pode ser nulo.");

        // Repara o plano para proibir bloqueio de pontes e evitar estouro de orçamento
        PlanoObstaculos planoReparado = AnalisadorConectividade.reparar(g, plano != null ? plano : PlanoObstaculos.vazio(), cfg.orcamentoDesastre);

        double[] pesosReais = g.pesosBase();
        double[] custosObstaculosPorAresta = new double[g.numArestas()];

        Map<Integer, List<Obstaculo>> ativacoesPorTick = new HashMap<>();
        for (Obstaculo obs : planoReparado.obstaculos()) {
            ativacoesPorTick.computeIfAbsent(obs.tickAtivacao(), k -> new java.util.ArrayList<>()).add(obs);
        }

        // Tempo limite T_max = 4 * T_ideal
        List<Integer> rotaIdeal = Dijkstra.caminho(g, g.pesosBase(), g.verticeInicio(), g.verticeDestino());
        int tIdeal = Math.max(1, rotaIdeal.size());
        int tMax = 4 * tIdeal;

        Entregador agente = new Entregador(g, entregadorGenoma);
        double distInicial = tIdeal;

        for (int tick = 0; tick < tMax; tick++) {
            // 1. Ativa obstáculos agendados para este tick
            List<Obstaculo> novosObstaculos = ativacoesPorTick.get(tick);
            if (novosObstaculos != null) {
                for (Obstaculo obs : novosObstaculos) {
                    pesosReais[obs.arestaId()] = obs.tipo().aplicarAoPeso(pesosReais[obs.arestaId()]);
                    custosObstaculosPorAresta[obs.arestaId()] = obs.tipo().custo;
                    if (gravador != null) {
                        gravador.registrarAtivacao(obs);
                    }
                }
            }

            // 2. Agente atualiza visão
            agente.atualizarVisao(cfg.raioVisao, pesosReais);

            // 3. Registra recálculo no gravador se necessário
            if (gravador != null && agente.deveRecalcular()) {
                agente.recalcularRota();
                gravador.registrarRecalculo(tick, agente.rotaPlanejada());
            }

            // 4. Registra posição atual
            if (gravador != null) {
                gravador.registrarTick(agente.posicaoAtual());
            }

            // 5. Agente avança 1 tick
            agente.mover(pesosReais, custosObstaculosPorAresta);

            // 6. Condição de vitória
            if (agente.posicaoAtual() == g.verticeDestino()) {
                if (gravador != null) {
                    gravador.registrarTick(agente.posicaoAtual());
                }
                return new ResultadoSimulacao(true, tick + 1, agente.recalculos(), agente.passosEmObstaculos(), agente.custoObstaculosUsados(), 1.0);
            }
        }

        // Progresso parcial em caso de esgotamento de T_max
        List<Integer> rotaRestante = Dijkstra.caminho(g, g.pesosBase(), agente.posicaoAtual(), g.verticeDestino());
        double distRestante = rotaRestante.size();
        double progresso = Math.max(0.0, Math.min(1.0, 1.0 - (distRestante / distInicial)));

        return new ResultadoSimulacao( false, tMax, agente.recalculos(), agente.passosEmObstaculos(), agente.custoObstaculosUsados(), progresso);
    }
}
