const BASE_URL = '/api/focos';

async function requisitar(caminho, opcoes) {
    let resposta;
    try {
        resposta = await fetch(BASE_URL + caminho, opcoes);
    } catch (erro) {
        throw new Error('Não foi possível comunicar com o servidor');
    }

    let dados = null;
    const texto = await resposta.text();
    if (texto) {
        try {
            dados = JSON.parse(texto);
        } catch (erro) {
            dados = null;
        }
    }

    if (!resposta.ok) {
        const mensagem = dados && dados.mensagem
            ? dados.mensagem
            : 'Erro ' + resposta.status + ' ao processar a requisição';
        throw new Error(mensagem);
    }
    return dados;
}

function montarQuery(parametros) {
    const query = new URLSearchParams();
    Object.entries(parametros).forEach(([chave, valor]) => {
        if (valor !== '' && valor !== null && valor !== undefined) {
            query.set(chave, valor);
        }
    });
    const texto = query.toString();
    return texto ? '?' + texto : '';
}

function temFiltro(filtros) {
    return Object.values(filtros).some(valor => valor !== '');
}

const api = {
    consultar(filtros, pagina, tamanho) {
        if (temFiltro(filtros)) {
            return requisitar('/filtro' + montarQuery({ ...filtros, page: pagina, size: tamanho }));
        }
        return requisitar(montarQuery({ page: pagina, size: tamanho }));
    },

    buscarPorId(id) {
        return requisitar('/' + id);
    },

    criar(foco) {
        return requisitar('', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(foco)
        });
    },

    atualizar(id, foco) {
        return requisitar('/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(foco)
        });
    },

    excluir(id) {
        return requisitar('/' + id, { method: 'DELETE' });
    },

    importar(arquivo) {
        const formulario = new FormData();
        formulario.append('arquivo', arquivo);
        return requisitar('/importar', { method: 'POST', body: formulario });
    },

    estatisticasMensais(filtros) {
        return requisitar('/estatisticas/mensal' + montarQuery(filtros));
    },

    opcoes() {
        return requisitar('/opcoes');
    }
};
