package com.intuitivecare.questao1;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Gerencia o download e a extração dos arquivos da ANS.
 * Utiliza a biblioteca Apache Commons IO para facilitar o download com timeouts de segurança.
 */
public class FileDownloader {

    // Define uma pasta "downloads" dentro do projeto para salvar tudo
    private static final String DOWNLOAD_DIR = "downloads";
    
    /**
     * Baixa a lista de arquivos ZIP e os extrai imediatamente.
     * Define timeouts de conexão para evitar que o programa trave se o site da ANS estiver lento.
     * @param links Lista de URLs diretas para os arquivos .zip
     */
    public void baixarEExtrair(List<String> links) {
        try {
            // Cria a pasta de downloads se ela não existir
            Files.createDirectories(Paths.get(DOWNLOAD_DIR));

            for (String link : links) {
                // Pega o nome do arquivo (ex: 3T2025.zip) a partir do link
                String nomeArquivo = link.substring(link.lastIndexOf("/") + 1);
                File destino = new File(DOWNLOAD_DIR, nomeArquivo);

                System.out.println("  Baixando: " + nomeArquivo + "...");
                
                // A mágica do Commons-IO: baixa o arquivo com uma linha de código!
                // (Link, Arquivo Destino, Timeout Conexão, Timeout Leitura)
                // Usei 10000ms (10s) para garantir que não fique esperando eternamente.
                FileUtils.copyURLToFile(new URL(link), destino, 10000, 10000);
                System.out.println("  Download concluído!");

                // Chama o método para extrair o zip baixado
                descompactar(destino);
            }
        } catch (IOException e) {
            System.err.println("  Erro no download: " + e.getMessage());
        }
    }

    /**
     * Descompacta um arquivo ZIP em uma subpasta própria.
     * Utiliza buffers para extração eficiente de memória.
     */

    private void descompactar(File arquivoZip) {
        System.out.println("  Extraindo " + arquivoZip.getName() + "...");
        // Buffer de 1KB para leitura otimizada
        byte[] buffer = new byte[1024];
        
        // Cria uma subpasta com o nome do arquivo (ex: downloads/3T2025)
        // Isso é importante para organizar os CSVs por período
        String nomePasta = arquivoZip.getName().replace(".zip", "");
        File pastaDestino = new File(DOWNLOAD_DIR, nomePasta);
        pastaDestino.mkdirs();

        // Código padrão Java para descompactar ZIPs
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(arquivoZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File novoArquivo = new File(pastaDestino, zipEntry.getName());
                
                // Se o item dentro do zip for uma pasta, cria o diretório
                if (zipEntry.isDirectory()) {
                    novoArquivo.mkdirs();
                } else {
                    // Garante que a pasta pai do arquivo existe
                    new File(novoArquivo.getParent()).mkdirs();
                    
                    // Escreve o arquivo no disco
                    try (FileOutputStream fos = new FileOutputStream(novoArquivo)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            System.out.println("  Extração concluída em: " + pastaDestino.getPath() + "\n");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}