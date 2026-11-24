package ui.componentes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.Objects;

/**
- Componente de botão interativo para Processing.
- Carrega texturas de estado normal e hover da pasta data/ sem desenhar formas geométricas.
 */
public class Botao {
    private float x;
    private float y;
    private float largura;
    private float altura;
    private String rotulo;
    private PImage imagemNormal;
    private PImage imagemHover;
    private boolean habilitado = true;
    private Runnable acao;

    public Botao(float x, float y, float largura, float altura, String rotulo, Runnable acao) {
        this(x, y, largura, altura, rotulo, null, null, acao);
    }

    public Botao(float x, float y, float largura, float altura, String rotulo, PImage imagemNormal, PImage imagemHover, Runnable acao) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.rotulo = Objects.requireNonNullElse(rotulo, "");
        this.imagemNormal = imagemNormal;
        this.imagemHover = imagemHover;
        this.acao = acao;
    }

    /**
- Carrega texturas da pasta data/ caso os arquivos existam.
     */
    public void carregarImagens(PApplet p, String caminhoNormal, String caminhoHover) {
        if (p == null) return;
        if (caminhoNormal != null && new File(caminhoNormal).exists()) {
            this.imagemNormal = p.loadImage(caminhoNormal);
        }
        if (caminhoHover != null && new File(caminhoHover).exists()) {
            this.imagemHover = p.loadImage(caminhoHover);
        }
    }

    public void setImagens(PImage imagemNormal, PImage imagemHover) {
        this.imagemNormal = imagemNormal;
        this.imagemHover = imagemHover;
    }

    public void desenhar(PApplet p) {
        if (p == null) return;

        boolean hover = habilitado && contemPonto(p.mouseX, p.mouseY);
        // Isola configurações de alinhamento, fonte e cores para não afetar outros elementos da tela
        p.pushStyle();

        PImage texturaAtual = (hover && imagemHover != null) ? imagemHover : imagemNormal;
        if (texturaAtual != null) {
            p.image(texturaAtual, x, y, largura, altura);
        }

        p.textAlign(PConstants.CENTER, PConstants.CENTER);
        p.textSize(16);

        if (!habilitado) {
            p.fill(0, 0, 0);
        } else if (hover) {
            p.fill(110, 210, 255);
        } else {
            p.fill(0, 0, 0);
        }

        float centroX = x + largura / 2.0f;
        float centroY = y + altura / 2.0f;
        p.text(rotulo, centroX, centroY);

        // Fallback visual quando não há textura carregada: sublinha o rótulo no estado hover
        if (texturaAtual == null && hover) {
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
