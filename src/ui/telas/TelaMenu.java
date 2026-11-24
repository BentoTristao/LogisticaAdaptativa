package ui.telas;

import processing.core.PApplet;
import processing.core.PConstants;
import ui.componentes.Botao;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela inicial do sistema.
 */
public class TelaMenu {
    private final List<Botao> botoes = new ArrayList<>();
    private boolean assetsCarregados = false;

    // Recebe callbacks de navegação disparados ao clicar nos botões principais do menu
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

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        for (Botao botao : botoes) {
            botao.carregarImagemHover(p, "data/btn_hover.png");
        }
    }

    public void desenhar(PApplet p) {
        if (p == null) return;
        inicializarAssets(p);

        p.pushStyle();
        p.background(18, 22, 30);

        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.fill(240, 244, 248);
        p.textSize(34);
        p.text("Logística Adaptativa", p.width / 2.0f, p.height * 0.24f);

        p.fill(140, 195, 235);
        p.textSize(16);
        p.text("Algoritmos em Grafos & Co-evolução Competitiva", p.width / 2.0f, p.height * 0.31f);

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