const UFS = ['AC', 'AL', 'AM', 'AP', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MG', 'MS', 'MT', 'PA',
    'PB', 'PE', 'PI', 'PR', 'RJ', 'RN', 'RO', 'RR', 'RS', 'SC', 'SE', 'SP', 'TO'];
const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
const FILTROS_VAZIOS = { uf: '', bioma: '', satelite: '', dataInicio: '', dataFim: '' };

const estado = {
    filtros: { ...FILTROS_VAZIOS },
    pagina: 0,
    tamanho: 20,
    totalPaginas: 0,
    focos: [],
    editandoId: null,
    grafico: null,
    temporizadorMensagem: null
};

const formatadorNumero = new Intl.NumberFormat('pt-BR');

function el(id) {
    return document.getElementById(id);
}

function formatarData(texto) {
    const [ano, mes, dia] = texto.split('-');
    return dia + '/' + mes + '/' + ano;
}

function formatarDataHora(texto) {
    const [data, hora] = texto.split('T');
    return formatarData(data) + ' ' + hora;
}

function mostrarMensagem(texto, tipo) {
    const caixa = el('mensagem');
    caixa.textContent = texto;
    caixa.className = 'mensagem ' + tipo;
    clearTimeout(estado.temporizadorMensagem);
    estado.temporizadorMensagem = setTimeout(() => caixa.classList.add('oculto'), tipo === 'erro' ? 10000 : 6000);
}

async function comBloqueio(botao, textoOcupado, tarefa) {
    if (botao.disabled) {
        return;
    }
    const textoOriginal = botao.textContent;
    botao.disabled = true;
    if (textoOcupado) {
        botao.textContent = textoOcupado;
    }
    try {
        await tarefa();
    } finally {
        botao.disabled = false;
        if (textoOcupado && botao.textContent === textoOcupado) {
            botao.textContent = textoOriginal;
        }
    }
}

function preencherSelect(select, primeiraOpcao, valores) {
    select.replaceChildren();
    select.append(new Option(primeiraOpcao, ''));
    valores.forEach(valor => select.append(new Option(valor, valor)));
}

function preencherLista(lista, valores) {
    lista.replaceChildren();
    valores.forEach(valor => {
        const opcao = document.createElement('option');
        opcao.value = valor;
        lista.append(opcao);
    });
}

async function carregarOpcoes() {
    try {
        const opcoes = await api.opcoes();
        const bioma = el('filtroBioma').value;
        const satelite = el('filtroSatelite').value;
        preencherSelect(el('filtroBioma'), 'Todos', opcoes.biomas);
        preencherSelect(el('filtroSatelite'), 'Todos', opcoes.satelites);
        el('filtroBioma').value = bioma;
        el('filtroSatelite').value = satelite;
        preencherLista(el('listaBiomas'), opcoes.biomas);
        preencherLista(el('listaSatelites'), opcoes.satelites);
    } catch (erro) {
        mostrarMensagem(erro.message, 'erro');
    }
}

function celula(texto) {
    const td = document.createElement('td');
    td.textContent = texto;
    return td;
}

function botaoAcao(texto, classe, aoClicar) {
    const botao = document.createElement('button');
    botao.type = 'button';
    botao.className = 'botao pequeno ' + classe;
    botao.textContent = texto;
    botao.addEventListener('click', () => aoClicar(botao));
    return botao;
}

function mensagemNaTabela(texto) {
    const linha = document.createElement('tr');
    const td = celula(texto);
    td.colSpan = 10;
    td.className = 'vazio';
    linha.append(td);
    el('corpoTabela').replaceChildren(linha);
}

function renderizarTabela(pagina) {
    const corpo = el('corpoTabela');
    corpo.replaceChildren();

    if (pagina.content.length === 0) {
        mensagemNaTabela('Nenhum registro encontrado');
    }

    pagina.content.forEach(foco => {
        const linha = document.createElement('tr');
        linha.append(
            celula(foco.id),
            celula(formatarDataHora(foco.dataHora)),
            celula(foco.satelite),
            celula(foco.uf),
            celula(foco.municipio),
            celula(foco.bioma),
            celula(foco.latitude),
            celula(foco.longitude),
            celula(foco.riscoFogo === null ? '—' : foco.riscoFogo)
        );

        const acoes = document.createElement('td');
        acoes.className = 'acoes';
        acoes.append(
            botaoAcao('Editar', 'secundario', () => iniciarEdicao(foco)),
            botaoAcao('Excluir', 'perigo', botao => excluirFoco(foco, botao))
        );
        linha.append(acoes);
        corpo.append(linha);
    });

    const total = pagina.totalElements;
    el('contador').textContent = formatadorNumero.format(total) + (total === 1 ? ' registro' : ' registros');
    const totalPaginas = Math.max(pagina.totalPages, 1);
    el('infoPagina').textContent = 'Página ' + (estado.pagina + 1) + ' de ' + formatadorNumero.format(totalPaginas);
    el('btnAnterior').disabled = estado.pagina === 0;
    el('btnProxima').disabled = estado.pagina + 1 >= totalPaginas;
}

