# Teste Técnico - Intuitive Care

Este repositório contém a solução para o teste técnico de desenvolvimento Java. O projeto foi estruturado para resolver as questões propostas de forma modular, limpa e escalável.

##  Índice
- [Questão 1: Web Scraping e ETL da ANS](#-questão-1-web-scraping-e-etl-da-ans)
- [Questão 2: Transformação de Dados e Teste de Desempenho](#questão-2-transformação-de-dados-e-teste-de-desempenho)
- [Questão 3: Em breve](#)

---

##  Questão 1: Web Scraping e ETL da ANS

### Objetivo
Desenvolver uma solução que acesse o site da ANS, identifique os arquivos de "Demonstrações Contábeis" mais recentes, realize o download, extração e transformação dos dados (ETL).

### Funcionalidades Implementadas
1.  **Busca Dinâmica (Scraping):** O sistema não possui anos "chumbados" no código. Ele varre o diretório da ANS e identifica automaticamente os anos e trimestres mais recentes.
2.  **Download Resiliente:** Uso de timeouts e tratamento de erros para baixar os arquivos `.zip`.
3.  **Processamento Otimizado (Stream):** Leitura dos arquivos CSV linha a linha para evitar estouro de memória (OutOfMemory), tratando encoding `ISO-8859-1` e removendo caracteres BOM.
4.  **Consolidação:** Filtra apenas as despesas de "Eventos/Sinistros" e gera um arquivo único `consolidado.csv`.
5.  **Entrega:** Compacta o resultado final em `consolidado_despesas.zip`.

###  Tecnologias e Bibliotecas
* **Java 11+**
* **Jsoup:** Para navegação e parsing HTML.
* **Apache Commons IO:** Para gerenciamento de arquivos e streams.
* **Apache Commons CSV:** Para leitura e escrita robusta de arquivos CSV.

###  Como Executar
Execute a classe `Main.java` localizada no pacote `com.intuitivecare.questao1`.
O fluxo será:
1.  Console exibirá o progresso da busca e download.
2.  Uma pasta `downloads/` será criada na raiz.
3.  O arquivo final `consolidado_despesas.zip` será gerado na raiz do projeto.

---

## Questão 2: Transformação de Dados e Teste de Desempenho

###  Objetivo
Transformar os dados consolidados da etapa anterior, enriquecendo-os com informações cadastrais oficiais da ANS, realizando cálculos estatísticos complexos e gerando uma saída estruturada e compactada.

###  Funcionalidades Implementadas
1.  **Validação Cruzada:** Valida as operadoras não apenas matematicamente, mas cruzando contra a base oficial de operadoras ativas da ANS (`Relatorio_Cadop.csv`).
2.  **Enriquecimento de Dados (Join):** Adiciona `RegistroANS`, `Modalidade` e `UF` ao CSV original.
3.  **Cálculos Estatísticos:** Calcula Soma Total, Média Trimestral e Desvio Padrão das despesas.
4.  **Ordenação Eficiente:** Ordena os resultados do maior custo para o menor.
5.  **Compactação Automática:** Gera o arquivo final `.zip` conforme solicitado.

###  Decisões Técnicas e Trade-offs (Justificativas)

O desafio exigiu escolhas arquiteturais específicas. Abaixo, detalho as decisões tomadas conforme solicitado no enunciado:

> **1. Estratégia de Validação de CNPJs/Operadoras**
> * **Decisão:** Utilizar validação por **Lookup na Base Oficial** (Join) em vez de apenas validação algorítmica de CNPJ.
> * **Justificativa:** Um CNPJ pode ser matematicamente válido (dígitos verificadores corretos) mas pertencer a uma operadora falida ou suspensa. Ao cruzar os dados com o `Relatorio_Cadop.csv` (Operadoras Ativas), garantimos a integridade do negócio, descartando registros que não possuem correspondência ativa na ANS. Isso é mais robusto para o contexto regulatório.

> **2. Estratégia de Processamento e Join**
> * **Decisão:** Join em Memória via `HashMap`.
> * **Justificativa:** O arquivo de cadastro (`Relatorio_Cadop.csv`) é pequeno (1.200 linhas). Carregá-lo inteiramente em um `HashMap<String, String[]>` consome memória insignificante e permite acesso O(1) instantâneo.
> * Comparado a um banco de dados (que adicionaria latência de I/O) ou loops aninhados (que teria complexidade O(N*M)), o uso de HashMaps permitiu processar as 170.000 linhas do arquivo principal em milissegundos.

> **3. Estratégia de Ordenação**
> * **Decisão:** Ordenação em Memória (`Collections.sort`) pós-agregação.
> * **Justificativa:** A ordenação foi solicitada no resultado **agregado** (agrupado por Operadora). Mesmo processando milhões de linhas de despesas, o resultado final (número de operadoras únicas) é pequeno (< 2.000 registros). Ordenar uma lista desse tamanho em memória é computacionalmente barato e não justifica o uso de algoritmos de ordenação externa (External Merge Sort).

### 🛠 Como Executar
1.  Certifique-se de que o arquivo `consolidado.csv` (gerado na Questão 1) e o arquivo `Relatorio_Cadop.csv` (baixado da ANS) estejam na raiz do projeto.
2.  Execute a classe `Main.java` localizada no pacote `com.intuitivecare.questao2`.
3.  O sistema irá processar os dados e gerar:
    * `despesas_agregadas.csv`: Relatório detalhado.
    * `Teste_{Seu_Nome}.zip`: Arquivo final para entrega.

---

### 👤 Autor
Desenvolvido como parte do processo seletivo da Intuitive Care.