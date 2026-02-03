package com.intuitivecare.questao1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Ponto de entrada da aplicação (Questão 1).
 * Orquestra todo o fluxo de ETL: Scraping -> Download -> Extração -> Processamento -> Compactação Final.
 */
public class Main {

    /**
     * Método principal que executa o pipeline de dados sequencialmente.
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        try {
            // 1. Busca Links (Scraping)
            // Identifica as URLs dos arquivos da ANS para os últimos 3 trimestres
            AnsScraper scraper = new AnsScraper();
            List<String> links = scraper.getLinksUltimos3Trimestres();

            // 2. Baixa e Extrai (Download)
            // Só inicia o download se encontrou links válidos
            if (!links.isEmpty()) {
                FileDownloader downloader = new FileDownloader();
                downloader.baixarEExtrair(links);
            }

            // 3. Identifica os arquivos CSV baixados (Busca Recursiva)
            System.out.println("\n--- Buscando arquivos CSV extraídos ---");
            List<String> csvFiles = listarArquivosCsv("downloads");

            if (csvFiles.isEmpty()) {
                System.out.println("  Nenhum arquivo CSV encontrado.");
                return;
            }

            // 4. Processa e Consolida (Transformação)
            // Lê os arquivos brutos e gera o 'consolidado.csv'
            CsvProcessor processor = new CsvProcessor();
            processor.processarArquivos(csvFiles);
            
            // 5. Compactar o arquivo final (Requisito do desafio)
            // Gera o ZIP final para entrega
            compactarResultado();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Busca recursivamente por arquivos .csv dentro do diretório de downloads.
     * Utiliza Stream API para varrer subpastas, garantindo que acharemos o arquivo
     * independente da estrutura interna do ZIP da ANS.
     */
    private static List<String> listarArquivosCsv(String diretorioRaiz) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(diretorioRaiz))) {
            return walk.map(Path::toString)
                    .filter(f -> f.endsWith(".csv"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Compacta o arquivo CSV consolidado em um arquivo ZIP final.
     * Método auxiliar para atender ao formato de entrega solicitado.
     */
    private static void compactarResultado() {
        String arquivoOrigem = "consolidado.csv";
        String arquivoDestino = "consolidado_despesas.zip";

        System.out.println("\n  Compactando arquivo final...");
        
        try (FileOutputStream fos = new FileOutputStream(arquivoDestino);
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(arquivoOrigem)) {

                // Cria a entrada dentro do ZIP
            ZipEntry zipEntry = new ZipEntry(new File(arquivoOrigem).getName());
            zipOut.putNextEntry(zipEntry);

            // Copia os bytes do arquivo para dentro do ZIP
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            System.out.println("  Sucesso! Arquivo gerado: " + arquivoDestino);
            
        } catch (IOException e) {
            System.err.println("Erro ao compactar: " + e.getMessage());
        }
    }
}