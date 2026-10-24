import sys

origem = sys.argv[1]
destino = sys.argv[2] if len(sys.argv) > 2 else "data/focos_amostra_2025.csv"
passo = int(sys.argv[3]) if len(sys.argv) > 3 else 35

lidas = 0
gravadas = 0
with open(origem, encoding="utf-8", newline="") as entrada, \
     open(destino, "w", encoding="utf-8", newline="") as saida:
    saida.write(entrada.readline())
    for i, linha in enumerate(entrada):
        lidas += 1
        if i % passo == 0:
            saida.write(linha)
            gravadas += 1
print(f"lidas: {lidas}, gravadas: {gravadas}")
