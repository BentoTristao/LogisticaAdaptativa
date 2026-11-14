import evolucao.core.CoEvolucao;
import evolucao.estatistica.EstatisticasGeracao;
import grafo.core.Grafo;
import grafo.gerador.GeradorLabirinto;
import modelo.config.Configuracao;
import modelo.resultado.ReplaySimulacao;
import processing.core.PApplet;
import simulacao.engine.MotorSimulacao;
import ui.telas.TelaConfig;
import ui.telas.TelaEvolucao;
import ui.telas.TelaMenu;
import util.ExporterIO;
import util.RandomManager;
import visual.VisualizadorSimulacao;

import java.io.File;
import java.io.IOException;

/**
 * Ponto de entrada principal da aplicação (Processing PApplet).
 * Coordena a navegação entre telas e integra o front-end aos motores
 * de geração de grafos, simulação e co-evolução.
 */
public class Main extends PApplet {
    public static final int LARGURA_TELA = 1280;
    public static final int ALTURA_TELA = 720;
    private static final String ARQUIVO_CONFIG_PADRAO = "data/config.properties";

    private enum EstadoTela {MENU, CONFIGURACAO, EVOLUCAO, VISUALIZACAO}

    private EstadoTela estadoAtual = EstadoTela.MENU;

    private Configuracao configuracao;
    private Grafo grafoAtual;
    private MotorSimulacao motorSimulacao;
    private CoEvolucao coEvolucaoAtual;

    private TelaMenu telaMenu;
    private TelaConfig telaConfig;
    private TelaEvolucao telaEvolucao;
    private VisualizadorSimulacao visualizadorSimulacao;

    public static void main(String[] args) {
        PApplet.main(Main.class.getName());
    }

    @Override
    public void settings() {
        size(LARGURA_TELA, ALTURA_TELA);
    }

    @Override
    public void setup() {
        surface.setTitle("Logística Adaptativa — Grafos e Co-evolução Competitiva");
        frameRate(60);

        this.configuracao = carregarConfiguracaoInicial();
        this.motorSimulacao = new MotorSimulacao();

        this.telaMenu = new TelaMenu(LARGURA_TELA, ALTURA_TELA, () -> iniciarNovaEvolucao(this.configuracao), () -> this.estadoAtual = EstadoTela.CONFIGURACAO, this::encerrarAplicacao);

        this.telaConfig = new TelaConfig(LARGURA_TELA, ALTURA_TELA, this.configuracao, this::iniciarNovaEvolucao, () -> this.estadoAtual = EstadoTela.MENU);

        this.telaEvolucao = new TelaEvolucao(LARGURA_TELA, ALTURA_TELA, this::abrirVisualizacaoConfronto, () -> this.estadoAtual = EstadoTela.MENU);

        this.visualizadorSimulacao = new VisualizadorSimulacao(LARGURA_TELA, ALTURA_TELA, () -> this.estadoAtual = EstadoTela.EVOLUCAO);
    }

    @Override
    public void draw() {
        switch (estadoAtual) {
            case MENU -> telaMenu.desenhar(this);
            case CONFIGURACAO -> telaConfig.desenhar(this);
            case EVOLUCAO -> telaEvolucao.desenhar(this);
            case VISUALIZACAO -> visualizadorSimulacao.desenhar(this);
        }
    }

    @Override
    public void mousePressed() {
        switch (estadoAtual) {
            case MENU -> telaMenu.mousePressionado(mouseX, mouseY);
            case CONFIGURACAO -> telaConfig.mousePressionado(mouseX, mouseY);
            case EVOLUCAO -> telaEvolucao.mousePressionado(mouseX, mouseY);
            case VISUALIZACAO -> visualizadorSimulacao.mousePressionado(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged() {
        if (estadoAtual == EstadoTela.CONFIGURACAO) {
            telaConfig.mouseArrastado(mouseX);
        }
    }

    @Override
    public void mouseReleased() {
        if (estadoAtual == EstadoTela.CONFIGURACAO) {
            telaConfig.mouseSolto();
        }
    }

    @Override
    public void exit() {
        pararCoEvolucaoAtiva();
        super.exit();
    }

    private void iniciarNovaEvolucao(Configuracao cfg) {
        if (cfg == null) {
            cfg = new Configuracao();
        }
        cfg.validar();
        this.configuracao = cfg;

        pararCoEvolucaoAtiva();

        RandomManager.inicializar(cfg.seed);
        this.grafoAtual = GeradorLabirinto.gerar(cfg.linhas, cfg.colunas, cfg.fracaoAtalhos, cfg.seed);

        this.coEvolucaoAtual = new CoEvolucao(this.grafoAtual, this.configuracao, this.motorSimulacao, this.telaEvolucao);

        this.telaEvolucao.vincularCoEvolucao(this.coEvolucaoAtual);
        this.coEvolucaoAtual.iniciar();
        this.estadoAtual = EstadoTela.EVOLUCAO;
    }

    private void abrirVisualizacaoConfronto(EstatisticasGeracao estatistica) {
        if (estatistica == null || grafoAtual == null) {
            return;
        }

        ReplaySimulacao replay = motorSimulacao.executarComReplay(grafoAtual, estatistica.melhorGenomaEntregador(), estatistica.melhorPlano(), configuracao, configuracao.seed);

        visualizadorSimulacao.carregarReplay(grafoAtual, replay, configuracao, estatistica.contagemBloqueiosPorAresta());

        this.estadoAtual = EstadoTela.VISUALIZACAO;
    }

    private Configuracao carregarConfiguracaoInicial() {
        File arquivo = new File(ARQUIVO_CONFIG_PADRAO);
        if (arquivo.exists()) {
            try {
                Configuracao carregada = ExporterIO.carregarConfiguracao(ARQUIVO_CONFIG_PADRAO);
                carregada.validar();
                return carregada;
            } catch (IOException | IllegalArgumentException ignored) {
                // Mantém os valores padrão caso o arquivo esteja corrompido
            }
        }
        return new Configuracao();
    }

    private void pararCoEvolucaoAtiva() {
        if (coEvolucaoAtual != null && coEvolucaoAtual.estaRodando()) {
            coEvolucaoAtual.parar();
        }
    }

    private void encerrarAplicacao() {
        pararCoEvolucaoAtiva();
        exit();
    }
}
