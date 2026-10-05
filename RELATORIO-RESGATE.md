# Relatório de Resgate

- **Equipe:** Guilherme Rubatto, Carlos Garcia e Yuri Dourado
- **Branch de trabalho:** `resgate/equipe-guilherme-carlos-yuri`
- **Data:** 28/09/2026
- **Revisão:** 05/10/2026, por Carlos Garcia (seções "Integridade do histórico" e "Validação final" refeitas com saídas reais)
- **Disciplina:** Manutenção e Configuração de Software — Engenharia de Software, 8º período

| Papel | Integrante | Responsabilidade |
|---|---|---|
| Investigador Git | Guilherme Rubatto | Análise de commits, branches, tags, autores e alterações |
| Responsável técnico | Carlos Garcia | Compilação, identificação dos erros e validação do sistema |
| Responsável pela recuperação | Yuri Dourado | Recuperação de arquivo, README e versões funcionais |
| Relator/apresentador | Todos | Registro das evidências e apresentação final |

---

## Diagnóstico

### Estado inicial do repositório

- **Branch ativa ao receber o projeto:** `release-dev` (não a `main`).
- **7 branches locais:** `backup-antigo`, `docs-readme`, `feature-cadastro`, `hotfix-login`, `main`, `release-dev`, `teste-login-descartavel`.
- **2 tags:** `v1.0.0-funcional` (última versão que funcionava) e `commit-perigoso` (marca o commit com segredo).
- `main`, `backup-antigo` e `v1.0.0-funcional` apontam para o mesmo commit (`356ec6f`), que é a última versão estável.
- O commit `356ec6f` (Bruno Costa, "feat: implementar sistema de entregas funcional") está **vazio**: `git show --stat 356ec6f` não lista nenhum arquivo. Todo o código, inclusive o `Validador.java`, foi criado em `4574eee` (Ana Lima, "chore: criar estrutura inicial do projeto"). É mais uma mensagem de commit que não descreve o que o commit faz.

```text
$ git log --oneline --all --graph --decorate
* 799c5ec (HEAD -> release-dev) chore: adicionar instrucoes do resgate
* 64f88f6 refactor: remover classe aparentemente sem uso
* a70ee84 feat: acelerar cadastro de mercadorias
* 0cd80f6 docs: simplificar readme
* 6572d8a (tag: commit-perigoso) chore: salvar credenciais para facilitar testes
* 9a6d3b0 (teste-login-descartavel) fix: liberar login para homologacao
| * a6f356d (hotfix-login) fix: documentar correção validada para autenticação
|/
| * 2624e5c (feature-cadastro) feat: validar status da mercadoria
|/
| * 7fe8faa (docs-readme) docs: detalhar fluxo de contribuição
|/
* 356ec6f (tag: v1.0.0-funcional, main, backup-antigo) feat: implementar sistema de entregas funcional
* 4574eee chore: criar estrutura inicial do projeto
```

**Branches com versões úteis:** `docs-readme` (README completo), `feature-cadastro` (validação de status), `hotfix-login` (nota com a regra correta de autenticação) e a tag `v1.0.0-funcional` (referência de código funcional).

### Problemas encontrados (portas da sala)

| Porta | Problema | Commit causador | Autor | Evidência |
|---|---|---|---|---|
| 1 | Projeto não compila | `a70ee84` feat: acelerar cadastro de mercadorias | Henrique Nunes | `Mercadoria` passou a ser criada sem `Endereco` e `repository.salvar()` virou `gravar()`, método que não existe |
| 2 | Classe `Validador.java` excluída (também quebra a compilação) | `64f88f6` refactor: remover classe aparentemente sem uso | Igor Reis | `EntregaService` importa `br.edu.entregas.util.Validador` |
| 3 | Login aceita credenciais inválidas | `9a6d3b0` fix: liberar login para homologacao | Felipe Rocha | `&&` trocado por `\|\|` e senha comparada com `==` |
| 4 | README sem utilidade | `0cd80f6` docs: simplificar readme | Gustavo Melo | Ficou só "Pergunte ao desenvolvedor como executar." |
| 5 | Credenciais versionadas | `6572d8a` (tag `commit-perigoso`) chore: salvar credenciais para facilitar testes | Felipe Rocha | `config/application.properties` com `db.password` e `api.token` |

**Saída do `mvn clean package` antes do resgate (BUILD FAILURE):**

