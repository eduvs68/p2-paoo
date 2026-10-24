"""Converte o .env.local gerado pelo Neon CLI (DATABASE_URL_UNPOOLED, formato
postgresql://usuario:senha@host/banco?params) para o .env usado pelo Spring
(DB_URL em formato JDBC, DB_USERNAME e DB_PASSWORD). Nao imprime a senha."""
from urllib.parse import urlsplit, unquote

valores = {}
for linha in open(".env.local", encoding="utf-8"):
    if "=" in linha and not linha.startswith("#"):
        chave, _, valor = linha.strip().partition("=")
        valores[chave] = valor.strip('"')

u = urlsplit(valores["DATABASE_URL_UNPOOLED"])
jdbc = f"jdbc:postgresql://{u.hostname}:{u.port or 5432}{u.path}?sslmode=require"
with open(".env", "w", encoding="utf-8") as f:
    f.write(f"DB_URL={jdbc}\nDB_USERNAME={unquote(u.username)}\nDB_PASSWORD={unquote(u.password)}\n")
print("Gerado .env com DB_URL =", jdbc, "| usuario =", u.username, "| senha = <oculta>")
