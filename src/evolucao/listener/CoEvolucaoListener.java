package evolucao.listener;

import evolucao.estatistica.EstatisticasGeracao;

/**
 * Ouvinte do ciclo de co-evolução.
 * Nota: Os callbacks são executados na Thread própria da evolução.
 */
public interface CoEvolucaoListener {
     // Chamado quando uma geração é concluída e suas estatísticas são consolidadas.
    void aoFimDaGeracao(EstatisticasGeracao estatisticas);

    // Chamado ao término do limite de gerações ou por interrupção do usuário.
    void aoTerminar();
}
