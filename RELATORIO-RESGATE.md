# Relatório de Resgate

- **Equipe:** Guilherme Rubatto, Carlos Garcia e Yuri Dourado
- **Branch de trabalho:** `resgate/equipe-guilherme-carlos-yuri`
- **Data:** 28/09/2026
- **Revisão:** 05/10/2026, por Carlos Garcia (integridade do histórico, saídas reais e redação)
- **Disciplina:** Manutenção e Configuração de Software, Engenharia de Software, 8º período

| Papel | Integrante | Responsabilidade |
|---|---|---|
| Investigador Git | Guilherme Rubatto | Análise de commits, branches, tags, autores e alterações |
| Responsável técnico | Carlos Garcia | Compilação, identificação dos erros e validação do sistema |
| Responsável pela recuperação | Yuri Dourado | Recuperação de arquivo, README e versões funcionais |
| Relator/apresentador | Todos | Registro das evidências e apresentação final |

## Resumo

Recebemos o projeto sem compilar, com um arquivo Java excluído, o login aceitando qualquer senha para o usuário `admin`, o README esvaziado e credenciais versionadas. Resolvemos as seis portas na branch `resgate/equipe-guilherme-carlos-yuri`, com um commit por correção, e integramos a branch na `main` com `git merge --no-ff`. Na `main`, o `mvn clean package` termina em BUILD SUCCESS, o login só aceita `admin / 12345678` e a mercadoria aparece com o endereço de entrega.

---

## Diagnóstico

### Estado inicial do repositório

Recebemos o repositório com a branch `release-dev` ativa, e não a `main`. Havia 7 branches locais (`backup-antigo`, `docs-readme`, `feature-cadastro`, `hotfix-login`, `main`, `release-dev` e `teste-login-descartavel`) e 2 tags: `v1.0.0-funcional`, que marca a última versão que funcionava, e `commit-perigoso`, que marca o commit com o segredo. A `main`, a `backup-antigo` e a tag `v1.0.0-funcional` apontavam para o mesmo commit, `356ec6f`.

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

Quatro referências guardavam versões úteis. A `docs-readme` tinha o README completo, a `feature-cadastro` tinha a validação de status, a `hotfix-login` tinha uma nota com a regra correta de autenticação e a tag `v1.0.0-funcional` servia de referência para o código que funcionava.

O commit `356ec6f` (Bruno Costa, "feat: implementar sistema de entregas funcional") está vazio: `git show --stat 356ec6f` não lista nenhum arquivo. Todo o código, inclusive o `Validador.java`, foi criado antes, em `4574eee` (Ana Lima, "chore: criar estrutura inicial do projeto"). É mais um caso de mensagem que não corresponde ao conteúdo do commit, como os de `64f88f6` e `a70ee84`.

### Problemas encontrados em cada porta

| Porta | Problema | Commit causador | Autor | Evidência |
|---|---|---|---|---|
| 1 | Projeto não compila | `a70ee84` feat: acelerar cadastro de mercadorias | Henrique Nunes | `Mercadoria` passou a ser criada sem `Endereco` e `repository.salvar()` virou `gravar()`, método que não existe |
| 2 | Classe `Validador.java` excluída (também quebra a compilação) | `64f88f6` refactor: remover classe aparentemente sem uso | Igor Reis | `EntregaService` importa `br.edu.entregas.util.Validador` |
| 3 | Login aceita credenciais inválidas | `9a6d3b0` fix: liberar login para homologacao | Felipe Rocha | `&&` trocado por `\|\|` e senha comparada com `==` |
| 4 | README sem utilidade | `0cd80f6` docs: simplificar readme | Gustavo Melo | Ficou só "Pergunte ao desenvolvedor como executar." |
| 5 | Credenciais versionadas | `6572d8a` (tag `commit-perigoso`) chore: salvar credenciais para facilitar testes | Felipe Rocha | `config/application.properties` com `db.password` e `api.token` |
| 6 | Entrega final fora da `main` | — | — | A `main` ainda apontava para `356ec6f`, sem nenhuma das correções |

Na `release-dev`, o `mvn clean package` falhava com 7 erros, todos no `EntregaService.java`. A saída abaixo está resumida, com os caminhos e os nomes de pacote encurtados:

