package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;

import conexionBD.ConexionSQLite;
import login.VistaLogin;
import login.Authentification.Main;
import modelo.MatrixManager;
import modelo.Room;
import modelo.Usuario;

public class VistaApp extends JFrame {
	JPanel panel;
	int i = 0;
	static VistaApp vtn;
	private static String since;

	private VistaApp() {
		FlatDraculaIJTheme.setup();
		setTitle("CodeLink");
		setExtendedState(MAXIMIZED_BOTH);
		setSize(980, 581);
		setMinimumSize(getSize());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		crearMenuBar();
		panel = new JPanel(new BorderLayout());
		panel.add(Home.getInstance(), BorderLayout.CENTER);
		setContentPane(panel);

		since = Main.getUserMain().getSince();
		iniciarHiloEscucha(Main.getUserMain().getAccessToken());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ConexionSQLite.actualizarSinceUser();
			}
		});
	}

	public static VistaApp getInstance() {
		if (vtn == null) {
			vtn = new VistaApp();
		}
		return vtn;
	}

	private void crearMenuBar() {

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Cambiar de cuenta");
		mntmNewMenuItem_1.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/cambiarCuenta.png")));
		mntmNewMenuItem_1.addActionListener(e -> {
			dispose();
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/logout"));
				Desktop.getDesktop().browse(new URI("http://localhost:8080/oauth2/authorization/github"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
		});

		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Cerrar sesión");
		mntmNewMenuItem_2.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/cerrarSesion.png")));
		mntmNewMenuItem_2.addActionListener(e -> {
			dispose();
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/logout"));
				FlatDraculaIJTheme.setup();
				FlatLaf.updateUI();
				VistaLogin login = VistaLogin.getInstance();
				login.setVisible(true);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		});

		JMenuItem mntmNewMenuTheme = new JMenuItem("Theme");
		mntmNewMenuTheme.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/sun.png")));
		mntmNewMenuTheme.addActionListener(e -> {
			if (i == 0) {
				FlatCyanLightIJTheme.setup();
				FlatLaf.updateUI();
				PanelHome.scrollComunidades.setBorder(null);
				PanelHome.scrollProyectos.setBorder(null);
				if (PanelHome.panelRoomsBusqueda.getComponentCount() == 0) {
					PanelHome.scrollBusquedaGrupos.setBorder(null);
				}
				mntmNewMenuTheme.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/moon.png")));
				i = 1;
			} else {
				FlatDraculaIJTheme.setup();
				FlatLaf.updateUI();
				PanelHome.scrollComunidades.setBorder(null);
				PanelHome.scrollProyectos.setBorder(null);
				if (PanelHome.panelRoomsBusqueda.getComponentCount() == 0) {
					PanelHome.scrollBusquedaGrupos.setBorder(null);
				}
				mntmNewMenuTheme.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/sun.png")));
				i = 0;
			}
		});
		ImageIcon img = new ImageIcon(getClass().getResource("/imgsLogin/logo4.png"));
		JMenu logoApp = new JMenu();
		logoApp.setIcon(new ImageIcon(img.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));

		JMenu iconoUsuario = new JMenu("");
		iconoUsuario.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/person.png")));
		iconoUsuario.add(mntmNewMenuItem_1);
		iconoUsuario.add(mntmNewMenuItem_2);

		JMenu iconoTheme = new JMenu("");
		iconoTheme.setIcon(new ImageIcon(getClass().getResource("/ImagenesMenuBar/MenuTheme.png")));
		iconoTheme.add(mntmNewMenuTheme);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(logoApp);
		menuBar.add(iconoUsuario);
		menuBar.add(iconoTheme);

	}

	public void iniciarHiloEscucha(String accessToken) {
		new Thread(() -> {

			while (true) {
				try {
					// 1) Construir URL de sync con long-polling
					String url = MatrixManager.SERVER_URL + "/_matrix/client/v3/sync?timeout=30000";
					if (since != null) {
						url += "&since=" + URLEncoder.encode(since, "UTF-8");
					}

					// 2) Hacer petición HTTP
					HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Authorization", "Bearer " + accessToken);
					connection.setRequestProperty("Content-Type", "application/json");

					// 3) Procesar respuesta
					int responseCode = connection.getResponseCode();
					if (responseCode == 200) {
						String response = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines()
								.collect(Collectors.joining("\n"));

						JSONObject json = new JSONObject(response);
						since = json.getString("next_batch");

						// 4) Procesar todos los rooms donde estoy en "join"
						JSONObject rooms = json.optJSONObject("rooms");
						if (rooms != null) {
							JSONObject joinRooms = rooms.optJSONObject("join");
							if (joinRooms != null) {
								for (String roomId : joinRooms.keySet()) {
									JSONObject room = joinRooms.getJSONObject(roomId);
									JSONObject timeline = room.optJSONObject("timeline");
									if (timeline != null) {
										JSONArray events = timeline.optJSONArray("events");

										// Primero procesamos todos los eventos m.room.member
										for (int i = 0; i < events.length(); i++) {
											JSONObject event = events.getJSONObject(i);
											String type = event.getString("type");
											if (!type.equals("m.room.member"))
												continue;

											JSONObject content = event.getJSONObject("content");
											String membership = content.getString("membership");
											String userId = event.getString("state_key");
											String userName = content.optString("displayname", userId);
											String avatarUrl = content.optString("avatar_url", "");

											if (!userId.equals(Main.getUserMain().getId())) {
												Usuario usuario = new Usuario.Builder().setId(userId)
														.setName(userName.substring(1)
																.replace(":matrixserver1.duckdns.org", ""))
														.setMxcUrl(avatarUrl).build();

												if (membership.equals("join")) {
													System.out.println("El usuario " + usuario.getId()
															+ " se ha unido al room " + roomId + "!");
													String htmlUrl = "";
													try {
														String type2 = "com.miapp.user.html_url";
														String path = URLEncoder.encode(userId, "UTF-8");
														String urlHtml = MatrixManager.SERVER_URL
																+ "/_matrix/client/r0/user/" + path + "/account_data/"
																+ type2;

														HttpURLConnection conn2 = (HttpURLConnection) new URL(urlHtml)
																.openConnection();
														conn2.setRequestMethod("GET");
														conn2.setRequestProperty("Authorization",
																"Bearer " + accessToken);
														conn2.setRequestProperty("Content-Type", "application/json");

														if (conn2.getResponseCode() == 200) {
															String body2 = new BufferedReader(
																	new InputStreamReader(conn2.getInputStream()))
																	.lines().collect(Collectors.joining("\n"));
															JSONObject json2 = new JSONObject(body2);
															htmlUrl = json2.optString("html_url", "");
														}
													} catch (Exception ignored) {
														// Si falla, htmlUrl queda como ""
													}
													usuario.setHtmlURL(htmlUrl);

													ConexionSQLite.insertUser(usuario);

													if (!ConexionSQLite.isUserInRoom(usuario.getId(), roomId)) {
														for (Room sala : Main.getUserMain().getListaRooms()) {
															if (sala.getId().equals(roomId)) {
																sala.getRoomMembers().add(usuario);
																break;
															}
														}

														Component[] listaPaneles = Home.panel.getComponents();
														for (int j = 1; j < listaPaneles.length; j++) {
															if (listaPaneles[j] instanceof PanelChat) {
																PanelChat panel = (PanelChat) listaPaneles[j];
																if (panel.room.getId().equals(roomId)) {
																	ButtonUser btnUser = new ButtonUser(usuario);
																	panel.panelRight.getPanelUsuariosRoom().add(btnUser,
																			"growx");
																	btnUser.addActionListener(e -> {
																		try {
																			Desktop.getDesktop().browse(
																					new URI(usuario.getHtmlURL()));
																		} catch (IOException | URISyntaxException e1) {
																			System.err.println(
																					"Error al abrir el perfil del usuario - "
																							+ e1);
																		}
																	});
																	panel.panelRight.getPanelUsuariosRoom()
																			.revalidate();
																	panel.panelRight.getPanelUsuariosRoom().repaint();

																	break;
																}
															}
														}
														ConexionSQLite.insertRoomMember(usuario.getId(), roomId);
													}
												} else if (membership.equals("leave")) {
													System.out.println("El usuario " + usuario.getId()
															+ " ha abandonado el room " + roomId + "!");

													boolean exists = false;
													for (Usuario u : ConexionSQLite.getListaUsers()) {
														if (u.getId().equals(usuario.getId())) {
															exists = true;
															break;
														}
													}
													if (exists) {
														for (Room sala : Main.getUserMain().getListaRooms()) {
															if (sala.getId().equals(roomId)) {
																sala.getRoomMembers().remove(usuario);
															}
															Component[] listaPaneles = Home.panel.getComponents();
															for (int j = 1; j < listaPaneles.length; j++) {
																if (listaPaneles[j] instanceof PanelChat) {
																	PanelChat panel = (PanelChat) listaPaneles[j];
																	if (panel.room.getId().equals(roomId)) {
																		for (Component element : panel.panelRight
																				.getPanelUsuariosRoom()
																				.getComponents()) {
																			if (element instanceof JButton) {
																				JButton btn = (JButton) element;
																				if (btn.getText()
																						.equals(usuario.getName())) {
																					panel.panelRight
																							.getPanelUsuariosRoom()
																							.remove(btn);
																					break;
																				}
																			}
																		}
																		panel.panelRight.getPanelUsuariosRoom()
																				.revalidate(); // Recalcula layout
																		panel.panelRight.getPanelUsuariosRoom()
																				.repaint(); // Redibuja
																							// visualmente
																		break;
																	}
																}
															}
														}
														ConexionSQLite.deleteRoomMember(usuario, roomId);
													}
												}
											}
										}

										// Luego procesamos todos los mensajes m.room.message
										for (int i = 0; i < events.length(); i++) {
											JSONObject event = events.getJSONObject(i);
											if (!event.getString("type").equals("m.room.message"))
												continue;

											if (event.getString("sender").equals(Main.getUserMain().getId()))
												continue;

											String sender = event.getString("sender");
											String eventId = event.getString("event_id");
											String content = event.getJSONObject("content").optString("body", "");

											System.out.println("Mensaje nuevo de " + sender + " recibido!");

											long ts = event.optLong("origin_server_ts", 0);
											Instant instant = Instant.ofEpochMilli(ts);
											LocalDateTime createdAt = LocalDateTime.ofInstant(instant,
													ZoneId.systemDefault());

											SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											Date date = parser.parse(createdAt.toString().replace("T", " "));
											SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
											String fechaCorrecta = formatter.format(date);

											Mensaje msj = new Mensaje(eventId, roomId, sender, content, fechaCorrecta);

											if (ConexionSQLite.mensajeExists(eventId)) {
												continue;
											}
											ConexionSQLite.insertMensaje(msj);

											Component[] listaPaneles = Home.panel.getComponents();
											for (int j = 1; j < listaPaneles.length; j++) {
												if (listaPaneles[j] instanceof PanelChat) {
													PanelChat panel = (PanelChat) listaPaneles[j];
													if (panel.room.getId().equals(roomId)) {

														if (!msj.getUser().getId().equals(Main.getUserMain().getId())) {
															panel.panelChat.add(msj, "align right");
															panel.room.getListaMensajes().add(msj);
														} else {
															panel.panelChat.add(msj);
															panel.room.getListaMensajes().add(msj);
														}
														if (!panel.room.isVisible()) {
															notificationNewMessage(panel.room);
														}
														panel.revalidate(); // Recalcula layout
														panel.repaint(); // Redibuja visualmente
														break;
													}
												}
											}

										}
									}
								}
							}
						}
					} else {
						System.out.println("Error en sincronización: " + responseCode);
						Thread.sleep(5000);
					}
				} catch (Exception e) {
					e.printStackTrace();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						break;
					}
				}
			}
		}).start();
	}

	public static String getSince() {
		return since;
	}

	public void setSince(String since) {
		this.since = since;
	}

	public static void notificationNewMessage(Room room) {
		ButtonUser btnRoom = Main.getUserMain().getListaRoomsBtns().get(room);
		if (!room.isVisible() && !btnRoom.getText().endsWith("   ·")) {
			btnRoom.setText(btnRoom.getText() + "   ·");
		}
	}
}
