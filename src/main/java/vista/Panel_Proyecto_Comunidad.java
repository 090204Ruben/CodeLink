package vista;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Room;
import net.miginfocom.swing.MigLayout;

public class Panel_Proyecto_Comunidad extends JPanel implements ActionListener {
	private Room room;

	public Panel_Proyecto_Comunidad(Room room) {
		this.room = room;

		setLayout(new BorderLayout());
		if (room.getAlias().startsWith("P")) {
			JPanel panel = getPanelProyecto();
			add(new JScrollPane(panel));

		} else if (room.getAlias().startsWith("C")) {
			JPanel panel = getPanelComunidad();
			add(new JScrollPane(panel));
		}
	}

	private JPanel getPanelProyecto() {
		JPanel panel = new JPanel(new MigLayout("", "", ""));

		ImageIcon img = room.getIcon();

		JLabel lblIcono = new JLabel();
		lblIcono.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		panel.add(lblIcono, "span 2 3,grow,gapright 5");
		panel.add(new JLabel(" "), "wrap");

		JTextField txtNombre = new JTextField(room.getName(), 10);
		txtNombre.setEnabled(false);
		panel.add(txtNombre, "wrap");

		JTextArea txtPropuesta = new JTextArea(room.getPropuesta(), 3, 5);
		txtPropuesta.setLineWrap(true);
		txtPropuesta.setWrapStyleWord(false);
		txtPropuesta.setEnabled(false);
		panel.add(new JScrollPane(txtPropuesta), "grow,gapbottom 18,wrap");

		panel.add(new JLabel("Inicio: " + room.getCreatedAt(), 10));

		JTextField txtParticipantes = new JTextField("Participantes: " + MatrixManager.getRoomMemberCount(room.getId()),
				10);
		txtParticipantes.setEnabled(false);
		panel.add(txtParticipantes, "split 2,growx,pushx,cell 2 3");

		JTextField txtEstado = new JTextField("Estado: " + room.getEstado(), 10);
		txtEstado.setEnabled(false);
		JScrollPane scroll = new JScrollPane(txtEstado);
		scroll.setBorder(null);
		panel.add(scroll);

		JButton btnAdd = new JButton("ADD");
		panel.add(btnAdd);
		btnAdd.addActionListener(this);

		return panel;
	}

	private JPanel getPanelComunidad() {
		JPanel panel = new JPanel(new MigLayout("", "", ""));

		ImageIcon img = room.getIcon();

		JLabel lblIcono = new JLabel();
		lblIcono.setIcon(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		panel.add(lblIcono, "span 2 3,grow,gapright 5");
		panel.add(new JLabel(" "), "wrap");

		JTextField txtNombre = new JTextField(room.getName(), 10);
		txtNombre.setEnabled(false);
		panel.add(txtNombre, "wrap");

		JTextArea txtPropuesta = new JTextArea(room.getPropuesta(), 3, 5);
		txtPropuesta.setLineWrap(true);
		txtPropuesta.setWrapStyleWord(false);
		txtPropuesta.setEnabled(false);
		panel.add(new JScrollPane(txtPropuesta), "grow,gapbottom 18,wrap");

		panel.add(new JLabel("Inicio: " + room.getCreatedAt(), 10));

		JTextField txtParticipantes = new JTextField("Participantes: " + MatrixManager.getRoomMemberCount(room.getId()),
				10);
		txtParticipantes.setEnabled(false);
		panel.add(txtParticipantes, "split 2,growx,pushx,cell 2 3");

		JButton btnAdd = new JButton("ADD");
		panel.add(btnAdd);
		btnAdd.addActionListener(this);

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (ConexionSQLite.insertRoom(room)) {
			Main.getUserMain().getListaRooms().add(room);

			ButtonUser btnRoom = new ButtonUser(room);
			room.getRoomMembers().add(Main.getUserMain());
			PanelChat panelChat = new PanelChat(room);
			Home.panel.add(panelChat, "Chat " + room.getName());
			
			for (Room chat : Main.getUserMain().getListaRooms()) {
				if (!chat.getId().equals(room.getId())) {
					chat.setVisibility(false);
				}
			}
			Home.layout.show(Home.panel, "Chat " + room.getName());
			room.setVisibility(true);

			Main.getUserMain().getListaRoomsBtns().put(room, btnRoom);
			PanelLeft.panel2.add(btnRoom, "grow,wrap");

			try {
				MatrixManager.joinRoomById(room.getId());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			ConexionSQLite.insertRoomMember(Main.getUserMain().getId(), room.getId());

			MatrixManager.cargarUsuariosRoom(room);

			PanelLeft.panel2.revalidate();
			PanelLeft.panel2.repaint();

			btnRoom.addActionListener(f -> {
				try {
					Room fetched = MatrixManager
							.getRoomByAliasLiteral("#" + room.getAlias() + ":matrixserver1.duckdns.org");
					if (fetched == null) {
						// Sala eliminada en el servidor
						Iterator<Room> it = Main.getUserMain().getListaRooms().iterator();
						while (it.hasNext()) {
							Room r = it.next();
							if (r.getId().equals(room.getId())) {
								ConexionSQLite.borrarRastroRoom(r);
								it.remove();
								break;
							}
						}
						PanelLeft.panel2.remove(Main.getUserMain().getListaRoomsBtns().get(room));
						PanelLeft.panel2.revalidate();
						PanelLeft.panel2.repaint();

						JOptionPane.showMessageDialog(null, "Room eliminada por el creador!");
						return;
					}
				} catch (IOException g) {
					g.printStackTrace();
				}

				// Si la sala existe, abrir el chat
				for (Room chat : Main.getUserMain().getListaRooms()) {
					if (!chat.getId().equals(room.getId())) {
						chat.setVisibility(false);
					}
				}
				room.setVisibility(true);
				ButtonUser btnSala = Main.getUserMain().getListaRoomsBtns().get(room);
				if (btnSala.getText().endsWith("   ·")) {
					btnSala.setText(btnSala.getText().replace("   ·", ""));
				}
				Home.layout.show(Home.panel, "Chat " + room.getName());
			});
		} else {
			JOptionPane.showMessageDialog(null, "Ya perteneces a este grupo");
		}
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

}