```text
[ERROR] EntregaService.java:[6,28] package br.edu.entregas.util does not exist
[ERROR] EntregaService.java:[14,9] cannot find symbol
  symbol:   variable Validador
[ERROR] EntregaService.java:[15,9] cannot find symbol
[ERROR] EntregaService.java:[16,9] cannot find symbol
[ERROR] EntregaService.java:[17,9] cannot find symbol
[ERROR] EntregaService.java:[19,33] constructor Mercadoria in class Mercadoria cannot be applied to given types;
  required: long,String,String,double,double,String,Endereco
  found:    long,String,String,double,double,String
[ERROR] EntregaService.java:[20,19] cannot find symbol
  symbol:   method gravar(Mercadoria)
[INFO] 7 errors
[ERROR] BUILD FAILURE
```

O `git diff v1.0.0-funcional -- .../EntregaService.java` mostra o que `a70ee84` mudou no cadastro:

```diff
-        Mercadoria mercadoria = new Mercadoria(id, nome, descricao, peso, valor, status, endereco);
-        repository.salvar(mercadoria);
+        Mercadoria mercadoria = new Mercadoria(id, nome, descricao, peso, valor, status);
+        repository.gravar(mercadoria);
```

No login, o `git diff v1.0.0-funcional -- .../LoginService.java` mostra a troca feita em `9a6d3b0`:

```diff
-        return USUARIO.equals(usuario) && SENHA.equals(senha);
+        return USUARIO.equals(usuario) || senha == SENHA; // alteração urgente
```

Com `||`, basta o usuário ser `admin` para entrar com qualquer senha. O `==` também está errado, porque compara a referência do objeto e não o conteúdo da String. A branch `hotfix-login` (commit `a6f356d`, Eva Martins) já registrava a regra correta: *"a comparação da senha deve usar equals e exigir usuário e senha corretos"*.

O `git show commit-perigoso` mostra o arquivo de configuração com o segredo:

```text
+db.user=admin
+db.password=<valor omitido neste relatório>
+api.token=<valor omitido neste relatório>
```

Os valores reais continuam no histórico, no commit `6572d8a` e na primeira versão deste relatório (`3015647`). Eles serão mostrados ao vivo na apresentação, com `git show commit-perigoso`. Copiá-los aqui colocaria o segredo de volta na versão atual da `main`.

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

Na revisão de 05/10/2026, usamos também estes comandos:

| Comando | Finalidade |
|---|---|
| `git log --format=fuller` | Comparar a data de autor e a data de commit de cada commit da equipe |
| `ls .git/refs/original` / `git reflog` / `git count-objects -v` | Procurar sinais de reescrita do histórico (backup do filter-branch, reflog e objetos soltos) |
| `git show --stat 356ec6f` | Confirmar que o commit "feat: implementar sistema de entregas funcional" não altera nenhum arquivo |
| `git rev-parse main:<arquivo>` / `git rev-parse v1.0.0-funcional:<arquivo>` | Comparar o blob de cada arquivo recuperado com o da versão de referência |
| `git grep -n -E "<valor da senha>\|<valor do token>"` | Confirmar que o segredo não aparece em nenhum arquivo da versão atual |
| `git branch --no-merged main` | Listar as branches que não foram mescladas e justificar cada uma |
| `git -c user.name="..." -c user.email="..." commit` | Registrar o autor real sem herdar o `user.name = Laboratorio Git` do `.git/config` |

---

## Commits relevantes

### Commits investigados

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
| `a5f04b0` | docs(relatorio): corrigir o relatorio com evidencias verificadas (revisão) | — |
| *(este commit)* | docs(relatorio): revisar a redacao do relatorio (revisão) | — |
| *(merge na main)* | merge: integrar revisao do resgate | 6 |

Os hashes de `1a5d50a` a `3015647` são da versão reescrita desses commits, como explica a seção "Integridade do histórico".

Três branches continuam fora da `main`, e isso é esperado. O `git branch --no-merged main` lista `docs-readme`, `feature-cadastro` e `hotfix-login`. O README da `docs-readme` entrou com `git restore --source`, que copia o arquivo e não o commit. A validação da `feature-cadastro` entrou por `cherry-pick`, que cria um commit novo (`8dfcfaf`). A `hotfix-login` só tem uma nota, que usamos como referência. Não apagamos nenhuma branch.

