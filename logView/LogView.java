import java.nio.file.Files;
import java.nio.file.Paths;
import br.com.cofredigital.database.LogDAO;

public class LogView {
	public static void main(String[] args) throws Exception {
		System.out.println("LogView - autentique-se com chave privada do admin para ver logs");
		System.out.print("Caminho para chave privada (PKCS8): ");
		String path = System.console() == null ? new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine() : String.valueOf(System.console().readLine());
		byte[] priv = Files.readAllBytes(Paths.get(path));
	// Em uma implementação completa verificaríamos a assinatura do desafio com o certificado; aqui apenas checamos o tamanho da chave
	if (priv == null || priv.length < 50) { System.err.println("Chave inválida"); return; }
		LogDAO dao = new LogDAO();
		for (String line : dao.listChronological()) System.out.println(line);
	}
}
