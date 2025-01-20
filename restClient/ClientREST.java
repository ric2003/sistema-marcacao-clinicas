import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class ClientREST {
	
	

    public static void main(String[] args) {
        
    	Scanner inputScanner = new Scanner(System.in); // Scanner for user input
        
        
        try {
        	boolean autenticado = false;
            String utlilizadorAtual = null;
            String payload = "";
            int opcao = -1;
            while (true) {
                displayMenu(autenticado);
                opcao = -1;
                while (opcao == -1) {
                    System.out.print("Opção: ");
                    try {
                        opcao = Integer.parseInt(inputScanner.nextLine());
                        
                    } catch (NumberFormatException e) {
                        System.err.println("Erro: A opção fornecida não é um número válido. Tente novamente.");
                    }
                }
                
                if (!autenticado) {
                    switch (opcao) {
                        case 1 -> {
                            System.out.print("Utlilizador: ");
                            String utlilizador = inputScanner.nextLine();
                            System.out.print("Email: ");
                            String email = inputScanner.nextLine();
                            System.out.print("Numero de Telefone: ");
                            String numTelefone = inputScanner.nextLine();
                            System.out.print("Numero de Utente de saúde: ");
                            String numUtente = inputScanner.nextLine();
                            System.out.print("Senha: ");
                            String senha = inputScanner.nextLine();
                            
                            payload = utlilizador + "," + email + "," + numTelefone + "," + numUtente + "," + senha;
                            String registo = makePostRequest("http://localhost:8080/cdserverRest/rest/registarUtilizador", "application/json", payload);
                            System.out.println(registo);
                        }case 2 -> {
                        	
                        	System.out.print("Email: ");
                            String email = inputScanner.nextLine();
                            System.out.print("Senha: ");
                            String senha = inputScanner.nextLine();
                            payload = email + "," + senha;
                            String registo = makePostRequest("http://localhost:8080/cdserverRest/rest/autenticarutilizador", "application/json", payload);
    
                            if (!registo.trim().equals("error")) {
                                autenticado = true;
                                utlilizadorAtual = registo;
                                System.out.println("Login bem sucedido! Bem-vindo, " + registo);
                            }
                            
          
                        }
                        default -> System.out.println("Opção inválida.");
                    }
                }else {
                	switch (opcao) {
                    case 3 -> {
                    	boolean exitCase = false;
                        System.out.println("Digite 'menu' a qualquer momento para voltar ao menu principal.");
                        System.out.println("Selecione a clínica:");
                        
                        String lista_clinicas = makePostRequest("http://localhost:8080/cdserverRest/rest/listaClinicas", "application/json", " ");
                        System.out.println(lista_clinicas);
          
                  
                        String clinica = null;
                        while (clinica == null) {
                            System.out.print("Número da clínica: ");
                            String input = inputScanner.nextLine();
                            if (input.equalsIgnoreCase("menu")){
                            	exitCase = true;
                            	break;
                            }
                          
                            clinica = makePostRequest("http://localhost:8080/cdserverRest/rest/selecionarClinica", "application/json", input);
                            System.out.println(clinica);
                            
                        }
                        if (exitCase) break;
                        String lista_especialidades = makePostRequest("http://localhost:8080/cdserverRest/rest/listaEspecialidades", "application/json", clinica);
                        System.out.println(lista_especialidades);
                     
                        String especialidade = null;
                        while (especialidade == null) {
                            System.out.print("Número da especialidade: ");
                            String input = inputScanner.nextLine();
                            if (input.equalsIgnoreCase("menu")) {
                            	exitCase = true;
                            	break;
                            }
                            
                            payload = clinica + "," + input;
                            especialidade = makePostRequest("http://localhost:8080/cdserverRest/rest/selecionarEspecialidade", "application/json", payload);
                          
                        }
                        if (exitCase) break;
                        payload = clinica + "," + especialidade;
                        String lista_medico = makePostRequest("http://localhost:8080/cdserverRest/rest/listaMedico", "application/json", payload);
                      
                        System.out.println(lista_medico);
                        String medico = null;
                        while (medico == null) {
                            System.out.print("Número do médico: ");
                            String input = inputScanner.nextLine();
                            if (input.equalsIgnoreCase("menu")){
                            	exitCase = true;
                            	break;
                            }
                            payload = clinica + "," + especialidade + "," + input;
                            medico = makePostRequest("http://localhost:8080/cdserverRest/rest/selecionarMedico", "application/json", payload);
                            
                        }
                        if (exitCase) break;
                       
                        LocalDate data = null;
                        String lista_horario = null;
                        while (data == null || lista_horario==null) {
                        	System.out.print("Digite a data (dd/mm/yyyy): ");
                            String input = inputScanner.nextLine();
                            if (input.equalsIgnoreCase("menu")){
                            	exitCase = true;
                            	break;
                            }
            
                            try {
                                data = LocalDate.parse(input, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                payload = clinica + "," + especialidade + "," + medico + "," + (data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                lista_horario = makePostRequest("http://localhost:8080/cdserverRest/rest/listaHorario", "application/json", payload);
                                
                                if (lista_horario != null && lista_horario.trim().equals("Nenhum horário disponível para a data selecionada.")) {
                                    System.out.println("Nenhum horário está disponível para a data selecionada.");
                                    lista_horario = null;
                                }
                            } catch (DateTimeParseException e) {
                                System.out.println("Data inválida. Certifique-se de usar o formato 'dd/mm/yyyy' e tente novamente.");
                                data = null; // Reset `data` to null to signal invalid input
                            }

                            
                        }
                        if (exitCase) break;
                        
                        System.out.println(lista_horario);
                        System.out.print("Digite o horário disponível (hh:mm): ");
                        String dataHora = null;
                        while (dataHora == null) {
                            String input = inputScanner.nextLine();
                            if (input.equalsIgnoreCase("menu")){
                            	exitCase = true;
                            	break;
                            }
                            // Call the modified method which now returns a String
                            payload = clinica + "," + especialidade + "," + medico + "," + (data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+ ","+ input;
                            dataHora = makePostRequest("http://localhost:8080/cdserverRest/rest/selecionarHorario", "application/json", payload);
                        
                        }
                        if (exitCase) break;
                        payload = utlilizadorAtual + "," + dataHora + "," + clinica + "," + especialidade + "," + medico;
                        
                        String id_consulta = makePostRequest("http://localhost:8080/cdserverRest/rest/reservarConsulta", "application/json", payload);
                        
                       
                        if (id_consulta.equals("erro")) {
                            System.out.println("Erro: Não foi possível reservar a consulta.");
                        } else {
                            System.out.println("Consulta reservada com sucesso! ID: " + id_consulta);
                        }
    
                        
                    }
                    case 4 -> {
                        System.out.println("(Digite 'AJUDA' para listar as suas consultas)");
                        System.out.print("ID da consulta para cancelamento: ");
                        String idConsulta = inputScanner.nextLine().trim().toUpperCase();

                        if (idConsulta.equals("AJUDA")) {
                        	payload = "";
                            String lista_consultas = makePostRequest("http://localhost:8080/cdserverRest/rest/listarConsultas", "application/json", utlilizadorAtual);
                        	System.out.println(lista_consultas);
                            System.out.print("Agora, insira o ID da consulta para cancelamento: ");
                            idConsulta = inputScanner.nextLine().trim().toUpperCase();
                        }
                        if (idConsulta.equalsIgnoreCase("menu"))
                            break;
                        
                        payload = utlilizadorAtual + "," + idConsulta;
                        String resultado = makePostRequest("http://localhost:8080/cdserverRest/rest/cancelarConsulta", "application/json", payload);
                   
                        if (resultado.equals("cancelado")) {
                            System.out.println("Consulta cancelada com sucesso!");
                        } else {
                            System.out.println("Falha ao cancelar a consulta. Verifique se o ID da consulta está correto.");
                        }

                    }
                    case 5 -> {
                    	String lista_consultas = makePostRequest("http://localhost:8080/cdserverRest/rest/listarConsultas", "application/json", utlilizadorAtual);
                    	System.out.println(lista_consultas);
                    	
                    }
                    case 6 -> {
                        System.out.println("Sessão encerrada.");
                        autenticado = false;
                        utlilizadorAtual = null;
                    }
                    default -> System.out.println("Opção inválida.");
                    
                	}
                }

          }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputScanner.close(); // Close the input scanner
        }
    }

    
    public static String makePostRequest(String urlString, String contentType, String payload) throws IOException {
        HttpURLConnection conn = null;
        try {
            // Prepare the URL and connection
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", contentType);

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(payload.getBytes());
            os.flush();

            // Read the response
            Scanner responseScanner;
            String response;
            if (conn.getResponseCode() != 200) {
                responseScanner = new Scanner(conn.getErrorStream());
                response = "";//Error From Server \n\n
            } else {
                responseScanner = new Scanner(conn.getInputStream());
                response = "";//Response From Server \n\n
            }
            responseScanner.useDelimiter("\\Z");
            response += responseScanner.next();
            responseScanner.close();

            return response;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL: " + urlString, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    public static void displayMenu(boolean autenticado) {
        System.out.println("\nEscolha uma opção:");
        if (!autenticado) {
            System.out.println("1. Registrar Utlilizador");
            System.out.println("2. Autenticar");
        } else {
            System.out.println("3. Reservar consulta");
            System.out.println("4. Cancelar consulta");
            System.out.println("5. Listar consultas");
            System.out.println("6. Sair");
        }
    }
    
    
}

/**
 * Makes a POST request to a given URL with the specified content type and payload.
 *
 * @param urlString    The URL of the endpoint.
 * @param contentType  The content type of the request (e.g., "application/json").
 * @param payload      The data to send in the request body.
 * @return The server's response as a string.
 * @throws IOException If an I/O error occurs.
 */
