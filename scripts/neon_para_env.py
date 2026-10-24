from urllib.parse import urlsplit, unquote

valores = {}
for linha in open(".env.local", encoding="utf-8"):
    if "=" in linha:
        chave, _, valor = linha.strip().partition("=")
        valores[chave] = valor.strip('"')

u = urlsplit(valores["DATABASE_URL_UNPOOLED"])
url = f"jdbc:postgresql://{u.hostname}:{u.port or 5432}{u.path}?sslmode=require"
with open(".env", "w", encoding="utf-8") as f:
    f.write(f"DB_URL={url}\nDB_USERNAME={unquote(u.username)}\nDB_PASSWORD={unquote(u.password)}\n")