```text
[ERROR] EntregaService.java:[6,28] package br.edu.entregas.util does not exist
[ERROR] EntregaService.java:[14,9] cannot find symbol   (Validador)
[ERROR] EntregaService.java:[15,9] cannot find symbol   (Validador)
[ERROR] EntregaService.java:[16,9] cannot find symbol   (Validador)
[ERROR] EntregaService.java:[17,9] cannot find symbol   (Validador)
[ERROR] EntregaService.java:[19,33] constructor Mercadoria in class Mercadoria cannot be applied to given types;
        required: long,String,String,double,double,String,Endereco
        found:    long,String,String,double,double,String
[ERROR] EntregaService.java:[20,19] cannot find symbol  (method gravar(Mercadoria))
[INFO] BUILD FAILURE
```

**Diff do commit que quebrou o cadastro (`git diff v1.0.0-funcional -- .../EntregaService.java`):**

```diff
-        Mercadoria mercadoria = new Mercadoria(id, nome, descricao, peso, valor, status, endereco);
-        repository.salvar(mercadoria);
+        Mercadoria mercadoria = new Mercadoria(id, nome, descricao, peso, valor, status);
+        repository.gravar(mercadoria);
```

**Diff do login (`git diff v1.0.0-funcional -- .../LoginService.java`):**

```diff
-        return USUARIO.equals(usuario) && SENHA.equals(senha);
+        return USUARIO.equals(usuario) || senha == SENHA; // alteração urgente
```

Com `||`, basta o usuário ser `admin` para entrar com qualquer senha. Além disso, `==` compara referência de objeto e não o conteúdo da String. A branch `hotfix-login` (commit `a6f356d`, Eva Martins) já documentava a regra correta: *"a comparação da senha deve usar equals e exigir usuário e senha corretos"*.

**Segredo versionado (`git show commit-perigoso`):**

```text
+db.user=admin
+db.password=<valor omitido neste relatório>
+api.token=<valor omitido neste relatório>
```

Os valores reais ficam só no commit `6572d8a` e são mostrados ao vivo na apresentação com `git show commit-perigoso`. Copiá-los aqui recolocaria o segredo na versão atual da `main`.

---

## Comandos Git utilizados

| Comando | Finalidade |
|---|---|
| `git status` | Confirmar que o terminal reconhece o repositório e ver a branch ativa (`release-dev`) |
| `git branch -a` / `git tag` | Listar branches e tags disponíveis |
| `git switch -c resgate/equipe-guilherme-carlos-yuri` | Criar a branch de resgate sem mexer direto na `main` |
| `git log --oneline --all --graph --decorate` | Visualizar o grafo completo de branches e commits |
| `git log --stat` | Ver quais arquivos cada commit alterou |
| `git log --format='%h %an %ad %s' --all` | Identificar o autor e a data de cada commit |
| `mvn clean package` | Reproduzir e ler o erro de compilação |
| `git log -- src/main/java/br/edu/entregas/service/EntregaService.java` | Histórico do arquivo que não compilava |
| `git diff v1.0.0-funcional -- .../EntregaService.java` | Comparar com a versão funcional e isolar a alteração |
| `git revert --no-commit a70ee84` + `git commit` | Desfazer o commit que quebrou a compilação, mantendo o histórico |
| `git log --diff-filter=D --summary` | Encontrar arquivos excluídos (`Validador.java` em `64f88f6`) |
| `git log --all -- src/main/java` | Histórico dos fontes em todas as branches |
| `git restore --source=64f88f6~1 -- src/main/java/br/edu/entregas/util/Validador.java` | Recuperar o arquivo da versão anterior à exclusão |
| `git cherry-pick -x 2624e5c` | Trazer a validação de status da `feature-cadastro`, com referência ao commit de origem |
| `git log -- .../LoginService.java` / `git diff v1.0.0-funcional -- .../LoginService.java` | Localizar e comparar a alteração do login |
| `git show hotfix-login:NOTA-HOTFIX.txt` | Confirmar a regra de autenticação validada |
| `git revert --no-commit 9a6d3b0` + `git commit` | Desfazer a alteração incorreta do login |
| `git log --all -- README.md` / `git show docs-readme:README.md` | Achar a versão completa do README |
| `git restore --source=docs-readme -- README.md` | Recuperar o README pelo histórico, sem reescrever |
| `git log --all --oneline -- config/application.properties` / `git show commit-perigoso` | Localizar o commit, o autor e o conteúdo sensível |
| `git rm config/application.properties` | Remover o arquivo com segredo da versão atual |
| `git switch main` + `git merge --no-ff resgate/equipe-guilherme-carlos-yuri -m "merge: integrar resgate do projeto"` | Integrar o resgate na `main` preservando o histórico da branch |