---

## Auditoria de segurança (Porta 5)

O arquivo `config/application.properties` foi criado no commit `6572d8a` (tag `commit-perigoso`), em 03/09/2026 às 08:30, por Felipe Rocha `<felipe@entregas.local>`. Ele trazia as chaves `db.password` e `api.token` preenchidas. Os valores ficam fora deste relatório pelo motivo explicado no diagnóstico.

No commit `2ceff3a`, tomamos três medidas:

1. Removemos o arquivo da versão atual com `git rm config/application.properties`.
2. Incluímos `config/application.properties` no `.gitignore`, para que ele não volte a ser versionado.
3. Criamos `config/application.properties.example` com as chaves sem valores, como modelo.

No mesmo commit, incluímos no `.gitignore` os arquivos de configuração do Eclipse (`.classpath`, `.project` e `.settings/`), que são próprios de cada máquina. Na revisão, o commit `02cb8c3` tirou deste relatório os valores do segredo, que a primeira versão tinha copiado do `6572d8a`.

### O segredo continua no histórico

Apagar o arquivo não apaga o segredo, porque o Git guarda cada versão como um snapshot imutável. O commit `6572d8a` continua existindo, e qualquer pessoa com acesso ao repositório recupera a senha com `git show 6572d8a:config/application.properties`. A remoção protege só as versões **futuras**.

A medida principal é revogar ou trocar a senha do banco e o token de API, tratando o segredo como vazado. Se o repositório for público ou compartilhado, também é preciso reescrever o histórico com `git filter-repo` ou BFG e forçar o push, sabendo que isso muda os hashes e obriga todos a clonar de novo.

Não reescrevemos o histórico para apagar o segredo, porque isso apagaria as evidências: a regra 3 do `DESAFIO.md` exige que as correções sejam identificáveis no histórico. Os commits da própria equipe, porém, foram reescritos, como mostra a próxima seção.

A senha de demonstração `12345678` continua no `LoginService` e no README, porque a atividade exige esse acesso. Registramos isso como risco: num sistema real, ela viria de uma variável de ambiente e ficaria guardada como hash, nunca no código.

---

## Integridade do histórico

A primeira versão deste relatório dizia que a equipe não tinha reescrito o histórico. Na revisão de 05/10/2026, encontramos o contrário nos commits da própria equipe, de `1a5d50a` a `3015647`:

| Evidência | O que mostra |
|---|---|
| A pasta `.git/refs/original/` existe (vazia) | O `git filter-branch` cria essa pasta para guardar as refs de antes da reescrita |
| `.git/logs/HEAD` com 0 bytes, mesmo com `core.logAllRefUpdates = true` | O reflog foi expirado (`git reflog expire --expire=now --all`) |
| `git count-objects -v` sem nenhum objeto solto e com um único pack | Coerente com `git gc --prune=now`, que apaga os commits antigos |
| Seis commits têm data de commit `28/09 21:00:00` exata, e o filho `3015647` e o merge `6dd6736` têm `28/09 20:43:59` | Um commit não pode ser mais antigo que o pai, então as datas foram fixadas à mão |

Apagar `refs/original`, expirar o reflog e rodar `gc --prune=now` é a lista de limpeza que a documentação do `git filter-branch` recomenda depois de uma reescrita.

A causa provável está no `.git/config` do repositório recebido, que define `user.name = Laboratorio Git` e `user.email = laboratorio@local`. A configuração local tem prioridade sobre a global, então quem commita sem ajustar o próprio nome aparece como "Laboratorio Git". Depois da reescrita, Guilherme Rubatto aparece como committer em todos os commits da equipe e como autor em todos, menos no cherry-pick `8dfcfaf`, que manteve a autoria de Diego Alves.

A reescrita não mudou o código final. Os arquivos recuperados são idênticos, byte a byte, às versões de referência: `Validador.java` e `LoginService.java` têm o mesmo blob da `v1.0.0-funcional`, o `README.md` tem o mesmo blob da `docs-readme` e o `EntregaService.java` tem o mesmo blob da `feature-cadastro`. O que se perdeu foi a rastreabilidade. Os hashes não são os originais e as datas não mostram quando o trabalho foi feito. O reflog, que provaria o caminho percorrido, foi apagado.

