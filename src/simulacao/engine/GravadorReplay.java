package simulacao.engine;

import modelo.obstaculo.Obstaculo;
import modelo.resultado.ReplaySimulacao;
import modelo.resultado.ResultadoSimulacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Acumulador de ticks e eventos durante a execução da simulação para geração do ReplaySimulacao.
 */
public class GravadorReplay {
    private final List<Integer> posicoesPorTick;
    private final List<Obstaculo> ativacoes;
    private final List<int[]> rotasPlanejadas;
    private final List<Integer> ticksDosRecalculos;

    public GravadorReplay() {
        this.posicoesPorTick = new ArrayList<>();
        this.ativacoes = new ArrayList<>();
        this.rotasPlanejadas = new ArrayList<>();
        this.ticksDosRecalculos = new ArrayList<>();
    }

    public void registrarTick(int posicaoEntregador) {
        posicoesPorTick.add(posicaoEntregador);
    }

    public void registrarAtivacao(Obstaculo obstaculo) {
        if (obstaculo != null) {
            ativacoes.add(obstaculo);
        }
    }

    public void registrarRecalculo(int tick, List<Integer> rota) {
        ticksDosRecalculos.add(tick);
        if (rota != null) {
            int[] arr = rota.stream().mapToInt(Integer::intValue).toArray();
            rotasPlanejadas.add(arr);
        } else {
            rotasPlanejadas.add(new int[0]);
        }
    }

    public ReplaySimulacao finalizar(ResultadoSimulacao resultado) {
        int[] verticeArr = posicoesPorTick.stream().mapToInt(Integer::intValue).toArray();
        int[] ticksRec = ticksDosRecalculos.stream().mapToInt(Integer::intValue).toArray();

        return new ReplaySimulacao(resultado, verticeArr, ativacoes, rotasPlanejadas, ticksRec);
    }
}
