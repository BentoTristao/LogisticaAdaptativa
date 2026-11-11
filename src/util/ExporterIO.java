package util;

import evolucao.estatistica.EstatisticasGeracao;
import modelo.config.Configuracao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Leitor e escritor de arquivos para persistência de estatísticas (CSV) e configurações (.properties).
 */
public final class ExporterIO {
    private ExporterIO() {
        // Classe utilitária
    }

    /**
     * Salva o histórico de gerações em um arquivo CSV formatado.
     */
    public static void exportarEstatisticasCSV(List<EstatisticasGeracao> estatisticas, String caminhoArquivo) throws IOException {
        File file = new File(caminhoArquivo);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("geracao;melhorEntregador;mediaEntregador;melhorObstaculo;mediaObstaculo\n");
            for (EstatisticasGeracao e : estatisticas) {
                writer.write(String.format(Locale.US, "%d;%.4f;%.4f;%.4f;%.4f\n", e.geracao(), e.melhorEntregador(), e.mediaEntregador(), e.melhorObstaculo(), e.mediaObstaculo()));
            }
        }
    }

    /**
     * Grava as configurações vigentes em um arquivo no formato chave-valor.
     */
    public static void salvarConfiguracao(Configuracao cfg, String caminhoArquivo) throws IOException {
        File file = new File(caminhoArquivo);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("linhas=" + cfg.linhas + "\n");
            writer.write("colunas=" + cfg.colunas + "\n");
            writer.write("fracaoAtalhos=" + cfg.fracaoAtalhos + "\n");
            writer.write("raioVisao=" + cfg.raioVisao + "\n");
            writer.write("tamPopEntregador=" + cfg.tamPopEntregador + "\n");
            writer.write("tamPopObstaculo=" + cfg.tamPopObstaculo + "\n");
            writer.write("geracoes=" + cfg.geracoes + "\n");
            writer.write("confrontosPorIndividuo=" + cfg.confrontosPorIndividuo + "\n");
            writer.write("maxObstaculos=" + cfg.maxObstaculos + "\n");
            writer.write("orcamentoDesastre=" + cfg.orcamentoDesastre + "\n");
            writer.write("taxaCrossover=" + cfg.taxaCrossover + "\n");
            writer.write("taxaMutacao=" + cfg.taxaMutacao + "\n");
            writer.write("seed=" + cfg.seed + "\n");
        }
    }

    /**
     * Carrega as configurações salva no disco.
     */
    public static Configuracao carregarConfiguracao(String caminhoArquivo) throws IOException {
        Configuracao cfg = new Configuracao();
        File file = new File(caminhoArquivo);
        if (!file.exists()) return cfg;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] partes = linha.split("=");
                if (partes.length == 2) {
                    String chave = partes[0].trim();
                    String valor = partes[1].trim();
                    switch (chave) {
                        case "linhas" -> cfg.linhas = Integer.parseInt(valor);
                        case "colunas" -> cfg.colunas = Integer.parseInt(valor);
                        case "fracaoAtalhos" -> cfg.fracaoAtalhos = Double.parseDouble(valor);
                        case "raioVisao" -> cfg.raioVisao = Integer.parseInt(valor);
                        case "tamPopEntregador" -> cfg.tamPopEntregador = Integer.parseInt(valor);
                        case "tamPopObstaculo" -> cfg.tamPopObstaculo = Integer.parseInt(valor);
                        case "geracoes" -> cfg.geracoes = Integer.parseInt(valor);
                        case "confrontosPorIndividuo" -> cfg.confrontosPorIndividuo = Integer.parseInt(valor);
                        case "maxObstaculos" -> cfg.maxObstaculos = Integer.parseInt(valor);
                        case "orcamentoDesastre" -> cfg.orcamentoDesastre = Integer.parseInt(valor);
                        case "taxaCrossover" -> cfg.taxaCrossover = Double.parseDouble(valor);
                        case "taxaMutacao" -> cfg.taxaMutacao = Double.parseDouble(valor);
                        case "seed" -> cfg.seed = Long.parseLong(valor);
                    }
                }
            }
        }
        return cfg;
    }
}
