package vista;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;

public class MensajeFile extends JPanel {
	JPanel contenido;
	ImageIcon img = new ImageIcon(getClass().getResource("/documento1.png"));
	JLabel lblimg, lblLink, lblTamaño;

	public MensajeFile(File file) {

		contenido = new JPanel(new MigLayout());

		lblimg = new JLabel();
		lblimg.setIcon(new ImageIcon(img.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
		contenido.add(lblimg, "span 1 2");

		lblLink = new JLabel(file.getName());
		lblLink.setForeground(new Color(51, 104, 255));
		contenido.add(lblLink, "gapleft 10,wrap");

		long tamaño = file.length() / 1024;
		lblTamaño = new JLabel(String.valueOf(tamaño) + " KB");
		lblTamaño.setForeground(Color.gray);
		contenido.add(lblTamaño, "gapleft 10");

		lblLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblLink.setForeground(new Color(114, 150, 255));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				lblLink.setForeground(new Color(51, 104, 255));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		JScrollPane scroll = new JScrollPane(contenido, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		add(scroll);
	}
}
