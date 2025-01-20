# Sistema de Marcação de Consultas Clínicas

Este projeto foi desenvolvido no âmbito da disciplina de Computação Distribuída e tem como objetivo criar um sistema para gerir a marcação e a gestão de consultas clínicas em diferentes clínicas. Utiliza uma arquitetura distribuída baseada em Web Services (SOAP e REST), integrando FrontEnd e BackEnd com comunicação via RMI.

## Arquitetura do Sistema
O sistema segue uma arquitetura cliente-servidor distribuída e é dividido entre as plataformas Windows, MacOS e Linux. Abaixo estão os principais componentes:

### Servidor FrontEnd
- **Tecnologias Utilizadas:**
  - Apache Tomcat 9
  - JAX-RS (REST) e JAX-WS (SOAP)
- **Responsabilidades:**
  - Gerir interações com os utilizadores, como registo e autenticação.
  - Encaminhar pedidos para o servidor BackEnd.
  - Disponibilizar dois tipos de clientes:
    - Cliente REST: comunica via HTTP e JSON.
    - Cliente SOAP: comunica via XML e WSDL.
  - Armazenar dados dos utilizadores em "utilizadores.txt".

### Servidor BackEnd
- **Tecnologias Utilizadas:**
  - Java RMI
- **Responsabilidades:**
  - Gerir as informações sobre reservas, incluindo criação, cancelamento e listagem de consultas.
  - Interagir com o FrontEnd via RMI.
  - Armazenar dados em "clinicas.txt" e "consultas.txt".

### Base de Dados em Ficheiros
- `utilizadores.txt`: Contém informações de autenticação.
- `clinicas.txt`: Registra as clínicas e suas especialidades.
- `consultas.txt`: Armazena as consultas agendadas pelos utilizadores.

## Funcionalidades Implementadas
- Reservar uma consulta (escolha de especialidade, clínica, médico, data e hora).
- Cancelar uma consulta existente através do ID.
- Listar consultas marcadas por um utilizador autenticado.
- Registar utilizadores no sistema.

## Fluxo de Dados
1. O utilizador interage com o cliente (REST ou SOAP).
2. O cliente envia pedidos ao servidor FrontEnd.
3. O servidor FrontEnd processa os pedidos e comunica com o BackEnd via RMI.
4. O BackEnd executa as operações e retorna os resultados ao FrontEnd, que os apresenta ao cliente.

## Manual de Instalação
### Requisitos
- **Java JDK 17 ou superior**
- **Apache Tomcat 9**
- **Bibliotecas necessárias:**
  - JAX-RS e JAX-WS para os web services.
  - Configuração de CXF para comunicação SOAP e REST.

### Passos para Instalação
1. **Servidor BackEnd:**
   - Compile os ficheiros `ConsultasServer.java`, `ConsultasInterface.java` e `Consultas.java`.
   - Execute o servidor utilizando o comando:
     ```bash
     java ConsultasServer <porta_rmi>
     ```

2. **Servidor FrontEnd:**
   - Configure os web services no Apache Tomcat.
   - Implemente os arquivos relevantes para os clientes REST e SOAP.

3. **Clientes REST e SOAP:**
   - Configure os clientes utilizando os respetivos arquivos (e.g., `ClientRest.java` ou `ConsultasClient.java`).
   - Certifique-se de que o classpath está corretamente configurado para incluir as dependências.

## Exemplos de Uso
### Autenticação de Utilizador
1. O utilizador insere o email e a palavra-passe.
2. O sistema valida as credenciais no `utilizadores.txt` e retorna a confirmação.

### Reserva de Consulta
1. O utilizador escolhe a clínica, especialidade, médico, data e hora.
2. O sistema registra a reserva no `consultas.txt`.

### Listagem de Consultas
1. O utilizador solicita uma listagem das consultas marcadas.
2. O sistema retorna as consultas filtradas pelo ID do utilizador.

## Equipa de Desenvolvimento
- **Daniel Nascimento** (a22208338)
- **Ricardo Gonçalves** (a22208676)
- **Ricardo Piedade** (a22207722)
