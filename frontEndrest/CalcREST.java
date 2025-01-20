import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


@WebService(targetNamespace = "http://default_package/", portName = "CalcRESTPort", serviceName = "CalcRESTService")
@Consumes("application/json")
@Produces("application/json")
public class CalcREST {
 
	
	public String url = "//192.168.1.217:1234/Consultas";
	
	
	@POST
	@Path("/registarUtilizador")
	public String registrarUtilizador(String data) {
	    
	    String[] parts = data.split(",");
	    
	    if (parts.length != 5) {
	        return "Erro: Formato de entrada inválido. Por favor, forneça 'utilizador,email,telefone,utente,senha'.";
	    }

	    String utilizador = parts[0].trim();
	    String email = parts[1].trim();
	    String numTelefone = parts[2].trim();
	    String numUtente = parts[3].trim();
	    String senha = parts[4].trim();

	    if (utilizador == null || utilizador.trim().isEmpty() || 
	    	    email == null || email.trim().isEmpty() || 
	    	    numTelefone == null || numTelefone.trim().isEmpty() || 
	    	    numUtente == null || numUtente.trim().isEmpty() || 
	    	    senha == null || senha.trim().isEmpty()) {
	    	    System.err.println("Erro: Todos os campos (utilizador, email, número de telefone, número de utente e senha) devem ser preenchidos.");
	    	    return "Erro: Todos os campos (utilizador, email, número de telefone, número de utente e senha) devem ser preenchidos.";
	    	}


	    synchronized ("utilizadores.txt") {
	        File file = new File("utilizadores.txt");

	        // Criar o ficheiro se não existir
	        if (!file.exists()) {
	            try {
	                if (!file.createNewFile()) {
	                    System.err.println("Erro: Não foi possível criar o ficheiro 'utilizadores.txt'.");
	                    return "Erro: Não foi possível criar o ficheiro 'utilizadores.txt'.";
	                }
	            } catch (IOException e) {
	                System.err.println("Erro ao criar o ficheiro 'utilizadores.txt': " + e.getMessage());
	                return "Erro: Falha ao criar o ficheiro de utilizadores.";
	            }
	        }

	        try (BufferedReader reader = new BufferedReader(new FileReader(file));
	             BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

	            // Verificar se o utilizador já existe
	            String line;
	            while ((line = reader.readLine()) != null) {
	                if (line.trim().isEmpty() || !line.contains(",")) continue;

	                String[] userParts = line.split(",");
	                if (userParts.length > 1 && userParts[0].equals(utilizador)) {
	                    return "Erro: Utilizador já existe.";
	                }
	            }

	            // Registar novo utilizador
	            writer.write(utilizador + "," + email + "," + numTelefone + "," + numUtente + "," + senha);
	            writer.newLine();
	            return "Sucesso: Utilizador registado com sucesso.";

	        } catch (IOException e) {
	            System.err.println("Erro ao registar o utilizador: " + e.getMessage());
	            return "Erro: Falha ao registar o utilizador devido a um erro interno.";
	        }
	    }
	}




	@POST
	@Path("/autenticarutilizador")
	public String autenticarutilizador(String data) {
	    // Split the input string into utilizador and senha
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 2) {
	        return "Invalid input format. Expected 'utilizador,senha'.";
	    }
	    
	    String email = partes[0];
	    String senha = partes[1];
	    
