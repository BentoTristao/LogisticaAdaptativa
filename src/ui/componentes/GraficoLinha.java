package ui.componentes;

import evolucao.estatistica.EstatisticasGeracao;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Gráfico de linhas em tempo real para acompanhar a evolução do fitness
 * (melhor e média) das populações de Entregadores e Planejadores de Obstáculos.
 */
public class GraficoLinha {
    private float x;
    private float y;
    private float largura;
    private float altura;
    private String titulo;
    private PImage imagemFundo;

    private final List<Integer> geracoes = new ArrayList<>();
    private final List<Double> melhorEntregador = new ArrayList<>();
    private final List<Double> mediaEntregador = new ArrayList<>();
    private final List<Double> melhorObstaculo = new ArrayList<>();
    private final List<Double> mediaObstaculo = new ArrayList<>();

    public GraficoLinha(float x, float y, float largura, float altura, String titulo) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.titulo = titulo != null ? titulo : "Evolução do Fitness";
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

    public void adicionarPonto(EstatisticasGeracao est) {
        if (est == null) return;
        geracoes.add(est.geracao());
        melhorEntregador.add(est.melhorEntregador());
        mediaEntregador.add(est.mediaEntregador());
        melhorObstaculo.add(est.melhorObstaculo());
        mediaObstaculo.add(est.mediaObstaculo());
    }

    public void limpar() {
        geracoes.clear();
        melhorEntregador.clear();
        mediaEntregador.clear();
        melhorObstaculo.clear();
        mediaObstaculo.clear();
    }

    public void desenhar(PApplet p) {
        if (p == null) return;

        p.pushStyle();

        if (imagemFundo != null) {
            p.image(imagemFundo, x, y, largura, altura);
        }

        float margemEsq = 58.0f;
        float margemDir = 20.0f;
        float margemSup = 52.0f;
        float margemInf = 36.0f;

        float areaX = x + margemEsq;
        float areaY = y + margemSup;
        float areaW = Math.max(10.0f, largura - margemEsq - margemDir);
        float areaH = Math.max(10.0f, altura - margemSup - margemInf);

        p.fill(240, 244, 248);
        p.textSize(16);
        p.textAlign(PConstants.LEFT, PConstants.TOP);
        p.text(titulo, x + 12.0f, y + 8.0f);

        desenharLegenda(p, x + 12.0f, y + 30.0f);

        // Eixos X e Y
        p.stroke(140, 150, 165);
        p.strokeWeight(1.5f);
        p.line(areaX, areaY, areaX, areaY + areaH);
        p.line(areaX, areaY + areaH, areaX + areaW, areaY + areaH);

        if (geracoes.isEmpty()) {
            p.fill(160, 170, 185);
            p.textSize(14);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.text("Aguardando dados das gerações...", areaX + areaW / 2.0f, areaY + areaH / 2.0f);
            p.popStyle();
            return;
        }

        double maxValor = calcularValorMaximo();
        if (maxValor <= 0.0) {
            maxValor = 1.0;
        }

        // Escala eixo Y
        p.fill(180, 190, 205);
        p.textSize(11);
        p.textAlign(PConstants.RIGHT, PConstants.CENTER);
        p.text("0", areaX - 6.0f, areaY + areaH);
        p.text(String.format(Locale.US, "%.0f", maxValor / 2.0), areaX - 6.0f, areaY + areaH / 2.0f);
        p.text(String.format(Locale.US, "%.0f", maxValor), areaX - 6.0f, areaY);

        // Escala eixo X
        p.textAlign(PConstants.CENTER, PConstants.TOP);
        p.text("G" + geracoes.get(0), areaX, areaY + areaH + 6.0f);
        if (geracoes.size() > 1) {
            p.text("G" + geracoes.get(geracoes.size() - 1), areaX + areaW, areaY + areaH + 6.0f);
        }

        desenharSerie(p, melhorEntregador, areaX, areaY, areaW, areaH, maxValor, 80, 220, 140, 2.5f);
        desenharSerie(p, mediaEntregador, areaX, areaY, areaW, areaH, maxValor, 140, 235, 180, 1.5f);
        desenharSerie(p, melhorObstaculo, areaX, areaY, areaW, areaH, maxValor, 255, 110, 110, 2.5f);
        desenharSerie(p, mediaObstaculo, areaX, areaY, areaW, areaH, maxValor, 255, 175, 130, 1.5f);

        p.popStyle();
    }

    private void desenharLegenda(PApplet p, float lx, float ly) {
        p.textSize(12);
        p.textAlign(PConstants.LEFT, PConstants.CENTER);

        desenharItemLegenda(p, lx, ly, "Melhor Entregador", 80, 220, 140);
        desenharItemLegenda(p, lx + 145.0f, ly, "Média Entregador", 140, 235, 180);
        desenharItemLegenda(p, lx + 285.0f, ly, "Melhor Obstáculo", 255, 110, 110);
        desenharItemLegenda(p, lx + 425.0f, ly, "Média Obstáculo", 255, 175, 130);
    }

    private void desenharItemLegenda(PApplet p, float ix, float iy, String texto, int r, int g, int b) {
        p.stroke(r, g, b);
        p.strokeWeight(2.5f);
        p.line(ix, iy, ix + 14.0f, iy);
        p.fill(215, 222, 232);
        p.text(texto, ix + 18.0f, iy);
    }

    private void desenharSerie(
            PApplet p,
            List<Double> valores,
            float areaX,
            float areaY,
            float areaW,
            float areaH,
            double maxValor,
            int r,
            int g,
            int b,
            float espessura
    ) {
        if (valores.size() < 2) return;

        p.stroke(r, g, b);
        p.strokeWeight(espessura);

        int n = valores.size();
        for (int i = 0; i < n - 1; i++) {
            float x1 = areaX + (i / (float) (n - 1)) * areaW;
            float y1 = areaY + areaH - (float) (Math.max(0.0, valores.get(i)) / maxValor) * areaH;

            float x2 = areaX + ((i + 1) / (float) (n - 1)) * areaW;
            float y2 = areaY + areaH - (float) (Math.max(0.0, valores.get(i + 1)) / maxValor) * areaH;

            p.line(x1, y1, x2, y2);
        }
    }

    private double calcularValorMaximo() {
        double max = 0.0;
        for (double v : melhorEntregador) max = Math.max(max, v);
        for (double v : mediaEntregador) max = Math.max(max, v);
        for (double v : melhorObstaculo) max = Math.max(max, v);
        for (double v : mediaObstaculo) max = Math.max(max, v);
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
}
