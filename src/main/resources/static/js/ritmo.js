document.addEventListener("click", function (event) {
    if (!(event.target instanceof Element)) return;

    const botao = event.target.closest("[data-alternar-senha]");
    if (!botao) return;

    const campoId = botao.getAttribute("aria-controls");
    const campo = campoId ? document.getElementById(campoId) : null;
    if (!campo) return;

    const mostrar = campo.type === "password";
    campo.type = mostrar ? "text" : "password";
    botao.textContent = mostrar ? "Ocultar" : "Mostrar";
});

document.addEventListener("submit", function (event) {
    const formulario = event.target;
    if (!(formulario instanceof HTMLFormElement)) return;

    const mensagem = formulario.dataset.confirmacao;
    if (mensagem && !window.confirm(mensagem)) {
        event.preventDefault();
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const regra = document.querySelector("[data-regra-bloco-texto]");
    const formularioBloco = document.querySelector(
        'form[action$="/blocos/configuracao"][method="post"]'
    );

    if (!regra || !(formularioBloco instanceof HTMLFormElement)) return;

    const duracoesPermitidas = new Set([5, 15, 30]);
    const rotulosTopicos = formularioBloco.querySelectorAll(
        'input[name="topicoIds"]'
    );
    const rotulosDuracao = formularioBloco.querySelectorAll(
        'input[name="duracaoMinutos"]'
    );

    function atualizarRegraBloco() {
        const duracaoSelecionada = formularioBloco.querySelector(
            'input[name="duracaoMinutos"]:checked'
        );
        const duracao = Number(duracaoSelecionada?.value);

        if (!duracoesPermitidas.has(duracao)) {
            regra.textContent =
                "Escolha uma duração para ver os requisitos do bloco.";
            return;
        }

        const quantidadeTopicos = formularioBloco.querySelectorAll(
            'input[name="topicoIds"]:checked'
        ).length;
        const limiteTopicos = duracao / 5;

        if (quantidadeTopicos === 0) {
            regra.textContent =
                `Bloco de ${duracao} minutos: selecione de 1 a ${limiteTopicos} ` +
                `${limiteTopicos === 1 ? "tópico" : "tópicos"} da mesma disciplina. ` +
                `O bloco terá ${duracao} perguntas.`;
            return;
        }

        const minimoPorTopico = Math.ceil(duracao / quantidadeTopicos);
        const descricaoTopicos = quantidadeTopicos === 1
            ? "1 tópico selecionado"
            : `${quantidadeTopicos} tópicos selecionados`;

        regra.textContent =
            `Bloco de ${duracao} minutos: selecione até ${limiteTopicos} ` +
            `${limiteTopicos === 1 ? "tópico" : "tópicos"} da mesma disciplina. ` +
            `O bloco terá ${duracao} perguntas. Com ${descricaoTopicos}, ` +
            `cada tópico precisa ter pelo menos ${minimoPorTopico} ` +
            `${minimoPorTopico === 1 ? "pergunta" : "perguntas"} disponíveis.`;
    }

    rotulosDuracao.forEach(function (duracao) {
        duracao.addEventListener("change", atualizarRegraBloco);
    });
    rotulosTopicos.forEach(function (topico) {
        topico.addEventListener("change", atualizarRegraBloco);
    });

    atualizarRegraBloco();
});