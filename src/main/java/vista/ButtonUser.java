package vista;

import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import modelo.Room;
import modelo.Usuario;

public class ButtonUser extends JButton {
	public ButtonUser(Room room) {
		ImageIcon img;
		if (room.getImage() != null) {// Para grupos creados en ese momento
//			img = ImagenConMarcoRedondo.crearImagenConMarcoRedondo(room.getImage().toString());
			img = room.getImage();
		} else {// Para grupos que se cargan desde la bbdd
			if (room.getIcon() == null) {
				room.setIcon(room.getMxcUrl());
			}
//			img = ImagenConMarcoRedondo.crearImagenConMarcoRedondo(room.getIcon().toString());
			img = room.getIcon();
		}
		setText(room.getName());
//		setContentAreaFilled(false);
		setIcon(new ImageIcon(img.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
		setFont(new Font("Tahoma", Font.PLAIN, 20));
		setHorizontalAlignment(SwingConstants.LEFT);
		setIconTextGap(25);

	}

	public ButtonUser(Usuario user) {
		ImageIcon img;
//		if (user.getId().equals(Main.getUserMain().getId())) {
//			user.setImageUser(user.getMxcUrl());
//			img = new ImageIcon(user.getImageUser());
//		} else {
		if (user.getIcon() == null) {
			user.setIcon(user.getMxcUrl());
		}
		img = user.getIcon();
//		}

		setText(user.getName());
//		setContentAreaFilled(false);
		setIcon(new ImageIcon(img.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
		setFont(new Font("Tahoma", Font.PLAIN, 20));
		setHorizontalAlignment(SwingConstants.LEFT);
		setIconTextGap(25);

	}

}
