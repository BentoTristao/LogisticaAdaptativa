package ui.telas;

import evolucao.core.CoEvolucao;
import evolucao.estatistica.EstatisticasGeracao;
import evolucao.listener.CoEvolucaoListener;
import modelo.genoma.GenomaEntregador;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import ui.componentes.Botao;
import ui.componentes.GraficoLinha;
import util.ExporterIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Tela de acompanhamento da co-evolução em tempo real e análise de resultados.
 * Implementa CoEvolucaoListener respeitando o isolamento entre a thread de evolução
 * e a thread de renderização do Processing.
 */
public class TelaEvolucao implements CoEvolucaoListener {
    private final GraficoLinha graficoEntregador;
    private final GraficoLinha graficoObstaculo;
    private final List<Botao> botoes = new ArrayList<>();
    private final List<EstatisticasGeracao> historico = new ArrayList<>();
    // Fila thread-safe para transferir métricas da thread de CoEvolucao para a thread gráfica do Processing
    private final ConcurrentLinkedQueue<EstatisticasGeracao> filaRecebidos = new ConcurrentLinkedQueue<>();

    private CoEvolucao coEvolucao;
    private volatile EstatisticasGeracao ultimaEstatistica;
    private volatile boolean evolucaoConcluida = false;

    private Botao botaoPausarRetomar;
    private PImage imagemFundo;
    private boolean assetsCarregados = false;
    private String mensagemFeedback = "";

    public TelaEvolucao(float larguraTela, float alturaTela, Consumer<EstatisticasGeracao> onVisualizarMelhorConfronto, Runnable onVoltarMenu) {
        float graficoX = 36.0f;
        float graficoY = 66.0f;
        float graficoW = larguraTela * 0.54f;
        float alturaTotalGraficos = alturaTela - 174.0f;
        float gapGraficos = 14.0f;
        float graficoH = (alturaTotalGraficos - gapGraficos) / 2.0f;

        this.graficoEntregador = new GraficoLinha(graficoX, graficoY, graficoW, graficoH, "Fitness por Geração — Entregadores", "Melhor da Geração", 80, 220, 140, "Média da População", 155, 238, 190);
        this.graficoEntregador.setValorMinimoEixoY(1100.0);
        this.graficoObstaculo = new GraficoLinha(graficoX, graficoY + graficoH + gapGraficos, graficoW, graficoH, "Fitness por Geração — Obstáculos", "Melhor da Geração", 255, 110, 110, "Média da População", 255, 180, 135);

        float rodapeY = alturaTela - 72.0f;
        float btnW = 175.0f;
        float btnH = 42.0f;
        float gap = 16.0f;
        float startX = 36.0f;

        botaoPausarRetomar = new Botao(startX, rodapeY, btnW, btnH, "Pausar", this::alternarPausa);
        Botao botaoParar = new Botao(startX + (btnW + gap), rodapeY, btnW, btnH, "Parar", this::pararEvolucao);
        Botao botaoReplay = new Botao(startX + (btnW + gap) * 2, rodapeY, btnW, btnH, "Ver Simulação", () -> {
            EstatisticasGeracao atual = ultimaEstatistica;
            if (atual != null && onVisualizarMelhorConfronto != null) {
                onVisualizarMelhorConfronto.accept(atual);
            }
        });
        Botao botaoExportar = new Botao(startX + (btnW + gap) * 3, rodapeY, btnW, btnH, "Exportar CSV", this::exportarCSV);
        Botao botaoMenu = new Botao(startX + (btnW + gap) * 4, rodapeY, btnW, btnH, "Menu Principal", () -> {
            pararEvolucao();
            if (onVoltarMenu != null) {
                onVoltarMenu.run();
            }
        });

        botoes.addAll(List.of(botaoPausarRetomar, botaoParar, botaoReplay, botaoExportar, botaoMenu));
    }

