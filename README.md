# Teste Técnico - Intuitive Care

Este repositório contém a solução para o teste técnico de desenvolvimento Java. O projeto foi estruturado para resolver as questões propostas de forma modular, limpa e escalável.

##  Índice
- [Questão 1: Web Scraping e ETL da ANS](#-questão-1-web-scraping-e-etl-da-ans)
- [Questão 2: Em breve](#)
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

### 👤 Autor
Desenvolvido como parte do processo seletivo da Intuitive Care.