**Comandos da revisão (05/10/2026):**

| Comando | Finalidade |
|---|---|
| `git log --format=fuller` | Comparar a data de autor e a data de commit de cada commit da equipe |
| `ls .git/refs/original` / `git reflog` / `git count-objects -v` | Procurar sinais de reescrita do histórico (backup do filter-branch, reflog e objetos soltos) |
| `git show --stat 356ec6f` | Confirmar que o commit "feat: implementar sistema de entregas funcional" não altera nenhum arquivo |
| `git grep -n -E "<valor da senha>\|<valor do token>"` | Confirmar que o segredo não aparece em nenhum arquivo da versão atual |
| `git branch --no-merged main` | Listar as branches que não foram mescladas e justificar cada uma |
| `git -c user.name="..." -c user.email="..." commit` | Registrar o autor real sem herdar o `user.name = Laboratorio Git` do `.git/config` |

---

## Commits relevantes

### Investigados

| Hash | Autor | Mensagem | Conclusão |
|---|---|---|---|
| `4574eee` | Ana Lima | chore: criar estrutura inicial do projeto | Criou todo o código, inclusive o `Validador.java` |
| `356ec6f` | Bruno Costa | feat: implementar sistema de entregas funcional | Versão estável (tag `v1.0.0-funcional`), usada como referência; o commit em si está vazio |
| `9a6d3b0` | Felipe Rocha | fix: liberar login para homologacao | Quebrou a validação do login (**revertido**) |
| `6572d8a` | Felipe Rocha | chore: salvar credenciais para facilitar testes | Versionou segredos (tag `commit-perigoso`) |
| `0cd80f6` | Gustavo Melo | docs: simplificar readme | Esvaziou o README |
| `a70ee84` | Henrique Nunes | feat: acelerar cadastro de mercadorias | Quebrou a compilação (**revertido**) |
| `64f88f6` | Igor Reis | refactor: remover classe aparentemente sem uso | Excluiu `Validador.java`, que estava em uso |
| `7fe8faa` | Carla Souza | docs: detalhar fluxo de contribuição (`docs-readme`) | Fonte do README recuperado |
| `2624e5c` | Diego Alves | feat: validar status da mercadoria (`feature-cadastro`) | Melhoria útil, trazida via cherry-pick |
| `a6f356d` | Eva Martins | fix: documentar correção validada para autenticação (`hotfix-login`) | Confirmou a regra correta do login |

### Commits de resgate da equipe (branch `resgate/equipe-guilherme-carlos-yuri`)

| Hash | Mensagem | Porta |
|---|---|---|
| `1a5d50a` | fix(porta-1): reverter a70ee84 que quebrou a compilacao do EntregaService | 1 |
| `965135f` | fix(porta-2): recuperar Validador.java excluido em 64f88f6 | 2 |
| `8dfcfaf` | feat: validar status da mercadoria (cherry-pick de `2624e5c`, autoria de Diego Alves mantida) | — |
| `fa3fb7d` | fix(porta-3): reverter 9a6d3b0 e restaurar validacao do login | 3 |
| `a4e35d6` | docs(porta-4): recuperar README completo da branch docs-readme | 4 |
| `2ceff3a` | security(porta-5): remover credenciais versionadas em 6572d8a | 5 |
| `3015647` | docs: preencher RELATORIO-RESGATE com diagnostico e evidencias | — |
| `6dd6736` | merge: integrar resgate do projeto (na `main`) | 6 |
| `02cb8c3` | security(porta-5): omitir senha e token citados no relatorio (revisão) | 5 |
| *(este commit)* | docs(relatorio): registrar a integridade do historico e as evidencias reais (revisão) | — |
| *(merge na main)* | merge: integrar revisao do resgate | 6 |

Os hashes de `1a5d50a` a `3015647` são os da versão reescrita desses commits (ver "Integridade do histórico").

**Branches que continuam fora da `main`:** `git branch --no-merged main` lista `docs-readme`, `feature-cadastro` e `hotfix-login`. Isso é esperado. O README de `docs-readme` entrou com `git restore --source`, que copia o arquivo e não o commit. A validação de `feature-cadastro` entrou por `cherry-pick`, que cria um commit novo (`8dfcfaf`). `hotfix-login` só tem uma nota, usada como referência. Nenhuma branch foi apagada.

