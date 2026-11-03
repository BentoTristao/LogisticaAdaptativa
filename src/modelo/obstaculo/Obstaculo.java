package modelo.obstaculo;

import java.util.Objects;

/** Obstáculo programado para afetar uma aresta em determinado tick. */
public record Obstaculo(int arestaId, int tickAtivacao, TipoObstaculo tipo) {

    public Obstaculo {
        if (arestaId < 0) {
            throw new IllegalArgumentException("O id da aresta não pode ser negativo.");
        }
        if (tickAtivacao < 0) {
            throw new IllegalArgumentException("O tick de ativação não pode ser negativo.");
        }
        Objects.requireNonNull(tipo, "O tipo do obstáculo não pode ser nulo.");
    }
}
