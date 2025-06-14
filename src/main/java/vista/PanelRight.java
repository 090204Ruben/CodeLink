package vista;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import login.Authentification.Main;
import modelo.Room;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

public class PanelRight extends JPanel {

	private Usuario user = Main.getUserMain();
	private JPanel panelUsuariosRoom;

	public PanelRight(Room room) {
		setLayout(new MigLayout()); // Permitir que el JScrollPane ocupe todo el espacio

		JPanel panel = new JPanel(new MigLayout("fill, insets 5", "", "")); // <- AQUÍ el cambio

		ImageIcon img = room.getIcon();
		JLabel lblIcono = new JLabel(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));

		JLabel lblName = new JLabel("MIEMBROS SALA");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 15));

		JSeparator separator = new JSeparator();

		panelUsuariosRoom = new JPanel(new MigLayout("wrap,fillx", "2[CENTER]2", ""));
		System.out.println(room.getRoomMembers());
		for (Usuario user : room.getRoomMembers()) {
			ButtonUser btnUser = new ButtonUser(user);
			panelUsuariosRoom.add(btnUser, "growx");
			btnUser.addActionListener(e -> {
				System.out.println(room.getRoomMembers());
				try {
					Desktop.getDesktop().browse(new URI(user.getHtmlURL()));
				} catch (IOException | URISyntaxException e1) {
					System.err.println("Error al abrir el perfil del usuario - " + e1);
				}
			});
		}

		ImageIcon img2 = new ImageIcon(getClass().getResource("/roomData.png"));
		JButton btnDataRoom = new JButton(img2);
		btnDataRoom.setIcon(new ImageIcon(img2.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
		btnDataRoom.setContentAreaFilled(false);
		btnDataRoom.setBorder(null);

		btnDataRoom.addActionListener(e -> {
			DialogoDataRoom dialog = new DialogoDataRoom(room);
			dialog.setVisible(true);
		});

		JPanel panels = new JPanel();
		panels.add(btnDataRoom);

		panel.add(lblIcono, "gaptop 15,align center, wrap");
		panel.add(lblName, "gaptop 10,align center, wrap");
		panel.add(separator, "gaptop 5,growx, h 5!, gapbottom 10, wrap");
		panel.add(new JScrollPane(panelUsuariosRoom), "align center,growy, pushy, span, wrap");

		// ESPACIADOR para empujar hacia abajo
		panel.add(Box.createVerticalGlue(), "growy, pushy, span, wrap");

		// Botón abajo a la derecha
		panel.add(panels, "align right, span, wrap");

		JScrollPane scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		add(scroll, "growy, pushy");
	}

	public PanelRight() {
		setLayout(new MigLayout("fill")); // Permitir que el JScrollPane ocupe todo el espacio

		JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[grow, center]", ""));

		ImageIcon img = new ImageIcon(user.getImageUser());
		JLabel lblIcono = new JLabel(new ImageIcon(img.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));

		JLabel lblName = new JLabel(user.getName());
		lblName.setFont(new Font("Tahoma", Font.BOLD, 15));

		JSeparator separator = new JSeparator();

		JButton btn = new JButton("Ver perfil");
		btn.setFont(new Font("Tahoma", Font.BOLD, 15));
		btn.addActionListener(e -> {
			try {
				Desktop.getDesktop().browse(new URI(user.getHtmlURL()));
			} catch (IOException | URISyntaxException e1) {
				System.err.println("Error al abrir el perfil del usuario - " + e1);
			}
		});

		panel.add(lblIcono, "gaptop 15,align center, wrap");
		panel.add(lblName, "gaptop 10,align center, wrap");
		panel.add(separator, "gaptop 5,growx, h 5!, gapbottom 10, wrap");
		panel.add(btn, "gaptop 5,align center, wrap");

		// CREAR SCROLLPANE
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// Permitir que el JScrollPane crezca pero NO sus componentes internos
		add(scroll, "growy, pushy"); // El ScrollPane se expande verticalmente, pero el panel interno no
	}

	public JPanel getPanelUsuariosRoom() {
		return panelUsuariosRoom;
	}

	public void setPanelUsuariosRoom(JPanel panelUsuariosRoom) {
		this.panelUsuariosRoom = panelUsuariosRoom;
	}
}
