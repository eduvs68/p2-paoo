"""Gera a amostra programatica do CSV oficial de focos do INPE.

Metodo: amostragem sistematica. Mantem o cabecalho e, a partir da linha de
dados `deslocamento`, copia 1 linha a cada `passo`. As linhas sao copiadas
exatamente como estao no original (sem limpeza); o tratamento dos dados
acontece na importacao pelo Service. Reprodutivel: mesma entrada e mesmos
parametros geram sempre a mesma saida.

Uso:
    python scripts/gerar_amostra.py <csv_oficial> [saida] [passo]
"""
import sys

def main():
    origem = sys.argv[1]
    destino = sys.argv[2] if len(sys.argv) > 2 else "data/focos_amostra_2025.csv"
    passo = int(sys.argv[3]) if len(sys.argv) > 3 else 35
    deslocamento = 0

    lidas = gravadas = 0
    with open(origem, encoding="utf-8", newline="") as fin, \
         open(destino, "w", encoding="utf-8", newline="") as fout:
        fout.write(fin.readline())  # cabecalho
        for i, linha in enumerate(fin):
            lidas += 1
            if i % passo == deslocamento:
                fout.write(linha)
                gravadas += 1
    print(f"linhas de dados lidas: {lidas}; gravadas na amostra: {gravadas} (1 a cada {passo})")

if __name__ == "__main__":
    main()