	    synchronized ("utilizadores.txt") {
	        File file = new File("utilizadores.txt");
	        
	        // Check if the file exists
	        if (!file.exists()) {
	            System.err.println("Erro: O ficheiro 'utilizadores.txt' não foi encontrado.");
	            return "error";
	        }

	        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	            String linha;
	            while ((linha = reader.readLine()) != null) {
	                String[] partesDoArquivo = linha.split(",");
	                // Check if user exists with matching credentials
	                if (partesDoArquivo.length == 5 && partesDoArquivo[1].equals(email) && partesDoArquivo[4].equals(senha)) {
	                    return partesDoArquivo[0]; // Return the utilizador (first part of the line)
	                }
	            }
	            return "error"; // Invalid credentials

	        } catch (IOException e) {
	            System.err.println("Erro ao autenticar: " + e.getMessage());
	            return "error"; // Indicates an error occurred
	        }
	    }
	}




	@POST
	@Path("/listaClinicas")
	public String listaClinicas(String start) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> clinicas = consultasService.getClinicas();

	        if (clinicas.isEmpty()) {
	            return "Erro: Não há clínicas disponíveis.";
	        }

	        StringBuilder clinicasString = new StringBuilder("Clínicas disponíveis:\n");
	        for (int i = 0; i < clinicas.size(); i++) {
	            clinicasString.append((i + 1)).append(". ").append(clinicas.get(i)).append("\n");
	        }
	        return clinicasString.toString();
	    } catch (Exception e) {
	        return "Erro ao listar clínicas: " + e.getMessage();
	    }
	}
	@POST
	@Path("/listaEspecialidades")
	public String listaEspecialidades(String clinica) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> especialidades = consultasService.getEspecialidades(clinica);

	        if (especialidades.isEmpty()) {
	            return "Erro: Não há especialidades disponíveis para esta clínica.";
	        }

	        StringBuilder especialidadesString = new StringBuilder("Especialidades disponíveis na clínica ").append(clinica).append(":\n");
	        for (int i = 0; i < especialidades.size(); i++) {
	            especialidadesString.append((i + 1)).append(". ").append(especialidades.get(i)).append("\n");
	        }
	        return especialidadesString.toString();
	    } catch (Exception e) {
	        return "Erro ao listar especialidades: " + e.getMessage();
	    }
	}
	
	@POST
	@Path("/listaMedico")
	public String listaMedico(String data) {
	    // Split the input string into clinica and especialidade
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 2) {
	        return "Formato de entrada inválido. Esperado 'clinica,especialidade'.";
	    }
	    
	    String clinica = partes[0];
	    String especialidade = partes[1];
	    
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> medicosDisponiveis = consultasService.getMedicos(clinica, especialidade);

	        if (medicosDisponiveis.isEmpty()) {
	            return "Erro: Não há médicos disponíveis para esta especialidade.";
	        }

	        StringBuilder medicosString = new StringBuilder("Médicos disponíveis para ").append(especialidade).append(":\n");
	        for (int i = 0; i < medicosDisponiveis.size(); i++) {
	            medicosString.append((i + 1)).append(". ").append(medicosDisponiveis.get(i)).append("\n");
	        }
	        return medicosString.toString();
	    } catch (Exception e) {
	        return "Erro ao listar médicos: " + e.getMessage();
	    }
	}


	@POST
	@Path("/listaHorario")
	public String listaHorario(String data) {
	    // Split the input string into clinica, especialidade, medico, and data
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 4) {
	        return "Formato de entrada inválido. Esperado 'clinica,especialidade,medico,data'.";
	    }

	    String clinica = partes[0];
	    String especialidade = partes[1];
	    String medico = partes[2];
	    String dataConsulta = partes[3]; // Renamed 'data' to 'dataConsulta' to avoid naming conflict

	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> timeSlots = consultasService.getAvailableHours(clinica, especialidade, medico, dataConsulta);

	        if (timeSlots.isEmpty()) {
	            return "Nenhum horário disponível para a data selecionada.";
	        }

	        StringBuilder horariosString = new StringBuilder("Horários disponíveis:\n");
	        for (String slot : timeSlots) {
	            horariosString.append(slot).append("\n");
	        }
	        return horariosString.toString();
	    } catch (Exception e) {
	        return "Erro ao listar horários: " + e.getMessage();
	    }
	}

	
	@POST
	@Path("/selecionarClinica")
	public String selecionarClinica(String clinicaIndexStr) {
	    try {
	        // Converter o índice para um inteiro
	        int clinicaIndex;
	        try {
	            clinicaIndex = Integer.parseInt(clinicaIndexStr);
	        } catch (NumberFormatException e) {
	            return "Error: Invalid input. Please provide a valid integer index.";
	        }

	        // Chamar o serviço remoto para obter a lista de clínicas
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> clinicas = consultasService.getClinicas();

	        if (clinicas.isEmpty()) {
	            return "Error: No clinics available.";
	        }

	        if (clinicaIndex < 1 || clinicaIndex > clinicas.size()) {
	            return "Error: Index out of range. Please provide a number between 1 and " + clinicas.size() + ".";
	        }

	        // Retorna o nome da clínica selecionada
	        return clinicas.get(clinicaIndex - 1);
	    } catch (Exception e) {
	        return "Error: Failed to retrieve clinic information. " + e.getMessage();
	    }
	}


	@POST
	@Path("/selecionarEspecialidade")
	public String selecionarEspecialidade(String data) {
	    // Split the input string into clinica and especialidadeIndex
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 2) {
	        return "Formato de entrada inválido. Esperado 'clinica,especialidadeIndex'.";
	    }

	    String clinica = partes[0];
	    int especialidadeIndex;
	    
	    // Parse especialidadeIndex to an integer
	    try {
	        especialidadeIndex = Integer.parseInt(partes[1]);
	    } catch (NumberFormatException e) {
	        return "Índice de especialidade inválido.";
	    }

	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> especialidades = consultasService.getEspecialidades(clinica);

	        if (especialidades.isEmpty()) {
	            return "Nenhuma especialidade encontrada para a clínica fornecida.";
	        }

	        if (especialidadeIndex < 1 || especialidadeIndex > especialidades.size()) {
	            return "Índice de especialidade fora do intervalo disponível.";
	        }

	        return especialidades.get(especialidadeIndex - 1);
	    } catch (Exception e) {
	        return "Erro ao selecionar especialidade: " + e.getMessage();
	    }
	}


	@POST
	@Path("/selecionarMedico")
	public String selecionarMedico(String data) {
	    // Split the input string into clinica, especialidade, and medicoIndex
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 3) {
	        return "Formato de entrada inválido. Esperado 'clinica,especialidade,medicoIndex'.";
	    }

	    String clinica = partes[0];
	    String especialidade = partes[1];
	    int medicoIndex;
	    
	    // Parse medicoIndex to an integer
	    try {
	        medicoIndex = Integer.parseInt(partes[2]);
	    } catch (NumberFormatException e) {
	        return "Índice de médico inválido.";
	    }

	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> medicosDisponiveis = consultasService.getMedicos(clinica, especialidade);

	        if (medicosDisponiveis.isEmpty()) {
	            return "Nenhum médico disponível para a especialidade fornecida.";
	        }

	        if (medicoIndex < 1 || medicoIndex > medicosDisponiveis.size()) {
	            return "Índice de médico fora do intervalo disponível.";
	        }

	        return medicosDisponiveis.get(medicoIndex - 1);
	    } catch (Exception e) {
	        return "Erro ao selecionar médico: " + e.getMessage();
	    }
	}


	
	@POST
	@Path("/selecionarHorario")
	public String selecionarHorario(String data) {
	    // Split the input string into clinica, especialidade, medico, data, and selectedTime
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 5) {
	        return "Formato de entrada inválido. Esperado 'clinica,especialidade,medico,data,selectedTime'.";
	    }

	    String clinica = partes[0];
	    String especialidade = partes[1];
	    String medico = partes[2];
	    String dataString = partes[3];
	    String selectedTime = partes[4];

	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> timeSlots = consultasService.getAvailableHours(clinica, especialidade, medico, dataString);

	        // Check if selectedTime is valid
	        LocalTime chosenTime = LocalTime.parse(selectedTime, timeFormatter);
	        if (!timeSlots.contains(selectedTime)) {
	            return "Horário selecionado não está disponível."; // If the time slot is not valid, return a message
	        }

	        // Parse the date string into a LocalDate
	        LocalDate selectedDate = LocalDate.parse(dataString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	        LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, chosenTime);

	        // Format the LocalDateTime to a string and return it
	        return selectedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	    } catch (DateTimeParseException e) {
	        return "Formato de data ou hora inválido."; // In case of invalid date or time format
	    } catch (Exception e) {
	        return "Erro ao selecionar horário: " + e.getMessage(); // Handle other exceptions
	    }
	}



	@POST
	@Path("/reservarConsulta")
	public String reservarConsulta(String data) {
	    // Split the input string into utilizador, dataHoraString, clinica, especialidade, and medico
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 5) {
	        return "Formato de entrada inválido. Esperado 'utilizador,dataHoraString,clinica,especialidade,medico'.";
	    }

	    String utilizador = partes[0];
	    String dataHoraString = partes[1];
	    String clinica = partes[2];
	    String especialidade = partes[3];
	    String medico = partes[4];

	    try {
	        // Convert the received String to LocalDateTime
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	        LocalDateTime dataHora = LocalDateTime.parse(dataHoraString, formatter);

	        // Use the converted LocalDateTime
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        String idConsulta = consultasService.reservarConsulta(utilizador,
	                dataHora.format(formatter), clinica, especialidade, medico);

	        if (idConsulta != null) {
	            System.out.println("Consulta reservada com sucesso! ID: " + idConsulta);
	            return idConsulta;
	        } else {
	            System.out.println("Erro: Já existe uma consulta nesse horário ou os dados são inválidos.");
	            return "erro";
	        }
	    } catch (Exception e) {
	        System.err.println("Erro ao reservar consulta: " + e.getMessage());
	        return "erro";
	    }
	}


	@POST
	@Path("/cancelarConsulta")
	public String cancelarConsulta(String data) {
	    // Split the input string into utilizador and idConsulta
	    String[] partes = data.split(",");
	    
	    // Check if the input string is properly formatted
	    if (partes.length != 2) {
	        System.err.println("Formato de entrada inválido. Esperado 'utilizador,idConsulta'.");
	        return "erro"; // Invalid format
	    }

	    String utilizador = partes[0];
	    String idConsulta = partes[1];

	    try {
	        // Use the provided user and idConsulta to call the service
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);

	        // Try to cancel the consultation
	        if (consultasService.cancelarConsulta(utilizador, idConsulta)) {
	            return "cancelado"; // Successfully cancelled
	        } else {
	            return "erro"; // Cancellation failed (e.g., invalid consulta ID)
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "erro"; // Error occurred during cancellation
	    }
	}


	@POST
	@Path("/listarConsultas")
    public String listarConsultas(String utilizador) {
        try {
            //String url = "//192.168.1.217:1234/Consultas";
            ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
            List<String> consultas = consultasService.listarConsultas(utilizador);
            if (consultas.isEmpty()) {
                return "Você não tem consultas.";
            } else {
                StringBuilder consultasString = new StringBuilder();
                consultasString.append("=====================================\n");
                consultasString.append("              CONSULTAS              \n");
                consultasString.append("=====================================\n");

                for (String consulta : consultas) {
                    String[] partes = consulta.split(",");
                    String id = partes[0];
                    String dataHora = partes[2];
                    String clinica = partes[3];
                    String especialidade = partes[4];
                    String medico = partes[5];

                    LocalDateTime inicio = LocalDateTime.parse(dataHora,
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    LocalDateTime fim = inicio.plusHours(1);
                    String horarioConsulta = "Das " + inicio.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " as " +
                            fim.format(DateTimeFormatter.ofPattern("HH:mm"));

                    consultasString.append("-------------------------------------\n");
                    consultasString.append("| ID da Consulta: ").append(id).append("\n");
                    consultasString.append("| Data: ").append(inicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
                    consultasString.append("| Horário: ").append(horarioConsulta).append("\n");
                    consultasString.append("| Clínica: ").append(clinica).append("\n");
                    consultasString.append("| Especialidade: ").append(especialidade).append("\n");
                    consultasString.append("| Médico: ").append(medico).append("\n");
                    consultasString.append("-------------------------------------\n");
                }
                return consultasString.toString();
            }
        } catch (Exception e) {
            return "Erro ao listar consultas: " + e.getMessage();
        }
    }
}