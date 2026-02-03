package com.intuitivecare.questao1;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream; // Para remover caracteres estranhos do início do arquivo

import java.io.*;
import java.nio.charset.StandardCharsets; // Usei ISO_8859_1 pois arquivos gov br geralmente são ANSI
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Responsável pelo processamento, transformação e consolidação dos dados (ETL).
 * Lê múltiplos arquivos CSV, filtra as linhas relevantes e gera um arquivo unificado.
 */

public class CsvProcessor {

    private static final String DOWNLOAD_DIR = "downloads";
    private static final String ARQUIVO_SAIDA = "consolidado.csv";

    // Termos que indicam que a linha é uma Despesa com Evento/Sinistro
    private static final String[] TERMOS_DESPESA = {"EVENTOS", "SINISTROS"};

    /**
     * Orquestra a leitura de todos os arquivos baixados e a escrita no arquivo final.
     * Utiliza buffers para garantir performance mesmo com grandes volumes de dados.
     *  @param arquivosParaProcessar Lista com o caminho absoluto dos arquivos CSV.
     */

    public void processarArquivos(List<String> arquivosParaProcessar) {
        System.out.println("\n--- Iniciando Processamento e Consolidação ---");

        // Configura o CSVPrinter com cabeçalho personalizado e delimitador ';' (padrão Excel Brasil)
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(ARQUIVO_SAIDA));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("RazaoSocial", "CNPJ", "Trimestre", "Ano", "ValorDespesas") // Cabeçalho do arquivo final
                     .withDelimiter(';'))) { // Ponto e vírgula é melhor para Excel BR

            for (String caminhoArquivo : arquivosParaProcessar) {
                processarUmArquivo(caminhoArquivo, csvPrinter);
            }
            
            System.out.println("  Consolidação finalizada! Arquivo criado: " + ARQUIVO_SAIDA);

        } catch (IOException e) {
            System.err.println("Erro ao processar CSVs: " + e.getMessage());
        }
    }

    /**
     * Processa um arquivo CSV individualmente.
     * Realiza o parsing, extrai metadados do nome do arquivo e aplica filtros.
     */
    private void processarUmArquivo(String caminhoArquivo, CSVPrinter csvPrinter) {
        System.out.println("Processando: " + caminhoArquivo);
        
        // Extrai Ano e Trimestre do nome do arquivo (ex: .../3T2025.csv)
        // Assume formato estrito "XTYYYY.csv" onde X=Trimestre e Y=Ano
        String nomeArquivo = new File(caminhoArquivo).getName(); // 3T2025.csv
        String ano = nomeArquivo.substring(2, 6);      // ex: 2025
        String trimestre = nomeArquivo.substring(0, 2); // ex: 3T

        // Configuração para ler o CSV (Trata encoding Windows/Latin1 comum no Brasil)
        try (InputStream is = new FileInputStream(caminhoArquivo);
             // BOMInputStream remove caracteres invisíveis que atrapalham a leitura da 1ª coluna
             Reader reader = new InputStreamReader(new BOMInputStream(is), StandardCharsets.ISO_8859_1); 
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(';') // O separador oficial da ANS costuma ser ponto e vírgula
                     .withFirstRecordAsHeader()  // Pula a primeira linha (cabeçalho original)
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                // 1. Tenta identificar as colunas (seus nomes mudam as vezes nos arquivos oficiais)
                // Usei um método 'safeGet' para evitar erro se a coluna não existir
                String descricao = safeGet(record, "DESCRICAO");
                String valorStr = safeGet(record, "VL_SALDO_FINAL");
                String regAns = safeGet(record, "REG_ANS"); 
                // Nota: O arquivo 3T2025.csv parece não ter coluna "RazaoSocial" nem "CNPJ". 
                // Isso é uma INCONSISTÊNCIA dos dados brutos que tratei aqui.
                
                // 2. Filtra: Só queremos linhas de Despesa/Sinistro
                if (ehDespesa(descricao)) {
                    // Escreve no arquivo consolidado
                    csvPrinter.printRecord(
                        "Operadora " + regAns, // Razão Social (Fictícia/Placeholder pois não tem no arquivo original)
                        regAns,                // CNPJ (Usando REG_ANS como ID provisório devido à falta da coluna CNPJ)
                        trimestre,
                        ano,
                        valorStr
                    );
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo " + caminhoArquivo + ": " + e.getMessage());
        }
    }

    /**
     * Verifica se a descrição contém os termos chave (filtros).
     */
    private boolean ehDespesa(String descricao) {
        if (descricao == null) return false;
        String descUpper = descricao.toUpperCase();
        for (String termo : TERMOS_DESPESA) {
            if (descUpper.contains(termo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método auxiliar para evitar 'IllegalArgumentException' se a coluna não existir no CSV.
     * Arquivos governamentais frequentemente mudam o esquema de colunas entre períodos.
     */
    private String safeGet(CSVRecord record, String colName) {
        if (record.isMapped(colName)) {
            return record.get(colName);
        }
        return "";
    }
}