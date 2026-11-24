package visual.camadas;

import grafo.core.Aresta;
import grafo.core.Grafo;
import modelo.obstaculo.TipoObstaculo;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.Map;
import java.util.Objects;

/**
- Camada base de renderização do grafo, obstáculos ativos e agente Entregador.
- Carrega sprites da pasta data/ sem desenhar formas geométricas.
 */
public class CamadaGrafo {
    private PImage imagemFundoMapa;
    private PImage imagemNo;
    private PImage imagemInicio;
    private PImage imagemDestino;
    private PImage imagemEntregador;
    private PImage imagemDesvio;
    private PImage imagemCongestionamento;
    private PImage imagemBloqueio;
    private boolean assetsCarregados = false;

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        imagemFundoMapa = carregarSeExistir(p, "data/fundo_mapa.png");
        imagemNo = carregarSeExistir(p, "data/no_grafo.png");
        imagemInicio = carregarSeExistir(p, "data/icone_inicio.png");
        imagemDestino = carregarSeExistir(p, "data/icone_destino.png");
        imagemEntregador = carregarSeExistir(p, "data/entregador.png");
        imagemDesvio = carregarSeExistir(p, "data/obs_desvio.png");
        imagemCongestionamento = carregarSeExistir(p, "data/obs_congestionamento.png");
        imagemBloqueio = carregarSeExistir(p, "data/obs_bloqueio.png");
    }

    private PImage carregarSeExistir(PApplet p, String caminho) {
        if (new File(caminho).exists()) {
            return p.loadImage(caminho);
        }
        return null;
    }

    public void desenhar(PApplet p, Grafo grafo, double[] pesosAtuais, Map<Integer, TipoObstaculo> obstaculosAtivos, int posicaoEntregador, float offsetX, float offsetY, float celula) {
        if (p == null || grafo == null) return;
        Objects.requireNonNull(pesosAtuais, "O vetor de pesos não pode ser nulo.");
        inicializarAssets(p);

        p.pushStyle();

        float larguraGrid = celula * grafo.colunas();
        float alturaGrid = celula * grafo.linhas();

        if (imagemFundoMapa != null) {
    p.tint(95); // Deixa o mapa mais escuro e discreto
    p.image(imagemFundoMapa, offsetX, offsetY, larguraGrid, alturaGrid);
    p.noTint();
}
        // 1. Renderiza arestas e obstáculos ativos
        // Classifica o estado visual da aresta priorizando a severidade (Bloqueio > Congestionamento > Desvio > Livre)
        for (Aresta aresta : grafo.arestas()) {
            int u = aresta.origem();
            int v = aresta.destino();

            float x1 = centroX(grafo, u, offsetX, celula);
            float y1 = centroY(grafo, u, offsetY, celula);
            float x2 = centroX(grafo, v, offsetX, celula);
            float y2 = centroY(grafo, v, offsetY, celula);

            double peso = pesosAtuais[aresta.id()];
            TipoObstaculo tipo = obstaculosAtivos != null ? obstaculosAtivos.get(aresta.id()) : null;

            if (Double.isInfinite(peso) || tipo == TipoObstaculo.BLOQUEIO) {
                p.stroke(245, 75, 75, 190);
                p.strokeWeight(Math.max(2.0f, celula * 0.14f));
                p.line(x1, y1, x2, y2);
                desenharSpriteObstaculo(p, imagemBloqueio, "X", (x1 + x2) / 2.0f, (y1 + y2) / 2.0f, celula, 245, 75, 75);
            } else if (tipo == TipoObstaculo.CONGESTIONAMENTO || peso >= 5.0) {
                p.stroke(255, 145, 55, 220);
                p.strokeWeight(Math.max(2.5f, celula * 0.16f));
                p.line(x1, y1, x2, y2);
                desenharSpriteObstaculo(p, imagemCongestionamento, "!", (x1 + x2) / 2.0f, (y1 + y2) / 2.0f, celula, 255, 145, 55);
            } else if (tipo == TipoObstaculo.DESVIO || peso > 1.0) {
                p.stroke(245, 210, 65, 220);
                p.strokeWeight(Math.max(2.0f, celula * 0.14f));
                p.line(x1, y1, x2, y2);
                desenharSpriteObstaculo(p, imagemDesvio, "~", (x1 + x2) / 2.0f, (y1 + y2) / 2.0f, celula, 245, 210, 65);
            } else {
                p.stroke(85, 100, 122);
                p.strokeWeight(Math.max(1.5f, celula * 0.10f));
                p.line(x1, y1, x2, y2);
            }
        }

        // 2. Renderiza vértices da grade
        for (int v = 0; v < grafo.numVertices(); v++) {
            float cx = centroX(grafo, v, offsetX, celula);
            float cy = centroY(grafo, v, offsetY, celula);

            if (imagemNo != null) {
                float tamNo = celula * 0.32f;
                p.image(imagemNo, cx - tamNo / 2.0f, cy - tamNo / 2.0f, tamNo, tamNo);
            } else {
                float cruz = Math.max(1.5f, celula * 0.08f);
                p.stroke(130, 148, 172);
                p.strokeWeight(1.5f);
                p.line(cx - cruz, cy, cx + cruz, cy);
                p.line(cx, cy - cruz, cx, cy + cruz);
            }
        }

        // 3. Marcador de Início
        desenharMarcadorVertice(p, grafo, grafo.verticeInicio(), offsetX, offsetY, celula, imagemInicio, "S", 80, 220, 140);

        // 4. Marcador de Destino
        desenharMarcadorVertice(p, grafo, grafo.verticeDestino(), offsetX, offsetY, celula, imagemDestino, "D", 110, 210, 255);

        // 5. Agente Entregador
        if (posicaoEntregador >= 0 && posicaoEntregador < grafo.numVertices()) {
            float ex = centroX(grafo, posicaoEntregador, offsetX, celula);
            float ey = centroY(grafo, posicaoEntregador, offsetY, celula);

            if (imagemEntregador != null) {
                float tamAgente = celula * 0.78f;
                p.image(imagemEntregador, ex - tamAgente / 2.0f, ey - tamAgente / 2.0f, tamAgente, tamAgente);
            } else {
                float r = celula * 0.28f;
                p.stroke(255, 255, 255);
                p.strokeWeight(2.5f);
                p.line(ex - r, ey - r, ex + r, ey + r);
                p.line(ex - r, ey + r, ex + r, ey - r);

                p.fill(255, 255, 255);
                p.textAlign(PConstants.CENTER, PConstants.BOTTOM);
                p.textSize(Math.max(11, celula * 0.42f));
                p.text("E", ex, ey - r * 0.6f);
            }
        }

        p.popStyle();
    }

    private void desenharSpriteObstaculo(PApplet p, PImage sprite, String simboloFallback, float mx, float my, float celula, int r, int g, int b) {
        if (sprite != null) {
            float tam = celula * 0.55f;
            p.image(sprite, mx - tam / 2.0f, my - tam / 2.0f, tam, tam);
        } else {
            p.fill(r, g, b);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(Math.max(10, celula * 0.38f));
            p.text(simboloFallback, mx, my);
        }
    }

    private void desenharMarcadorVertice(PApplet p, Grafo grafo, int vertice, float offsetX, float offsetY, float celula, PImage sprite, String rotuloFallback, int r, int g,int b) {
        float cx = centroX(grafo, vertice, offsetX, celula);
        float cy = centroY(grafo, vertice, offsetY, celula);

        if (sprite != null) {
            float tam = celula * 0.72f;
            p.image(sprite, cx - tam / 2.0f, cy - tam / 2.0f, tam, tam);
        } else {
            p.fill(r, g, b);
            p.textAlign(PConstants.CENTER, PConstants.CENTER);
            p.textSize(Math.max(12, celula * 0.48f));
            p.text(rotuloFallback, cx, cy);
        }
    }

    // Converte o índice do vértice para o ponto central (em pixels) da sua célula na grade
    public static float centroX(Grafo grafo, int vertice, float offsetX, float celula) {
        return offsetX + (grafo.colunaDe(vertice) + 0.5f) * celula;
    }

    public static float centroY(Grafo grafo, int vertice, float offsetY, float celula) {
        return offsetY + (grafo.linhaDe(vertice) + 0.5f) * celula;
    }
}
