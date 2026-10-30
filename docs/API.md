# API REST — SIMAQ INPE

Base: `http://localhost:8080/api/focos`

Todas as respostas são JSON, exceto a importação, que recebe `multipart/form-data`. Datas e horas seguem o padrão ISO-8601 (`2025-01-01T00:35:00`). Os exemplos de resposta abaixo foram escritos a partir do código; os valores são ilustrativos.

## Endpoints

| Método | Rota | Descrição | Sucesso | Erros |
|--------|------|-----------|---------|-------|
| GET | `/api/focos` | Lista paginada, sem filtros | 200 | — |
| GET | `/api/focos/filtro` | Lista paginada com filtros | 200 | 400 |
| GET | `/api/focos/estatisticas/mensal` | Total de focos por ano e mês | 200 | 400 |
| GET | `/api/focos/opcoes` | Biomas e satélites existentes | 200 | — |
| GET | `/api/focos/{id}` | Busca um foco pelo ID | 200 | 400, 404 |
| POST | `/api/focos` | Cadastra um foco | 201 | 400 |
| PUT | `/api/focos/{id}` | Atualiza um foco | 200 | 400, 404 |
| DELETE | `/api/focos/{id}` | Exclui um foco | 204 | 400, 404 |
| POST | `/api/focos/importar` | Importa um CSV do INPE | 200 | 400 |

## Modelo `FocoFogo`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `id` | número | não | Gerado pelo banco. É ignorado se enviado no corpo |
| `dataHora` | data e hora | sim | Formato `yyyy-MM-ddTHH:mm:ss` |
| `satelite` | texto | sim | Até 30 caracteres. Gravado em maiúsculas |
| `uf` | texto | sim | Sigla de 2 letras de uma UF válida. Gravada em maiúsculas |
| `municipio` | texto | sim | Até 100 caracteres. Gravado em maiúsculas |
| `bioma` | texto | sim | Até 30 caracteres |
| `latitude` | decimal | sim | Entre -90 e 90, até 6 casas decimais |
| `longitude` | decimal | sim | Entre -180 e 180, até 6 casas decimais |
| `riscoFogo` | decimal | não | Entre 0 e 1, até 2 casas decimais |

Não podem existir dois focos com a mesma combinação de `latitude`, `longitude`, `dataHora` e `satelite`.

```json
{
  "id": 1000,
  "dataHora": "2025-01-01T00:35:00",
  "satelite": "METOP-B",
  "uf": "MS",
  "municipio": "AQUIDAUANA",
  "bioma": "Pantanal",
  "latitude": -19.733400,
  "longitude": -55.399899,
  "riscoFogo": 0.74
}
```

## Formato de erro

Todo erro tratado pela API (400 e 404) devolve o mesmo corpo:

```json
{ "mensagem": "A UF deve ter 2 letras" }
```

Quando o corpo da requisição tem mais de uma violação de validação, as mensagens vêm separadas por `; `.

## Paginação

Os endpoints de listagem aceitam os parâmetros do Spring Data:

| Parâmetro | Padrão | Descrição |
|-----------|--------|-----------|
| `page` | `0` | Número da página, começando em 0 |
| `size` | `20` | Registros por página |
| `sort` | `id` | Campo e sentido, por exemplo `sort=dataHora,desc` |

A resposta é um objeto `Page` do Spring Data. Os campos principais são `content` (os registros), `totalElements`, `totalPages`, `number` e `size`. O exemplo omite os demais campos de paginação.

```json
{
  "content": [ { "id": 1, "dataHora": "2025-01-01T00:35:00", "satelite": "METOP-B", "uf": "MS",
                 "municipio": "AQUIDAUANA", "bioma": "Pantanal",
                 "latitude": -19.733400, "longitude": -55.399899, "riscoFogo": 0.74 } ],
  "totalElements": 99040,
  "totalPages": 4952,
  "number": 0,
  "size": 20
}
```

---

## GET `/api/focos`

Lista todos os focos, com paginação.

```bash
curl "http://localhost:8080/api/focos?page=0&size=10&sort=dataHora,desc"
```

**200** — `Page` de focos.

## GET `/api/focos/filtro`

Mesma resposta de `/api/focos`, aplicando os filtros abaixo. Todos são opcionais e se combinam com E. Também aceita `page`, `size` e `sort`.

| Parâmetro | Exemplo | Regra |
|-----------|---------|-------|
| `uf` | `MT` | Sigla da UF. Aceita minúsculas |
| `bioma` | `Amazônia` | Comparação sem diferenciar maiúsculas |
| `satelite` | `AQUA_M-T` | Aceita minúsculas |
| `dataInicio` | `2025-08-01` | Formato `yyyy-MM-dd`. Inclui o dia informado |
| `dataFim` | `2025-08-31` | Formato `yyyy-MM-dd`. Inclui o dia informado |

```bash
curl "http://localhost:8080/api/focos/filtro?uf=MT&bioma=Amazônia&dataInicio=2025-08-01&dataFim=2025-08-31"
```

**200** — `Page` de focos.

**400** — exemplos de `mensagem`:
- `UF inválida: XX`
- `A data inicial não pode ser posterior à data final`
- `Valor inválido para o parâmetro 'dataInicio'`

## GET `/api/focos/estatisticas/mensal`

Soma os focos por ano e mês. Aceita os mesmos filtros de `/filtro` (sem paginação). O resultado vem ordenado por ano e mês, e meses sem focos não aparecem.

