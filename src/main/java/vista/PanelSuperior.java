package vista;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.time.LocalDate;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Room;
import net.miginfocom.swing.MigLayout;

public class PanelSuperior extends JPanel {
	final Font fuenteTxt = new Font("Arial", Font.PLAIN, 20);
	int iterador = 1;
	String meses[] = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
			"Octubre", "Noviembre", "Diciembre" };

	LocalDate fecha = LocalDate.now();
	JButton btnAddGroup;
	int dia = fecha.getDayOfMonth();
	String mes = meses[fecha.getMonthValue() - 1];
	int año = fecha.getYear();

	public PanelSuperior() {
		setLayout(new MigLayout("fill", "0[grow,fill]0", "0[]0"));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 10", "0[]0", "0[]0"));

		// Etiqueta con el nombre del compañero y un icono
		ImageIcon img = new ImageIcon(getClass().getResource("/imgsLogin/logo4.png"));
		JLabel lblLogoApp = new JLabel();
		lblLogoApp.setIconTextGap(10);
		lblLogoApp.setIcon(new ImageIcon(img.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
		lblLogoApp.setFont(fuenteTxt);

		JLabel lblFecha = new JLabel("Hoy es " + dia + " de " + mes + " de " + año);
		lblFecha.setFont(new Font("Arial", Font.BOLD, 20));

		btnAddGroup = new JButton(new ImageIcon(getClass().getResource("/addGroup.png")));
		btnAddGroup.setBorder(null);
		btnAddGroup.setContentAreaFilled(false);
		btnAddGroup.addActionListener(e -> {
			if (iterador == 1) {
				btnAddGroup.setIcon(new ImageIcon(getClass().getResource("/backward.png")));
				PanelHome.layout.show(PanelHome.paneles, "Creacion grupos");
				iterador--;
			} else {
				btnAddGroup.setIcon(new ImageIcon(getClass().getResource("/addGroup.png")));
				PanelHome.layout.show(PanelHome.paneles, "Busqueda y Sugerencias");
				iterador++;
			}
		});

		panel.add(lblLogoApp, "gapleft 10,gaptop 10,gapbottom 10,growx, pushx");
		panel.add(lblFecha, "al center,gaptop 10,gapbottom 10,growx, pushx");
		panel.add(btnAddGroup, "gaptop 10,gapbottom 10,gapright 10,al right");
		JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll, "growx");
	}

	public PanelSuperior(String name, Room room) {
		setLayout(new MigLayout("fill", "0[grow,fill]0", "0[]0"));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 10", "0[]0", "0[]0"));

		// Etiqueta con el nombre del compañero y un icono
		ImageIcon img = new ImageIcon(getClass().getResource("/imgsLogin/logo4.png"));
		JLabel lblCompañero = new JLabel(name);
		lblCompañero.setIconTextGap(5);
		lblCompañero.setIcon(new ImageIcon(img.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
		lblCompañero.setIconTextGap(10);
		lblCompañero.setFont(fuenteTxt);

		ImageIcon img2;
		if (Main.getUserMain().getId().equals(room.getUserCreator())) {
			img2 = new ImageIcon(getClass().getResource("/closeRoom.png"));

		} else {
			img2 = new ImageIcon(getClass().getResource("/exitRoom.png"));
		}

		JButton btnEliminarRoom = new JButton(img2);
		btnEliminarRoom.setIcon(new ImageIcon(img2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
		btnEliminarRoom.setContentAreaFilled(false);
		btnEliminarRoom.setBorder(null);
		btnEliminarRoom.addActionListener(e -> {
			String[] options = { "CONFIRMAR", "CANCELAR" };
			if (room.getUserCreator().equals(Main.getUserMain().getId())) {// El usuario es el creador del room por lo
																			// que lo puede borrar de Matrix
				int option = JOptionPane.showOptionDialog(null, "¿Estás seguro de que deseas eliminar la sala?",
						"Eliminar Sala", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
						options[1]);
				if (option == 0) {// OPTION CONFIRMAR
					if (room.getType().equals("PROYECTO")) {
						for (Component panel_proyecto : PanelHome.proyectosMasBuscados.getComponents()) {
							Panel_Proyecto_Comunidad temp = (Panel_Proyecto_Comunidad) panel_proyecto;
							if (temp.getRoom().getId().equals(room.getId())) {
								PanelHome.proyectosMasBuscados.remove(temp);

								PanelHome.proyectosMasBuscados.revalidate();
								PanelHome.proyectosMasBuscados.repaint();
								break;
							}
						}
					} else if (room.getType().equals("COMUNIDAD")) {
						for (Component panel_comunidad : PanelHome.comunidadesMasBuscadas.getComponents()) {
							Panel_Proyecto_Comunidad temp = (Panel_Proyecto_Comunidad) panel_comunidad;
							if (temp.getRoom().getId().equals(room.getId())) {
								PanelHome.comunidadesMasBuscadas.remove(temp);

								PanelHome.comunidadesMasBuscadas.revalidate();
								PanelHome.comunidadesMasBuscadas.repaint();
								break;
							}
						}
					}
					ConexionSQLite.borrarRastroRoom(room);
					try {
						MatrixManager.deleteRoom(room);
					} catch (IOException e1) {
						System.out.println("❌ Error al eliminar sala: " + room.getId());
						e1.printStackTrace();
					}
					Main.getUserMain().getListaRooms().remove(room);

					PanelLeft.panel2.remove(Main.getUserMain().getListaRoomsBtns().get(room));
					PanelLeft.panel2.revalidate();
					PanelLeft.panel2.repaint();

					Home.layout.show(Home.panel, "Panel Home");
				}
			} else {// El usuario no es el creador por lo que solo lo borrara de la base de datos y
					// se saldra en matrix
				int option = JOptionPane.showOptionDialog(null, "¿Estás seguro de que deseas salirte de la sala?",
						"Eliminar Sala", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
						options[1]);
				if (option == 0) {// OPTION CONFIRMAR
					ConexionSQLite.borrarRastroRoom(room);// Este metodo borra tanto la Room, como los mensajes
															// dentro de ella, como sus miembros
					MatrixManager.leaveRoomById(room.getId());
					Main.getUserMain().getListaRooms().remove(room);

					PanelLeft.panel2.remove(Main.getUserMain().getListaRoomsBtns().get(room));
					PanelLeft.panel2.revalidate();
					PanelLeft.panel2.repaint();
					if (room.getType().equals("PROYECTO")) {// Esto lo hacemos para actualizar las sugerencias en caso
															// de que borremos una sala y aparezca en sugerencias para
															// unirse dando pie a errores
						Component[] listaProyectos = PanelHome.comunidadesMasBuscadas.getComponents();
						for (Component panel_P : listaProyectos) {
							if (panel_P instanceof Panel_Proyecto_Comunidad) {
								Panel_Proyecto_Comunidad ppc = (Panel_Proyecto_Comunidad) panel_P;
								if (ppc.getRoom().getId().equals(room.getId())) {
									PanelHome.proyectosMasBuscados.remove(ppc);
									break;
								}
							}
						}
					} else {
						Component[] listaComunidades = PanelHome.proyectosMasBuscados.getComponents();
						for (Component panel_P : listaComunidades) {
							if (panel_P instanceof Panel_Proyecto_Comunidad) {
								Panel_Proyecto_Comunidad ppc = (Panel_Proyecto_Comunidad) panel_P;
								if (ppc.getRoom().getId().equals(room.getId())) {
									PanelHome.comunidadesMasBuscadas.remove(ppc);
									break;
								}
							}
						}
					}

					Home.layout.show(Home.panel, "Panel Home");
				}
			}
		});

		panel.add(lblCompañero, "gaptop 10,gapbottom 10,growx, pushx");
		panel.add(btnEliminarRoom, "align right,gapright 10,gaptop 10,gapbottom 10");
		JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll, "growx");
	}

	public int getIterador() {
		return iterador;
	}

	public void setIterador(int iterador) {
		this.iterador = iterador;
	}

	public JButton getBtnAddGroup() {
		return btnAddGroup;
	}

	public void setBtnAddGroup(JButton btnAddGroup) {
		this.btnAddGroup = btnAddGroup;
	}

}
