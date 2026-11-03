package modelo.obstaculo;

import java.util.List;
import java.util.Objects;

/** Lista imutável de obstáculos que compõe o genoma do Planejador. */
public record PlanoObstaculos(List<Obstaculo> obstaculos) {

    public PlanoObstaculos {
        Objects.requireNonNull(obstaculos, "A lista de obstáculos não pode ser nula.");
        obstaculos = List.copyOf(obstaculos);
    }

    public static PlanoObstaculos vazio() {
        return new PlanoObstaculos(List.of());
    }

    /** Soma os pontos de desastre consumidos por todos os obstáculos do plano. */
    public int custoTotal() {
        return obstaculos.stream()
                .mapToInt(obstaculo -> obstaculo.tipo().custo)
                .sum();
    }
}