async function carregarTabela() {
    mensagemNaTabela('Carregando...');
    try {
        const pagina = await api.consultar(estado.filtros, estado.pagina, estado.tamanho);
        if (pagina.content.length === 0 && estado.pagina > 0 && pagina.totalPages > 0) {
            estado.pagina = pagina.totalPages - 1;
            return carregarTabela();
        }
        estado.totalPaginas = pagina.totalPages;
        renderizarTabela(pagina);
    } catch (erro) {
        mensagemNaTabela('Não foi possível carregar os registros');
        el('contador').textContent = '—';
        mostrarMensagem(erro.message, 'erro');
    }
}

function mostrarEstadoGrafico(texto) {
    el('graficoMensagem').textContent = texto;
    el('graficoMensagem').classList.remove('oculto');
    el('graficoMensal').classList.add('oculto');
    if (estado.grafico) {
        estado.grafico.destroy();
        estado.grafico = null;
    }
}

function descreverFiltros() {
    const partes = [];
    const f = estado.filtros;
    if (f.uf) partes.push('UF ' + f.uf);
    if (f.bioma) partes.push('bioma ' + f.bioma);
    if (f.satelite) partes.push('satélite ' + f.satelite);
    if (f.dataInicio) partes.push('a partir de ' + formatarData(f.dataInicio));
    if (f.dataFim) partes.push('até ' + formatarData(f.dataFim));
    return partes.length ? partes.join(', ') : 'todos os registros';
}

function renderizarGrafico(dados) {
    const total = dados.reduce((soma, item) => soma + item.total, 0);
    el('graficoSubtitulo').textContent = formatadorNumero.format(total) + ' focos (' + descreverFiltros() + ')';

    if (dados.length === 0) {
        mostrarEstadoGrafico('Não há dados para exibir com os filtros atuais');
        return;
    }

    const anos = new Set(dados.map(item => item.ano));
    const rotulos = dados.map(item =>
        anos.size > 1 ? MESES[item.mes - 1] + '/' + item.ano : MESES[item.mes - 1]);
    const valores = dados.map(item => item.total);
    const maior = Math.max(...valores);
    const cores = valores.map(valor => valor === maior ? '#b3410f' : '#e8895f');

    el('graficoMensagem').classList.add('oculto');
    el('graficoMensal').classList.remove('oculto');
    if (estado.grafico) {
        estado.grafico.destroy();
    }
    estado.grafico = new Chart(el('graficoMensal'), {
        type: 'bar',
        data: {
            labels: rotulos,
            datasets: [{ label: 'Focos de fogo', data: valores, backgroundColor: cores }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: contexto => formatadorNumero.format(contexto.parsed.y) + ' focos'
                    }
                }
            },
            scales: {
                x: { title: { display: true, text: 'Mês de detecção' } },
                y: {
                    beginAtZero: true,
                    title: { display: true, text: 'Quantidade de focos' },
                    ticks: { callback: valor => formatadorNumero.format(valor) }
                }
            }
        }
    });
}

async function carregarGrafico() {
    el('graficoSubtitulo').textContent = 'Carregando gráfico...';
    try {
        renderizarGrafico(await api.estatisticasMensais(estado.filtros));
    } catch (erro) {
        el('graficoSubtitulo').textContent = '';
        mostrarEstadoGrafico('Não foi possível carregar o gráfico');
        mostrarMensagem(erro.message, 'erro');
    }
}

function recarregarDados() {
    return Promise.all([carregarTabela(), carregarGrafico()]);
}

function lerFiltros() {
    return {
        uf: el('filtroUf').value,
        bioma: el('filtroBioma').value,
        satelite: el('filtroSatelite').value,
        dataInicio: el('filtroInicio').value,
        dataFim: el('filtroFim').value
    };
}

async function aplicarFiltros(evento) {
    evento.preventDefault();
    const filtros = lerFiltros();
    if (filtros.dataInicio && filtros.dataFim && filtros.dataInicio > filtros.dataFim) {
        mostrarMensagem('A data inicial não pode ser posterior à data final', 'erro');
        return;
    }
    await comBloqueio(el('btnFiltrar'), 'Filtrando...', async () => {
        estado.filtros = filtros;
        estado.pagina = 0;
        await recarregarDados();
    });
}

