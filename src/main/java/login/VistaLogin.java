package login;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

public class VistaLogin extends JDialog {

	private final JPanel panel = new JPanel();
	protected JTextField textField;
	protected JPasswordField passwordField;
	protected JToggleButton tglbtnVista;
	protected ImageIcon visible, notVisible;
	protected char echoChar;
	protected JLabel lblRegistro;
	private static VistaLogin login;
	protected JButton btnInicioConGit, btnEntrar;

	private VistaLogin() {

		setSize(320,450);
		setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		getContentPane().setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout());

		JPanel panel_1 = new JPanel(new MigLayout("wrap","",""));
		panel_1.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));

//		visible = new ImageIcon(getClass().getResource("/imgsLogin/visible.png"));
//		notVisible = new ImageIcon(getClass().getResource("/imgsLogin/notVisible.png"));
//
//		tglbtnVista = new JToggleButton("");
//		tglbtnVista.setFocusPainted(false);
//		tglbtnVista.setBorderPainted(false);
//		tglbtnVista.setBounds(282, 127, 38, 38);
//		tglbtnVista.setIcon(new ImageIcon(visible.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
//		panel_1.add(tglbtnVista);

//		JButton btnIniciarSesion = new JButton("Iniciar sesión con GitHub");
//		btnIniciarSesion.setBounds(10, 343, 210, 31);
//		panel_1.add(btnIniciarSesion);
//		btnIniciarSesion.setFont(new Font("Bahnschrift", Font.PLAIN, 15));
//
//		JLabel lblRegistrarse = new JLabel("¿Aun no te has registrado?");
//		lblRegistrarse.setBounds(258, 360, 129, 14);
//		panel_1.add(lblRegistrarse);
//
//		textField = new JTextField("Inserte su gmail");
//		textField.setBounds(29, 63, 291, 38);
//		panel_1.add(textField);
//		textField.setColumns(10);
//
//		passwordField = new JPasswordField("Inserte su contraseña");
//		passwordField.setBounds(29, 127, 291, 38);
//
//		echoChar = passwordField.getEchoChar();
//		panel_1.add(passwordField);

		btnInicioConGit = new JButton("Iniciar con GitHub");
		btnInicioConGit.setIcon(new ImageIcon(getClass().getResource("/imgsLogin/GitHubLogo.png")));
		btnInicioConGit.setFont(new Font("Tahoma", Font.PLAIN, 20));
		btnInicioConGit.setHorizontalAlignment(JButton.CENTER);
		btnInicioConGit.setIconTextGap(10);
		panel_1.add(btnInicioConGit,"growx,pushx,gaptop 10,gapbottom 5");

		lblRegistro = new JLabel("¿Aun no estas registrado?");
		lblRegistro.setHorizontalAlignment(JLabel.CENTER);
		lblRegistro.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel_1.add(lblRegistro,"growx");

//		btnEntrar = new JButton("Entrar");
//		btnEntrar.setFont(new Font("Tahoma", Font.BOLD, 15));
//		btnEntrar.setBounds(29, 210, 291, 54);
//		panel_1.add(btnEntrar);

		ImageIcon imgLogo = new ImageIcon(getClass().getResource("/imgsLogin/logo3.png"));
		JLabel lblLogo = new JLabel("",JLabel.CENTER);
		lblLogo.setIcon(new ImageIcon(
				imgLogo.getImage().getScaledInstance(171, 110, Image.SCALE_SMOOTH)));
		panel.add(lblLogo,BorderLayout.NORTH);

//		if (passwordField.getText().equals("Inserte su contraseña")) {
//			passwordField.setEchoChar((char) 0);
//		}
		panel.add(panel_1,BorderLayout.CENTER);
		 setFocusableWindowState(false); // Usamos este metodo para que no seleccione
		// ningun campo por predeterminado,
		// ponemos este metodo al final(culpa de FlatLaf) para que se aplique
		 
	}

	public static VistaLogin getInstance() {
		if (login == null) {
			login = new VistaLogin();
		}
		return login;
	}
}
