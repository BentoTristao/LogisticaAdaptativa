package visual;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class SpriteManager {
    private PImage mapa;
    private PImage entregador;
    private PImage origem;
    private PImage destino;
    private PImage acidente;
    private PImage obra;
    private PImage bueiro;

    public void carregar(PApplet p) {
        // O Processing busca automaticamente dentro da pasta data/
        mapa = carregarSeguro(p, "mapa.jpg");
        entregador = carregarSeguro(p, "entregador.png");
        origem = carregarSeguro(p, "origem.png");
        destino = carregarSeguro(p, "destino.png");
        acidente = carregarSeguro(p, "acidente.png");
        obra = carregarSeguro(p, "obra.png");
        bueiro = carregarSeguro(p, "bueiro.png");
    }

    private PImage carregarSeguro(PApplet p, String nomeArquivo) {
        try {
            PImage img = p.loadImage(nomeArquivo);
            if (img == null) {
                System.err.println("Aviso: Sprite não encontrado em data/: " + nomeArquivo);
            }
            return img;
        } catch (Exception e) {
            System.err.println("Erro ao carregar sprite " + nomeArquivo + ": " + e.getMessage());
            return null;
        }
    }

    // 1. Desenha o mapa de fundo levemente escurecido para destacar o grafo
    public void desenharMapa(PApplet p, float x, float y, float largura, float altura) {
        if (mapa == null) return;
        p.pushStyle();
        p.imageMode(PConstants.CORNER);
        p.tint(150); // Escurece o fundo para dar contraste às arestas
        p.image(mapa, x, y, largura, altura);
        p.popStyle();
    }

    // 2. Desenha o ponto de origem (loja)
    public void desenharOrigem(PApplet p, float x, float y, float tamanho) {
        if (origem == null) return;
        p.pushStyle();
        p.imageMode(PConstants.CENTER);
        p.image(origem, x, y, tamanho, tamanho);
        p.popStyle();
    }

    // 3. Desenha o ponto de destino (pino vermelho com a ponta no centro do nó)
    public void desenharDestino(PApplet p, float x, float y, float tamanho) {
        if (destino == null) return;
        p.pushStyle();
        p.imageMode(PConstants.CENTER);
        // Desloca metade da altura para cima para a ponta do pino tocar (x, y)
        p.image(destino, x, y - (tamanho * 0.45f), tamanho, tamanho);
        p.popStyle();
    }

    // 4. Desenha o entregador (scooter) virado para a direção do movimento
    public void desenharEntregador(PApplet p, float x, float y, float tamanho, boolean indoParaDireita) {
        if (entregador == null) return;
        p.pushMatrix();
        p.pushStyle();
        p.translate(x, y);
        if (indoParaDireita) {
            p.scale(-1, 1); // Espelha horizontalmente (a imagem original aponta para a esquerda)
        }
        p.imageMode(PConstants.CENTER);
        p.image(entregador, 0, 0, tamanho, tamanho);
        p.popStyle();
        p.popMatrix();
    }

    // 5. Desenha o obstáculo no ponto médio da aresta
    public void desenharObstaculo(PApplet p, String tipoNome, float x, float y, float tamanho) {
        PImage sprite = switch (tipoNome.toUpperCase()) {
            case "ACIDENTE", "BLOQUEIO" -> acidente;
            case "OBRA", "INTERDICAO" -> obra;
            case "BUEIRO", "BURACO", "TRANSITO" -> bueiro;
            default -> obra;
        };

        if (sprite == null) return;

        p.pushStyle();
        // Se for o bueiro (escuro), desenha uma aura amarela sutil para destacar no asfalto
        if (sprite == bueiro) {
            p.noStroke();
            p.fill(255, 220, 0, 95);
            p.ellipse(x, y, tamanho * 1.15f, tamanho * 1.15f);
        }
        p.imageMode(PConstants.CENTER);
        p.image(sprite, x, y, tamanho, tamanho);
        p.popStyle();
    }
}
