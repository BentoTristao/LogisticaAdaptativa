package visual.camadas;

import grafo.core.Aresta;
import grafo.core.Grafo;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;

/**
- Camada de mapa de calor (Heatmap) que destaca as arestas mais visadas
- pela população de Planejadores de Obstáculos ao longo da evolução.
 */
public class CamadaHeatmap {
    private PImage imagemPontoCalor;
    private boolean assetsCarregados = false;
    private boolean ativa = false;

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/heatmap_ponto.png").exists()) {
            imagemPontoCalor = p.loadImage("data/heatmap_ponto.png");
        }
    }

    public void desenhar(PApplet p, Grafo grafo, int[] contagemBloqueiosPorAresta, float offsetX, float offsetY, float celula) {
        if (!ativa || p == null || grafo == null || contagemBloqueiosPorAresta == null) return;
        inicializarAssets(p);

        // Encontra o pico de frequência para normalizar a intensidade de todas as arestas entre 0.0 e 1.0
        int maxContagem = 0;
        for (int c : contagemBloqueiosPorAresta) {
            if (c > maxContagem) {
                maxContagem = c;
            }
        }
        if (maxContagem <= 0) return;

        p.pushStyle();

        int limiteArestas = Math.min(grafo.numArestas(), contagemBloqueiosPorAresta.length);
        for (int id = 0; id < limiteArestas; id++) {
            int contagem = contagemBloqueiosPorAresta[id];
            if (contagem <= 0) continue;

            float intensidade = (float) contagem / (float) maxContagem;
            Aresta aresta = grafo.arestas().get(id);

            float x1 = CamadaGrafo.centroX(grafo, aresta.origem(), offsetX, celula);
            float y1 = CamadaGrafo.centroY(grafo, aresta.origem(), offsetY, celula);
            float x2 = CamadaGrafo.centroX(grafo, aresta.destino(), offsetX, celula);
            float y2 = CamadaGrafo.centroY(grafo, aresta.destino(), offsetY, celula);

            // Interpolação de cor: amarelo quente -> vermelho intenso
            int r = 255;
            int g = (int) (220 * (1.0f - intensidade) + 40 * intensidade);
            int b = 45;
            int alfa = (int) (85 + 155 * intensidade);

            p.stroke(r, g, b, alfa);
            p.strokeWeight(Math.max(3.0f, celula * (0.16f + 0.24f * intensidade)));
            p.line(x1, y1, x2, y2);

            if (imagemPontoCalor != null) {
                float mx = (x1 + x2) / 2.0f;
                float my = (y1 + y2) / 2.0f;
                float tam = celula * (0.55f + 0.35f * intensidade);
                p.tint(r, g, b, alfa);
                p.image(imagemPontoCalor, mx - tam / 2.0f, my - tam / 2.0f, tam, tam);
                p.noTint();
            }
        }

        p.popStyle();
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public void alternar() {
        this.ativa = !this.ativa;
    }
}