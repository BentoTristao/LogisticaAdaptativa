package ui.componentes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.Objects;

/**
 * Componente de botão interativo para Processing.
 * Renderiza rótulo em branco e destaca o estado hover com textura opcional ou sublinhado.
 */
public class Botao {
    private float x;
    private float y;
    private float largura;
    private float altura;
    private String rotulo;
    private PImage imagemHover;
    private boolean habilitado = true;
    private Runnable acao;

    public Botao(float x, float y, float largura, float altura, String rotulo, Runnable acao) {
        this(x, y, largura, altura, rotulo, null, acao);
    }

    public Botao(float x, float y, float largura, float altura, String rotulo, PImage imagemHover, Runnable acao) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.rotulo = Objects.requireNonNullElse(rotulo, "");
        this.imagemHover = imagemHover;
        this.acao = acao;
    }

    /**
     * Carrega textura de estado hover da pasta data/ caso o arquivo exista.
     */
    public void carregarImagemHover(PApplet p, String caminhoHover) {
        if (p == null) return;
        if (caminhoHover != null && new File(caminhoHover).exists()) {
            this.imagemHover = p.loadImage(caminhoHover);
        }
    }

    public void setImagemHover(PImage imagemHover) {
        this.imagemHover = imagemHover;
    }

    public void desenhar(PApplet p) {
        if (p == null) return;

        boolean hover = habilitado && contemPonto(p.mouseX, p.mouseY);
        // Isola configurações de alinhamento, fonte e cores para não afetar outros elementos da tela
        p.pushStyle();

        if (hover && imagemHover != null) {
            p.image(imagemHover, x, y, largura, altura);
        }

        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.textSize(16);

        if (!habilitado) {
            p.fill(130, 136, 148);
        } else if (hover) {
            p.fill(110, 210, 255);
        } else {
            p.fill(255, 255, 255);
        }

        float centroX = x + largura / 2.0f;
        float centroY = y + altura / 2.0f;
        p.text(rotulo, centroX, centroY);

        // Fallback visual quando não há textura de hover: sublinha o rótulo ao passar o mouse
        if ((!hover || imagemHover == null) && hover) {
            float meiaLarguraTexto = Math.min(largura * 0.45f, p.textWidth(rotulo) / 2.0f + 8.0f);
            p.stroke(110, 210, 255);
            p.strokeWeight(2.0f);
            p.line(centroX - meiaLarguraTexto, centroY + 13.0f, centroX + meiaLarguraTexto, centroY + 13.0f);
        }

        p.popStyle();
    }

    public boolean contemPonto(float px, float py) {
        return px >= x && px <= (x + largura) && py >= y && py <= (y + altura);
    }

    public boolean clicar(float px, float py) {
        if (habilitado && contemPonto(px, py)) {
            if (acao != null) {
                acao.run();
            }
            return true;
        }
        return false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getLargura() { return largura; }
    public float getAltura() { return altura; }
    public String getRotulo() { return rotulo; }
    public boolean isHabilitado() { return habilitado; }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDimensoes(float largura, float altura) {
        this.largura = largura;
        this.altura = altura;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = Objects.requireNonNullElse(rotulo, "");
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }

    public void setAcao(Runnable acao) {
        this.acao = acao;
    }
}