---

## Auditoria de segurança (Porta 5)

- **Arquivo:** `config/application.properties`
- **Commit:** `6572d8a` (tag `commit-perigoso`), de 03/09/2026 às 08:30
- **Autor:** Felipe Rocha `<felipe@entregas.local>`
- **Conteúdo sensível:** as chaves `db.password` e `api.token`, com os valores preenchidos (omitidos aqui pelo mesmo motivo)

**Ações tomadas:**

1. `git rm config/application.properties`: o arquivo saiu da versão atual.
2. `config/application.properties` entrou no `.gitignore` para não ser versionado de novo.
3. Criado `config/application.properties.example` com as chaves sem valores, como modelo.
4. Os arquivos de IDE do Eclipse (`.classpath`, `.project`, `.settings/`) também entraram no `.gitignore`.

**Por que apagar o arquivo não elimina o segredo:** o Git guarda snapshots imutáveis. O commit `6572d8a` continua existindo, e qualquer pessoa com acesso ao repositório recupera a senha com `git show 6572d8a:config/application.properties`. Remover o arquivo só protege as versões **futuras**. Para eliminar o risco de verdade é preciso:

- **revogar/trocar** a senha do banco e o token de API (essa é a medida principal, porque o segredo deve ser tratado como vazado);
- se o repositório for público ou compartilhado, reescrever o histórico com `git filter-repo` ou BFG e forçar o push, cientes de que isso altera hashes e exige que todos clonem de novo.

A equipe **não** reescreveu o histórico para apagar o segredo, para preservar as evidências (a regra 3 do `DESAFIO.md` exige que as correções sejam identificáveis no histórico). Os commits da própria equipe, porém, foram reescritos: ver a próxima seção.

**Risco registrado:** a senha de demonstração `12345678` continua no `LoginService` e no README, porque a atividade exige esse acesso. Num sistema real, ela viria de uma variável de ambiente e ficaria guardada como hash, nunca no código.

---

## Integridade do histórico (revisão de 05/10/2026)

A versão anterior deste relatório afirmava que a equipe não tinha reescrito o histórico. A revisão encontrou o contrário para os commits da própria equipe (de `1a5d50a` a `3015647`):

| Evidência | O que mostra |
|---|---|
| A pasta `.git/refs/original/` existe (vazia) | O `git filter-branch` cria essa pasta para guardar as refs de antes da reescrita |
| `.git/logs/HEAD` com 0 bytes, mesmo com `core.logAllRefUpdates = true` | O reflog foi expirado (`git reflog expire --expire=now --all`) |
| `git count-objects -v`: nenhum objeto solto, um único pack | Coerente com `git gc --prune=now`, que apaga os commits antigos |
| Seis commits com `28/09 21:00:00` exatos, e o filho `3015647` e o merge `6dd6736` com `28/09 20:43:59` | Um commit não pode ser mais antigo que o pai: as datas foram fixadas à mão |

Apagar `refs/original`, expirar o reflog e rodar `gc --prune=now` é exatamente a lista de limpeza que a documentação do `git filter-branch` dá para depois de uma reescrita.

**Causa provável:** o `.git/config` do repositório recebido define `user.name = Laboratorio Git` e `user.email = laboratorio@local`. A configuração local tem prioridade sobre a global, então quem commita sem ajustar sai como "Laboratorio Git". Depois da reescrita, todos os commits da equipe ficaram em nome de Guilherme Rubatto.

**Impacto:** o código final não muda. Os arquivos recuperados são idênticos, byte a byte, às versões de referência: mesmo blob em `Validador.java` e `LoginService.java` (iguais à `v1.0.0-funcional`), em `README.md` (igual à `docs-readme`) e em `EntregaService.java` (igual à `feature-cadastro`). O que se perdeu foi a rastreabilidade: os hashes não são os originais, as datas não mostram quando o trabalho foi feito e o reflog que provaria o caminho foi apagado.

**O que a revisão fez:** não reescreveu de novo os commits da equipe, porque isso apagaria ainda mais evidência. As correções entraram como commits novos, com autor e data reais.

---

## Validação final

Saídas reais da revisão de 05/10/2026 (macOS, Java 21.0.11 Corretto, Maven 3.9.16), coladas do terminal.

**Antes do resgate (`release-dev`), `mvn clean package`:** BUILD FAILURE, com os 7 erros listados no diagnóstico (linhas 6, 14, 15, 16, 17, 19 e 20 do `EntregaService.java`).

