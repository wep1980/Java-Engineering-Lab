#!/usr/bin/env node
/**
 * Rede de segurança (não autoritativa) para o registro obrigatório de
 * conversas do Java Engineering Lab em docs/conversation-history.md.
 *
 * Mecanismo oficial (ver docs/conversation-history.md e CLAUDE.md):
 *   1. Ao receber uma nova mensagem, a primeira ação do Claude é registrar
 *      a mensagem do usuário integralmente em docs/conversation-history.md.
 *   2. Antes de encerrar o turno, o Claude registra sua própria resposta
 *      final na mesma entrada.
 *
 * Por que esse script não escreve o conteúdo da conversa:
 *   Os hooks "UserPromptSubmit" e "Stop" do Claude Code (versão 2.1.240)
 *   recebem via stdin um payload contendo "transcript_path", mas não o
 *   texto bruto do prompt ou da resposta. Reconstruir o texto exato a
 *   partir do transcript JSONL e reescrevê-lo automaticamente no arquivo
 *   de histórico introduziria risco real de duplicidade, divergência de
 *   formatação ou corrupção do arquivo (dois escritores concorrentes).
 *
 * Por isso este script atua apenas como um verificador (safety net):
 *   - No evento "UserPromptSubmit", grava o tamanho atual de
 *     docs/conversation-history.md em um arquivo de estado local.
 *   - No evento "Stop", compara o tamanho atual com o tamanho registrado.
 *     Se o arquivo não cresceu, emite um aviso (não bloqueante) lembrando
 *     que o registro pode não ter sido feito nesta interação.
 *
 * Este é um mecanismo de primeira versão, registrado como PROPOSTA em
 * specs/manifest/SPEC-JEL-001-bootstrap.md, sujeito a revisão.
 */

'use strict';

const fs = require('fs');
const path = require('path');

const PROJECT_DIR = process.env.CLAUDE_PROJECT_DIR || process.cwd();
const HISTORY_FILE = path.join(PROJECT_DIR, 'docs', 'conversation-history.md');
const STATE_FILE = path.join(PROJECT_DIR, '.claude', '.jel-history-check.json');

function lerEntradaStdin() {
  try {
    const raw = fs.readFileSync(0, 'utf8');
    return raw.trim() ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

function tamanhoAtualDoHistorico() {
  try {
    return fs.statSync(HISTORY_FILE).size;
  } catch {
    return null;
  }
}

function lerEstado() {
  try {
    return JSON.parse(fs.readFileSync(STATE_FILE, 'utf8'));
  } catch {
    return null;
  }
}

function gravarEstado(estado) {
  try {
    fs.mkdirSync(path.dirname(STATE_FILE), { recursive: true });
    fs.writeFileSync(STATE_FILE, JSON.stringify(estado), 'utf8');
  } catch {
    // Falha ao gravar estado não deve interromper a sessão.
  }
}

function principal() {
  const payload = lerEntradaStdin();
  const evento = payload.hook_event_name;
  const tamanhoAtual = tamanhoAtualDoHistorico();

  if (evento === 'UserPromptSubmit') {
    gravarEstado({
      sessionId: payload.session_id || null,
      promptId: payload.prompt_id || null,
      tamanhoAntes: tamanhoAtual,
    });
    process.exit(0);
  }

  if (evento === 'Stop') {
    const estado = lerEstado();

    if (!estado || tamanhoAtual === null) {
      // Sem estado anterior ou arquivo inexistente: não bloquear nem alarmar.
      process.exit(0);
    }

    if (tamanhoAtual <= estado.tamanhoAntes) {
      process.stderr.write(
        '[java-engineering-lab] Aviso: docs/conversation-history.md ' +
        'nao parece ter crescido nesta interacao. Confirme se a mensagem ' +
        'do usuario e a resposta do Claude foram registradas, conforme ' +
        'CLAUDE.md e docs/conversation-history.md.\n'
      );
      process.exit(1); // erro não bloqueante: apenas um aviso visível
    }

    process.exit(0);
  }

  // Evento não tratado por este script.
  process.exit(0);
}

principal();
