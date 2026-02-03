package com.intuitivecare.questao2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsavel por carregar os Dados Cadastrais das Operadoras (Relatorio_Cadop.csv).
 * Serve como tabela de dominio para o enriquecimento de dados.
 */
public class AnsCadastro {
    
    // Mapa estatico: Chave = RegistroANS (String), Valor = Array de Strings (Linha completa do CSV)
    private static Map<String, String[]> mapaOperadoras = new HashMap<>();

    public static void carregarCadastro(String nomeArquivo) {
        System.out.println("  Lendo arquivo de cadastro: '" + nomeArquivo + "'...");
        
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(nomeArquivo), StandardCharsets.ISO_8859_1)
            );

            String linha;
            int cont = 0;

            while ((linha = reader.readLine()) != null) {
                cont++;
                // Pula as primeiras 3 linhas que geralmente são metadados/cabeçalho da ANS
                if (cont <= 3) continue; 

                // O arquivo da ANS geralmente usa ponto e vírgula
                String[] colunas = linha.split(";");

                if (colunas.length > 0) {
                    // Coluna 0 geralmente é o RegistroANS
                    String regAns = colunas[0].replace("\"", "").trim();
                    
                    if (!regAns.isEmpty()) {
                        mapaOperadoras.put(regAns, colunas);
                    }
                }
            }
            reader.close();
            System.out.println("   Sucesso! Cadastro carregado na memória RAM. Total de Operadoras: " + mapaOperadoras.size());

        } catch (Exception e) {
            System.err.println("  Erro ao ler cadastro: " + e.getMessage());
        }
    }

    /**
     * Busca os dados de uma operadora pelo Registro ANS.
     * @param registroAns O código de registro.
     * @return Array de Strings com os dados ou null se não encontrar.
     */
    public static String[] buscarOperadora(String registroAns) {
        return mapaOperadoras.get(registroAns);
    }
}