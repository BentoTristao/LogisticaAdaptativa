package visual.camadas;

import grafo.core.Aresta;
import grafo.core.Grafo;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.io.File;
import java.util.List;

/**
 * Camada responsável por exibir a trilha percorrida pelo Entregador,
 * a rota planejada pelo A* e os pontos de recálculo.
 */
public class CamadaTrilha {
    private PImage imagemPasso;
    private PImage imagemRecalculo;
    private boolean assetsCarregados = false;

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/trilha_passo.png").exists()) {
            imagemPasso = p.loadImage("data/trilha_passo.png");
        }
        if (new File("data/icone_recalculo.png").exists()) {
            imagemRecalculo = p.loadImage("data/icone_recalculo.png");
        }
    }

    public void desenhar(PApplet p, Grafo grafo, int[] verticePorTick, int tickAtual, List<Integer> rotaPlanejada, int[] ticksDosRecalculos, float offsetX, float offsetY, float celula) {
        if (p == null || grafo == null) return;
        inicializarAssets(p);

        p.pushStyle();

        // 1. Rota planejada atual (A*)
        if (rotaPlanejada != null && !rotaPlanejada.isEmpty()) {
            p.stroke(110, 210, 255, 175);
            p.strokeWeight(Math.max(2.0f, celula * 0.14f));

            for (int arestaId : rotaPlanejada) {
                if (arestaId < 0 || arestaId >= grafo.numArestas()) continue;
                Aresta a = grafo.arestas().get(arestaId);
                float x1 = CamadaGrafo.centroX(grafo, a.origem(), offsetX, celula);
                float y1 = CamadaGrafo.centroY(grafo, a.origem(), offsetY, celula);
                float x2 = CamadaGrafo.centroX(grafo, a.destino(), offsetX, celula);
                float y2 = CamadaGrafo.centroY(grafo, a.destino(), offsetY, celula);
                p.line(x1, y1, x2, y2);
            }
        }

        // 2. Trilha já percorrida até tickAtual
        if (verticePorTick != null && verticePorTick.length > 0) {
            int limite = Math.min(tickAtual, verticePorTick.length - 1);

            p.stroke(80, 220, 140, 230);
            p.strokeWeight(Math.max(2.5f, celula * 0.18f));

            for (int t = 0; t < limite; t++) {
                int v1 = verticePorTick[t];
                int v2 = verticePorTick[t + 1];
                if (v1 == v2) continue;

                float x1 = CamadaGrafo.centroX(grafo, v1, offsetX, celula);
                float y1 = CamadaGrafo.centroY(grafo, v1, offsetY, celula);
                float x2 = CamadaGrafo.centroX(grafo, v2, offsetX, celula);
                float y2 = CamadaGrafo.centroY(grafo, v2, offsetY, celula);
                p.line(x1, y1, x2, y2);

                if (imagemPasso != null) {
                    float tam = celula * 0.35f;
                    p.image(imagemPasso, x1 - tam / 2.0f, y1 - tam / 2.0f, tam, tam);
                }
            }
        }

        // 3. Marcadores de recálculo ocorridos até tickAtual
        if (ticksDosRecalculos != null && verticePorTick != null && verticePorTick.length > 0) {
            for (int tickRec : ticksDosRecalculos) {
                if (tickRec <= tickAtual && tickRec >= 0 && tickRec < verticePorTick.length) {
                    int vRec = verticePorTick[tickRec];
                    float rx = CamadaGrafo.centroX(grafo, vRec, offsetX, celula);
                    float ry = CamadaGrafo.centroY(grafo, vRec, offsetY, celula);

                    if (imagemRecalculo != null) {
                        float tam = celula * 0.55f;
                        p.image(imagemRecalculo, rx - tam / 2.0f, ry - tam / 2.0f, tam, tam);
                    } else {
                        p.fill(255, 215, 90);
                        p.textAlign(PConstants.CENTER, PConstants.TOP);
                        p.textSize(Math.max(10, celula * 0.35f));
                        p.text("R", rx, ry + celula * 0.12f);
                    }
                }
            }
        }

        p.popStyle();
    }
}
