package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import modelo.Comunidad;
import modelo.MatrixManager;
import modelo.Proyecto;
import modelo.Room;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

public class PanelHome extends JPanel implements KeyListener {

	// private static PanelHome home;
	final Font fuenteTxt = new Font("Arial", Font.PLAIN, 20);
	private JButton btnCrearProyecto, btnCrearComunidad;
	public static CardLayout layout = new CardLayout();
	public static JPanel paneles, proyectosMasBuscados, comunidadesMasBuscadas;
	private Usuario user = Main.getUserMain();
	public static JScrollPane scrollProyectos, scrollComunidades, scrollBusquedaGrupos;
	public static JPanel panelRoomsBusqueda;
	PanelRight panelRight;
	ButtonGroup group;
	String estado = "";
	PanelSuperior panelSuperior;
	Border bordeScroll;

	public PanelHome() {
		setLayout(new MigLayout("wrap", "0[grow]0", ""));

		panelSuperior = new PanelSuperior();
		add(panelSuperior, "growx,hmin 60");

		panelRight = new PanelRight();
		add(panelRight, "growy,dock east");

		JPanel panel = new JPanel(new MigLayout("wrap", "0[center]0", ""));

		JPanel panelAddGroup = getJPanelAddGroup();

		panelRoomsBusqueda = new JPanel();
		scrollBusquedaGrupos = new JScrollPane(panelRoomsBusqueda);
		bordeScroll = scrollBusquedaGrupos.getBorder();
		scrollBusquedaGrupos.setBorder(null);
		scrollBusquedaGrupos.setPreferredSize(new Dimension(700, 250));
		panel.add(scrollBusquedaGrupos);

		JPanel panelGroupMain = new JPanel(new MigLayout("wrap", "[]", ""));
		panelGroupMain.add(panelAddGroup, "gaptop 20,gapbottom 5,gapleft 10,gapright 10,growx");
		panelGroupMain.add(scrollBusquedaGrupos, "gapbottom 5,gapleft 10,gapright 10,growx");
		panelGroupMain.add(new JSeparator(), "gapleft 10,gapbottom 10,gapright 10,growx, h 2");
		panelGroupMain.add(getJPanelSugerencias());

		paneles = new JPanel(layout);
		paneles.add(panelGroupMain, "Busqueda y Sugerencias");
		paneles.add(getJPanelCreacionGrupo(), "Creacion grupos");

		panel.add(paneles, "gapleft 10,gapright 10,grow,push");

		JScrollPane scroll2 = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll2, "grow,push");

	}

	private JPanel getJPanelSugerencias() {
		JPanel panel = new JPanel(new MigLayout("wrap", "", ""));

		JLabel lblProyectosMasBuscados = new JLabel("Proyectos mas buscados");
		lblProyectosMasBuscados.setFont(fuenteTxt);
		panel.add(lblProyectosMasBuscados, "gaptop 10,gapbottom 10");

		proyectosMasBuscados = new JPanel(new MigLayout("insets 10 0 15 0", "[center]", "[]"));

		scrollProyectos = new JScrollPane(proyectosMasBuscados, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollProyectos.setBorder(null);
		panel.add(scrollProyectos, " hmax 200");

		JLabel lblComunidadesMAsBuscada = new JLabel("Comunidades mas buscados");
		lblComunidadesMAsBuscada.setFont(fuenteTxt);
		panel.add(lblComunidadesMAsBuscada, "gaptop 10,gapbottom 10");

		comunidadesMasBuscadas = new JPanel(new MigLayout("insets 10 0 15 0", "[center]", "[]"));

		scrollComunidades = new JScrollPane(comunidadesMasBuscadas, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollComunidades.setBorder(null);
		panel.add(scrollComunidades, " hmax 200");

		try {
			ArrayList<Room> listaSugerencias = MatrixManager.getRoomByAliasOrCategoria("", 6);

			for (Room sala : listaSugerencias) {

				if (sala.getType().equals("PROYECTO")) {
					proyectosMasBuscados.add(new Panel_Proyecto_Comunidad(sala));
				} else if (sala.getType().equals("COMUNIDAD")) {
					comunidadesMasBuscadas.add(new Panel_Proyecto_Comunidad(sala));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		scrollProyectos.setPreferredSize(new Dimension(1000, 250));
		scrollComunidades.setPreferredSize(new Dimension(1000, 250));

		return panel;
	}

	private JPanel getJPanelAddGroup() {
		JPanel panel = new JPanel(new MigLayout("", "0[]0", "0[]0"));
		JLabel lbl = new JLabel("Buscar :");
		lbl.setFont(fuenteTxt);
		panel.add(lbl, "gapright 5"); // Alinea la etiqueta al centro

		JTextField txtBusqueda = new JTextField();
		panel.add(txtBusqueda, "h 35, growx,push"); // Alinea el campo de texto al centro

		JButton btnBusqueda = new JButton(new ImageIcon(getClass().getResource("/informacion.png")));
		btnBusqueda.setContentAreaFilled(false);
		btnBusqueda.setBorder(null);
		panel.add(btnBusqueda, "gapleft 5,wrap");

		btnBusqueda.addActionListener(e -> {
			ArrayList<Room> listaBusqueda = null;
			panelRoomsBusqueda.removeAll();
			if (!txtBusqueda.getText().isBlank()) {
				listaBusqueda = MatrixManager.getRoomByAliasOrCategoria(txtBusqueda.getText().trim(), 6);
				if (listaBusqueda.size() == 0) {
					JOptionPane.showMessageDialog(null, "No hay Proyectos/Comunidades con ese nombre/categoría",
							"Sala no encontrada", JOptionPane.WARNING_MESSAGE);
					scrollBusquedaGrupos.setBorder(null);
					return;
				}
				for (Room sala : listaBusqueda) {
					panelRoomsBusqueda.add(new Panel_Proyecto_Comunidad(sala));
				}

			} else {
				JOptionPane.showMessageDialog(null, "No hay Proyectos/Comunidades con ese nombre/categoría",
						"Sala no encontrada", JOptionPane.WARNING_MESSAGE);
				scrollBusquedaGrupos.setBorder(null);
				return;
			}

			scrollBusquedaGrupos.setBorder(bordeScroll);
			this.revalidate();
			this.repaint();
		});

		return panel;
	}

	private JPanel getJPanelCreacionGrupo() {
		JPanel panel = new JPanel(new BorderLayout(10, 22));

		JLabel lblTtle = new JLabel("¿Desea crear un grupo?");
		lblTtle.setFont(fuenteTxt);
		panel.add(lblTtle, BorderLayout.NORTH);

		panel.add(getJTabbedPane(), BorderLayout.CENTER);
		return panel;
	}

	private JTabbedPane getJTabbedPane() {
		JTabbedPane eleccionCreacion = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

		PanelCeldaNombre panelCeldaNombre1 = new PanelCeldaNombre();
		PanelCeldaNombre panelCeldaNombre2 = new PanelCeldaNombre();

		eleccionCreacion.addTab("Proyecto", getJPanelProyecto(panelCeldaNombre1));
		eleccionCreacion.addTab("Comunidad", getJPanelComunidad(panelCeldaNombre2));

		return eleccionCreacion;
	}

	private JPanel getJPanelProyecto(PanelCeldaNombre panelCeldaNombre) {
		JPanel panel = new JPanel(new MigLayout("insets 20", "", ""));

		panel.add(panelCeldaNombre, "wrap");
		panel.add(getJPanelFormularioProyecto(panelCeldaNombre));

		return panel;
	}

	private JPanel getJPanelComunidad(PanelCeldaNombre panelCeldaNombre) {
		JPanel panel = new JPanel(new MigLayout("insets 20", "", ""));

		panel.add(panelCeldaNombre, "wrap");
		panel.add(getJPanelFormularioComunidad(panelCeldaNombre));

		return panel;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		limitarLengthJTextAreas(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		limitarLengthJTextAreas(e);
	}

	private void limitarLengthJTextAreas(KeyEvent e) {
		JTextArea component = ((JTextArea) e.getSource());
		if (component.getText().length() > 293) {
			component.setText(component.getText().substring(0, 293));
		}
	}

	private JPanel getJPanelFormularioComunidad(PanelCeldaNombre panelCeldaNombre) {
		JPanel panel = new JPanel(new MigLayout());
		JTextField txtCategoria;
		JTextArea txtDescripcion, txtReglas, txtPropuesta;

		// Categoría
		panel.add(new JLabel("Categoría:"), "aligny center");
		txtCategoria = new JTextField("Ejemplo: #VideoJuegosAccion", 40);
		panel.add(txtCategoria, "wrap");

		// Descripción
		panel.add(new JLabel("Descripción:"), "aligny top");
		txtDescripcion = new JTextArea(3, 40);
		txtDescripcion.setLineWrap(true);
		txtDescripcion.setWrapStyleWord(true);
		panel.add(new JScrollPane(txtDescripcion), "wrap, growx");
		txtDescripcion.addKeyListener(this);

		// Propuesta comunidad
		panel.add(new JLabel("Propuesta comunidad:"), "aligny top");
		txtPropuesta = new JTextArea(3, 40);
		txtPropuesta.setLineWrap(true);
		txtPropuesta.setWrapStyleWord(false);
		panel.add(new JScrollPane(txtPropuesta), "wrap, growx");
		txtPropuesta.addKeyListener(this);

		// Reglas del grupo
		panel.add(new JLabel("Reglas del grupo:"), "aligny top");
		txtReglas = new JTextArea(3, 40);
		txtReglas.setLineWrap(true);
		txtReglas.setWrapStyleWord(false);
		panel.add(new JScrollPane(txtReglas), "wrap, growx");
		txtReglas.addKeyListener(this);

		btnCrearComunidad = new JButton("Crear Comunidad");
		panel.add(btnCrearComunidad, "gaptop 10,grow,push,align right");
		btnCrearComunidad.addActionListener(e -> {
			if (panelCeldaNombre.getTxtNombre().getText().contains(" ")) {
				JOptionPane.showMessageDialog(null, "El nombre de la sala no puede contener espacios", "Alias erroneo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!hayParametrosSinRellenar(panelCeldaNombre.getTxtNombre().getText(), txtCategoria.getText(),
					txtDescripcion.getText(), null, null, txtReglas.getText(), txtPropuesta.getText(), "COMUNIDAD")) {

				LocalDate fechaActual = LocalDate.now();
				DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				String fecha = fechaActual.format(formato);

				Room room = new Comunidad(null, null, panelCeldaNombre.getTxtNombre().getText().trim(),
						user.getId(), fecha, "COMUNIDAD", panelCeldaNombre.getImg(),
						panelCeldaNombre.getTxtNombre().getText().trim(), txtCategoria.getText(),
						txtDescripcion.getText(), txtReglas.getText(), txtPropuesta.getText());

				if (MatrixManager.createRoomWithAlias(room, user.getAccessToken(), panelCeldaNombre.getFicheroImg())) {

					ButtonUser btnNewCommunity = new ButtonUser(room);

					user.getListaRooms().add(room);
					System.out.println("Room " + room.getName() + "->" + room.getRoomMembers());
					user.getListaRoomsBtns().put(room, btnNewCommunity);
					ConexionSQLite.insertRoomMember(user.getId(), room.getId());

					comunidadesMasBuscadas.add(new Panel_Proyecto_Comunidad(room));
					PanelLeft.panel2.add(btnNewCommunity, "grow,wrap");

					room.getRoomMembers().add(user);

					JOptionPane.showMessageDialog(null, "Comunidad creada con éxito");

					PanelLeft.panel2.revalidate();
					PanelLeft.panel2.repaint();

					PanelChat panelChat = new PanelChat(room);
					Home.panel.add(panelChat, "Chat " + room.getName());
					btnNewCommunity.addActionListener(f -> {
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
					layout.show(paneles, "Busqueda y Sugerencias");
					resetearDatosRoom(panelCeldaNombre, txtCategoria, txtDescripcion, null, txtReglas, txtPropuesta,
							"COMUNIDAD");
				}
			} else {
				JOptionPane.showMessageDialog(null, "Rellene todos los campos para poder crear la Comunidad",
						"Campos sin completar", JOptionPane.WARNING_MESSAGE);
			}

		});

		return panel;
	}

	private JPanel getJPanelFormularioProyecto(PanelCeldaNombre panelCeldaNombre) {
		JPanel panel = new JPanel(new MigLayout());
		JTextField txtCategoria, txtTecnologias;
		JTextArea txtReglas, txtPropuesta, txtDescripcion;

		// Categoría
		panel.add(new JLabel("Categoría:"), "aligny center");
		txtCategoria = new JTextField("Ejemplo: #DesarrolloDeSoftware", 40);
		panel.add(txtCategoria, "wrap");

		// Descripción
		panel.add(new JLabel("Descripción:"), "aligny top");
		txtDescripcion = new JTextArea(3, 40);
		txtDescripcion.setLineWrap(true);
		txtDescripcion.setWrapStyleWord(true);
		panel.add(new JScrollPane(txtDescripcion), "wrap, growx");
		txtDescripcion.addKeyListener(this);

		// Tecnologías/Herramientas
		panel.add(new JLabel("Tecnologías/Herramientas:"), "aligny top");
		txtTecnologias = new JTextField(40);
		panel.add(txtTecnologias, "wrap, growx");

		// Estado del proyecto
		panel.add(new JLabel("Estado del proyecto:"), "aligny top");

		group = new ButtonGroup();

		JRadioButton rbtnInicio = new JRadioButton("Inicio");
		group.add(rbtnInicio);
		rbtnInicio.addActionListener(e -> estado = "Inicio");

		JRadioButton rbtnPlanificación = new JRadioButton("Planificación");
		group.add(rbtnPlanificación);
		rbtnPlanificación.addActionListener(e -> estado = "Planificación");

		JRadioButton rbtnDesarrollo = new JRadioButton("Desarrollo");
		group.add(rbtnDesarrollo);
		rbtnDesarrollo.addActionListener(e -> estado = "Desarrollo");

		JRadioButton rbtnMonitoreo_Control = new JRadioButton("Monitoreo/Control");
		group.add(rbtnMonitoreo_Control);
		rbtnMonitoreo_Control.addActionListener(e -> estado = "Monitoreo/Control");

		JRadioButton rbtnCierre = new JRadioButton("Cierre");
		group.add(rbtnCierre);
		rbtnCierre.addActionListener(e -> estado = "Cierre");

		JPanel panelEstadosProyecto = new JPanel();
		panelEstadosProyecto.add(rbtnInicio);
		panelEstadosProyecto.add(rbtnPlanificación);
		panelEstadosProyecto.add(rbtnDesarrollo);
		panelEstadosProyecto.add(rbtnMonitoreo_Control);
		panelEstadosProyecto.add(rbtnCierre);

		panel.add(panelEstadosProyecto, "wrap, growx");

		// Reglas del grupo
		panel.add(new JLabel("Reglas del grupo:"), "aligny top");
		txtReglas = new JTextArea(3, 40);
		txtReglas.setLineWrap(true);
		txtReglas.setWrapStyleWord(false);
		panel.add(new JScrollPane(txtReglas), "wrap, growx");
		txtReglas.addKeyListener(this);

		panel.add(new JLabel("Propuesta de proyecto:"), "aligny top");
		txtPropuesta = new JTextArea(3, 40);
		txtPropuesta.setLineWrap(true);
		txtPropuesta.setWrapStyleWord(false);
		panel.add(new JScrollPane(txtPropuesta), "wrap, growx");
		txtPropuesta.addKeyListener(this);

		btnCrearProyecto = new JButton("Crear Proyecto");
		panel.add(btnCrearProyecto, "gaptop 10,grow,push,align right");
		btnCrearProyecto.addActionListener(e -> {
			if (panelCeldaNombre.getTxtNombre().getText().contains(" ")) {
				JOptionPane.showMessageDialog(null, "El nombre de la sala no puede contener espacios", "Alias erroneo",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (!hayParametrosSinRellenar(panelCeldaNombre.getTxtNombre().getText(), txtCategoria.getText(),
					txtDescripcion.getText(), txtTecnologias.getText(), estado, txtReglas.getText(),
					txtPropuesta.getText(), "PROYECTO")) {

				// Cuando un usuario cree un proyecto, creara a su vez un chat que se le añadira
				// a su panel izquierdo y un panel informativo para que mas gente pueda unirse
				// si lo desea
				LocalDate fechaActual = LocalDate.now();
				DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				String fecha = fechaActual.format(formato);

				Room room = new Proyecto(null, panelCeldaNombre.getTxtNombre().getText().trim(), txtCategoria.getText(),
						txtDescripcion.getText(), estado, txtReglas.getText(), txtPropuesta.getText(),
						txtTecnologias.getText(), panelCeldaNombre.getImg(), null,
						panelCeldaNombre.getTxtNombre().getText().trim(), user.getId(), fecha, "PROYECTO");

				// Creamos el Proyecto en matrix y si devuelve false es que no se ha creado con
				// éxito
				if (MatrixManager.createRoomWithAlias(room, user.getAccessToken(), panelCeldaNombre.getFicheroImg())) {

					proyectosMasBuscados.add(new Panel_Proyecto_Comunidad(room));

					user.getListaRooms().add(room);

					ButtonUser btnNewProyect = new ButtonUser(room);
					user.getListaRoomsBtns().put(room, btnNewProyect);

					ConexionSQLite.insertRoomMember(user.getId(), room.getId());

					PanelLeft.panel2.add(btnNewProyect, "grow,wrap");

					room.getRoomMembers().add(user);
					System.out.println("Room " + room.getName() + "->" + room.getRoomMembers());

					JOptionPane.showMessageDialog(null, "Proyecto creado con éxito");

					PanelLeft.panel2.revalidate();
					PanelLeft.panel2.repaint();

					PanelChat panelChat = new PanelChat(room);
					Home.panel.add(panelChat, "Chat " + room.getName());
					btnNewProyect.addActionListener(f -> {
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
					layout.show(paneles, "Busqueda y Sugerencias");
					resetearDatosRoom(panelCeldaNombre, txtCategoria, txtDescripcion, txtTecnologias, txtReglas,
							txtPropuesta, "PROYECTO");
				}
			} else {
				JOptionPane.showMessageDialog(null, "Rellene todos los campos para poder crear el Proyecto",
						"Campos sin completar", JOptionPane.WARNING_MESSAGE);
			}
		});

		return panel;
	}

	public static JPanel getPaneles() {
		return paneles;
	}

	private class PanelCeldaNombre extends JPanel {

		JTextField txtNombre;
		ImageIcon imgDefecto;
		ImageIcon imgNueva;
		File ficheroImg;
		JLabel lblIcono;

		public PanelCeldaNombre() {
			// Imagen/Icono del proyecto
			JLayeredPane capas = new JLayeredPane();
			capas.setPreferredSize(new Dimension(120, 120));

			URL urlImgDefecto = getClass().getResource("/imgsLogin/logo5.png");
			imgDefecto = ImagenConMarcoRedondo.crearImagenConMarcoRedondo(urlImgDefecto);
			lblIcono = new JLabel();
			lblIcono.setIcon(new ImageIcon(imgDefecto.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
			lblIcono.setBounds(0, 0, 100, 100);

			ImageIcon imgBtn = new ImageIcon(getClass().getResource("/editIcon.png"));
			JButton btn = new JButton(new ImageIcon(imgBtn.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH)));
			btn.setBorder(null);
			btn.setContentAreaFilled(false);
			btn.setBounds(70, 70, 30, 30);

			capas.add(lblIcono, Integer.valueOf(1));
			capas.add(btn, Integer.valueOf(2));
			// Acción para seleccionar imagen
			btn.addActionListener(e -> {
				ficheroImg = null;
				imgNueva = null;
				JFileChooser exploradorArchivos = new JFileChooser();
				if (exploradorArchivos.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					ficheroImg = exploradorArchivos.getSelectedFile();
					if (ficheroImg != null && ficheroImg.isFile()
							&& (ficheroImg.getAbsolutePath().endsWith(".png")
									|| ficheroImg.getAbsolutePath().endsWith(".jpg")
									|| ficheroImg.getAbsolutePath().endsWith(".ico")
									|| ficheroImg.getAbsolutePath().endsWith(".jpeg"))) {

						try {
							imgNueva = ImagenConMarcoRedondo.crearImagenConMarcoRedondo(ficheroImg.toURI().toURL());
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
						lblIcono.setIcon(
								new ImageIcon(imgNueva.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
						capas.revalidate();
						capas.repaint();
					} else {
						JOptionPane.showMessageDialog(null, "Error al cargar la imagen");
					}
				}
			});
			add(capas);
			add(new JLabel("Nombre:"));
			txtNombre = new JTextField(20);
			add(txtNombre);
		}

		public File getFicheroImg() {
			// 1) Si el usuario ya eligió algo:
			if (ficheroImg != null && ficheroImg.isFile()) {
				return ficheroImg;
			}

			// 2) Si no, volcamos el recurso embebido a un temp file:
			try (InputStream is = getClass().getResourceAsStream("/imgsLogin/logo5.png")) {
				if (is == null) {
					System.err.println("No se encontró el recurso /imgsLogin/logo5.png");
					return null;
				}
				// Creamos un temporal con extensión .png
				File tempFile = File.createTempFile("room_icon_", ".png");
				tempFile.deleteOnExit();
				// Copiamos bytes
				Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return tempFile;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		public JTextField getTxtNombre() {
			return txtNombre;
		}

		public void setTxtNombre(JTextField txtNombre) {
			this.txtNombre = txtNombre;
		}

		public ImageIcon getImg() {
			if (imgNueva == null) {
				return imgDefecto;
			} else {
				return imgNueva;
			}
		}

		public JLabel getLblIcono() {
			return lblIcono;
		}

		public void setLblIcono(JLabel lblIcono) {
			this.lblIcono = lblIcono;
		}

		public ImageIcon getImgDefecto() {
			return imgDefecto;
		}

		public void setImgDefecto(ImageIcon imgDefecto) {
			this.imgDefecto = imgDefecto;
		}

	}

	private boolean hayParametrosSinRellenar(String panelCeldaNombre, String txtCategoria, String txtDescripcion,
			String txtTecnologias, String txtEstado, String txtReglas, String txtPropuesta, String tipo) {
		if (tipo.equals("COMUNIDAD")) {
			if (panelCeldaNombre.isBlank() || txtCategoria.isBlank() || txtDescripcion.isBlank()
					|| txtPropuesta.isBlank() || txtReglas.isBlank()) {
				return true;
			}
		} else {// Para proyectos
			if (panelCeldaNombre.isBlank() || txtCategoria.isBlank() || txtDescripcion.isBlank()
					|| txtPropuesta.isBlank() || txtReglas.isBlank() || txtTecnologias.isBlank()
					|| txtEstado.isBlank()) {
				return true;
			}
		}
		return false;
	}

	private void resetearDatosRoom(PanelCeldaNombre panelCeldaNombre, JTextField txtCategoria, JTextArea txtDescripcion,
			JTextField txtTecnologias, JTextArea txtReglas, JTextArea txtPropuesta, String tipo) {
		if (tipo.equals("COMUNIDAD")) {
			panelCeldaNombre.getTxtNombre().setText("");
			txtCategoria.setText("Ejemplo: #VideoJuegosAccion");
			txtDescripcion.setText("");
			txtPropuesta.setText("");
			txtReglas.setText("");
			panelCeldaNombre.getLblIcono().setIcon(new ImageIcon(
					panelCeldaNombre.getImgDefecto().getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		} else {// Para proyectos
			panelCeldaNombre.getTxtNombre().setText("");
			txtCategoria.setText("Ejemplo: #DesarrolloDeSoftware");
			txtDescripcion.setText("");
			txtPropuesta.setText("");
			txtReglas.setText("");
			txtTecnologias.setText("");
			group.clearSelection();

			panelCeldaNombre.getLblIcono().setIcon(new ImageIcon(
					panelCeldaNombre.getImgDefecto().getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
		}
		// Para que cuando mostremos el panel de sugerencias sin darle al botón
		// btnAddGroup de PanelSuperior no falle el botón al hacer clic nuevamente
		panelSuperior.setIterador(1);
		panelSuperior.getBtnAddGroup().setIcon(new ImageIcon(getClass().getResource("/addGroup.png")));
		revalidate();
		repaint();
	}
}
