package visual;

import grafo.busca.BfsVisao;
import grafo.core.Grafo;
import modelo.config.Configuracao;
import modelo.obstaculo.Obstaculo;
import modelo.obstaculo.TipoObstaculo;
import modelo.resultado.ReplaySimulacao;
import modelo.resultado.ResultadoSimulacao;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import ui.componentes.Botao;
import visual.camadas.CamadaGrafo;
import visual.camadas.CamadaHeatmap;
import visual.camadas.CamadaNevoa;
import visual.camadas.CamadaTrilha;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Visualizador interativo de replay da simulação tick-a-tick.
 * Coordena as camadas de Grafo, Heatmap, Trilha e Névoa de Guerra utilizando
 * apenas imagens da pasta data/, linhas e tipografia.
 */
public class VisualizadorSimulacao {
    private final CamadaGrafo camadaGrafo = new CamadaGrafo();
    private final CamadaTrilha camadaTrilha = new CamadaTrilha();
    private final CamadaNevoa camadaNevoa = new CamadaNevoa();
    private final CamadaHeatmap camadaHeatmap = new CamadaHeatmap();

    private final List<Botao> botoes = new ArrayList<>();
    private Botao botaoPlayPause;
    private Botao botaoVelocidade;
    private Botao botaoNevoa;
    private Botao botaoHeatmap;
    private Botao botaoAlternarGeracao;

    private Grafo grafo;
    private ReplaySimulacao replay;
    private ReplaySimulacao replayFinal;
    private ReplaySimulacao replayGeracao1;
    private boolean exibindoGeracao1 = false;

    private Configuracao configuracao;
    private int[] heatmapContagem;
    private int[] heatmapFinal;
    private int[] heatmapGeracao1;

    private int tickAtual = 0;
    private boolean reproduzindo = true;
    private int multiplicadorVelocidade = 1;
    private int contadorFrames = 0;

    private PImage imagemFundo;
    private boolean assetsCarregados = false;

    public VisualizadorSimulacao(float larguraTela, float alturaTela, Runnable onVoltar) {
        float rodapeY = alturaTela - 66.0f;
        float btnW = 122.0f;
        float btnWGeracao = 148.0f;
        float btnH = 40.0f;
        float gap = 10.0f;
        float startX = 32.0f;

        botaoPlayPause = new Botao(startX, rodapeY, btnW, btnH, "Pausar", this::alternarPlayPause);
        Botao botaoAnterior = new Botao(startX + (btnW + gap), rodapeY, btnW, btnH, "Tick -1", this::tickAnterior);
        Botao botaoProximo = new Botao(startX + (btnW + gap) * 2, rodapeY, btnW, btnH, "Tick +1", this::proximoTick);
        Botao botaoReiniciar = new Botao(startX + (btnW + gap) * 3, rodapeY, btnW, btnH, "Reiniciar", this::reiniciar);
        botaoVelocidade = new Botao(startX + (btnW + gap) * 4, rodapeY, btnW, btnH, "Vel: 1x", this::alternarVelocidade);
        botaoNevoa = new Botao(startX + (btnW + gap) * 5, rodapeY, btnW, btnH, "Névoa: ON", this::alternarNevoa);
        botaoHeatmap = new Botao(startX + (btnW + gap) * 6, rodapeY, btnW, btnH, "Heatmap: OFF", this::alternarHeatmap);
        botaoAlternarGeracao = new Botao(startX + (btnW + gap) * 7, rodapeY, btnWGeracao, btnH, "Gen: Final", this::alternarGeracao);

        float xVoltar = startX + (btnW + gap) * 7 + btnWGeracao + gap;
        Botao botaoVoltar = new Botao(xVoltar, rodapeY, btnW, btnH, "Voltar", () -> {
            reproduzindo = false;
            if (onVoltar != null) {
                onVoltar.run();
            }
        });

        botoes.addAll(List.of(
                botaoPlayPause, botaoAnterior, botaoProximo, botaoReiniciar,
                botaoVelocidade, botaoNevoa, botaoHeatmap, botaoAlternarGeracao, botaoVoltar
        ));
    }

