package util;

import java.util.Random;

/**
 * Singleton determinístico para gestão de sementes aleatórias em todo o sistema.
 */
public final class RandomManager {
    private static volatile RandomManager instance;
    private final long sementeInicial;
    private final Random rngGlobal;

    private RandomManager(long seed) {
        this.sementeInicial = seed;
        this.rngGlobal = new Random(seed);
    }

    /**
     * Inicializa a instância Singleton com a semente definida na Configuracao.
     */
    public static synchronized void inicializar(long seed) {
        instance = new RandomManager(seed);
    }

    /**
     * Retorna a instância ativa (se não inicializada, utiliza a seed padrão 42L).
     */
    public static RandomManager getInstance() {
        if (instance == null) {
            synchronized (RandomManager.class) {
                if (instance == null) {
                    instance = new RandomManager(42L);
                }
            }
        }
        return instance;
    }

    /**
     * Retorna o gerador global de aleatoriedade.
     */
    public Random getRng() {
        return rngGlobal;
    }

    /**
     * Deriva um gerador secundário garantindo isolamento entre threads e reprodutibilidade.
     */
    public Random criarRngDerivado(long subSeed) {
        return new Random(this.sementeInicial ^ subSeed);
    }

    public long getSementeInicial() {
        return sementeInicial;
    }
}
