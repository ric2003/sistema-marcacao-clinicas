import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConsultasServer {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Uso: java ConsultasServer <porta_rmi>");
			System.exit(1);
		}

		try {

			System.setProperty("java.rmi.server.hostname", "192.168.1.217");

			// Adicionar permissões de segurança se necessário
			System.out.println("Endereço IP configurado para RMI: " + System.getProperty("java.rmi.server.hostname"));

			int porta = Integer.parseInt(args[0]);
			System.out.println("Registro RMI na porta: " + porta);
			// Iniciar o registro RMI na porta especificada
			try {
				LocateRegistry.createRegistry(porta);
				System.out.println("Registro RMI iniciado na porta " + porta);
			} catch (RemoteException e) {
				System.out.println("Registro RMI já está em execução.");
			}

			// Obter o IP da máquina local automaticamente
			// String ipServidor = InetAddress.getLocalHost().getHostAddress();
			// String ipServidor="192.168.1.253";
			// String url = "//" + ipServidor + ":" + porta + "/Consultas";
			String url = "//192.168.1.217:" + porta + "/Consultas";
			Consultas consultasService = new Consultas();
			Naming.rebind(url, consultasService);
			System.out.println("Servidor de Consultas está ativo em: " + url);
		} catch (Exception e) {
			System.err.println("Erro no servidor: " + e.getMessage());
			e.printStackTrace();
		}
	}
}