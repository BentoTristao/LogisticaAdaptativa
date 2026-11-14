package ui.componentes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

/**
 * Controle deslizante numérico para ajuste de parâmetros da simulação.
 * Utiliza imagens da pasta data/ para trilha e ponteiro quando disponíveis.
 */
public class Slider {
    private float x;
    private float y;
    private float largura;
    private float altura;
    private final String rotulo;
    private final double min;
    private final double max;
    private double valor;
    private final boolean inteiro;
    private boolean arrastando = false;

    private PImage imagemTrilha;
    private PImage imagemPonteiro;

    public Slider(float x, float y, float largura, float altura, String rotulo, double min, double max, double valorInicial, boolean inteiro) {
        if (max <= min) {
            throw new IllegalArgumentException("O valor máximo do slider deve ser maior que o mínimo.");
        }
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.rotulo = Objects.requireNonNullElse(rotulo, "");
        this.min = min;
        this.max = max;
        this.inteiro = inteiro;
        setValor(valorInicial);
    }

    /**
     * Carrega texturas da trilha e do ponteiro a partir da pasta data/.
     */
    public void carregarImagens(PApplet p, String caminhoTrilha, String caminhoPonteiro) {
        if (p == null) return;
        if (caminhoTrilha != null && new File(caminhoTrilha).exists()) {
            this.imagemTrilha = p.loadImage(caminhoTrilha);
        }
        if (caminhoPonteiro != null && new File(caminhoPonteiro).exists()) {
            this.imagemPonteiro = p.loadImage(caminhoPonteiro);
        }
    }

    public void setImagens(PImage imagemTrilha, PImage imagemPonteiro) {
        this.imagemTrilha = imagemTrilha;
        this.imagemPonteiro = imagemPonteiro;
    }

    public void desenhar(PApplet p) {
        if (p == null) return;

        if (arrastando) {
            if (p.mousePressed) {
                atualizarPorPosicaoX(p.mouseX);
            } else {
                arrastando = false;
            }
        }

        p.pushStyle();

        p.textSize(14);
        p.fill(225, 232, 240);
        p.textAlign(PConstants.LEFT, PConstants.BOTTOM);
        p.text(rotulo, x, y + altura * 0.42f);

        p.fill(110, 210, 255);
        p.textAlign(PConstants.RIGHT, PConstants.BOTTOM);
        p.text(formatarValor(), x + largura, y + altura * 0.42f);

        float trilhaY = y + altura * 0.72f;
        float proporcao = (float) ((valor - min) / (max - min));
        float ponteiroX = x + proporcao * largura;

        if (imagemTrilha != null) {
            float hTrilha = Math.max(6.0f, altura * 0.25f);
            p.image(imagemTrilha, x, trilhaY - hTrilha / 2.0f, largura, hTrilha);
        } else {
            p.stroke(90, 102, 120);
            p.strokeWeight(2.0f);
            p.line(x, trilhaY, x + largura, trilhaY);

            p.stroke(110, 210, 255);
            p.strokeWeight(3.0f);
            p.line(x, trilhaY, ponteiroX, trilhaY);
        }

        if (imagemPonteiro != null) {
            float tamPonteiro = Math.max(16.0f, altura * 0.55f);
            p.image(imagemPonteiro, ponteiroX - tamPonteiro / 2.0f, trilhaY - tamPonteiro / 2.0f, tamPonteiro, tamPonteiro);
        } else {
            p.stroke(arrastando ? 255 : 230, 245, 255);
            p.strokeWeight(3.0f);
            p.line(ponteiroX, trilhaY - 8.0f, ponteiroX, trilhaY + 8.0f);
        }

        p.popStyle();
    }

    public boolean mousePressionado(float px, float py) {
        if (px >= x - 8.0f && px <= x + largura + 8.0f && py >= y && py <= y + altura) {
            arrastando = true;
            atualizarPorPosicaoX(px);
            return true;
        }
        return false;
    }

    public void mouseArrastado(float px) {
        if (arrastando) {
            atualizarPorPosicaoX(px);
        }
    }

    public void mouseSolto() {
        arrastando = false;
    }

    private void atualizarPorPosicaoX(float px) {
        float t = (px - x) / largura;
        t = Math.max(0.0f, Math.min(1.0f, t));
        double bruto = min + t * (max - min);
        setValor(bruto);
    }

    public void setValor(double novoValor) {
        double limitado = Math.max(min, Math.min(max, novoValor));
        if (inteiro) {
            this.valor = Math.round(limitado);
        } else {
            this.valor = limitado;
        }
    }

    public double getValor() {
        return valor;
    }

    public int getValorInteiro() {
        return (int) Math.round(valor);
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDimensoes(float largura, float altura) {
        this.largura = largura;
        this.altura = altura;
    }

    private String formatarValor() {
        if (inteiro) {
            return String.valueOf(getValorInteiro());
        }
        return String.format(Locale.US, "%.2f", valor);
    }
}