async function limparFiltros() {
    el('formFiltros').reset();
    estado.filtros = { ...FILTROS_VAZIOS };
    estado.pagina = 0;
    await comBloqueio(el('btnLimparFiltros'), null, recarregarDados);
}

function limparFormulario() {
    el('formFoco').reset();
    estado.editandoId = null;
    el('tituloForm').textContent = 'Cadastrar foco';
    el('btnSalvar').textContent = 'Cadastrar';
    el('btnCancelarEdicao').classList.add('oculto');
}

function iniciarEdicao(foco) {
    estado.editandoId = foco.id;
    el('fDataHora').value = foco.dataHora;
    el('fSatelite').value = foco.satelite;
    el('fUf').value = foco.uf;
    el('fMunicipio').value = foco.municipio;
    el('fBioma').value = foco.bioma;
    el('fLatitude').value = foco.latitude;
    el('fLongitude').value = foco.longitude;
    el('fRisco').value = foco.riscoFogo === null ? '' : foco.riscoFogo;
    el('tituloForm').textContent = 'Editar foco #' + foco.id;
    el('btnSalvar').textContent = 'Salvar alterações';
    el('btnCancelarEdicao').classList.remove('oculto');
    el('formFoco').scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function numeroOuNulo(texto) {
    return texto === '' ? null : Number(texto);
}

function lerFormulario() {
    let dataHora = el('fDataHora').value;
    if (dataHora.length === 16) {
        dataHora += ':00';
    }
    return {
        dataHora: dataHora || null,
        satelite: el('fSatelite').value.trim(),
        uf: el('fUf').value,
        municipio: el('fMunicipio').value.trim(),
        bioma: el('fBioma').value.trim(),
        latitude: numeroOuNulo(el('fLatitude').value),
        longitude: numeroOuNulo(el('fLongitude').value),
        riscoFogo: numeroOuNulo(el('fRisco').value)
    };
}

async function salvarFoco(evento) {
    evento.preventDefault();
    const foco = lerFormulario();
    await comBloqueio(el('btnSalvar'), 'Salvando...', async () => {
        try {
            if (estado.editandoId === null) {
                const criado = await api.criar(foco);
                mostrarMensagem('Foco cadastrado com sucesso (ID ' + criado.id + ')', 'sucesso');
            } else {
                const atualizado = await api.atualizar(estado.editandoId, foco);
                mostrarMensagem('Foco ' + atualizado.id + ' atualizado com sucesso', 'sucesso');
            }
            limparFormulario();
            await recarregarDados();
        } catch (erro) {
            mostrarMensagem(erro.message, 'erro');
        }
    });
}

async function excluirFoco(foco, botao) {
    if (!confirm('Excluir o foco de fogo ID ' + foco.id + '?')) {
        return;
    }
    await comBloqueio(botao, 'Excluindo...', async () => {
        try {
            await api.excluir(foco.id);
            mostrarMensagem('Foco ' + foco.id + ' excluído com sucesso', 'sucesso');
            if (estado.editandoId === foco.id) {
                limparFormulario();
            }
            await recarregarDados();
        } catch (erro) {
            mostrarMensagem(erro.message, 'erro');
        }
    });
}

function linhaDetalhe(lista, nome, valor) {
    const dt = document.createElement('dt');
    dt.textContent = nome;
    const dd = document.createElement('dd');
    dd.textContent = valor;
    lista.append(dt, dd);
}

function mostrarDetalhe(foco) {
    const area = el('resultadoBusca');
    area.className = 'detalhe';
    area.replaceChildren();

    const lista = document.createElement('dl');
    linhaDetalhe(lista, 'ID', foco.id);
    linhaDetalhe(lista, 'Data e hora', formatarDataHora(foco.dataHora));
    linhaDetalhe(lista, 'Satélite', foco.satelite);
    linhaDetalhe(lista, 'Local', foco.municipio + ' / ' + foco.uf);
    linhaDetalhe(lista, 'Bioma', foco.bioma);
    linhaDetalhe(lista, 'Coordenadas', foco.latitude + ', ' + foco.longitude);
    linhaDetalhe(lista, 'Risco de fogo', foco.riscoFogo === null ? 'não informado' : foco.riscoFogo);
    area.append(lista, botaoAcao('Editar este registro', 'secundario', () => iniciarEdicao(foco)));
}

function mostrarErroBusca(texto) {
    const area = el('resultadoBusca');
    area.className = 'detalhe erro';
    area.textContent = texto;
}

async function buscarPorId(evento) {
    evento.preventDefault();
    const id = el('buscaId').value.trim();
    if (!/^[1-9][0-9]*$/.test(id)) {
        mostrarErroBusca('Informe um ID numérico válido');
        return;
    }
    await comBloqueio(el('btnBuscar'), 'Buscando...', async () => {
        try {
            mostrarDetalhe(await api.buscarPorId(id));
        } catch (erro) {
            mostrarErroBusca(erro.message);
        }
    });
}

function mostrarResumoImportacao(resumo) {
    const area = el('resumoImportacao');
    area.className = 'resumo';
    area.replaceChildren();

    const titulo = document.createElement('strong');
    titulo.textContent = 'Importação concluída';
    const lista = document.createElement('ul');
    [
        ['Linhas lidas', resumo.linhasLidas],
        ['Registros importados', resumo.importados],
        ['Registros rejeitados', resumo.rejeitados],
        ['Registros duplicados', resumo.duplicados]
    ].forEach(([nome, valor]) => {
        const item = document.createElement('li');
        item.textContent = nome + ': ' + formatadorNumero.format(valor);
        lista.append(item);
    });
    area.append(titulo, lista);

    const motivos = Object.entries(resumo.motivosRejeicao);
    if (motivos.length > 0) {
        const subtitulo = document.createElement('div');
        subtitulo.textContent = 'Motivos das rejeições:';
        const listaMotivos = document.createElement('ul');
        motivos.forEach(([motivo, quantidade]) => {
            const item = document.createElement('li');
            item.textContent = motivo + ': ' + formatadorNumero.format(quantidade);
            listaMotivos.append(item);
        });
        area.append(subtitulo, listaMotivos);
    }
}

function mostrarStatusImportacao(texto, ehErro) {
    const area = el('resumoImportacao');
    area.className = ehErro ? 'resumo erro' : 'resumo';
    area.textContent = texto;
}

async function importarArquivo(evento) {
    evento.preventDefault();
    const arquivo = el('arquivoCsv').files[0];
    if (!arquivo) {
        mostrarMensagem('Selecione um arquivo CSV para importar', 'erro');
        return;
    }
    const aviso = 'A importação apaga TODOS os registros atuais e os substitui pelo conteúdo do arquivo "'
        + arquivo.name + '". Deseja continuar?';
    if (!confirm(aviso)) {
        return;
    }
    await comBloqueio(el('btnImportar'), 'Importando...', async () => {
        mostrarStatusImportacao('Importando "' + arquivo.name
            + '". Arquivos grandes podem levar vários minutos, não feche a página.', false);
        try {
            const resumo = await api.importar(arquivo);
            mostrarResumoImportacao(resumo);
            mostrarMensagem(resumo.mensagem, 'sucesso');
            el('arquivoCsv').value = '';
            el('formFiltros').reset();
            estado.filtros = { ...FILTROS_VAZIOS };
            estado.pagina = 0;
            limparFormulario();
            await Promise.all([carregarOpcoes(), recarregarDados()]);
        } catch (erro) {
            mostrarStatusImportacao(erro.message, true);
            mostrarMensagem(erro.message, 'erro');
        }
    });
}

function mudarPagina(delta) {
    estado.pagina += delta;
    carregarTabela();
}

function iniciar() {
    preencherSelect(el('filtroUf'), 'Todas', UFS);
    preencherSelect(el('fUf'), 'Selecione', UFS);
    preencherSelect(el('filtroBioma'), 'Todos', []);
    preencherSelect(el('filtroSatelite'), 'Todos', []);

    el('mensagem').addEventListener('click', () => el('mensagem').classList.add('oculto'));
    el('formImportar').addEventListener('submit', importarArquivo);
    el('formBusca').addEventListener('submit', buscarPorId);
    el('formFiltros').addEventListener('submit', aplicarFiltros);
    el('btnLimparFiltros').addEventListener('click', limparFiltros);
    el('formFoco').addEventListener('submit', salvarFoco);
    el('btnCancelarEdicao').addEventListener('click', limparFormulario);
    el('btnAnterior').addEventListener('click', () => mudarPagina(-1));
    el('btnProxima').addEventListener('click', () => mudarPagina(1));
    el('tamanhoPagina').addEventListener('change', evento => {
        estado.tamanho = Number(evento.target.value);
        estado.pagina = 0;
        carregarTabela();
    });

    carregarOpcoes();
    recarregarDados();
}

document.addEventListener('DOMContentLoaded', iniciar);