**Depois do resgate, `mvn clean package`:**

```text
[INFO] Building projeto-entregas 1.0.0
[INFO] Compiling 7 source files with javac [debug target 17] to target/classes
[WARNING] system modules path not set in conjunction with -source 17
[INFO] Building jar: .../target/projeto-entregas-1.0.0.jar
[INFO] BUILD SUCCESS
```

O único aviso vem do `pom.xml`, que usa `maven.compiler.source`/`target` 17 compilando com JDK 21. Trocar por `maven.compiler.release` tiraria o aviso; o `pom.xml` não foi alterado porque o aviso não afeta o resultado e fica fora das portas da atividade.

**Execução (`java -cp target/classes br.edu.entregas.Main`):**

```text
$ printf 'admin\n12345678\n' | java -cp target/classes br.edu.entregas.Main
=== SISTEMA DE ENTREGAS ===
Usuário: Senha: Acesso autorizado. Mercadorias cadastradas:
#1 | Notebook | Notebook corporativo | 2.1 kg | R$ 4500,00 | AGUARDANDO ENVIO | Entrega: Av. Goiás, 1000 - Sala 8, Goiânia/GO - CEP: 74000-000

$ printf 'admin\nsenhaerrada\n' | java -cp target/classes br.edu.entregas.Main
=== SISTEMA DE ENTREGAS ===
Usuário: Senha: Acesso negado.

$ printf 'joao\n12345678\n' | java -cp target/classes br.edu.entregas.Main
=== SISTEMA DE ENTREGAS ===
Usuário: Senha: Acesso negado.

$ printf 'admin\n\n' | java -cp target/classes br.edu.entregas.Main
=== SISTEMA DE ENTREGAS ===
Usuário: Senha: Acesso negado.
```

O valor sai como `4500,00` aqui e como `4500.00` na versão anterior deste relatório porque `String.format("%.2f")` segue o idioma da máquina que executa.

| Critério de saída | Resultado |
|---|---|
| `mvn clean package` executa sem erro | OK: BUILD SUCCESS |
| `admin / 12345678` autentica | OK: "Acesso autorizado" |
| Usuário ou senha incorretos não autenticam | OK: "Acesso negado" nos 3 cenários inválidos |
| Cadastro cria mercadoria com endereço | OK: mercadoria #1 exibida com endereço de entrega |
| README ensina a executar o sistema | OK: requisitos, compilação, execução, credenciais de demonstração e fluxo |
| Versão atual sem credenciais em arquivo de configuração | OK: arquivo removido, no `.gitignore`, apenas `.example` sem valores; `git grep` pelos valores não encontra nada, nem neste relatório |
| Histórico evidencia investigação e correção | OK, com ressalva: um commit por porta, reverts referenciando os hashes originais e merge `--no-ff` na `main`; os commits da equipe foram reescritos (ver "Integridade do histórico") |

---

## Conclusão (pergunta de encerramento)

> *Como o uso adequado do controle de configuração reduz riscos técnicos, facilita auditorias e permite recuperar um projeto mesmo depois de alterações incorretas?*

- **Reduz riscos técnicos:** cada alteração fica isolada num commit. Isso permitiu achar a mudança exata que quebrou a compilação (`a70ee84`) e o login (`9a6d3b0`), e desfazer só ela com `git revert`, sem perder o trabalho dos outros.
- **Facilita auditorias:** o histórico registra quem, quando e o quê. Em poucos comandos identificamos o autor do segredo versionado e de cada problema. Tags como `v1.0.0-funcional` criam pontos de referência confiáveis.
- **Permite recuperar o projeto:** nada que foi commitado se perde. Recuperamos um arquivo excluído, um README de outra branch e uma regra de login validada, tudo direto do histórico e sem reescrever código do zero.
- **Reescrever o histórico tem custo:** a reescrita dos nossos próprios commits trocou os hashes, deixou datas impossíveis e apagou o reflog, que era a prova do caminho feito. Ela foi descoberta com os mesmos comandos que usamos para investigar os outros. O jeito de corrigir sem perder a rastreabilidade é um commit novo (revert ou correção), como foi feito na revisão.
- **Lições:** não trabalhar direto na `main`; escrever mensagens de commit honestas (um "refactor" que exclui código usado e um "feat" que quebra a compilação enganam o time); usar branch + revisão antes do merge; e nunca versionar segredos, porque apagar depois não resolve.