    public void vincularCoEvolucao(CoEvolucao coEvolucao) {
        this.coEvolucao = coEvolucao;
        this.historico.clear();
        this.filaRecebidos.clear();
        this.graficoEntregador.limpar();
        this.graficoObstaculo.limpar();
        this.ultimaEstatistica = null;
        this.evolucaoConcluida = false;
        this.mensagemFeedback = "";
        this.botaoPausarRetomar.setRotulo("Pausar");
        this.botaoPausarRetomar.setHabilitado(true);
    }

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/fundo_evolucao.png").exists()) {
            imagemFundo = p.loadImage("data/fundo_evolucao.png");
        }
        graficoEntregador.carregarFundo(p, "data/fundo_grafico.png");
        graficoObstaculo.carregarFundo(p, "data/fundo_grafico.png");

        for (Botao b : botoes) {
            b.carregarImagemHover(p, "data/btn_hover.png");
        }
    }

    // Invocado pela thread em segundo plano de CoEvolucao; apenas enfileira sem tocar na UI
    @Override
    public void aoFimDaGeracao(EstatisticasGeracao estatisticas) {
        if (estatisticas != null) {
            filaRecebidos.add(estatisticas);
            ultimaEstatistica = estatisticas;
        }
    }

    @Override
    public void aoTerminar() {
        evolucaoConcluida = true;
    }

    public void desenhar(PApplet p) {
        if (p == null) return;
        inicializarAssets(p);
        consumirFilaPendentes();

        p.pushStyle();
        p.background(18, 22, 30);

        if (imagemFundo != null) {
            p.image(imagemFundo, 0, 0, p.width, p.height);
        }

        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.fill(240, 244, 248);
        p.textSize(24);
        p.text("Painel de Co-evolução Competitiva", 36.0f, 34.0f);

        graficoEntregador.desenhar(p);
        graficoObstaculo.desenhar(p);

        float painelX = 36.0f + p.width * 0.54f + 24.0f;
        float painelY = 66.0f;
        desenharTelemetria(p, painelX, painelY);

        if (!mensagemFeedback.isEmpty()) {
            p.textAlign(PConstants.LEFT, PConstants.CENTER);
            p.textSize(13);
            p.fill(110, 230, 155);
            p.text(mensagemFeedback, 36.0f, p.height - 96.0f);
        }

        for (Botao botao : botoes) {
            botao.desenhar(p);
        }

        p.popStyle();
    }

    // Drena os resultados acumulados na thread de renderização antes de atualizar o gráfico
    private void consumirFilaPendentes() {
        EstatisticasGeracao est;
        while ((est = filaRecebidos.poll()) != null) {
            historico.add(est);
            graficoEntregador.adicionarPonto(est.geracao(), est.melhorEntregador(), est.mediaEntregador());
            graficoObstaculo.adicionarPonto(est.geracao(), est.melhorObstaculo(), est.mediaObstaculo());
        }
    }

    private void desenharTelemetria(PApplet p, float px, float py) {
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        float linhaY = py;

        p.fill(110, 210, 255);
        p.textSize(17);
        p.text("Legenda e Telemetria da Geração", px, linhaY);
        linhaY += 26.0f;

        String statusTexto = evolucaoConcluida ? "Concluída" : (coEvolucao != null && coEvolucao.estaPausado() ? "Pausada" : "Em execução");

        EstatisticasGeracao est = ultimaEstatistica;
        if (est == null) {
            p.fill(225, 232, 240);
            p.textSize(13);
            p.text("Status: " + statusTexto, px, linhaY);
            linhaY += 22.0f;
            p.fill(160, 170, 185);
            p.text("Inicializando populações...", px, linhaY);
            return;
        }

        p.fill(225, 232, 240);
        p.textSize(13);
        p.text("Status: " + statusTexto + "   |   Geração Atual: " + est.geracao(), px, linhaY);
        linhaY += 28.0f;

        // 1. Gráfico Superior: Entregadores
        p.fill(80, 220, 140);
        p.textSize(14);
        p.text("1. Entregadores (Gráfico Superior)", px, linhaY);
        linhaY += 19.0f;

        p.fill(175, 186, 202);
        p.textSize(11);
        p.text("Aptidão (pts): rapidez na entrega menos recálculos e atrasos.", px, linhaY);
        linhaY += 18.0f;

        desenharLinhaIndicadora(p, px, linhaY + 7.0f, 80, 220, 140, 2.6f);
        p.fill(230, 238, 245);
        p.textSize(12);
        p.text(String.format(Locale.US, "Melhor da geração: %.2f pts (líder atual)", est.melhorEntregador()), px + 22.0f, linhaY);
        linhaY += 19.0f;

        desenharLinhaIndicadora(p, px, linhaY + 7.0f, 155, 238, 190, 1.8f);
        p.fill(205, 220, 232);
        p.text(String.format(Locale.US, "Média da população: %.2f pts (desempenho geral)", est.mediaEntregador()), px + 22.0f, linhaY);
        linhaY += 26.0f;

        // 2. Gráfico Inferior: Obstáculos
        p.fill(255, 110, 110);
        p.textSize(14);
        p.text("2. Obstáculos (Gráfico Inferior)", px, linhaY);
        linhaY += 19.0f;

        p.fill(175, 186, 202);
        p.textSize(11);
        p.text("Aptidão (pts): atraso imposto ao entregador menos custo gasto.", px, linhaY);
        linhaY += 18.0f;

        desenharLinhaIndicadora(p, px, linhaY + 7.0f, 255, 110, 110, 2.6f);
        p.fill(230, 238, 245);
        p.textSize(12);
        p.text(String.format(Locale.US, "Melhor da geração: %.2f pts (plano mais eficaz)", est.melhorObstaculo()), px + 22.0f, linhaY);
        linhaY += 19.0f;

        desenharLinhaIndicadora(p, px, linhaY + 7.0f, 255, 180, 135, 1.8f);
        p.fill(205, 220, 232);
        p.text(String.format(Locale.US, "Média da população: %.2f pts (desempenho geral)", est.mediaObstaculo()), px + 22.0f, linhaY);
        linhaY += 26.0f;

        // 3. Genes do Melhor Entregador
        GenomaEntregador g = est.melhorGenomaEntregador();
        p.fill(110, 210, 255);
        p.textSize(14);
        p.text("Estratégia do Melhor Entregador (Genes)", px, linhaY);
        linhaY += 20.0f;

        p.fill(220, 228, 238);
        p.textSize(12);
        p.text(String.format(Locale.US, "g1 = %.2f — Peso do tempo/custo das arestas", g.g1()), px, linhaY);
        linhaY += 19.0f;
        p.text(String.format(Locale.US, "g2 = %.2f — Foco da heurística A* até o destino", g.g2()), px, linhaY);
        linhaY += 19.0f;
        p.text(String.format(Locale.US, "g3 = %.2f — Aversão a vias com risco/atraso", g.g3()), px, linhaY);
        linhaY += 19.0f;
        p.text(String.format(Locale.US, "g4 = %.2f — Preferência por cruzamentos (hubs)", g.g4()), px, linhaY);
        linhaY += 19.0f;
        p.text(String.format(Locale.US, "g5 = %.1f%% — Tolerância de atraso p/ recalcular rota", g.g5() * 100.0), px, linhaY);
        linhaY += 25.0f;

        // 4. Melhor Plano de Obstáculos
        p.fill(110, 210, 255);
        p.textSize(14);
        p.text("Melhor Plano de Obstáculos", px, linhaY);
        linhaY += 20.0f;

        p.fill(220, 228, 238);
        p.textSize(12);
        p.text("Eventos ativos: " + est.melhorPlano().obstaculos().size() + " obstáculo(s) programado(s)", px, linhaY);
        linhaY += 19.0f;
        p.text("Orçamento usado: " + est.melhorPlano().custoTotal() + " pts de desastre", px, linhaY);
        linhaY += 18.0f;
        p.fill(175, 186, 202);
        p.textSize(11);
        p.text("Custos: Desvio (1pt), Congestionamento (2pt), Bloqueio (3pt)", px, linhaY);
    }

    private void desenharLinhaIndicadora(PApplet p, float x1, float y1, int r, int g, int b, float espessura) {
        p.stroke(r, g, b);
        p.strokeWeight(espessura);
        p.line(x1, y1, x1 + 16.0f, y1);
    }

    private void alternarPausa() {
        if (coEvolucao == null || evolucaoConcluida) return;
        if (coEvolucao.estaPausado()) {
            coEvolucao.retomar();
            botaoPausarRetomar.setRotulo("Pausar");
        } else {
            coEvolucao.pausar();
            botaoPausarRetomar.setRotulo("Retomar");
        }
    }

    private void pararEvolucao() {
        if (coEvolucao != null && coEvolucao.estaRodando()) {
            coEvolucao.parar();
            botaoPausarRetomar.setHabilitado(false);
        }
    }

    private void exportarCSV() {
        if (historico.isEmpty()) {
            mensagemFeedback = "Nenhum dado disponível para exportação.";
            return;
        }
        String caminho = "data/estatisticas_evolucao.csv";
        try {
            ExporterIO.exportarEstatisticasCSV(List.copyOf(historico), caminho);
            mensagemFeedback = "Estatísticas exportadas para " + caminho;
        } catch (IOException ex) {
            mensagemFeedback = "Erro ao exportar CSV: " + ex.getMessage();
        }
    }

    public void mousePressionado(float px, float py) {
        for (Botao botao : botoes) {
            if (botao.clicar(px, py)) {
                break;
            }
        }
    }

    public List<EstatisticasGeracao> getHistorico() {
        return List.copyOf(historico);
    }

    public EstatisticasGeracao getUltimaEstatistica() {
        return ultimaEstatistica;
    }
}