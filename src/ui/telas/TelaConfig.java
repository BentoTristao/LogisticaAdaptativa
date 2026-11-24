package ui.telas;

import modelo.config.Configuracao;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import ui.componentes.Botao;
import ui.componentes.Slider;
import util.ExporterIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tela de configuração dos parâmetros do labirinto, simulação e co-evolução.
 * Suporta imagem de fundo em data/ e não desenha formas geométricas como fundo.
 */
public class TelaConfig {
    private static final String ARQUIVO_CONFIG_PADRAO = "data/config.properties";

    private final Configuracao configuracao;
    private final List<Slider> sliders = new ArrayList<>();
    private final List<Botao> botoes = new ArrayList<>();

    private Slider sliderLinhas;
    private Slider sliderColunas;
    private Slider sliderAtalhos;
    private Slider sliderRaioVisao;
    private Slider sliderPopEntregador;
    private Slider sliderPopObstaculo;
    private Slider sliderGeracoes;
    private Slider sliderMaxObstaculos;
    private Slider sliderOrcamento;
    private Slider sliderTaxaCrossover;
    private Slider sliderTaxaMutacao;
    private Slider sliderSeed;

    private PImage imagemFundo;
    private boolean assetsCarregados = false;
    private String mensagemStatus = "";
    private boolean statusErro = false;

