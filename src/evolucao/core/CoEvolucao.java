package evolucao.core;

import evolucao.estatistica.EstatisticasGeracao;
import evolucao.estatistica.HallOfFame;
import evolucao.listener.CoEvolucaoListener;
import evolucao.operadores.Crossover;
import evolucao.operadores.Mutacao;
import evolucao.operadores.Selecao;
import grafo.analise.AnalisadorConectividade;
import grafo.busca.Dijkstra;
import grafo.core.Grafo;
import modelo.config.Configuracao;
import modelo.genoma.GenomaEntregador;
import modelo.genoma.GenomaObstaculos;
import modelo.resultado.ResultadoSimulacao;
import simulacao.engine.MotorSimulacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Executor assíncrono do loop co-evolutivo (Rainha Vermelha com Hall of Fame).
 */
public class CoEvolucao implements Runnable {
    private final Grafo grafo;
    private final Configuracao cfg;
    private final MotorSimulacao motor;
    private final CoEvolucaoListener listener;
    private final Random rng;

    private Thread thread;
    private volatile boolean rodando = false;
    private volatile boolean pausado = false;

    private List<GenomaEntregador> popEntregador;
    private List<GenomaObstaculos> popObstaculo;

    private final HallOfFame<GenomaEntregador> hofEntregador;
    private final HallOfFame<GenomaObstaculos> hofObstaculo;

    public CoEvolucao(Grafo grafo, Configuracao cfg, MotorSimulacao motor, CoEvolucaoListener listener) {
        Objects.requireNonNull(grafo, "Grafo não pode ser nulo.");
        Objects.requireNonNull(cfg, "Configuração não pode ser nula.");
        Objects.requireNonNull(motor, "MotorSimulacao não pode ser nulo.");

        this.grafo = grafo;
        this.cfg = cfg;
        this.motor = motor;
        this.listener = listener;
        this.rng = new Random(cfg.seed);

        this.hofEntregador = new HallOfFame<>(30);
        this.hofObstaculo = new HallOfFame<>(30);
    }

    public synchronized void iniciar() {
        if (rodando) return;
        rodando = true;
        pausado = false;
        thread = new Thread(this, "CoEvolucao-Thread");
        thread.start();
    }

    public synchronized void pausar() {
        pausado = true;
    }

    public synchronized void retomar() {
        pausado = false;
        notifyAll();
    }

    public synchronized void parar() {
        rodando = false;
        pausado = false;
        notifyAll();
    }

    @Override
    public void run() {
        inicializarPopulacoes();

        List<Integer> rotaIdeal = Dijkstra.caminho(grafo, grafo.pesosBase(), grafo.verticeInicio(), grafo.verticeDestino());
        double tIdeal = Math.max(1.0, (double) rotaIdeal.size());
        int maxTick = (int) (4 * tIdeal);

        for (int geracao = 1; geracao <= cfg.geracoes && rodando; geracao++) {
            checarPausa();
            if (!rodando) break;

            int nEnt = popEntregador.size();
            int nObs = popObstaculo.size();

            double[] fitEnt = new double[nEnt];
            double[] fitObs = new double[nObs];

            // 1. AMOSTRAGEM E AVALIAÇÃO DE CONFRONTOS
            for (int i = 0; i < nEnt; i++) {
                GenomaEntregador e = popEntregador.get(i);
                double somaFit = 0;
                for (int c = 0; c < cfg.confrontosPorIndividuo; c++) {
                    GenomaObstaculos o = sortearOponenteObstaculo();
                    ResultadoSimulacao res = motor.executar(grafo, e, o.plano(), cfg, rng.nextLong());
                    somaFit += Aptidao.calcularFitnessEntregador(res, tIdeal, 1000.0, 1.0);
                }
                fitEnt[i] = somaFit / cfg.confrontosPorIndividuo;
            }

            for (int j = 0; j < nObs; j++) {
                GenomaObstaculos o = popObstaculo.get(j);
                double somaFit = 0;
                for (int c = 0; c < cfg.confrontosPorIndividuo; c++) {
                    GenomaEntregador e = sortearOponenteEntregador();
                    ResultadoSimulacao res = motor.executar(grafo, e, o.plano(), cfg, rng.nextLong());
                    somaFit += Aptidao.calcularFitnessObstaculo(res, tIdeal, 1.0, 2.0, 3.0);
                }
                fitObs[j] = somaFit / cfg.confrontosPorIndividuo;
            }

            // 2. REGISTRO NO HALL OF FAME
            int melhorEntIdx = obterMelhorIndice(fitEnt);
            int melhorObsIdx = obterMelhorIndice(fitObs);

            hofEntregador.adicionar(popEntregador.get(melhorEntIdx), fitEnt[melhorEntIdx]);
            hofObstaculo.adicionar(popObstaculo.get(melhorObsIdx), fitObs[melhorObsIdx]);

            // Heatmap de contagem de bloqueios por aresta
            int[] bloqueiosPorAresta = new int[grafo.numArestas()];
            for (GenomaObstaculos go : popObstaculo) {
                go.plano().obstaculos().forEach(o -> {
                    if (o.arestaId() >= 0 && o.arestaId() < bloqueiosPorAresta.length) {
                        bloqueiosPorAresta[o.arestaId()]++;
                    }
                });
            }

            // Notifica Listener com estatísticas
            if (listener != null) {
                double mediaEnt = calcularMedia(fitEnt);
                double mediaObs = calcularMedia(fitObs);

                EstatisticasGeracao est = new EstatisticasGeracao(geracao, fitEnt[melhorEntIdx], mediaEnt, fitObs[melhorObsIdx], mediaObs, popEntregador.get(melhorEntIdx), popObstaculo.get(melhorObsIdx).plano(), bloqueiosPorAresta);
                listener.aoFimDaGeracao(est);
            }

            // 3. REPRODUÇÃO - ENTREGADORES
            popEntregador = evoluirEntregadores(popEntregador, fitEnt);

            // 4. REPRODUÇÃO - OBSTÁCULOS
            popObstaculo = evoluirObstaculos(popObstaculo, fitObs, maxTick);
        }

        rodando = false;
        if (listener != null) {
            listener.aoTerminar();
        }
    }