```bash
curl "http://localhost:8080/api/focos/estatisticas/mensal?uf=MT"
```

**200**

```json
[
  { "ano": 2025, "mes": 1, "total": 1520 },
  { "ano": 2025, "mes": 2, "total": 980 }
]
```

**400** — os mesmos erros de `/filtro`.

## GET `/api/focos/opcoes`

Devolve os valores distintos presentes no banco, usados para preencher os filtros da tela.

```bash
curl "http://localhost:8080/api/focos/opcoes"
```

**200**

```json
{
  "biomas": ["Amazônia", "Caatinga", "Cerrado", "Mata Atlântica", "Pampa", "Pantanal"],
  "satelites": ["AQUA_M-T", "METOP-B", "NOAA-20"]
}
```

## GET `/api/focos/{id}`

```bash
curl "http://localhost:8080/api/focos/1000"
```

**200** — o foco.

**404**

```json
{ "mensagem": "Foco de fogo não encontrado: id 9999999" }
```

**400** — ID que não é número: `Valor inválido para o parâmetro 'id'`.

## POST `/api/focos`

Cadastra um foco. O corpo é um `FocoFogo` sem `id`.

```bash
curl -X POST "http://localhost:8080/api/focos" \
  -H "Content-Type: application/json" \
  -d '{
    "dataHora": "2025-01-01T00:35:00",
    "satelite": "metop-b",
    "uf": "ms",
    "municipio": "Aquidauana",
    "bioma": "Pantanal",
    "latitude": -19.7334,
    "longitude": -55.399899,
    "riscoFogo": 0.74
  }'
```

**201** — o foco criado, já com `id` e com `satelite`, `uf` e `municipio` em maiúsculas.

**400** — exemplos de `mensagem`:
- `O município é obrigatório` (e as demais mensagens de campo obrigatório)
- `A latitude deve estar entre -90 e 90`
- `UF inválida: XX`
- `Já existe um foco com a mesma data, satélite e coordenadas`
- `JSON inválido ou com valores em formato incorreto`

## PUT `/api/focos/{id}`

Substitui os dados do foco. O corpo segue as mesmas regras do `POST`, e todos os campos obrigatórios devem ser enviados.

```bash
curl -X PUT "http://localhost:8080/api/focos/1000" \
  -H "Content-Type: application/json" \
  -d '{ "dataHora": "2025-01-01T00:35:00", "satelite": "METOP-B", "uf": "MS",
        "municipio": "AQUIDAUANA", "bioma": "Pantanal",
        "latitude": -19.7334, "longitude": -55.399899, "riscoFogo": 0.80 }'
```

**200** — o foco atualizado.

**400** — os mesmos erros do `POST`. A verificação de duplicidade ignora o próprio registro.

**404** — `Foco de fogo não encontrado: id 1000`.

## DELETE `/api/focos/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/focos/1000"
```

**204** — sem corpo.

**404** — `Foco de fogo não encontrado: id 1000`.

## POST `/api/focos/importar`

Importa um CSV do Programa Queimadas do INPE. **A operação apaga todos os registros existentes** e os substitui pelo conteúdo do arquivo. Se o arquivo for rejeitado antes da leitura dos registros (extensão, cabeçalho ou arquivo vazio), nada é apagado.

O corpo é `multipart/form-data` com o campo `arquivo`. O tamanho máximo é 500 MB.

```bash
curl -X POST "http://localhost:8080/api/focos/importar" \
  -F "arquivo=@data/focos_amostra_2025.csv"
```

### Formato do arquivo

- Extensão `.csv`, codificação UTF-8 e separador vírgula. Um BOM no início é aceito.
- Colunas obrigatórias: `latitude` (ou `lat`), `longitude` (ou `lon`), `data_pas`, `estado`, `municipio` e `bioma`.
- Colunas opcionais: `satelite` (se ausente, usa `AQUA_M-T`, o satélite de referência dos arquivos antigos), `risco_fogo` (valores fora de 0 a 1 ou ilegíveis viram vazio) e `pais` (não é gravada, mas uma linha com outro país que não `Brasil` é rejeitada).
- `data_pas` no formato `yyyy-MM-dd HH:mm:ss`. `estado` com o nome por extenso, como `MATO GROSSO DO SUL`, convertido para a sigla.
- As demais colunas são ignoradas.

### Respostas

**200**

```json
{
  "linhasLidas": 99040,
  "importados": 99037,
  "rejeitados": 3,
  "duplicados": 0,
  "motivosRejeicao": { "Estado vazio ou desconhecido": 3 },
  "mensagem": "Importação concluída. Linhas lidas: 99040 | importadas: 99037 | rejeitadas: 3 | duplicadas: 0"
}
```

`rejeitados` conta as linhas inválidas, agrupadas por motivo em `motivosRejeicao`. `duplicados` conta as linhas válidas que repetem a combinação de latitude, longitude, data e satélite.

**400** — exemplos de `mensagem`:
- `O arquivo está vazio`
- `O arquivo deve ter a extensão .csv`
- `O arquivo não possui registros além do cabeçalho`
- `Estrutura do arquivo inválida: colunas ausentes [...]. Verifique o cabeçalho e se o separador é a vírgula`
- `Nenhum arquivo foi enviado`
- `A requisição deve ser multipart/form-data com o campo 'arquivo'`
- `O arquivo excede o tamanho máximo de 500 MB`