    /**
     * Carrega um novo replay de simulação para exibição (Geração Final).
     */
    public void carregarReplay(Grafo grafo, ReplaySimulacao replay, Configuracao configuracao, int[] heatmapContagem) {
        this.grafo = grafo;
        this.replayFinal = replay;
        this.replay = replay;
        this.configuracao = configuracao != null ? configuracao : new Configuracao();
        this.heatmapFinal = heatmapContagem != null ? heatmapContagem.clone() : null;
        this.heatmapContagem = this.heatmapFinal;
        this.exibindoGeracao1 = false;

        this.tickAtual = 0;
        this.contadorFrames = 0;
        this.reproduzindo = true;
        this.botaoPlayPause.setRotulo("Pausar");
        if (this.botaoAlternarGeracao != null) {
            this.botaoAlternarGeracao.setRotulo("Gen: Final");
        }
    }

    /**
     * Define o replay e o heatmap da 1ª geração para comparação A/B.
     */
    public void setReplayGeracao1(ReplaySimulacao replayGeracao1, int[] heatmapGeracao1) {
        this.replayGeracao1 = replayGeracao1;
        this.heatmapGeracao1 = heatmapGeracao1 != null ? heatmapGeracao1.clone() : null;
    }

    public void setReplayGeracao1(ReplaySimulacao replayGeracao1) {
        setReplayGeracao1(replayGeracao1, null);
    }

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/fundo_simulacao.png").exists()) {
            imagemFundo = p.loadImage("data/fundo_simulacao.png");
        }

        for (Botao b : botoes) {
            b.carregarImagemHover(p, "data/btn_hover.png");
        }

        camadaGrafo.inicializarAssets(p);
        camadaTrilha.inicializarAssets(p);
        camadaNevoa.inicializarAssets(p);
        camadaHeatmap.inicializarAssets(p);
    }

    public void desenhar(PApplet p) {
        if (p == null) return;
        inicializarAssets(p);

        atualizarPlayback();

        p.pushStyle();
        p.background(18, 22, 30);

        if (imagemFundo != null) {
            p.image(imagemFundo, 0, 0, p.width, p.height);
        }

        // Cabeçalho
        p.textAlign(PConstants.LEFT, PConstants.CENTER);
        p.fill(240, 244, 248);
        p.textSize(22);
        p.text("Visualizador de Simulação & Replay", 32.0f, 32.0f);

        if (grafo == null || replay == null) {
            p.fill(170, 180, 195);
            p.textSize(16);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.text("Nenhuma simulação carregada para visualização.", p.width / 2.0f, p.height / 2.0f);
            for (Botao botao : botoes) {
                botao.desenhar(p);
            }
            p.popStyle();
            return;
        }

        // Área do labirinto (esquerda) e painel de telemetria (direita)
        float areaMapaX = 32.0f;
        float areaMapaY = 64.0f;
        float areaMapaW = p.width * 0.66f;
        float areaMapaH = p.height - 152.0f;

        float celula = Math.min(areaMapaW / grafo.colunas(), areaMapaH / grafo.linhas());
        float larguraGrid = celula * grafo.colunas();
        float alturaGrid = celula * grafo.linhas();
        float offsetX = areaMapaX + (areaMapaW - larguraGrid) / 2.0f;
        float offsetY = areaMapaY + (areaMapaH - alturaGrid) / 2.0f;

        int[] posicoes = replay.verticePorTick();
        int maxTickIdx = Math.max(0, posicoes.length - 1);
        int tickSeguro = Math.max(0, Math.min(tickAtual, maxTickIdx));
        int posEntregador = posicoes.length > 0 ? posicoes[tickSeguro] : grafo.verticeInicio();

        // Reconstrói pesos e obstáculos ativos até o tick atual
        double[] pesosNoTick = grafo.pesosBase();
        Map<Integer, TipoObstaculo> tiposAtivos = new HashMap<>();
        for (Obstaculo obs : replay.ativacoes()) {
            if (obs.tickAtivacao() <= tickSeguro && obs.arestaId() < pesosNoTick.length) {
                pesosNoTick[obs.arestaId()] = obs.tipo().aplicarAoPeso(pesosNoTick[obs.arestaId()]);
                tiposAtivos.put(obs.arestaId(), obs.tipo());
            }
        }

        // Obtém a rota planejada vigente no tick atual
        List<Integer> rotaVigente = obterRotaPlanejadaNoTick(tickSeguro);

        // Calcula o conjunto de vértices já explorados até o tick atual
        Set<Integer> explorados = calcularVerticesExploradosAte(tickSeguro);

        // Renderiza as 4 camadas ordenadas (do plano de fundo para a sobreposição superior)
        camadaGrafo.desenhar(p, grafo, pesosNoTick, tiposAtivos, posEntregador, offsetX, offsetY, celula);
        camadaHeatmap.desenhar(p, grafo, heatmapContagem, offsetX, offsetY, celula);
        camadaTrilha.desenhar(p, grafo, posicoes, tickSeguro, rotaVigente, replay.ticksDosRecalculos(), offsetX, offsetY, celula);
        camadaNevoa.desenhar(p, grafo, posEntregador, configuracao.raioVisao, explorados, offsetX, offsetY, celula);

        // Painel lateral de telemetria
        float painelX = areaMapaX + areaMapaW + 28.0f;
        desenharTelemetria(p, painelX, areaMapaY + 8.0f, tickSeguro, maxTickIdx, tiposAtivos);

        for (Botao botao : botoes) {
            botao.desenhar(p);
        }

        p.popStyle();
    }

    private void atualizarPlayback() {
        if (!reproduzindo || replay == null) return;

        int maxTick = Math.max(0, replay.verticePorTick().length - 1);
        if (tickAtual >= maxTick) {
            reproduzindo = false;
            botaoPlayPause.setRotulo("Play");
            return;
        }

        int framesPorTick = Math.max(2, 16 / multiplicadorVelocidade);
        contadorFrames++;
        if (contadorFrames >= framesPorTick) {
            contadorFrames = 0;
            tickAtual++;
            if (tickAtual >= maxTick) {
                reproduzindo = false;
                botaoPlayPause.setRotulo("Play");
            }
        }
    }

    private List<Integer> obterRotaPlanejadaNoTick(int tick) {
        int[] ticksRec = replay.ticksDosRecalculos();
        List<int[]> rotas = replay.rotasPlanejadas();
        if (ticksRec == null || rotas == null || rotas.isEmpty()) {
            return List.of();
        }

        int indiceEscolhido = -1;
        for (int i = 0; i < ticksRec.length && i < rotas.size(); i++) {
            if (ticksRec[i] <= tick) {
                indiceEscolhido = i;
            } else {
                break;
            }
        }

        if (indiceEscolhido < 0) return List.of();

        int[] arr = rotas.get(indiceEscolhido);
        List<Integer> lista = new ArrayList<>(arr.length);
        for (int id : arr) {
            lista.add(id);
        }
        return lista;
    }

    private Set<Integer> calcularVerticesExploradosAte(int tickLimite) {
        Set<Integer> explorados = new HashSet<>();
        int[] posicoes = replay.verticePorTick();
        if (posicoes == null || posicoes.length == 0) return explorados;

        int fim = Math.min(tickLimite, posicoes.length - 1);
        for (int t = 0; t <= fim; t++) {
            explorados.addAll(BfsVisao.bfsAteRaio(grafo, posicoes[t], configuracao.raioVisao));
        }
        return explorados;
    }

    private void desenharTelemetria(PApplet p, float px, float py, int tickSeguro, int maxTick, Map<Integer, TipoObstaculo> tiposAtivos) {
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        float y = py;
        float passo = 26.0f;

        p.fill(110, 210, 255);
        p.textSize(17);
        p.text("Telemetria da Partida", px, y);
        y += passo * 1.15f;

        // Indicador da geração em exibição
        p.textSize(13);
        if (exibindoGeracao1) {
            p.fill(250, 210, 65);
            p.text("Exibindo: 1ª Geração (Inicial)", px, y);
        } else {
            p.fill(90, 235, 165);
            p.text("Exibindo: Geração Final (Adaptada)", px, y);
        }
        y += passo * 1.15f;

        p.fill(235, 240, 246);
        p.textSize(14);
        p.text("Tick: " + tickSeguro + " / " + maxTick, px, y);
        y += passo;

        ResultadoSimulacao res = replay.resultado();
        String estadoPartida = (tickSeguro < maxTick) ? "Em andamento" : (res.chegou() ? "Destino alcançado!" : "Tempo esgotado (T_max)");

        p.fill(res.chegou() && tickSeguro == maxTick ? 80 : 225, 225, 160);
        p.text("Estado: " + estadoPartida, px, y);
        y += passo * 1.4f;

        long desvios = tiposAtivos.values().stream().filter(t -> t == TipoObstaculo.DESVIO).count();
        long congest = tiposAtivos.values().stream().filter(t -> t == TipoObstaculo.CONGESTIONAMENTO).count();
        long bloqueios = tiposAtivos.values().stream().filter(t -> t == TipoObstaculo.BLOQUEIO).count();

        p.fill(110, 210, 255);
        p.text("Obstáculos Ativos no Mapa:", px, y);
        y += passo;

        p.fill(245, 210, 65);
        p.text("Desvios (+2.0): " + desvios, px, y);
        y += 22.0f;
        p.fill(255, 145, 55);
        p.text("Congestionamentos (+5.0): " + congest, px, y);
        y += 22.0f;
        p.fill(245, 75, 75);
        p.text("Bloqueios Totais (∞): " + bloqueios, px, y);
        y += passo * 1.4f;

        p.fill(110, 210, 255);
        p.text("Métricas Consolidadas:", px, y);
        y += passo;

        p.fill(220, 228, 238);
        p.textSize(13);
        p.text("Duração Total: " + res.ticks() + " ticks", px, y);
        y += 22.0f;
        p.text("Recálculos A*: " + res.recalculos(), px, y);
        y += 22.0f;
        p.text("Passos em Obstáculos: " + res.passosEmObstaculos(), px, y);
        y += 22.0f;
        p.text(String.format(Locale.US, "Orçamento Usado: %.1f pts", res.custoObstaculosUsados()), px, y);
        y += 22.0f;
        p.text(String.format(Locale.US, "Progresso Final: %.1f%%", res.progresso() * 100.0), px, y);
    }

    public void mousePressionado(float px, float py) {
        for (Botao botao : botoes) {
            if (botao.clicar(px, py)) {
                break;
            }
        }
    }

    private void alternarPlayPause() {
        if (replay == null) return;
        int maxTick = Math.max(0, replay.verticePorTick().length - 1);
        if (!reproduzindo && tickAtual >= maxTick) {
            tickAtual = 0;
        }
        reproduzindo = !reproduzindo;
        botaoPlayPause.setRotulo(reproduzindo ? "Pausar" : "Play");
    }

    private void tickAnterior() {
        reproduzindo = false;
        botaoPlayPause.setRotulo("Play");
        if (tickAtual > 0) {
            tickAtual--;
        }
    }

    private void proximoTick() {
        reproduzindo = false;
        botaoPlayPause.setRotulo("Play");
        if (replay != null) {
            int maxTick = Math.max(0, replay.verticePorTick().length - 1);
            if (tickAtual < maxTick) {
                tickAtual++;
            }
        }
    }

    private void reiniciar() {
        tickAtual = 0;
        contadorFrames = 0;
        reproduzindo = true;
        botaoPlayPause.setRotulo("Pausar");
    }

    private void alternarVelocidade() {
        multiplicadorVelocidade = switch (multiplicadorVelocidade) {
            case 1 -> 2;
            case 2 -> 4;
            default -> 1;
        };
        botaoVelocidade.setRotulo("Vel: " + multiplicadorVelocidade + "x");
    }

    private void alternarNevoa() {
        camadaNevoa.alternar();
        botaoNevoa.setRotulo(camadaNevoa.isAtiva() ? "Névoa: ON" : "Névoa: OFF");
    }

    private void alternarHeatmap() {
        camadaHeatmap.alternar();
        botaoHeatmap.setRotulo(camadaHeatmap.isAtiva() ? "Heatmap: ON" : "Heatmap: OFF");
    }

    private void alternarGeracao() {
        if (replayGeracao1 == null || replayFinal == null) return;

        exibindoGeracao1 = !exibindoGeracao1;
        this.replay = exibindoGeracao1 ? replayGeracao1 : replayFinal;
        this.heatmapContagem = exibindoGeracao1 ? heatmapGeracao1 : heatmapFinal;

        this.tickAtual = 0;
        this.contadorFrames = 0;
        this.reproduzindo = true;
        this.botaoPlayPause.setRotulo("Pausar");
        this.botaoAlternarGeracao.setRotulo(exibindoGeracao1 ? "Gen: 1ª (Inicial)" : "Gen: Final");
    }
}