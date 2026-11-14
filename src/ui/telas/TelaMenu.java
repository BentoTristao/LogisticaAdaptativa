package ui.telas;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import ui.componentes.Botao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela inicial do sistema.
 * Carrega fundo e logotipo da pasta data/ quando presentes, sem formas geométricas decorativas.
 */
public class TelaMenu {
    private final List<Botao> botoes = new ArrayList<>();
    private PImage imagemFundo;
    private PImage imagemLogo;
    private boolean assetsCarregados = false;

    // Uma variavel Runnable é um bloco de código que executa em outra thread.
    public TelaMenu(float larguraTela, float alturaTela, Runnable onIniciarSimulacao, Runnable onAbrirConfiguracoes, Runnable onSair) {
        float btnLargura = 260.0f;
        float btnAltura = 48.0f;
        float centroX = (larguraTela - btnLargura) / 2.0f;
        float inicioY = alturaTela * 0.48f;
        float espacamento = 64.0f;

        botoes.add(new Botao(centroX, inicioY, btnLargura, btnAltura, "Iniciar Simulação", onIniciarSimulacao));
        botoes.add(new Botao(centroX, inicioY + espacamento, btnLargura, btnAltura, "Configurações", onAbrirConfiguracoes));
        botoes.add(new Botao(centroX, inicioY + espacamento * 2.0f, btnLargura, btnAltura, "Sair", onSair));
    }

    /**
     * Carrega as imagens da pasta data/ caso existam no diretório do projeto.
     */
    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/fundo_menu.png").exists()) {
            imagemFundo = p.loadImage("data/fundo_menu.png");
        }
        if (new File("data/logo.png").exists()) {
            imagemLogo = p.loadImage("data/logo.png");
        }

        for (Botao botao : botoes) {
            botao.carregarImagens(p, "data/btn_normal.png", "data/btn_hover.png");
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

        if (imagemLogo != null) {
            float logoX = (p.width - imagemLogo.width) / 2.0f;
            float logoY = p.height * 0.14f;
            p.image(imagemLogo, logoX, logoY);
        } else {
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.fill(240, 244, 248);
            p.textSize(34);
            p.text("Logística Adaptativa", p.width / 2.0f, p.height * 0.24f);

            p.fill(140, 195, 235);
            p.textSize(16);
            p.text("Algoritmos em Grafos & Co-evolução Competitiva", p.width / 2.0f, p.height * 0.31f);
        }

        for (Botao botao : botoes) {
            botao.desenhar(p);
        }

        p.popStyle();
    }

    public void mousePressionado(float px, float py) {
        for (Botao botao : botoes) {
            if (botao.clicar(px, py)) {
                break;
            }
        }
    }
}