    // Callbacks de navegação: onConfirmarEIniciar recebe a Configuracao validada; onVoltarMenu retorna à tela anterior
    public TelaConfig(float larguraTela, float alturaTela, Configuracao configuracaoInicial, Consumer<Configuracao> onConfirmarEIniciar, Runnable onVoltarMenu) {
        this.configuracao = Objects.requireNonNullElseGet(configuracaoInicial, Configuracao::new);

        float colEsqX = larguraTela * 0.10f;
        float colDirX = larguraTela * 0.54f;
        float sliderLargura = larguraTela * 0.36f;
        float sliderAltura = 42.0f;
        float inicioY = 95.0f;
        float passoY = 64.0f;

        // Coluna Esquerda: Ambiente e Grafo
        sliderLinhas = new Slider(colEsqX, inicioY, sliderLargura, sliderAltura, "Linhas do Labirinto", 10, 60, configuracao.linhas, true);
        sliderColunas = new Slider(colEsqX, inicioY + passoY, sliderLargura, sliderAltura, "Colunas do Labirinto", 10, 60, configuracao.colunas, true);
        sliderAtalhos = new Slider(colEsqX, inicioY + passoY * 2, sliderLargura, sliderAltura, "Fração de Atalhos", 0.05, 0.25, configuracao.fracaoAtalhos, false);
        sliderRaioVisao = new Slider(colEsqX, inicioY + passoY * 3, sliderLargura, sliderAltura, "Raio de Visão (BFS k)", 2, 6, configuracao.raioVisao, true);
        sliderMaxObstaculos = new Slider(colEsqX, inicioY + passoY * 4, sliderLargura, sliderAltura, "Limite de Obstáculos", 4, 25, configuracao.maxObstaculos, true);
        sliderOrcamento = new Slider(colEsqX, inicioY + passoY * 5, sliderLargura, sliderAltura, "Orçamento de Desastre", 15, 45, configuracao.orcamentoDesastre, true);

        // Coluna Direita: Co-evolução
        sliderPopEntregador = new Slider(colDirX, inicioY, sliderLargura, sliderAltura, "População Entregadores", 10, 100, configuracao.tamPopEntregador, true);
        sliderPopObstaculo = new Slider(colDirX, inicioY + passoY, sliderLargura, sliderAltura, "População Obstáculos", 10, 100, configuracao.tamPopObstaculo, true);
        sliderGeracoes = new Slider(colDirX, inicioY + passoY * 2, sliderLargura, sliderAltura, "Gerações", 10, 500, configuracao.geracoes, true);
        sliderTaxaCrossover = new Slider(colDirX, inicioY + passoY * 3, sliderLargura, sliderAltura, "Taxa de Crossover", 0.0, 1.0, configuracao.taxaCrossover, false);
        sliderTaxaMutacao = new Slider(colDirX, inicioY + passoY * 4, sliderLargura, sliderAltura, "Taxa de Mutação", 0.05, 0.15, configuracao.taxaMutacao, false);
        sliderSeed = new Slider(colDirX, inicioY + passoY * 5, sliderLargura, sliderAltura, "Semente Aleatória (Seed)", 1, 9999, configuracao.seed, true);

        sliders.addAll(List.of(
                sliderLinhas, sliderColunas, sliderAtalhos, sliderRaioVisao, sliderMaxObstaculos, sliderOrcamento,
                sliderPopEntregador, sliderPopObstaculo, sliderGeracoes, sliderTaxaCrossover, sliderTaxaMutacao, sliderSeed
        ));

        float rodapeY = alturaTela - 78.0f;
        float btnW = 190.0f;
        float btnH = 44.0f;
        float gap = 20.0f;
        float totalW = btnW * 4 + gap * 3;
        float startX = (larguraTela - totalW) / 2.0f;

        botoes.add(new Botao(startX, rodapeY, btnW, btnH, "Voltar", onVoltarMenu));
        botoes.add(new Botao(startX + (btnW + gap), rodapeY, btnW, btnH, "Carregar Config", this::carregarDoDisco));
        botoes.add(new Botao(startX + (btnW + gap) * 2, rodapeY, btnW, btnH, "Salvar Config", this::salvarNoDisco));
        botoes.add(new Botao(startX + (btnW + gap) * 3, rodapeY, btnW, btnH, "Iniciar Simulação", () -> {
            if (aplicarEValidar() && onConfirmarEIniciar != null) {
                onConfirmarEIniciar.accept(configuracao);
            }
        }));
    }

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/fundo_config.png").exists()) {
            imagemFundo = p.loadImage("data/fundo_config.png");
        }
        for (Slider s : sliders) {
            s.carregarImagens(p, "data/slider_trilha.png", "data/slider_ponteiro.png");
        }
        for (Botao b : botoes) {
            b.carregarImagens(p, "data/btn_normal.png", "data/btn_hover.png");
        }
    }

    public void desenhar(PApplet p) {
        if (p == null) return;
        inicializarAssets(p);

        p.pushStyle();
        p.background(18, 22, 30);

        if (imagemFundo != null) {
            p.image(imagemFundo, 0, 0, p.width, p.height);
        }

        p.textAlign(PConstants.CENTER, PConstants.TOP);
        p.fill(240, 244, 248);
        p.textSize(26);
        p.text("Configuração da Simulação e Co-evolução", p.width / 2.0f, 28.0f);

        for (Slider slider : sliders) {
            slider.desenhar(p);
        }

        if (!mensagemStatus.isEmpty()) {
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(14);
            if (statusErro) {
                p.fill(255, 110, 110);
            } else {
                p.fill(110, 230, 155);
            }
            p.text(mensagemStatus, p.width / 2.0f, p.height - 108.0f);
        }

        for (Botao botao : botoes) {
            botao.desenhar(p);
        }

        p.popStyle();
    }

    public void mousePressionado(float px, float py) {
        for (Slider slider : sliders) {
            if (slider.mousePressionado(px, py)) {
                return;
            }
        }
        for (Botao botao : botoes) {
            if (botao.clicar(px, py)) {
                return;
            }
        }
    }

    public void mouseArrastado(float px) {
        for (Slider slider : sliders) {
            slider.mouseArrastado(px);
        }
    }

    public void mouseSolto() {
        for (Slider slider : sliders) {
            slider.mouseSolto();
        }
    }

    private boolean aplicarEValidar() {
        configuracao.linhas = sliderLinhas.getValorInteiro();
        configuracao.colunas = sliderColunas.getValorInteiro();
        configuracao.fracaoAtalhos = sliderAtalhos.getValor();
        configuracao.raioVisao = sliderRaioVisao.getValorInteiro();
        configuracao.tamPopEntregador = sliderPopEntregador.getValorInteiro();
        configuracao.tamPopObstaculo = sliderPopObstaculo.getValorInteiro();
        configuracao.geracoes = sliderGeracoes.getValorInteiro();
        configuracao.maxObstaculos = sliderMaxObstaculos.getValorInteiro();
        configuracao.orcamentoDesastre = sliderOrcamento.getValorInteiro();
        configuracao.taxaCrossover = sliderTaxaCrossover.getValor();
        configuracao.taxaMutacao = sliderTaxaMutacao.getValor();
        configuracao.seed = sliderSeed.getValorInteiro();

        try {
            configuracao.validar();
            mensagemStatus = "";
            statusErro = false;
            return true;
        } catch (IllegalArgumentException ex) {
            mensagemStatus = "Erro de validação: " + ex.getMessage();
            statusErro = true;
            return false;
        }
    }

    private void salvarNoDisco() {
        if (!aplicarEValidar()) return;
        try {
            ExporterIO.salvarConfiguracao(configuracao, ARQUIVO_CONFIG_PADRAO);
            mensagemStatus = "Configuração salva em " + ARQUIVO_CONFIG_PADRAO;
            statusErro = false;
        } catch (IOException ex) {
            mensagemStatus = "Falha ao salvar configuração: " + ex.getMessage();
            statusErro = true;
        }
    }

    private void carregarDoDisco() {
        try {
            Configuracao carregada = ExporterIO.carregarConfiguracao(ARQUIVO_CONFIG_PADRAO);
            sliderLinhas.setValor(carregada.linhas);
            sliderColunas.setValor(carregada.colunas);
            sliderAtalhos.setValor(carregada.fracaoAtalhos);
            sliderRaioVisao.setValor(carregada.raioVisao);
            sliderPopEntregador.setValor(carregada.tamPopEntregador);
            sliderPopObstaculo.setValor(carregada.tamPopObstaculo);
            sliderGeracoes.setValor(carregada.geracoes);
            sliderMaxObstaculos.setValor(carregada.maxObstaculos);
            sliderOrcamento.setValor(carregada.orcamentoDesastre);
            sliderTaxaCrossover.setValor(carregada.taxaCrossover);
            sliderTaxaMutacao.setValor(carregada.taxaMutacao);
            sliderSeed.setValor(carregada.seed);
            aplicarEValidar();
            mensagemStatus = "Configuração carregada de " + ARQUIVO_CONFIG_PADRAO;
            statusErro = false;
        } catch (IOException ex) {
            mensagemStatus = "Falha ao carregar configuração: " + ex.getMessage();
            statusErro = true;
        }
    }

    public Configuracao getConfiguracao() {
        return configuracao;
    }
}
