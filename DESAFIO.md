# Git Escape Room: Projeto Entregas
## Cenário
A entrega ao cliente acontece em breve. O sistema não compila, o login apresenta comportamento incorreto, a documentação foi prejudicada e há indícios de informação sensível no histórico. Sua equipe deve recuperar o projeto usando o Git.
## Regras
1. Não apague a pasta `.git`.
2. Não copie um projeto novo por cima deste.
3. Toda correção deve ser identificável no histórico.
4. Trabalhe em uma nova branch com o padrão `resgate/equipe-NOME`.
5. Ao final, faça merge da branch de resgate em `main`.
6. Registre no `RELATORIO-RESGATE.md` os comandos usados e as evidências.
## Portas da sala
- **Porta 1:** descobrir por que o projeto não compila.
- **Porta 2:** recuperar o arquivo excluído.
- **Porta 3:** corrigir o login sem aceitar credenciais inválidas.
- **Porta 4:** recuperar um README realmente útil.
- **Porta 5:** encontrar o commit inadequado e remover o segredo da versão atual.
- **Porta 6:** organizar a entrega final na `main`.
## Critérios de saída
- `mvn clean package` executa sem erro.
- `admin / 12345678` autentica.
- Usuário ou senha incorretos não autenticam.
- O cadastro cria uma mercadoria com um endereço.
- O README ensina a executar o sistema.
- A versão atual não contém credenciais em arquivo de configuração.
- O histórico da equipe evidencia investigação e correção.
> Dica geral: o projeto já funcionou no passado. O histórico e as branches guardam pistas.
