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



@WebService(targetNamespace = "http://default_package/", portName = "ServiceUtilityPort", serviceName = "ServiceUtilityService")
public class ServiceUtility {

	public String url = "//192.168.1.217:1234/Consultas";


	public int registarUtilizador(String utilizador, String email, String numTelefone, String numUtente, String senha) {
	    synchronized ("utilizadores.txt") {
	        File file = new File("utilizadores.txt");
	        
	        if (utilizador == null || utilizador.trim().isEmpty() || 
		    	    email == null || email.trim().isEmpty() || 
		    	    numTelefone == null || numTelefone.trim().isEmpty() || 
		    	    numUtente == null || numUtente.trim().isEmpty() || 
		    	    senha == null || senha.trim().isEmpty()) {
		    	    System.err.println("Erro: Todos os campos (utilizador, email, número de telefone, número de utente e senha) devem ser preenchidos.");
		    	    return -1;
		    	}

	        // Se o ficheiro não existe, cria-o
	        if (!file.exists()) {
	            try {
	                boolean created = file.createNewFile();
	                if (!created) {
	                    System.err.println("Erro: Não foi possível criar o ficheiro 'utilizadores.txt'.");
	                    return -2; // Indica que não foi possível criar o ficheiro
	                }
	            } catch (IOException e) {
	                System.err.println("Erro ao criar o ficheiro 'utilizadores.txt': " + e.getMessage());
	                return -2; // Indica que não foi possível criar o ficheiro
	            }
	        }

	        // Continuar com a lógica de leitura e escrita no ficheiro
	        try (BufferedReader reader = new BufferedReader(new FileReader(file));
	             BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

	            // Verificar se o usuário já existe
	            String linha;
	            while ((linha = reader.readLine()) != null) {

	                // Ignorar linhas vazias e linhas sem vírgula
	                if (linha.trim().isEmpty() || !linha.contains(",")) {
	                    continue; // Ignorar linhas malformadas
	                }

	                String[] parts = linha.split(",");
	                if (parts.length == 5 && parts[0].equals(utilizador)) {
	                    return 0; // Usuário já existe
	                }
	            }
	            //ric,ric@gmail.com,123456789,123456789,lol
	            // Registrar novo usuário
	            writer.write(utilizador + "," + email + "," + numTelefone + "," + numUtente + "," + senha);
	            writer.newLine();
	            return 1; // Registro bem-sucedido

	        } catch (IOException e) {
	            System.err.println("Erro ao registrar utilizador: " + e.getMessage());
	            return -1; // Indica que ocorreu um erro
	        }
	    }
	}


	public String autenticarUtilizador(String email, String senha) {
	    synchronized ("utilizadores.txt") {
	        File file = new File("utilizadores.txt");
	        if (!file.exists()) {
	            System.err.println("Erro: O ficheiro 'utilizadores.txt' não foi encontrado.");
	            return null;
	        }

	        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	            String linha;
	            while ((linha = reader.readLine()) != null) {
	                String[] partes = linha.split(",");
	                if (partes[1].equals(email) && partes[4].equals(senha)) {
	                    return partes[0]; // Credenciais válidas
	                }
	            }
	            return ""; // Credenciais inválidas

	        } catch (IOException e) {
	            System.err.println("Erro ao autenticar: " + e.getMessage());
	            return null; // Indica que ocorreu um erro
	        }
	    }
	}




	public String listaClinicas() {
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

	public String listaMedico(String clinica, String especialidade) {
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

	public String listaHorario(String clinica, String especialidade, String medico, String data) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> timeSlots = consultasService.getAvailableHours(clinica, especialidade, medico, data);

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

	public String selecionarClinica(int clinicaIndex) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> clinicas = consultasService.getClinicas();

	        if (clinicas.isEmpty()) {
	            return null;
	        }

	        if (clinicaIndex < 1 || clinicaIndex > clinicas.size()) {
	            return null;
	        }

	        return clinicas.get(clinicaIndex - 1);
	    } catch (Exception e) {
	        return null;
	    }
	}

	public String selecionarEspecialidade(String clinica, int especialidadeIndex) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> especialidades = consultasService.getEspecialidades(clinica);

	        if (especialidades.isEmpty()) {
	            return null;
	        }

	        if (especialidadeIndex < 1 || especialidadeIndex > especialidades.size()) {
	            return null;
	        }

	        return especialidades.get(especialidadeIndex - 1);
	    } catch (Exception e) {
	        return null;
	    }
	}

	public String selecionarMedico(String clinica, String especialidade, int medicoIndex) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> medicosDisponiveis = consultasService.getMedicos(clinica, especialidade);

	        if (medicosDisponiveis.isEmpty()) {
	            return null;
	        }

	        if (medicoIndex < 1 || medicoIndex > medicosDisponiveis.size()) {
	            return null;
	        }

	        return medicosDisponiveis.get(medicoIndex - 1);
	    } catch (Exception e) {
	        return null;
	    }
	}

	
	public String selecionarHorario(String clinica, String especialidade, String medico, String data, String selectedTime) {
	    try {
	        //String url = "//192.168.1.217:1234/Consultas";
	        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	        ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
	        List<String> timeSlots = consultasService.getAvailableHours(clinica, especialidade, medico, data);

	        LocalTime chosenTime = LocalTime.parse(selectedTime, timeFormatter);
	        if (!timeSlots.contains(selectedTime)) {
	            return null; // If the time slot is not valid, return null
	        }

	        LocalDate selectedDate = LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	        LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, chosenTime);

	        // Format the LocalDateTime to a string
	        return selectedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
	    } catch (DateTimeParseException e) {
	        return null; // In case of invalid date or time format
	    } catch (Exception e) {
	        return null; // Handle other exceptions
	    }
	}


    
    public String reservarConsulta(String utilizador, String dataHoraString,
            String clinica, String especialidade, String medico) {
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
                return null;
            }
        } catch (Exception e) {
            System.err.println("Erro ao reservar consulta: " + e.getMessage());
            return null;
        }
    }

    public int cancelarConsulta(String utilizador, String idConsulta) {
        try {
        	//String url = "//192.168.1.217:1234/Consultas";
            ConsultasInterface consultasService = (ConsultasInterface) Naming.lookup(url);
            if (consultasService.cancelarConsulta(utilizador, idConsulta)) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

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
