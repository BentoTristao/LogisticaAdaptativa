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
- Tela de acompanhamento da co-evolução em tempo real e análise de resultados.
- Implementa CoEvolucaoListener respeitando o isolamento entre a thread de evolução
- e a thread de renderização do Processing.
 */
public class TelaEvolucao implements CoEvolucaoListener {
    private final GraficoLinha graficoFitness;
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
        float graficoY = 74.0f;
        float graficoW = larguraTela * 0.63f;
        float graficoH = alturaTela - 170.0f;

        this.graficoFitness = new GraficoLinha(graficoX, graficoY, graficoW, graficoH, "Fitness por Geração (Entregadores vs. Obstáculos)");

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
        this.graficoFitness.limpar();
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
        graficoFitness.carregarFundo(p, "data/fundo_grafico.png");

        for (Botao b : botoes) {
            b.carregarImagens(p, "data/btn_normal.png", "data/btn_hover.png");
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
        p.text("Painel de Co-evolução Competitiva", 36.0f, 36.0f);

        graficoFitness.desenhar(p);

        float painelX = 36.0f + p.width * 0.63f + 28.0f;
        float painelY = 82.0f;
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
            graficoFitness.adicionarPonto(est);
        }
    }

    private void desenharTelemetria(PApplet p, float px, float py) {
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        float linhaY = py;
        float passo = 28.0f;

        p.fill(110, 210, 255);
        p.textSize(17);
        p.text("Telemetria da Geração", px, linhaY);
        linhaY += passo * 1.3f;

        String statusTexto = evolucaoConcluida? "Concluída": (coEvolucao != null && coEvolucao.estaPausado()? "Pausada": "Em execução");

        p.fill(225, 232, 240);
        p.textSize(14);
        p.text("Status: " + statusTexto, px, linhaY);
        linhaY += passo;

        EstatisticasGeracao est = ultimaEstatistica;
        if (est == null) {
            p.fill(160, 170, 185);
            p.text("Inicializando populações...", px, linhaY);
            return;
        }

        p.text("Geração Atual: " + est.geracao(), px, linhaY);
        linhaY += passo * 1.3f;

        p.fill(80, 220, 140);
        p.text(String.format(Locale.US, "Melhor Entregador: %.2f", est.melhorEntregador()), px, linhaY);
        linhaY += passo;
        p.fill(170, 235, 195);
        p.text(String.format(Locale.US, "Média Entregador: %.2f", est.mediaEntregador()), px, linhaY);
        linhaY += passo * 1.3f;

        p.fill(255, 110, 110);
        p.text(String.format(Locale.US, "Melhor Obstáculo: %.2f", est.melhorObstaculo()), px, linhaY);
        linhaY += passo;
        p.fill(255, 185, 150);
        p.text(String.format(Locale.US, "Média Obstáculo: %.2f", est.mediaObstaculo()), px, linhaY);
        linhaY += passo * 1.4f;

        GenomaEntregador g = est.melhorGenomaEntregador();
        p.fill(110, 210, 255);
        p.text("Melhor Genoma (Entregador):", px, linhaY);
        linhaY += passo;

        p.fill(220, 228, 238);
        p.textSize(13);
        p.text(String.format(Locale.US, "g1 (Custo): %.2f", g.g1()), px, linhaY);
        linhaY += 22.0f;
        p.text(String.format(Locale.US, "g2 (Heurística A*): %.2f", g.g2()), px, linhaY);
        linhaY += 22.0f;
        p.text(String.format(Locale.US, "g3 (Aversão Risco): %.2f", g.g3()), px, linhaY);
        linhaY += 22.0f;
        p.text(String.format(Locale.US, "g4 (Pref. Hubs): %.2f", g.g4()), px, linhaY);
        linhaY += 22.0f;
        p.text(String.format(Locale.US, "g5 (Limiar Recálc.): %.1f%%", g.g5() * 100.0), px, linhaY);
        linhaY += passo;

        p.text("Obstáculos no Melhor Plano: " + est.melhorPlano().obstaculos().size(), px, linhaY);
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