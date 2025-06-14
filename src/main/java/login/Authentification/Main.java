package login.Authentification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;

import conexionBD.ConexionSQLite;
import login.ControladorLogin;
import login.ModeloLogin;
import login.VistaLogin;
import modelo.Usuario;

@SpringBootApplication
public class Main { // Ponemos la clase Main dentro de este paquete y no en el paquete app ya que
					// SprintBot no lo mapea correctamente sino.

	private static Usuario userMain;

	public static void main(String[] args) {
		// Creamos la bbdd y su estructura si no lo esta ya
		ConexionSQLite sqliteStructure = new ConexionSQLite();

		FlatDraculaIJTheme.setup();
		ModeloLogin modelo = new ModeloLogin();
		VistaLogin login = VistaLogin.getInstance();
		SpringApplication.run(Main.class, args);
		ControladorLogin controlador = new ControladorLogin(modelo, login);
		login.setFocusableWindowState(false);
		login.setVisible(true); // Mostrar la ventana sin que se enfoque ningún componente
		login.setFocusableWindowState(true);
	}

	public static void setUserMain(Usuario user) {
		userMain = user;
	}

	public static Usuario getUserMain() {
		return userMain;
	}
}
