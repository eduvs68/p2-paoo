# SIMAQ INPE

Sistema de Monitoramento e Análise de Queimadas. A aplicação importa os focos de fogo publicados pelo Programa Queimadas do INPE, guarda os registros em um banco PostgreSQL e permite consultá-los, filtrá-los e mantê-los por uma interface web. Um gráfico mostra em que época do ano os focos se concentram.

## Integrantes

Grupo: 11	SIMAQ INPE

- Eduardo Vieira Santos
- Fernando Jun Iogi
- Guilherme Mendes de Carvalho Vieira

## Funcionalidades

- Importação de arquivos CSV do INPE, com resumo de linhas lidas, importadas, rejeitadas e duplicadas.
- Cadastro, consulta, edição e exclusão de focos (CRUD), com validação dos campos.
- Busca por ID.
- Listagem paginada com filtros por UF, bioma, satélite e período.
- Gráfico de barras com o total de focos por mês, que respeita os filtros aplicados.

## Tecnologias

- Java 21 e Spring Boot 4.1 (Web MVC, Data JPA, Validation)
- Lombok
- PostgreSQL hospedado no Neon, com o driver `org.postgresql`
- Frontend em HTML, CSS e JavaScript puro, servido pelo próprio Spring Boot
- [Chart.js](https://www.chartjs.org) 4.4.7 (arquivo local em `static/js`)
- Maven

## Estrutura do projeto

```
src/main/java/br/fatec/simaqinpe/
  controller/   endpoints REST e tratamento de erros
  service/      regras de negócio e importação do CSV
  repository/   acesso ao banco (JPA, filtros e importação em lote)
  model/        entidade FocoFogo
src/main/resources/
  application.properties
  static/       frontend (index.html, css/, js/)
data/           amostra de 2025 (focos_amostra_2025.csv)
scripts/        utilitários em Python
docs/API.md     documentação dos endpoints
```

## Requisitos

- JDK 21
- Maven 3.9 ou superior (o projeto não tem o Maven Wrapper; o Maven que acompanha o IntelliJ também serve)
- Um banco PostgreSQL acessível. O projeto foi feito para o Neon, mas qualquer PostgreSQL com SSL funciona
- Python 3, apenas se for usar os scripts de `scripts/`

## Configuração

A aplicação lê a conexão com o banco de variáveis de ambiente ou de um arquivo `.env` na **raiz do projeto**. Rode sempre a partir dessa pasta.

1. Copie o modelo e preencha os três valores:

   ```bash
   cp .env.example .env
   ```

   | Variável | Descrição | Exemplo |
   |----------|-----------|---------|
   | `DB_URL` | URL JDBC do banco | `jdbc:postgresql://<host>:5432/<banco>?sslmode=require` |
   | `DB_USERNAME` | Usuário do banco | |
   | `DB_PASSWORD` | Senha do banco | |

2. Alternativa para quem usa o Neon: salve a connection string não pooled como `DATABASE_URL_UNPOOLED` em `.env.local` e rode o script, que monta o `.env` já no formato JDBC:

   ```bash
   python scripts/neon_para_env.py
   ```

O `.env` e o `.env.local` estão no `.gitignore` e **não devem ser versionados**.

Na primeira execução, o Hibernate cria a tabela `foco_fogo` automaticamente (`spring.jpa.hibernate.ddl-auto=update`), com restrição de unicidade sobre latitude, longitude, data e hora e satélite.

## Executando

```bash
mvn spring-boot:run
```

Abra [http://localhost:8080](http://localhost:8080). A interface web e a API são servidas na mesma porta.

Para gerar um JAR executável:

```bash
mvn clean package
java -jar target/simaqinpe-0.0.1-SNAPSHOT.jar
```

## Dados

### Conjunto de dados escolhido

| Item | Informação |
|------|------------|
| Órgão | INPE, Programa Queimadas |
| Conjunto | Focos de fogo ativo detectados por satélite, Brasil, todos os satélites, ano de 2025 |
| Diretório | <https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/anual/Brasil_todos_sats/> |
| Arquivo | [`focos_br_todos-sats_2025.zip`](https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/anual/Brasil_todos_sats/focos_br_todos-sats_2025.zip), com 64.935.356 bytes (cerca de 65 MB) |
| Formato | CSV com cabeçalho, separador vírgula, UTF-8, 13 colunas |
| Volume | Cerca de 3,47 milhões de linhas (estimado pela amostra: 99.040 linhas × passo 35) |
| Data de acesso | 29/10/2026. Última modificação do arquivo no servidor: 11/02/2026 |
| Dicionário de dados | FAQ do Programa Queimadas, pergunta 43: <https://terrabrasilis.dpi.inpe.br/queimadas/portal/pages/secao_informacoes/faq/>. O cálculo do risco de fogo está em [RiscoFogo_Sucinto.pdf](https://dataserver-coids.inpe.br/queimadas/queimadas/Publicacoes-Impacto/documentos/RiscoFogo_Sucinto.pdf) |
| Uso e licença | Não há licença explícita no diretório nem no FAQ. A pergunta 36 do FAQ pede que o uso dos dados seja citado conforme a ABNT NBR 6023 (veja [Fonte dos dados](#fonte-dos-dados)) |

Entre os diretórios `anual/` do INPE, a escolha de `Brasil_todos_sats` mantém a detecção de todos os satélites, o que permite filtrar e comparar por satélite. O diretório `Brasil_sat_ref` traz apenas o satélite de referência.

### Como obter os dados

1. Baixe o ZIP do ano desejado, por exemplo `focos_br_todos-sats_2025.zip`.
2. Extraia o CSV. Guarde-o em `data/` se quiser; arquivos com o padrão `data/focos_br_todos-sats_*.csv` são ignorados pelo Git por serem muito grandes.
3. Envie o arquivo pela seção **Importar arquivo do INPE** da interface.

O envio aceita arquivos de até 500 MB. Os arquivos de anos anteriores, que não têm a coluna `satelite`, também são aceitos: nesse caso o satélite é registrado como `AQUA_M-T`. O formato esperado de cada coluna está descrito em [docs/API.md](docs/API.md#post-apifocosimportar).

> **Atenção:** a importação **apaga todos os registros atuais** e os substitui pelo conteúdo do arquivo. A interface pede confirmação antes de enviar.

### Campos aproveitados e descartados

| Coluna do CSV | Atributo | Tratamento |
|---------------|----------|------------|
| `data_pas` | `dataHora` | Convertida de `yyyy-MM-dd HH:mm:ss` para `LocalDateTime`. O INPE informa o horário em GMT |
| `satelite` | `satelite` | Maiúsculas. Se a coluna não existir, usa `AQUA_M-T` |
| `estado` | `uf` | Nome por extenso convertido para a sigla |
| `municipio` | `municipio` | Maiúsculas, até 100 caracteres |
| `bioma` | `bioma` | Até 30 caracteres |
| `latitude` (ou `lat`) | `latitude` | `BigDecimal` entre -90 e 90 |
| `longitude` (ou `lon`) | `longitude` | `BigDecimal` entre -180 e 180 |
| `risco_fogo` | `riscoFogo` | `BigDecimal` entre 0 e 1. Valores fora desse intervalo, como o `-999` do INPE, ficam vazios |
| `pais` | (não gravado) | Validado: se estiver preenchido e não for `Brasil`, a linha é rejeitada |

Descartadas: `numero_dias_sem_chuva`, `precipitacao`, `id_area_industrial` e `frp`, que não são usadas pelas consultas e pelo gráfico do sistema.

### Política de importação

- **Erro estrutural** (arquivo vazio, extensão diferente de `.csv`, sem cabeçalho, colunas obrigatórias ausentes ou separador incorreto, nenhuma linha de dados): a importação é cancelada com HTTP 400 e uma mensagem, e nenhum registro é alterado.
- **Linha inválida** (data ilegível, país diferente de `Brasil`, estado desconhecido, campo obrigatório vazio, coordenada inválida ou fora do intervalo, texto acima do tamanho, número de colunas diferente do cabeçalho): a linha é rejeitada e contada por motivo, e a importação continua.
- **Campo opcional inválido**: um `risco_fogo` vazio ou fora de 0 a 1 não rejeita a linha; o valor é gravado vazio.
- **Duplicidade**: duas linhas com a mesma latitude, longitude, data e hora e satélite são o mesmo foco. Fica a primeira e as demais entram na contagem de `duplicados`.
- **Reimportação do mesmo arquivo**: a tabela é esvaziada (`TRUNCATE`, reiniciando os IDs) antes da carga. Importar o mesmo arquivo de novo gera o mesmo resultado, sem registros repetidos. Os cadastros feitos à mão também são apagados.
- **Falha durante a carga**: a importação roda em uma única transação (`@Transactional`), então os dados anteriores são mantidos se ela falhar.
- **Resposta**: total de linhas lidas, importadas, rejeitadas e duplicadas, mais os motivos das rejeições.

### Amostra para testes

`data/focos_amostra_2025.csv` tem cerca de 99 mil linhas, uma a cada 35 do arquivo anual de 2025. É a opção mais rápida para testar a importação. Para gerar uma amostra de outro arquivo:

```bash
python scripts/gerar_amostra.py <arquivo_de_origem.csv> [destino.csv] [passo]
```

O destino padrão é `data/focos_amostra_2025.csv` e o passo padrão é 35.

## API

A API REST fica em `/api/focos`. Resumo:

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/focos` | Lista paginada |
| GET | `/api/focos/filtro` | Lista paginada com filtros (`uf`, `bioma`, `satelite`, `dataInicio`, `dataFim`) |
| GET | `/api/focos/estatisticas/mensal` | Total de focos por ano e mês |
| GET | `/api/focos/opcoes` | Biomas e satélites existentes |
| GET | `/api/focos/{id}` | Busca por ID |
| POST | `/api/focos` | Cadastra um foco |
| PUT | `/api/focos/{id}` | Atualiza um foco |
| DELETE | `/api/focos/{id}` | Exclui um foco |
| POST | `/api/focos/importar` | Importa um CSV do INPE |

Exemplo:

```bash
curl "http://localhost:8080/api/focos/filtro?uf=MT&dataInicio=2025-08-01&dataFim=2025-08-31&size=5"
```

Os parâmetros, os exemplos de JSON e os códigos HTTP de cada endpoint estão em [docs/API.md](docs/API.md).

## Fonte dos dados

Programa Queimadas, INPE (Instituto Nacional de Pesquisas Espaciais).

Referência, no formato indicado pelo FAQ do programa:

> INPE. Focos de fogo ativo, Brasil, todos os satélites, 2025 (`focos_br_todos-sats_2025.zip`). Disponível em: <https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/anual/Brasil_todos_sats/>. Acesso em: 29 out. 2026.