Na revisão, não reescrevemos de novo os commits da equipe, porque isso apagaria ainda mais evidência. As correções entraram como commits novos, com autor e data reais.

---

## Validação final

Rodamos a validação na `main` em 05/10/2026, num macOS com Java 21.0.11 (Corretto) e Maven 3.10.0, e colamos as saídas direto do terminal.

Antes do resgate, o `mvn clean package` na `release-dev` terminava em BUILD FAILURE, com os 7 erros listados no diagnóstico (linhas 6, 14, 15, 16, 17, 19 e 20 do `EntregaService.java`). Depois do resgate, o build passa:

```text
[INFO] Building projeto-entregas 1.0.0
[INFO] Compiling 7 source files with javac [debug target 17] to target/classes
[WARNING] system modules path not set in conjunction with -source 17
[INFO] Building jar: .../target/projeto-entregas-1.0.0.jar
[INFO] BUILD SUCCESS
```

O único aviso vem do `pom.xml`, que define `maven.compiler.source` e `target` como 17 enquanto o build usa o JDK 21. Trocar essas propriedades por `maven.compiler.release` tiraria o aviso. Não alteramos o `pom.xml`, porque o aviso não afeta o resultado e não faz parte das portas da atividade.

A execução com `java -cp target/classes br.edu.entregas.Main` deu este resultado nos quatro cenários:

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

O valor aparece como `4500,00` aqui e como `4500.00` na primeira versão deste relatório porque o `String.format("%.2f")` segue o idioma da máquina que executa o programa.

A integração na `main` aparece no histórico como um merge `--no-ff`, com os commits de cada porta preservados no ramo da direita:

```text
$ git log --oneline --graph -n 9 6dd6736
*   6dd6736 merge: integrar resgate do projeto
|\
| * 3015647 docs: preencher RELATORIO-RESGATE com diagnostico e evidencias
| * 2ceff3a security(porta-5): remover credenciais versionadas em 6572d8a
| * a4e35d6 docs(porta-4): recuperar README completo da branch docs-readme
| * fa3fb7d fix(porta-3): reverter 9a6d3b0 e restaurar validacao do login
| * 8dfcfaf feat: validar status da mercadoria
| * 965135f fix(porta-2): recuperar Validador.java excluido em 64f88f6
| * 1a5d50a fix(porta-1): reverter a70ee84 que quebrou a compilacao do EntregaService
| * 799c5ec chore: adicionar instrucoes do resgate
```

A revisão de 05/10/2026 entrou na `main` da mesma forma, por um segundo merge `--no-ff` ("merge: integrar revisao do resgate").

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

O controle de configuração reduz o risco técnico porque cada alteração fica isolada num commit. Foi isso que nos deixou achar a mudança exata que quebrou a compilação (`a70ee84`) e a que quebrou o login (`9a6d3b0`), e desfazer só essas duas com `git revert`, sem perder o trabalho dos outros.

Ele também facilita a auditoria, porque o histórico registra quem mudou o quê e quando. Com poucos comandos, identificamos o autor de cada problema, inclusive o do segredo versionado. Tags como `v1.0.0-funcional` servem de ponto de referência confiável para comparar versões.

A recuperação funciona porque o que foi commitado não se perde. Recuperamos um arquivo excluído, o README de outra branch e a regra correta do login, tudo a partir do histórico e sem reescrever código do zero.

Nosso próprio caso mostra o custo de reescrever o histórico. A reescrita dos commits da equipe trocou os hashes, deixou datas impossíveis e apagou o reflog, que era a prova do caminho percorrido. Ela foi descoberta com os mesmos comandos que usamos para investigar os commits dos outros autores. Para corrigir sem perder a rastreabilidade, o caminho é um commit novo, seja um revert ou uma correção, como fizemos na revisão.

O trabalho deixou quatro lições para a equipe. Não se trabalha direto na `main`. A mensagem de commit precisa ser honesta, porque um "refactor" que exclui código em uso e um "feat" que quebra a compilação enganam o time. Toda mudança passa por uma branch e por revisão antes do merge. E segredo não entra no repositório, porque apagar depois não resolve.
