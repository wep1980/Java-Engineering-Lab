# ADR-0008 — OWASP Dependency-Check exige chave de API da NVD

- **Status**: Aceita
- **Data**: 2026-08-23
- **Origem**: achado real durante a implementação de `SPEC-JEL-007-hardening.md` (RF-01)

## Contexto

Ao configurar `org.owasp:dependency-check-maven` (versões atuais 12.2.0
e 13.0.0) sem uma chave de API da NVD, a execução falha completamente:

```text
NvdApiException: Invalid API Key, length of 0 too short to provided a
masked partial key
```

Isso não é um erro de configuração deste projeto — é um bug conhecido e
não corrigido no upstream da ferramenta (issues `#8298` e `#8715` no
repositório `dependency-check/DependencyCheck`), que impede o uso
"sem chave" mesmo estando documentado como suportado (com sincronização
mais lenta). Confirmado localmente nesta sessão: `mvn
org.owasp:dependency-check-maven:13.0.0:check` sem `-DnvdApiKey` falha
de imediato, sem sequer tentar a sincronização mais lenta esperada.

## Decisão

Usar uma chave de API da NVD já existente do usuário (reaproveitada do
projeto `wepdev-financas`, onde o mesmo problema já havia sido
encontrado e documentado em 2026-08-07). A chave é cadastrada como
secret `NVD_API_KEY` no repositório GitHub (nunca versionada em
código), e o CI passa a chamá-la explicitamente:

```yaml
run: mvn -B org.owasp:dependency-check-maven:13.0.0:check -DnvdApiKey=${{ secrets.NVD_API_KEY }}
```

Um step de cache (`actions/cache`) para
`~/.m2/repository/org/owasp/dependency-check-data` evita rebaixar a base
de dados a cada execução — mesmo padrão já validado em `wepdev-financas`.

`failBuildOnCVSS` fica configurado em `11` (fora da escala real, 0-10) —
o scan é **informativo**, não bloqueia o build, conforme decisão de
design já registrada em `SPEC-JEL-007` (RF-01): travar o pipeline por
uma CVE sem triagem prévia de severidade pararia o CI sem necessidade
real. Pode evoluir para bloqueante numa fase futura, com critério
explícito.

## Consequências

- Sem o secret `NVD_API_KEY` configurado no repositório, o step de scan
  falha — mas o build principal (`mvn -B verify`, com todos os testes)
  não é afetado, porque são steps separados no workflow.
- A mesma limitação existiria em qualquer novo projeto que adote esta
  ferramenta enquanto o bug upstream não for corrigido — vale registrar
  isso como conhecimento reaproveitável entre os projetos do usuário
  (já era prática em `wepdev-financas`, agora também aqui).
- Reforça, mais uma vez, que decisões sobre credenciais/serviços
  externos são sempre do usuário — a chave não foi gerada nem
  presumida por mim; o usuário identificou que já possuía uma chave
  válida de outro projeto seu e decidiu reaproveitá-la.
