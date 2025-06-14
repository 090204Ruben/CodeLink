package vista;

import java.awt.Font;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Room;
import net.miginfocom.swing.MigLayout;

public class PanelChat extends JPanel {

	// Atributos
	final Font fuenteTxt = new Font("Arial", Font.PLAIN, 20);
	String name;
	JButton btnAddFile;
	JTextArea txtInsercio;
	ImageIcon img2;
	public JPanel panelChat;
	JPanel panelInferior;
	JScrollPane scrollChat;
	public Room room;
	public PanelRight panelRight;

	public PanelChat(Room room) {
		this.name = room.getName();
		this.room = room;

		// Configurar layout del panel principal
		setLayout(new MigLayout("wrap", "0[grow]0", "0[]0[grow]0[]0"));

		panelRight = new PanelRight(room);
		add(panelRight, "growy,dock east");

		// Añadir panel de información arriba
		add(new PanelSuperior(name, room), "growx,push, hmin 60");

		// Añadir el panel de chat al centro
		scrollChat = getPanelChat();
		add(scrollChat, "grow, push");

		panelInferior = getPanelSendMessage();
		add(panelInferior, "growx, hmin 60");


	}

	private JPanel getPanelSendMessage() {
		JPanel panelInferior = new JPanel(new MigLayout("fill", "0[]0", ""));

//		ImageIcon imgAddFile = new ImageIcon(getClass().getResource("/añadirArchivo.png"));
//		btnAddFile = new JButton();
//		btnAddFile.setBorder(null);
//		btnAddFile.setContentAreaFilled(false);
//		btnAddFile.setIcon(new ImageIcon(imgAddFile.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
//		panelInferior.add(btnAddFile, "split 2");

//		btnAddFile.addActionListener(e -> {
//			JFileChooser exploradorArchivos = new JFileChooser();
//			exploradorArchivos.showOpenDialog(null);
//
//			File fichero = exploradorArchivos.getSelectedFile();
//
//			if (fichero.getAbsolutePath().endsWith(".png") || fichero.getAbsolutePath().endsWith(".jpg")
//					|| fichero.getAbsolutePath().endsWith(".ico")
//					|| fichero.getAbsolutePath().endsWith(".gif") && fichero.isFile()) {
//
//				img2 = new ImageIcon(fichero.getAbsolutePath());
//				System.out.println(fichero.getAbsolutePath());
//
//				Mensaje msj = new Mensaje(null, img2, null, room.getId(), true);
//				panelChat.add(msj);
//
//				revalidate();
//				repaint();
//			} else {
//				if (fichero.isFile()) {
//					Mensaje msj = new Mensaje(null, null, fichero, room.getId(), true);
//					panelChat.add(msj);
//				}
//			}
//			revalidate();
//			repaint();
//		});

		txtInsercio = new JTextArea(3, 30);
		txtInsercio.setFont(new Font("Arial", Font.PLAIN, 18));

		ImageIcon imgSend = new ImageIcon(getClass().getResource("/send.png"));
		JButton btnSend = new JButton(imgSend);
		btnSend.addActionListener(e -> {
			if (!txtInsercio.getText().isEmpty()) {
				Mensaje msj = null;
				try {
					String idMsj = MatrixManager.sendMessageToRoom(room.getId(), txtInsercio.getText().trim(),
							Main.getUserMain().getAccessToken());
					msj = new Mensaje(idMsj, txtInsercio.getText().trim(), null, null, room.getId(), true);
					ConexionSQLite.insertMensaje(msj);
				} catch (IOException e1) {
					System.err.println("El mensaje no se a podido enviar al room " + room.getId() + " de Matrix.org");
					e1.printStackTrace();
				}
				panelChat.add(msj);

				txtInsercio.setText("");
				panelChat.revalidate();
				panelChat.repaint();

				// Aquí forzamos el scroll al final:
				SwingUtilities.invokeLater(() -> {
					JScrollBar bar = scrollChat.getVerticalScrollBar();
					bar.setValue(bar.getMaximum());
				});
			}
		});

//		panelInferior.add(btnAddFile, "align left,gapleft 5");
		panelInferior.add(txtInsercio, "h 30,grow,push,gapleft 5");
		panelInferior.add(btnSend, "h 30,align right button");

		return panelInferior;
	}

	private JScrollPane getPanelChat() {
		// Crear panel contenedor
		panelChat = new JPanel(new MigLayout("wrap", "0[grow]0", ""));

		// Añadir scroll al panel de chat
		JScrollPane scroll = new JScrollPane(panelChat, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		return scroll;
	}

}
