package visual.camadas;

import grafo.busca.BfsVisao;
import grafo.core.Grafo;
import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Camada de Névoa de Guerra (visão parcial via BFS até raio k).
 * Utiliza texturas da pasta data/ ou hachuras lineares como fallback, sem formas geométricas.
 */
public class CamadaNevoa {
    private PImage imagemNevoaDesconhecida;
    private PImage imagemNevoaExplorada;
    private boolean assetsCarregados = false;
    private boolean ativa = true;

    public void inicializarAssets(PApplet p) {
        if (assetsCarregados || p == null) return;
        assetsCarregados = true;

        if (new File("data/nevoa_desconhecida.png").exists()) {
            imagemNevoaDesconhecida = p.loadImage("data/nevoa_desconhecida.png");
        }
        if (new File("data/nevoa_explorada.png").exists()) {
            imagemNevoaExplorada = p.loadImage("data/nevoa_explorada.png");
        }
    }

    public void desenhar(PApplet p, Grafo grafo, int posicaoEntregador, int raioVisao, Set<Integer> verticesExplorados, float offsetX, float offsetY, float celula) {
        if (!ativa || p == null || grafo == null) return;
        inicializarAssets(p);

        Set<Integer> visiveisAgora = new HashSet<>(BfsVisao.bfsAteRaio(grafo, posicaoEntregador, raioVisao));
        Set<Integer> explorados = verticesExplorados != null ? verticesExplorados : Set.of();

        p.pushStyle();

        for (int v = 0; v < grafo.numVertices(); v++) {
            if (visiveisAgora.contains(v)) {
                continue; // Área totalmente visível no tick atual
            }

            int lin = grafo.linhaDe(v);
            int col = grafo.colunaDe(v);
            float x = offsetX + col * celula;
            float y = offsetY + lin * celula;

            boolean jaExplorado = explorados.contains(v);

            if (jaExplorado) {
                if (imagemNevoaExplorada != null) {
                    p.image(imagemNevoaExplorada, x, y, celula, celula);
                } else {
                    // Hachura diagonal leve para memória já explorada
                    p.stroke(30, 38, 52, 135);
                    p.strokeWeight(1.5f);
                    p.line(x + celula * 0.2f, y + celula * 0.2f, x + celula * 0.8f, y + celula * 0.8f);
                }
            } else {
                if (imagemNevoaDesconhecida != null) {
                    p.image(imagemNevoaDesconhecida, x, y, celula, celula);
                } else {
                    // Hachura cruzada para região ainda desconhecida
                    p.stroke(14, 18, 26, 215);
                    p.strokeWeight(2.0f);
                    p.line(x + 2.0f, y + 2.0f, x + celula - 2.0f, y + celula - 2.0f);
                    p.line(x + celula - 2.0f, y + 2.0f, x + 2.0f, y + celula - 2.0f);
                }
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
