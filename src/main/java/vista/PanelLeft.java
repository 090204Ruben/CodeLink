package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Room;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

public class PanelLeft extends JPanel {

//	private static PanelLeft main;
	public static JPanel panel1, panel2;
	JTextField txtBusqueda;
	JScrollPane scroll1, scroll2;
	JButton home;
	private Home homeMain;
	private Usuario user = Main.getUserMain();

	public PanelLeft(Home homeMain) {
		this.homeMain = homeMain;

		setLayout(new BorderLayout());

		panel1 = new JPanel(new MigLayout("", "2[grow,center]2", ""));
		try {
			crearInterfazUsuario();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		scroll1 = new JScrollPane(panel1, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll1, BorderLayout.NORTH);

		panel2 = new JPanel(new MigLayout("", "2[grow]2", ""));

		for (Room sala : Main.getUserMain().getListaRooms()) {
			if ("PROYECTO".equals(sala.getType()) || "COMUNIDAD".equals(sala.getType())) {
				ButtonUser btnNewRoom = new ButtonUser(sala);
				user.getListaRoomsBtns().put(sala, btnNewRoom);
				panel2.add(btnNewRoom, "grow,wrap");

				PanelChat panelChat = new PanelChat(sala);
				Home.panel.add(panelChat, "Chat " + sala.getName());

				btnNewRoom.addActionListener(f -> {
					try {
						Room fetched = MatrixManager
								.getRoomByAliasLiteral("#" + sala.getAlias() + ":matrixserver1.duckdns.org");
						if (fetched == null) {
							// Sala eliminada en el servidor
							Iterator<Room> it = Main.getUserMain().getListaRooms().iterator();
							while (it.hasNext()) {
								Room r = it.next();
								if (r.getId().equals(sala.getId())) {
									ConexionSQLite.borrarRastroRoom(r);
									it.remove();
									break;
								}
							}
							panel2.remove(user.getListaRoomsBtns().get(sala));
							panel2.revalidate();
							panel2.repaint();

							JOptionPane.showMessageDialog(null, "Room eliminada por el creador!");
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					// Si la sala existe, abrir el chat
					for (Room chat : Main.getUserMain().getListaRooms()) {
						if (!chat.getId().equals(sala.getId())) {
							chat.setVisibility(false);
						}
					}
					sala.setVisibility(true);
					ButtonUser btnRoom = Main.getUserMain().getListaRoomsBtns().get(sala);
					if (btnRoom.getText().endsWith("   ·")) {
						btnRoom.setText(btnRoom.getText().replace("   ·", ""));
					}
					Home.layout.show(Home.panel, "Chat " + sala.getName());
				});
			}
		}

		for (Room room : user.getListaRooms()) {
			if ("PROYECTO".equals(room.getType()) || "COMUNIDAD".equals(room.getType())) {
				ConexionSQLite.cargarMensajesRoom(room);
			}
		}

		scroll2 = new JScrollPane(panel2, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scroll2, BorderLayout.CENTER);
	}

//	public static PanelLeft getInstance() {
//		if (main==null) {
//			main=new PanelLeft();
//		}
//		return main;
//	}

	private void crearInterfazUsuario() throws MalformedURLException {
		ImageIcon img = new ImageIcon(user.getImageUser());// Cogemos la foto
															// de perfil del
															// usuario de su
															// cuenta de GitHub
															// y se la ponemos
															// como foto de
															// perfil de
															// CodeLink
		JLabel lblIcono = new JLabel();
		lblIcono.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		panel1.add(lblIcono, "gaptop 15,span,wrap");

		JLabel lblNamePosition = new JLabel(user.getName() + " | " + user.getType());
		lblNamePosition.setFont(new Font("Tahoma", Font.BOLD, 15));
		panel1.add(lblNamePosition, "gaptop 8,gapbottom 5,span,wrap");

		JSeparator separator = new JSeparator();
		panel1.add(separator, "growx,h 5!,gapbottom 10,gaptop 5,gapleft 5,gapright 5,span,wrap");

		txtBusqueda = new JTextField(20);
		panel1.add(txtBusqueda, "gapbottom 10,gaptop 5,gapleft 5,gapright 5");

		JButton btnLupa = new JButton(new ImageIcon(getClass().getResource("/informacion.png")));
		btnLupa.setContentAreaFilled(false);
		panel1.add(btnLupa, "grow,wrap");

		btnLupa.addActionListener(e -> {
			ButtonUser btn = null;
			if (txtBusqueda.getText().trim() != null) {// Si el usuario digita algo se busca, sino se muestran todos los
														// rooms

				ArrayList<Room> temp = new ArrayList<>();
				String busqueda = txtBusqueda.getText().toLowerCase().trim();

				for (Room room : user.getListaRooms()) {// Creamos y llenamos un arraylist solo con los rooms con el
														// nombre parecido al que el usuario a buscado
					if (room.getAlias().toLowerCase().contains(busqueda)) {
						temp.add(room);
					}
				}

				panel2.removeAll();

				for (int i = 0; i < temp.size(); i++) {
					btn = user.getListaRoomsBtns().get(temp.get(i));
					panel2.add(btn, "grow,wrap");
				}

			} else {
				panel2.removeAll();
				for (Room room : user.getListaRooms()) {
					btn = user.getListaRoomsBtns().get(room);
					panel2.add(btn, "grow,wrap");
				}
			}
			panel2.revalidate();
			panel2.repaint();
		});

		ImageIcon imgHome = new ImageIcon(getClass().getResource("/house2.png"));
		home = new JButton("Home");
		home.setIcon(new ImageIcon(imgHome.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
		home.setFont(new Font("Tahoma", Font.PLAIN, 20));
		home.setHorizontalAlignment(SwingConstants.LEFT);
		home.setIconTextGap(25);
		panel1.add(home, "grow,span,wrap");
		home.addActionListener(e -> {
			Home.layout.show(Home.panel, "Panel Home");
		});

	}
}
