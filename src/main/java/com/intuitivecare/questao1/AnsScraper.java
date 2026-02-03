package com.intuitivecare.questao1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Responsável por navegar no site da ANS e identificar os links de download
 * dos arquivos de Demonstrações Contábeis.
 * Implementa uma busca dinâmica que varre os diretórios de anos para encontrar
 * os arquivos mais recentes, independente da data atual.
 */

public class AnsScraper {

    private static final String BASE_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/demonstracoes_contabeis/";
    // User-Agent para simular um navegador real e evitar bloqueios
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * Busca os links dos arquivos .zip referentes aos últimos 3 trimestres disponíveis.
     * A lógica varre as pastas de anos (ex: 2025, 2024) e ordena os trimestres encontrados.
     *
     * @return Lista de Strings contendo as URLs completas para download.
     * @throws IOException em caso de erro de conexão com a ANS.
     */

    public List<String> getLinksUltimos3Trimestres() throws IOException {
        System.out.println("Iniciando busca pelos últimos 3 trimestres...");
        List<String> zipLinks = new ArrayList<>();

        // 1. Conecta no site e lista os diretórios de anos disponíveis
        Document docAnos = Jsoup.connect(BASE_URL)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get();

        List<String> anos = listarSubdiretorios(docAnos);
        anos.sort(Collections.reverseOrder()); // Ordena decrescente: 2025, 2024, 2023...

        // 2. Itera pelos anos para buscar os ZIPS dentro deles
        for (String ano : anos) {
            if (zipLinks.size() >= 3) break;  // Se já achou 3, para a busca

            String urlAno = BASE_URL + (ano.endsWith("/") ? ano : ano + "/");
            System.out.println("Entrando na pasta do ano: " + urlAno);

            try {
                Document docArquivos = Jsoup.connect(urlAno)
                        .userAgent(USER_AGENT)
                        .get();

                // Pega todos os links que terminam com .zip
                Elements links = docArquivos.select("a[href$=.zip]");
                
                // Lista temporária para ordenar os trimestres deste ano (4T antes de 1T)
                List<String> zipsDoAno = new ArrayList<>();

                for (Element link : links) {
                    String href = link.attr("href");
                    // Regex: garante que é um arquivo de trimestre (ex: 1T2023.zip ou 3T2025.zip)
                    if (href.matches(".*\\d{1,2}T\\d{4}.*zip")) {
                        zipsDoAno.add(urlAno + href);
                    }
                }

                // Ordena ZIPS do ano em ordem decrescente (4T antes de 1T)
                zipsDoAno.sort(Collections.reverseOrder());

                for (String zip : zipsDoAno) {
                    if (zipLinks.size() >= 3) break;
                    
                    zipLinks.add(zip);
                    System.out.println("  Adicionado: " + zip);
                }

            } catch (IOException e) {
                System.err.println("Erro ao ler ano " + ano + ": " + e.getMessage());
            }
        }

        return zipLinks;
    }

    /**
     * Método auxiliar para extrair apenas pastas que são Anos (4 dígitos) da listagem HTML.
     */
    private List<String> listarSubdiretorios(Document doc) {
        return doc.select("a[href]").stream()
                .map(link -> link.attr("href"))
                .filter(href -> href.matches("\\d{4}/?")) // Aceita apenas "2024/" ou "2023" etc.
                .filter(href -> !href.equals("../"))  // Ignora link de diretório pai
                .collect(Collectors.toList());
    }
}