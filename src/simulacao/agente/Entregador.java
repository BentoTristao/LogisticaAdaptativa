package simulacao.agente;

import grafo.busca.AStar;
import grafo.busca.BfsVisao;
import grafo.core.Aresta;
import grafo.core.Grafo;
import modelo.genoma.GenomaEntregador;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Agente Entregador que navega pelo grafo utilizando visão parcial (névoa de guerra)
 * e o algoritmo A* com pesos parametrizados pelos genes do genoma.
 */
public class Entregador {
    private final Grafo grafo;
    private final GenomaEntregador genoma;
    private int posicaoAtual;
    private final double[] pesoConhecido;

    private List<Integer> rotaPlanejada;
    private int indiceRota;

    private int ticksNaArestaAtual;
    private int ticksNecessariosAresta;
    private Aresta arestaEmTravessia;

    private int recalculos;
    private int passosEmObstaculos;
    private double custoObstaculosUsados;
    private double custoRotaOriginal;

    private final Set<Integer> obstaculosContabilizados;

    public Entregador(Grafo grafo, GenomaEntregador genoma) {
        Objects.requireNonNull(grafo, "O grafo não pode ser nulo.");
        Objects.requireNonNull(genoma, "O genoma não pode ser nulo.");

        this.grafo = grafo;
        this.genoma = genoma;
        this.posicaoAtual = grafo.verticeInicio();

        this.pesoConhecido = grafo.pesosBase();
        this.rotaPlanejada = new ArrayList<>();
        this.indiceRota = 0;

        this.ticksNaArestaAtual = 0;
        this.ticksNecessariosAresta = 0;
        this.arestaEmTravessia = null;

        this.recalculos = 0;
        this.passosEmObstaculos = 0;
        this.custoObstaculosUsados = 0.0;
        this.custoRotaOriginal = 0.0;
        this.obstaculosContabilizados = new HashSet<>();
    }

    /**
     * Atualiza o mapa de conhecimento do Entregador com base no raio de visão (BFS).
     */
    public void atualizarVisao(int raioVisao, double[] pesosReais) {
        List<Integer> verticesVisiveis = BfsVisao.bfsAteRaio(grafo, posicaoAtual, raioVisao);
        Set<Integer> conjuntoVisiveis = new HashSet<>(verticesVisiveis);

        for (int v : verticesVisiveis) {
            for (Aresta a : grafo.arestasDe(v)) {
                if (conjuntoVisiveis.contains(a.outro(v))) {
                    pesoConhecido[a.id()] = pesosReais[a.id()];
                }
            }
        }
    }

