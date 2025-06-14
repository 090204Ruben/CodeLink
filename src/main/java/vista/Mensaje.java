package vista;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

public class Mensaje extends JPanel {
	LocalDateTime fecha = LocalDateTime.now();

	String createdAt = fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();
	ImageIcon imagenUsuario;
	String body;
	String id; // Nos lo proporcinara el metodo que envie el mensaje a matrix
	String roomId; // Nos lo proporcinara el metodo que envie el mensaje a matrix
	String minutes;

	JButton iconoUser;
	JLabel lblUser;
	JLabel lblFecha;
	JLabel lblHora;
	Font fuente = new Font("Arial", Font.BOLD, 15);
	int originalWidth, originalHeight;// Temas de escalado para las imagenes
	double widthRatio, heightRatio, scaleRatio;
	int newWidth, newHeight;
	Usuario user;

	// El booleano sirve para que se diferencien los constructores en el caso en el
	// que el ImageIcon y el File sean null
	public Mensaje(String id, String texto, ImageIcon direccionImg, File fichero, String roomId, boolean isUserMain) {// Contructor
		this.roomId = roomId; // userMain en ese momento
		body = texto;
		this.id = id;
		user = Main.getUserMain();

		setLayout(new MigLayout());
		if (user.getIcon() == null) {
			user.setIcon(user.getMxcUrl());
		}
		imagenUsuario = user.getIcon();// Cogeremos la imagen del user de la bbdd con una
										// consulta
		iconoUser = new JButton();
		iconoUser.setIcon(new ImageIcon(imagenUsuario.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
		iconoUser.setBorder(null);
		iconoUser.setContentAreaFilled(false);
		add(iconoUser, "gapright 5,span 1 2,top");

		lblUser = new JLabel(user.getName());
		lblUser.setFont(fuente);
		add(lblUser, "gapright 5,split 3");

		if (fecha.getDayOfMonth() < 10) {
			createdAt = "0" + fecha.getDayOfMonth() + "/" + fecha.getMonthValue() + "/" + fecha.getYear();
		}

		lblFecha = new JLabel(createdAt);
		fuente = new Font("Arial", Font.PLAIN, 12);
		lblFecha.setFont(fuente);
		add(lblFecha);

		minutes = String.valueOf(fecha.getMinute());// Hacemos esto, porque si el minuto esta entre 0-9, nos
													// saldra algo asi: minute=9 en vez de minute=09
		if (minutes.length() == 1) {
			minutes = "0" + minutes;
		}
		minutes = fecha.getHour() + ":" + minutes;

		lblHora = new JLabel(minutes);
		lblHora.setFont(fuente);
		add(lblHora, "wrap");

		if (direccionImg != null) {
			JLabel lblImg = new JLabel();

			if (direccionImg.getIconWidth() > 500) {
				originalWidth = direccionImg.getIconWidth();
				originalHeight = direccionImg.getIconHeight();
				// Calcula la proporción para escalar la imagen
				widthRatio = (double) 500 / originalWidth;
				heightRatio = (double) 500 / originalHeight;
				scaleRatio = Math.min(widthRatio, heightRatio);

				// Nuevas dimensiones escaladas
				newWidth = (int) (originalWidth * scaleRatio);
				newHeight = (int) (originalHeight * scaleRatio);

				lblImg.setIcon(new ImageIcon(
						direccionImg.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)));
				add(lblImg);
			} else {
				lblImg.setIcon(direccionImg);
				add(lblImg);
			}
		} else if (fichero != null) {
			add(new MensajeFile(fichero));
		} else {
			JTextArea mensaje = new JTextArea(texto);
			mensaje.setFont(new Font("Arial", Font.PLAIN, 18));
			mensaje.setEditable(false);
			add(mensaje);
		}
	}

	public Mensaje(String eventId, String roomId, String userId, String body, String createdAt) {// Contructor para
																									// mensajes
																									// extraidos
																									// de la bbdd
		this.roomId = roomId;
		this.body = body;
		id = eventId;

		for (Usuario user : ConexionSQLite.getListaUsers()) {
			if (user.getId().equals(userId)) {
				this.user = user;
				break;
			}
		}
		if (user == null) {
			try {
				user = MatrixManager.getUserData(userId);
				ConexionSQLite.insertUser(user);
				ConexionSQLite.insertRoomMember(userId, roomId);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (createdAt != null) {// Si es true quiere decir que se esta cargando de la bbdd
			String[] fecha = createdAt.split(" ");
			this.createdAt = fecha[0];
			this.minutes = fecha[1];
		} else {
			minutes = String.valueOf(fecha.getMinute());// Hacemos esto, porque si el minuto esta entre 0-9, nos
			// saldra algo asi: minute=9 en vez de minute=09
			if (minutes.length() == 1) {
				minutes = "0" + minutes;
			}

			minutes = fecha.getHour() + ":" + minutes;
		}
		setLayout(new MigLayout());

		if (user.getIcon() == null) {
			user.setIcon(user.getMxcUrl());
		}
		imagenUsuario = user.getIcon();// Cogeremos la imagen del user de la bbdd con una
										// consulta
		iconoUser = new JButton();
		iconoUser.setIcon(new ImageIcon(imagenUsuario.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
		iconoUser.setBorder(null);
		iconoUser.setContentAreaFilled(false);
		add(iconoUser, "gapright 5,span 1 2,top");

		lblUser = new JLabel(user.getName());
		lblUser.setFont(fuente);
		add(lblUser, "gapright 5,split 3");

		lblFecha = new JLabel(this.createdAt);
		fuente = new Font("Arial", Font.PLAIN, 12);
		lblFecha.setFont(fuente);
		add(lblFecha);

		lblHora = new JLabel(this.minutes);
		lblHora.setFont(fuente);
		add(lblHora, "wrap");

		JTextArea mensaje = new JTextArea(body);
		mensaje.setFont(new Font("Arial", Font.PLAIN, 18));
		mensaje.setEditable(false);
		add(mensaje);
	}

	public Usuario getUser() {
		return user;
	}

	public void setUser(Usuario user) {
		this.user = user;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public String getBody() {
		return body;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public ImageIcon getImagenUsuario() {
		return imagenUsuario;
	}

	public void setImagenUsuario(ImageIcon imagenUsuario) {
		this.imagenUsuario = imagenUsuario;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	@Override
	public String toString() {
		return "Mensaje [createdAt=" + createdAt + ", imagenUsuario=" + imagenUsuario + ", body=" + body + ", id=" + id
				+ ", roomId=" + roomId + ", minutes=" + minutes + ", user=" + user + "]";
	}

}
