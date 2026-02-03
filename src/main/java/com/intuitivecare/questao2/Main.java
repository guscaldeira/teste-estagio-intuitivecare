package com.intuitivecare.questao2;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * CLASSE PRINCIPAL - QUESTÃO 2
 * Executa o pipeline de transformação de dados (ETL).
 * * Requisitos Atendidos:
 * 2.1 - Validação (CNPJ, Numéricos, Razão Social)
 * 2.2 - Enriquecimento (Join com Cadastro ANS: Modalidade, UF)
 * 2.3 - Agregação e Estatísticas (Soma, Média, Desvio Padrão) + ZIP
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Iniciando Questao 2: Versao Final 100% ===");

        // 1. Carrega Tabela de Dominio (Cadastro ANS) para memoria
        // Isso atende o requisito de "Join" (Enriquecimento)
        AnsCadastro.carregarCadastro("Relatorio_Cadop.csv");

        String arquivoEntrada = "consolidado.csv";
        String arquivoSaidaCSV = "despesas_agregadas.csv";
        String arquivoSaidaZIP = "Teste_Gustavo_Caldeira.zip";

        // Map para agrupar: Chave = "RazaoSocial;Modalidade;UF", Valor = Lista de despesas
        Map<String, List<Double>> agrupamento = new HashMap<>();

        int totalLinhas = 0;
        int registrosValidos = 0;
        int descartadosCnpjInvalido = 0;

        try {
            System.out.println("Processando linhas do CSV consolidado...");
            
            // Lê todas as linhas do CSV gerado na Questão 1
            List<String> linhas = Files.readAllLines(Paths.get(arquivoEntrada), StandardCharsets.ISO_8859_1);

            for (String linha : linhas) {
                totalLinhas++;
                String[] colunas = linha.split(";");
                
                // Validação básica de estrutura
                if (colunas.length < 3) continue;

                // Coluna 1 do consolidado é o RegistroANS (chave de busca)
                String regAns = colunas[1].replace("\"", "").trim();
                
                // --- REQUISITO 2.1: Validação de Numéricos Positivos ---
                String valorStr = "";
                if (colunas.length > 3) {
                     valorStr = colunas[3].replace("\"", "").replace(",", ".").trim();
                } else {
                    continue;
                }

                double valor = 0.0;
                try {
                    valor = Double.parseDouble(valorStr);
                    if (valor <= 0) continue; // Descarta valores negativos ou zero
                } catch (NumberFormatException e) {
                    continue; // Descarta se não for número
                }

                // --- REQUISITO 2.2: Enriquecimento (Join) ---
                // Busca os dados completos no cadastro da ANS usando o RegistroANS
                String[] dadosDaOperadora = AnsCadastro.buscarOperadora(regAns);

                if (dadosDaOperadora == null) {
                    // Decisão de Projeto (Trade-off): Se não está no cadastro ativo, ignoramos
                    // pois precisamos da UF e Modalidade oficiais.
                    continue; 
                }

                // Extraindo dados do Cadastro (Indices baseados no layout padrão CSV da ANS)
                String cnpj = dadosDaOperadora[1].replace("\"", "").trim();
                String razaoSocial = dadosDaOperadora[2].replace("\"", "").trim();
                String modalidade = "Desconhecida";
                if (dadosDaOperadora.length > 4) modalidade = dadosDaOperadora[4].replace("\"", "").trim();
                
                String uf = "ND"; 
                if (dadosDaOperadora.length > 10) uf = dadosDaOperadora[10].replace("\"", "").trim();

                // --- REQUISITO 2.1: Validação de CNPJ (Matemática) ---
                if (!CnpjValidator.isCNPJ(cnpj)) {
                    descartadosCnpjInvalido++;
                    continue; // Descarta operador se o CNPJ for matematicamente inválido
                }

                registrosValidos++;

                // Chave composta para Agrupamento (Requisito 2.3 + 2.2)
                String chave = razaoSocial + ";" + modalidade + ";" + uf;

                agrupamento.putIfAbsent(chave, new ArrayList<>());
                agrupamento.get(chave).add(valor);
            }

            // --- CÁLCULOS ESTATÍSTICOS (Soma, Média, Desvio Padrão) ---
            System.out.println("Calculando estatisticas para " + agrupamento.size() + " operadoras...");
            
            List<String> linhasParaSalvar = new ArrayList<>();
            Map<String, Double> mapOrdenacao = new HashMap<>(); // Auxiliar para ordenar

            for (Map.Entry<String, List<Double>> entry : agrupamento.entrySet()) {
                String chaveCompleta = entry.getKey(); 
                List<Double> valores = entry.getValue();

                double soma = 0;
                for (double v : valores) soma += v;
                
                double media = soma / valores.size();

                // Cálculo do Desvio Padrão
                double somaDiferencas = 0;
                for (double v : valores) somaDiferencas += Math.pow(v - media, 2);
                double desvioPadrao = Math.sqrt(somaDiferencas / valores.size());

                // Formata linha final: Razao;Modalidade;UF;Total;Media;Desvio
                String linhaFinal = String.format(Locale.US, "%s;%.2f;%.2f;%.2f", chaveCompleta, soma, media, desvioPadrao);
                
                linhasParaSalvar.add(linhaFinal);
                mapOrdenacao.put(linhaFinal, soma);
            }

            // --- REQUISITO 2.3: Ordenação (Maior Despesa -> Menor) ---
            linhasParaSalvar.sort((a, b) -> Double.compare(mapOrdenacao.get(b), mapOrdenacao.get(a)));

            // --- GRAVAR CSV ---
            FileWriter fw = new FileWriter(arquivoSaidaCSV);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("Razao_Social;Modalidade;UF;Total_Despesas;Media_Despesas;Desvio_Padrao");
            
            for (String l : linhasParaSalvar) {
                pw.println(l);
            }
            pw.close();
            System.out.println("  Arquivo CSV gerado: " + arquivoSaidaCSV);

            // --- REQUISITO FINAL: Compactar em ZIP ---
            compactarParaZip(arquivoSaidaCSV, arquivoSaidaZIP);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para criar o ZIP
    private static void compactarParaZip(String sourceFile, String zipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
        System.out.println("  ZIP FINAL PRONTO: " + zipFile);
    }
}