    /**
     * Avalia se é necessário recalcular a rota planejada.
     */
    public boolean deveRecalcular() {
        if (rotaPlanejada.isEmpty() || indiceRota >= rotaPlanejada.size()) {
            return true;
        }

        // Bloqueio total na próxima aresta do caminho planejado
        int proximaArestaId = rotaPlanejada.get(indiceRota);
        if (Double.isInfinite(pesoConhecido[proximaArestaId])) {
            return true;
        }

        // Disparo por aumento percentual no custo da rota remanescente (gene g5)
        if (custoRotaOriginal > 0.0) {
            double custoAtualRemanente = calcularCustoRemanescenteConhecido();
            double aumento = (custoAtualRemanente - custoRotaOriginal) / custoRotaOriginal;
            if (aumento > genoma.g5()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Recalcula a rota utilizando A* com pesos parametrizados pelos genes.
     */
    public void recalcularRota() {
        double[] pesosCustomizados = construirPesosCustomizados();
        List<Integer> novaRota = AStar.caminho(grafo, pesosCustomizados, posicaoAtual, grafo.verticeDestino(), genoma.g2());

        this.rotaPlanejada = novaRota;
        this.indiceRota = 0;
        this.custoRotaOriginal = calcularCustoRemanescenteCustomizado(novaRota, 0, pesosCustomizados);
        this.recalculos++;
    }

    /**
     * Executa 1 tick de movimento do Entregador.
     */
    public void mover(double[] pesosReais, double[] custosObstaculosPorAresta) {
        if (posicaoAtual == grafo.verticeDestino()) {
            return;
        }

        // Se já está no meio da travessia de uma aresta de múltiplos ticks
        if (arestaEmTravessia != null) {
            ticksNaArestaAtual++;
            if (ticksNaArestaAtual >= ticksNecessariosAresta) {
                posicaoAtual = arestaEmTravessia.outro(posicaoAtual);
                arestaEmTravessia = null;
                ticksNaArestaAtual = 0;
                ticksNecessariosAresta = 0;
                indiceRota++;
            }
            return;
        }

        if (deveRecalcular()) {
            recalcularRota();
        }

        if (rotaPlanejada.isEmpty() || indiceRota >= rotaPlanejada.size()) {
            return;
        }

        int proximaArestaId = rotaPlanejada.get(indiceRota);
        double pesoReal = pesosReais[proximaArestaId];

        // Bloqueio total real
        if (Double.isInfinite(pesoReal)) {
            if (custosObstaculosPorAresta != null && !obstaculosContabilizados.contains(proximaArestaId)) {
                custoObstaculosUsados += custosObstaculosPorAresta[proximaArestaId];
                obstaculosContabilizados.add(proximaArestaId);
            }
            recalcularRota();
            return;
        }

        Aresta aresta = grafo.arestas().get(proximaArestaId);
        int tempoNecessario = (int) Math.ceil(pesoReal);

        if (pesoReal > 1.0) {
            passosEmObstaculos++;
            if (custosObstaculosPorAresta != null && !obstaculosContabilizados.contains(proximaArestaId)) {
                custoObstaculosUsados += custosObstaculosPorAresta[proximaArestaId];
                obstaculosContabilizados.add(proximaArestaId);
            }
        }

        if (tempoNecessario <= 1) {
            posicaoAtual = aresta.outro(posicaoAtual);
            indiceRota++;
        } else {
            arestaEmTravessia = aresta;
            ticksNaArestaAtual = 1;
            ticksNecessariosAresta = tempoNecessario;
        }
    }

    private double[] construirPesosCustomizados() {
        double[] custom = new double[grafo.numArestas()];
        for (Aresta a : grafo.arestas()) {
            double pesoK = pesoConhecido[a.id()];
            if (Double.isInfinite(pesoK)) {
                custom[a.id()] = Double.POSITIVE_INFINITY;
                continue;
            }

            int vTarget = a.destino();
            int riscoTarget = calcularRisco(vTarget);
            int grauTarget = calcularGrauLivre(vTarget);

            // Custo = g1 * peso + g3 * Risco(v) - g4 * Grau(v)
            double c = genoma.g1() * pesoK + genoma.g3() * riscoTarget - genoma.g4() * grauTarget;
            custom[a.id()] = Math.max(0.001, c);
        }
        return custom;
    }

    private int calcularRisco(int vertice) {
        int risco = 0;
        for (Aresta a : grafo.arestasDe(vertice)) {
            if (pesoConhecido[a.id()] > 1.0) {
                risco++;
            }
        }
        return risco;
    }

    private int calcularGrauLivre(int vertice) {
        int grau = 0;
        for (Aresta a : grafo.arestasDe(vertice)) {
            if (pesoConhecido[a.id()] == 1.0) {
                grau++;
            }
        }
        return grau;
    }

    private double calcularCustoRemanescenteConhecido() {
        double total = 0.0;
        for (int i = indiceRota; i < rotaPlanejada.size(); i++) {
            total += pesoConhecido[rotaPlanejada.get(i)];
        }
        return total;
    }

    private double calcularCustoRemanescenteCustomizado(List<Integer> rota, int inicio, double[] pesosCustom) {
        double total = 0.0;
        for (int i = inicio; i < rota.size(); i++) {
            total += pesosCustom[rota.get(i)];
        }
        return total;
    }

    // Getters
    public int posicaoAtual() { return posicaoAtual; }
    public List<Integer> rotaPlanejada() { return List.copyOf(rotaPlanejada); }
    public int recalculos() { return recalculos; }
    public int passosEmObstaculos() { return passosEmObstaculos; }
    public double custoObstaculosUsados() { return custoObstaculosUsados; }
}
