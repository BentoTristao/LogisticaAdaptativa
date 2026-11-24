package ui.componentes;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráfico de linhas em tempo real para acompanhar a evolução do fitness
 * (melhor indivíduo e média da população) ao longo das gerações.
 */
public class GraficoLinha {
    private float x;
    private float y;
    private float largura;
    private float altura;
    private String titulo;
    private final String rotuloMelhor;
    private final String rotuloMedia;
    private final int rMelhor, gMelhor, bMelhor;
    private final int rMedia, gMedia, bMedia;
    private double valorMinimoEixoY = 0.0;
    private PImage imagemFundo;

    private final List<Integer> geracoes = new ArrayList<>();
    private final List<Double> serieMelhor = new ArrayList<>();
    private final List<Double> serieMedia = new ArrayList<>();

    public GraficoLinha(float x, float y, float largura, float altura, String titulo) {
        this(x, y, largura, altura, titulo, "Melhor da Geração", 80, 220, 140, "Média da População", 140, 235, 180);
    }

    public GraficoLinha(
            float x, float y, float largura, float altura, String titulo,
            String rotuloMelhor, int rMelhor, int gMelhor, int bMelhor,
            String rotuloMedia, int rMedia, int gMedia, int bMedia) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.titulo = titulo != null ? titulo : "Evolução do Fitness";
        this.rotuloMelhor = rotuloMelhor != null ? rotuloMelhor : "Melhor";
        this.rotuloMedia = rotuloMedia != null ? rotuloMedia : "Média";
        this.rMelhor = rMelhor;
        this.gMelhor = gMelhor;
        this.bMelhor = bMelhor;
        this.rMedia = rMedia;
        this.gMedia = gMedia;
        this.bMedia = bMedia;
    }

    /**
     * Carrega imagem de fundo opcional da pasta data/.
     */
    public void carregarFundo(PApplet p, String caminhoFundo) {
        if (p != null && caminhoFundo != null && new File(caminhoFundo).exists()) {
            this.imagemFundo = p.loadImage(caminhoFundo);
        }
    }

    public void setImagemFundo(PImage imagemFundo) {
        this.imagemFundo = imagemFundo;
    }

    public void adicionarPonto(int geracao, double valorMelhor, double valorMedia) {
        geracoes.add(geracao);
        serieMelhor.add(valorMelhor);
        serieMedia.add(valorMedia);
    }

    public void limpar() {
        geracoes.clear();
        serieMelhor.clear();
        serieMedia.clear();
    }

    public void desenhar(PApplet p) {
        if (p == null) return;

        p.pushStyle();

        if (imagemFundo != null) {
            p.image(imagemFundo, x, y, largura, altura);
        }

        float margemEsq = 58.0f;
        float margemDir = 20.0f;
        float margemSup = 48.0f;
        float margemInf = 28.0f;

        float areaX = x + margemEsq;
        float areaY = y + margemSup;
        float areaW = Math.max(10.0f, largura - margemEsq - margemDir);
        float areaH = Math.max(10.0f, altura - margemSup - margemInf);

        p.fill(240, 244, 248);
        p.textSize(14);
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        p.text(titulo, x + 12.0f, y + 6.0f);

        desenharLegenda(p, x + 12.0f, y + 27.0f);

        // Eixos X e Y
        p.stroke(140, 150, 165);
        p.strokeWeight(1.5f);
        p.line(areaX, areaY, areaX, areaY + areaH);
        p.line(areaX, areaY + areaH, areaX + areaW, areaY + areaH);

        if (geracoes.isEmpty()) {
            p.fill(160, 170, 185);
            p.textSize(13);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.text("Aguardando dados das gerações...", areaX + areaW / 2.0f, areaY + areaH / 2.0f);
            p.popStyle();
            return;
        }

        double minValor = calcularValorMinimo();
        double maxValor = calcularValorMaximo();
        if (maxValor <= minValor) {
            maxValor = minValor + 1.0;
        }
        double meioValor = (minValor + maxValor) / 2.0;

        // Linhas guia horizontais sutis
        p.stroke(75, 85, 102, 110);
        p.strokeWeight(1.0f);
        p.line(areaX, areaY + areaH / 2.0f, areaX + areaW, areaY + areaH / 2.0f);
        p.line(areaX, areaY, areaX + areaW, areaY);

        // Escala eixo Y
        double amplitude = maxValor - minValor;
        String formatoY = amplitude < 10.0 ? "%.1f" : "%.0f";
        p.fill(180, 190, 205);
        p.textSize(11);
        p.textAlign(PConstants.RIGHT, PConstants.CENTER);
        p.text(String.format(Locale.US, formatoY, minValor), areaX - 6.0f, areaY + areaH);
        p.text(String.format(Locale.US, formatoY, meioValor), areaX - 6.0f, areaY + areaH / 2.0f);
        p.text(String.format(Locale.US, formatoY, maxValor), areaX - 6.0f, areaY);

        // Escala eixo X
        p.textAlign(PConstants.CENTER, PConstants.TOP);
        p.text("G" + geracoes.get(0), areaX, areaY + areaH + 5.0f);
        if (geracoes.size() > 1) {
            p.text("G" + geracoes.get(geracoes.size() - 1), areaX + areaW, areaY + areaH + 5.0f);
        }

        desenharSerie(p, serieMedia, areaX, areaY, areaW, areaH, minValor, maxValor, rMedia, gMedia, bMedia, 1.8f);
        desenharSerie(p, serieMelhor, areaX, areaY, areaW, areaH, minValor, maxValor, rMelhor, gMelhor, bMelhor, 2.6f);

        p.popStyle();
    }

    private void desenharLegenda(PApplet p, float lx, float ly) {
        p.textSize(11);
        p.textAlign(PConstants.LEFT, PConstants.CENTER);

        desenharItemLegenda(p, lx, ly, rotuloMelhor, rMelhor, gMelhor, bMelhor, 2.6f);
        desenharItemLegenda(p, lx + 175.0f, ly, rotuloMedia, rMedia, gMedia, bMedia, 1.8f);
    }

    private void desenharItemLegenda(PApplet p, float ix, float iy, String texto, int r, int g, int b, float espessura) {
        p.stroke(r, g, b);
        p.strokeWeight(espessura);
        p.line(ix, iy, ix + 16.0f, iy);
        p.fill(215, 222, 232);
        p.text(texto, ix + 22.0f, iy);
    }

    private void desenharSerie(PApplet p, List<Double> valores, float areaX, float areaY, float areaW, float areaH, double minValor, double maxValor, int r, int g, int b, float espessura) {
        if (valores.isEmpty()) return;

        double amplitude = Math.max(1e-6, maxValor - minValor);

        p.stroke(r, g, b);
        if (valores.size() == 1) {
            double norm = Math.max(0.0, Math.min(1.0, (valores.get(0) - minValor) / amplitude));
            float py = areaY + areaH - (float) norm * areaH;
            p.strokeWeight(espessura + 2.5f);
            p.point(areaX, py);
            return;
        }

        p.strokeWeight(espessura);

        int n = valores.size();
        for (int i = 0; i < n - 1; i++) {
            float x1 = areaX + (i / (float) (n - 1)) * areaW;
            double norm1 = Math.max(0.0, Math.min(1.0, (valores.get(i) - minValor) / amplitude));
            float y1 = areaY + areaH - (float) norm1 * areaH;

            float x2 = areaX + ((i + 1) / (float) (n - 1)) * areaW;
            double norm2 = Math.max(0.0, Math.min(1.0, (valores.get(i + 1) - minValor) / amplitude));
            float y2 = areaY + areaH - (float) norm2 * areaH;

            p.line(x1, y1, x2, y2);
        }
    }

    private double calcularValorMinimo() {
        double min = valorMinimoEixoY;
        for (double v : serieMelhor) min = Math.min(min, v);
        for (double v : serieMedia) min = Math.min(min, v);
        return min;
    }

    private double calcularValorMaximo() {
        double max = valorMinimoEixoY;
        for (double v : serieMelhor) max = Math.max(max, v);
        for (double v : serieMedia) max = Math.max(max, v);
        return max;
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDimensoes(float largura, float altura) {
        this.largura = largura;
        this.altura = altura;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setValorMinimoEixoY(double valorMinimoEixoY) {
        this.valorMinimoEixoY = valorMinimoEixoY;
    }
}
