# <p style="text-align: center;">To Do</p>
# sd2425
Projeto SD 24/25

# Funcionalidades Básicas
 - [x] Registo do utilizador.
 - [x] Autenticação do utilizador.

 ## Operações de escrita e leitura simples
 - [x] Operação de escrita com par chave-valor, se a chave não existir é criada nova entrada no servidor com o par enviado. Caso contrário, a entrada deverá ser atualizada com o novo valor.
 - [x] Operação de leitura, para uma chave, deverá devolver ao cliente o valor da chave ou null caso não exista.

 ## Operações de escrita e leitura compostas
 - [x] Atualizar todos os pares chave-valor devem ser atualizados ou inseridos atomicamente.
 - [x] Dado um conjunto de chaves, o servidor devolve o conjunto de pares respetivos.

 ## Limite de utilizadores concorrentes
 - [x] Só podem existir no máximo S sessões concorrentes (diferentes clientes a usar o servidor). Quando atingido, a autenticação de um cliente ficará em espera até sair um cliente.

# Funcionalidade Avançada
 - [ ] O cliente deverá poder ter várias threads concorrentemente a submeter pedidos ao servidor. Um pedido que fique bloqueado no servidor, não pode impedir outros pedidos que o cliente submeta concorrentemente de serem servidos.
 - [ ] Numa leitura condicional. Deverá ser devolvido o valor da chave quando o valor relativo seja igual ao valueCond, devendo a operação ficar bloqueada até isto ser verdade.

# Programa Servidor
 - [x] Deve usar threads e sockets TCP
 - [x] Mantém em memória a informação relevante para suportar funcionalidades.
 - [x] Recebe conexões e input dos clientes
 - [x] Faz chegar informação aos clientes

# Biblioteca do cliente
 - [ ] Deve ser disponibilizada uma biblioteca (conjunto de classes e interfaces) que proporcione o acesso às funcionalidades.
 - [ ] A biblioteca tem que ser independente da interface com o utilizador.
 - [ ] Usa threads e sockets TCP.

# Interface do utilizador
 - [ ] Permite ao utilizador interagir com o serviço através da biblioteca cliente.
 - [ ] Permite usar o serviço de testes.

# Avaliação de desempenho
 - [ ] Testes que inclui cargas com diferentes tipos de operações
 - [ ] Testes de escalabilidade

# Requisitos
 - [x] Para cada cliente, deve haver apenas uma única conexão entre o cliente e servidor.
 - [x] O protocolo de comunicação deverá ser num formato binário, através de código desenvolvido no trabalho, podendo recorrer apenas a Data[Input|Output]Stream.
 - [x] Para o serviço não ficar vulnerável a clientes lentos, não deverá ter threads do servidor a escrever
 em mais do que um socket, devendo as escritas ser feitas por threads associadas a esse socket.
 - [x] Todas as operações devem ser atómicas.

# Relatório
 - [ ] ••.•´¯`•.••   🎀  𝟨 𝓅á𝑔𝒾𝓃𝒶𝓈  🎀   ••.•`¯´•.•• ( ͡ಠ ͜ʖ ͡ಠ)