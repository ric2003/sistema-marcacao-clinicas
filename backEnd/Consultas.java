import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Consultas extends UnicastRemoteObject implements ConsultasInterface {

	private static final String CONSULTAS_FILE = "consultas.txt";

	protected Consultas() throws RemoteException {
		super();
		// Garantir que os arquivos existam
		criarArquivoSeNaoExistir(CONSULTAS_FILE);
	}

	private void criarArquivoSeNaoExistir(String fileName) {
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile(); // Cria o arquivo vazio
			}
		} catch (IOException e) {
			System.err.println("Erro ao criar o arquivo " + fileName + ": " + e.getMessage());
		}
	}

	@Override
	public List<String> getClinicas() throws RemoteException {
		Set<String> clinicasSet = new HashSet<>(); // Use a Set to automatically remove duplicates
		try {
			List<String> clinicasInfo = Files.readAllLines(Paths.get("clinicas.txt"));
			for (String linha : clinicasInfo) {
				String[] partes = linha.split(",");
				if (partes.length >= 3) {
					clinicasSet.add(partes[0].trim()); // Add clinic name to the Set
				}
			}
		} catch (IOException e) {
			throw new RemoteException("Erro ao ler o arquivo de clínicas: " + e.getMessage());
		}

		// Convert the Set back to a List
		List<String> clinicasList = new ArrayList<>(clinicasSet);

		// Sort the list alphabetically
		Collections.sort(clinicasList);

		return clinicasList;
	}

	@Override
	public List<String> getEspecialidades(String clinica) throws RemoteException {
		List<String> especialidades = new ArrayList<>();

		try {
			// Read the "clinicas.txt" file
			List<String> clinicasInfo = Files.readAllLines(Paths.get("clinicas.txt"));

			// Iterate through the file to find the specialties of the selected clinic
			for (String linha : clinicasInfo) {
				String[] partes = linha.split(",");
				if (partes.length >= 3) {
					String nomeClinica = partes[0].trim();
					String especialidade = partes[1].trim();

					// If the clinic matches, add the specialty to the list
					if (nomeClinica.equalsIgnoreCase(clinica) && !especialidades.contains(especialidade)) {
						especialidades.add(especialidade);
					}
				}
			}
		} catch (IOException e) {
			throw new RemoteException("Erro ao ler o arquivo de clínicas: " + e.getMessage());
		}

		// Sort the specialties alphabetically
		Collections.sort(especialidades);

		return especialidades;
	}

	@Override
	public List<String> getMedicos(String clinica, String especialidade) throws RemoteException {
		List<String> medicos = new ArrayList<>();

		try {
			// Read the "clinicas.txt" file
			List<String> clinicasInfo = Files.readAllLines(Paths.get("clinicas.txt"));

			// Iterate through the file to find doctors for the selected clinic and
			// specialty
			for (String linha : clinicasInfo) {
				String[] partes = linha.split(",");
				if (partes.length >= 3) {
					String nomeClinica = partes[0].trim();
					String nomeEspecialidade = partes[1].trim();
					String[] listaMedicos = partes[2].split(";");

					// If the clinic and specialty match, add the doctors to the list
					if (nomeClinica.equalsIgnoreCase(clinica) && nomeEspecialidade.equalsIgnoreCase(especialidade)) {
						for (String medico : listaMedicos) {
							medicos.add(medico.trim());
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RemoteException("Erro ao ler o arquivo de clínicas: " + e.getMessage());
		}

		// Sort the doctors alphabetically
		Collections.sort(medicos);

		return medicos;
	}

	@Override
	public List<String> getAvailableHours(String clinica, String especialidade, String medico, String date)
			throws RemoteException {
		List<String> availableHours = new ArrayList<>();
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		try {
			// Read consultations from the file
			List<String> consultasInfo = Files.readAllLines(Paths.get("consultas.txt"));

			// Initialize the available time range from 08:00 to 20:00
			LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
			LocalDateTime start = LocalDateTime.of(parsedDate, LocalTime.of(8, 0));
			LocalDateTime end = LocalDateTime.of(parsedDate, LocalTime.of(19, 0));
			Set<LocalDateTime> takenSlots = new HashSet<>();

			// Get the current time for comparison
			LocalDateTime now = LocalDateTime.now();

			// Check for already booked slots for the given clinic, specialty, and doctor
			for (String linha : consultasInfo) {
				String[] partes = linha.split(",");
				if (partes.length >= 6) {
					String nomeClinica = partes[3].trim();
					String nomeEspecialidade = partes[4].trim();
					String nomeMedico = partes[5].trim();
					String dataHoraConsulta = partes[2].trim();
					LocalDateTime dataHora = LocalDateTime.parse(dataHoraConsulta,
							DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

					// If the clinic, specialty, and doctor match, mark the slot as taken
					if (nomeClinica.equalsIgnoreCase(clinica) &&
							nomeEspecialidade.equalsIgnoreCase(especialidade) &&
							nomeMedico.equalsIgnoreCase(medico)) {
						takenSlots.add(dataHora);
					}
				}
			}

			// Generate all time slots from 08:00 to 20:00 and check if they're taken
			for (LocalDateTime timeSlot = start; !timeSlot.isAfter(end); timeSlot = timeSlot.plusHours(1)) {
				// Only add future slots or slots on the current date/time
				if (!takenSlots.contains(timeSlot)
						&& (parsedDate.isAfter(now.toLocalDate()) || timeSlot.isAfter(now))) {
					// Add only the time (HH:mm) to the available hours list
					availableHours.add(timeSlot.toLocalTime().format(timeFormatter));
				}
			}
		} catch (Exception e) {
			throw new RemoteException("Erro ao verificar horas disponíveis: " + e.getMessage());
		}

		return availableHours;
	}

	@Override
	public String reservarConsulta(String usuario, String dataHora, String clinica, String especialidade, String medico)
			throws RemoteException {
		synchronized (CONSULTAS_FILE) {
			try {
				// Ler o arquivo de clínicas
				List<String> clinicasInfo = Files.readAllLines(Paths.get("clinicas.txt"));
				boolean validado = false;
				String nomeClinica = "";
				String nomeEspecialidade = "";

				// Validar se clínica, especialidade e médico são válidos
				for (String linha : clinicasInfo) {
					String[] partes = linha.split(",");
					if (partes.length >= 3) {
						nomeClinica = partes[0].trim();
						nomeEspecialidade = partes[1].trim();
						String[] medicosDisponiveis = partes[2].split(";");

						if (nomeClinica.equalsIgnoreCase(clinica) &&
								nomeEspecialidade.equalsIgnoreCase(especialidade) &&
								Arrays.stream(medicosDisponiveis).anyMatch(m -> m.trim().equalsIgnoreCase(medico))) {
							validado = true;
							break;
						}
					}
				}

				if (!validado) {
					throw new RemoteException("Dados inválidos: clínica, especialidade ou médico não encontrados.");
				}

				// Gerar ID único para a consulta
				String id = "C" + (int) (Math.random() * 10000);

				// Abrir o arquivo de consultas e verificar se o ID já existe
				List<String> consultasInfo = Files.readAllLines(Paths.get(CONSULTAS_FILE));
				for (String consulta : consultasInfo) {
					String[] partesConsulta = consulta.split(",");
					if (partesConsulta.length >= 6) {
						String idConsulta = partesConsulta[0].trim();

						// Verificar se o ID gerado já existe
						if (idConsulta.equals(id)) {
							// Se o ID já existe, gerar um novo ID e verificar novamente
							id = "C" + (int) (Math.random() * 10000);
							// Reiniciar a verificação com o novo ID
							consultasInfo = Files.readAllLines(Paths.get(CONSULTAS_FILE)); // Recarregar as consultas
							break;
						}
					}
				}

				// Verificar se já existe uma consulta para o mesmo médico, especialidade,
				// clínica e data/hora
				for (String consulta : consultasInfo) {
					String[] partesConsulta = consulta.split(",");
					if (partesConsulta.length >= 6) {
						String usuarioConsulta = partesConsulta[1].trim();
						String dataHoraConsulta = partesConsulta[2].trim();
						String clinicaConsulta = partesConsulta[3].trim();
						String especialidadeConsulta = partesConsulta[4].trim();
						String medicoConsulta = partesConsulta[5].trim();

						// Verificar se já existe uma consulta para o mesmo médico, especialidade,
						// clínica e data/hora
						if (clinicaConsulta.equalsIgnoreCase(clinica) &&
								especialidadeConsulta.equalsIgnoreCase(especialidade) &&
								medicoConsulta.equalsIgnoreCase(medico) &&
								dataHoraConsulta.equals(dataHora)) {
							throw new RemoteException(
									"Erro: Já existe uma consulta agendada para este médico, especialidade, clínica e data/hora.");
						}
					}
				}

				// Montar a linha para adicionar ao arquivo consultas.txt
				String consulta = String.format("%s,%s,%s,%s,%s,%s", id, usuario, dataHora, nomeClinica,
						nomeEspecialidade, medico);

				// Escrever a consulta no arquivo consultas.txt
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONSULTAS_FILE, true))) {
					writer.write(consulta);
					writer.newLine();
				}

				return id; // Retornar o ID gerado
			} catch (IOException e) {
				throw new RemoteException("Erro ao reservar consulta: " + e.getMessage());
			}
		}
	}

	@Override
	public boolean cancelarConsulta(String usuario, String idConsulta) throws RemoteException {
		synchronized (CONSULTAS_FILE) {
			try {
				List<String> consultas = new ArrayList<>();
				boolean consultaEncontrada = false;

				// Ler todas as consultas
				try (BufferedReader reader = new BufferedReader(new FileReader(CONSULTAS_FILE))) {
					String linha;
					while ((linha = reader.readLine()) != null) {
						String[] partes = linha.split(",");
						if (partes[0].equals(idConsulta) && partes[1].equals(usuario)) { // Verifica ID e usuário
							consultaEncontrada = true; // Encontrou a consulta para remover
						} else {
							consultas.add(linha); // Mantém as outras consultas
						}
					}
				}

				// Reescrever o arquivo sem a consulta cancelada
				if (consultaEncontrada) {
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONSULTAS_FILE))) {
						for (String consulta : consultas) {
							writer.write(consulta);
							writer.newLine();
						}
					}
				}

				return consultaEncontrada;
			} catch (IOException e) {
				throw new RemoteException("Erro ao cancelar consulta: " + e.getMessage());
			}
		}
	}

	@Override
	public List<String> listarConsultas(String usuario) throws RemoteException {
		synchronized (CONSULTAS_FILE) {
			try (BufferedReader reader = new BufferedReader(new FileReader(CONSULTAS_FILE))) {
				return reader.lines()
						.filter(linha -> linha.split(",")[1].equals(usuario)) // Verifica a coluna de usuário
						.sorted(Comparator.comparing(linha -> {
							String dataHora = linha.split(",")[2]; // Assume que a coluna 3 é Data/Hora
							return LocalDateTime.parse(dataHora, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
						}))
						.collect(Collectors.toList());
			} catch (IOException e) {
				throw new RemoteException("Erro ao listar consultas: " + e.getMessage());
			} catch (DateTimeParseException e) {
				throw new RemoteException("Erro ao ordenar consultas por Data/Hora: " + e.getMessage());
			}
		}
	}

}