    private void inicializarPopulacoes() {
        popEntregador = new ArrayList<>();
        for (int i = 0; i < cfg.tamPopEntregador; i++) {
            popEntregador.add(GenomaEntregador.aleatorio(rng));
        }

        popObstaculo = new ArrayList<>();
        for (int i = 0; i < cfg.tamPopObstaculo; i++) {
            GenomaObstaculos go = GenomaObstaculos.aleatorio(grafo, cfg.maxObstaculos, 100, rng);
            go = new GenomaObstaculos(AnalisadorConectividade.reparar(grafo, go.plano(), cfg.orcamentoDesastre));
            popObstaculo.add(go);
        }
    }

    private GenomaEntregador sortearOponenteEntregador() {
        if (!hofEntregador.estaVazio() && rng.nextBoolean()) {
            return hofEntregador.sortear(rng);
        }
        return popEntregador.get(rng.nextInt(popEntregador.size()));
    }

    private GenomaObstaculos sortearOponenteObstaculo() {
        if (!hofObstaculo.estaVazio() && rng.nextBoolean()) {
            return hofObstaculo.sortear(rng);
        }
        return popObstaculo.get(rng.nextInt(popObstaculo.size()));
    }

    private List<GenomaEntregador> evoluirEntregadores(List<GenomaEntregador> pop, double[] fits) {
        List<GenomaEntregador> nova = new ArrayList<>();
        List<Integer> ordenados = obterIndicesOrdenadosDecrescente(fits);
        
        // Elitismo: preserva os 2 melhores
        nova.add(pop.get(ordenados.get(0)));
        if (ordenados.size() > 1) nova.add(pop.get(ordenados.get(1)));

        while (nova.size() < cfg.tamPopEntregador) {
            GenomaEntregador p1 = Selecao.torneio(pop, fits, 3, rng);
            GenomaEntregador p2 = Selecao.torneio(pop, fits, 3, rng);

            GenomaEntregador filho = rng.nextDouble() < cfg.taxaCrossover
                    ? Crossover.blxAlfa(p1, p2, 0.3, rng)
                    : p1;

            filho = Mutacao.gaussiana(filho, cfg.taxaMutacao, 0.1, rng);
            nova.add(filho);
        }
        return nova;
    }

    private List<GenomaObstaculos> evoluirObstaculos(List<GenomaObstaculos> pop, double[] fits, int maxTick) {
        List<GenomaObstaculos> nova = new ArrayList<>();
        List<Integer> ordenados = obterIndicesOrdenadosDecrescente(fits);
        
        // Elitismo: preserva os 2 melhores
        nova.add(pop.get(ordenados.get(0)));
        if (ordenados.size() > 1) nova.add(pop.get(ordenados.get(1)));

        while (nova.size() < cfg.tamPopObstaculo) {
            GenomaObstaculos p1 = Selecao.torneio(pop, fits, 3, rng);
            GenomaObstaculos p2 = Selecao.torneio(pop, fits, 3, rng);

            GenomaObstaculos filho = rng.nextDouble() < cfg.taxaCrossover
                    ? Crossover.pontoUnico(p1, p2, rng)
                    : p1;

            filho = Mutacao.uniforme(filho, cfg.taxaMutacao, grafo, maxTick, rng);
            filho = new GenomaObstaculos(AnalisadorConectividade.reparar(grafo, filho.plano(), cfg.orcamentoDesastre));

            nova.add(filho);
        }
        return nova;
    }

    private synchronized void checarPausa() {
        while (pausado && rodando) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                rodando = false;
            }
        }
    }

    private int obterMelhorIndice(double[] fits) {
        int idx = 0;
        for (int i = 1; i < fits.length; i++) {
            if (fits[i] > fits[idx]) idx = i;
        }
        return idx;
    }

    private List<Integer> obterIndicesOrdenadosDecrescente(double[] fits) {
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < fits.length; i++) idxs.add(i);
        idxs.sort((a, b) -> Double.compare(fits[b], fits[a]));
        return idxs;
    }

    private double calcularMedia(double[] vals) {
        double s = 0;
        for (double v : vals) s += v;
        return vals.length > 0 ? s / vals.length : 0;
    }

    public boolean estaRodando() { return rodando; }
    public boolean estaPausado() { return pausado; }
}
