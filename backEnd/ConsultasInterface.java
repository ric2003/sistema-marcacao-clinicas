import java.rmi.*;
import java.util.List;

public interface ConsultasInterface extends Remote {

	List<String> getClinicas() throws RemoteException;

	List<String> getEspecialidades(String clinica) throws RemoteException;

	List<String> getMedicos(String clinica, String especialidade) throws RemoteException;

	List<String> getAvailableHours(String clinica, String especialidade, String medico, String date)
			throws RemoteException;

	String reservarConsulta(String usuario, String dataHora, String clinica, String especialidade, String medico)
			throws RemoteException;

	boolean cancelarConsulta(String usuario, String idConsulta) throws RemoteException;

	List<String> listarConsultas(String usuario) throws RemoteException;
}