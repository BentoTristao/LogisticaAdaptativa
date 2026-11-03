package modelo.config;

/**
 * Parâmetros compartilhados da simulação.
 *
 * Os valores iniciais seguem a seção 8 e a tabela de parâmetros da seção 5.5
 * do guia. Os campos são públicos para facilitar a integração com a tela de
 * configuração (F1), conforme o contrato do projeto.
 */
public class Configuracao {

    public int linhas = 30;
    public int colunas = 30;
    public double fracaoAtalhos = 0.10;
    public int raioVisao = 3;
    public int tamPopEntregador = 40;
    public int tamPopObstaculo = 40;
    public int geracoes = 150;
    public int confrontosPorIndividuo = 5;
    public int maxObstaculos = 12;
    public int orcamentoDesastre = 20;
    public double taxaCrossover = 0.8;
    public double taxaMutacao = 0.1;
    public long seed = 42L;

    /**
     * Verifica se os parâmetros estão dentro de intervalos utilizáveis.
     * Lança IllegalArgumentException com a primeira configuração inválida.
     */
    public void validar() {
        if (linhas < 2 || colunas < 2) {
            throw new IllegalArgumentException("O labirinto deve ter pelo menos 2 linhas e 2 colunas.");
        }
        if (!Double.isFinite(fracaoAtalhos) || fracaoAtalhos < 0.0 || fracaoAtalhos > 1.0) {
            throw new IllegalArgumentException("A fração de atalhos deve estar entre 0 e 1.");
        }
        if (raioVisao < 0) {
            throw new IllegalArgumentException("O raio de visão não pode ser negativo.");
        }
        if (tamPopEntregador < 1 || tamPopObstaculo < 1) {
            throw new IllegalArgumentException("As duas populações devem ter pelo menos um indivíduo.");
        }
        if (geracoes < 1 || confrontosPorIndividuo < 1) {
            throw new IllegalArgumentException("Gerações e confrontos por indivíduo devem ser positivos.");
        }
        if (maxObstaculos < 0 || orcamentoDesastre < 0) {
            throw new IllegalArgumentException("O limite de obstáculos e o orçamento não podem ser negativos.");
        }
        if (!probabilidadeValida(taxaCrossover) || !probabilidadeValida(taxaMutacao)) {
            throw new IllegalArgumentException("As taxas de crossover e mutação devem estar entre 0 e 1.");
        }
    }

    private boolean probabilidadeValida(double valor) {
        return Double.isFinite(valor) && valor >= 0.0 && valor <= 1.0;
    }
}
