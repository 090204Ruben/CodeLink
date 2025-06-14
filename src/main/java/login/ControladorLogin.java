package login;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import modelo.Usuario;

public class ControladorLogin implements ActionListener, MouseListener {
	private ModeloLogin modelo;
	private VistaLogin vista;
	private JPasswordField passwordField;
	private JTextField txtUser;
	private Usuario usuario;

	public ControladorLogin(ModeloLogin modelo, VistaLogin vista) {
		this.modelo = modelo;
		this.vista = vista;
		this.txtUser = vista.textField;
		this.passwordField = vista.passwordField;

//		vista.tglbtnVista.addActionListener(this);
		vista.lblRegistro.addMouseListener(this);
		vista.btnInicioConGit.addActionListener(this);
//		vista.btnEntrar.addActionListener(this);
//		vista.textField.addFocusListener(new FocusListener() {
//
//			@Override
//			public void focusLost(FocusEvent e) {
//				if (vista.textField.getText().equals("")) {
//					vista.textField.setText("Inserte su gmail");
//				}
//			}
//
//			@Override
//			public void focusGained(FocusEvent e) {
//				if (vista.textField.getText().equals("Inserte su gmail")) {
//					vista.textField.setText("");
//				}
//			}
//		});

//		vista.passwordField.addFocusListener(new FocusListener() {
//
//			@Override
//			public void focusLost(FocusEvent e) {
//				if (vista.passwordField.getText().trim().equals("")) {
//					vista.passwordField.setText("Inserte su contraseña");
//					vista.passwordField.setEchoChar((char) 0);
//				}
//			}
//
//			@Override
//			public void focusGained(FocusEvent e) {
//				if (vista.passwordField.getText().equals("Inserte su contraseña")) {
//					vista.passwordField.setText("");
//					vista.passwordField.setEchoChar(vista.echoChar);
//				}
//			}
//		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (e.getActionCommand().equals(vista.tglbtnVista.getActionCommand())) {
//			if (vista.tglbtnVista.isSelected()) {
//				vista.tglbtnVista.setIcon(
//						new ImageIcon(vista.notVisible.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
//				vista.passwordField.setEchoChar((char) 0);
//			} else {
//				vista.tglbtnVista
//						.setIcon(new ImageIcon(vista.visible.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
//				vista.passwordField.setEchoChar(vista.echoChar);
//			}
//		}
		if (e.getActionCommand().equals(vista.btnInicioConGit.getActionCommand())) {
			try {
				Desktop.getDesktop().browse(new URI("http://localhost:8080/secured"));
//				usuario = new Usuario();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		} else {
//			JOptionPane.showMessageDialog(null, "INICIE SESIÓN CON GITHUB!!");
//			if (txtUser.getText().trim() != null && passwordField.getText().trim()!= null) {
//				try {
//					MatrixManager.registerUser(txtUser.getText().trim(),passwordField.getText().trim(), false);
//				
//					vista.dispose();
//					VistaApp inicio = VistaApp.getInstance();
////					inicio.setFocusableWindowState(false);
//					inicio.setVisible(true);
////					inicio.setFocusableWindowState(true);
//				} catch (IOException e1) {
//					JOptionPane.showMessageDialog(null, "Introduzca correctamente el usuario/contraseña");
//					System.out.println("Usuario :"+txtUser.getText());
//					System.out.println("Contraseña :"+passwordField.getText());
//					e1.printStackTrace();
//				}
//			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			Desktop.getDesktop().browse(new URI(
					"https://github.com/signup?return_to=https%3A%2F%2Fgithub.com%2FJFormDesigner%2FFlatLaf&source=login"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		vista.lblRegistro.setForeground(Color.black);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		vista.lblRegistro.setForeground(Color.white);
	}
}
