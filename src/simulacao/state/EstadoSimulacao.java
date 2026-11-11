package simulacao.state;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representa o estado instantâneo da simulação em um tick específico.
 */
public final class EstadoSimulacao {
    private final int tick;
    private final int posicaoEntregador;
    private final double[] pesosAtuais;
    private final Set<Integer> obstaculosAtivosArestas;
    private final List<Integer> rotaPlanejada;
    private final boolean concluido;
    private final boolean chegou;

    public EstadoSimulacao(int tick, int posicaoEntregador, double[] pesosAtuais, Set<Integer> obstaculosAtivosArestas, List<Integer> rotaPlanejada, boolean concluido, boolean chegou) {
        if (tick < 0) {
            throw new IllegalArgumentException("Tick não pode ser negativo.");
        }
        if (posicaoEntregador < 0) {
            throw new IllegalArgumentException("Posição do entregador não pode ser negativa.");
        }
        Objects.requireNonNull(pesosAtuais, "Pesos atuais não podem ser nulos.");

        this.tick = tick;
        this.posicaoEntregador = posicaoEntregador;
        this.pesosAtuais = pesosAtuais.clone();
        this.obstaculosAtivosArestas = obstaculosAtivosArestas != null ? Set.copyOf(obstaculosAtivosArestas) : Set.of();
        this.rotaPlanejada = rotaPlanejada != null ? List.copyOf(rotaPlanejada) : List.of();
        this.concluido = concluido;
        this.chegou = chegou;
    }

    public int tick() { return tick; }
    public int posicaoEntregador() { return posicaoEntregador; }
    public double[] pesosAtuais() { return pesosAtuais.clone(); }
    public Set<Integer> obstaculosAtivosArestas() { return obstaculosAtivosArestas; }
    public List<Integer> rotaPlanejada() { return rotaPlanejada; }
    public boolean concluido() { return concluido; }
    public boolean chegou() { return chegou; }
}
