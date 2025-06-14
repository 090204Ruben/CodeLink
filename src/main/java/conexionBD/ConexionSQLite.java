package conexionBD;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import login.Authentification.Main;
import modelo.Canal;
import modelo.Comunidad;
import modelo.Proyecto;
import modelo.Room;
import modelo.Usuario;
import vista.Home;
import vista.Mensaje;
import vista.PanelChat;
import vista.VistaApp;

public class ConexionSQLite {
	private final static String DB_NAME = "dbCodeLink";
	public static ArrayList<Usuario> listaUsers;

	public ConexionSQLite() {
		try (Connection conn = getConnection()) {
			createTableUser(conn);
			createTableRoom(conn);
			createTableRoomMembers(conn);
			createTableMessage(conn);

		} catch (Exception e) {
			System.err.println("--Error al crear la estructura de SQLite: " + e.getMessage());
		}
	}

	public static ArrayList<Usuario> getListaUsers() {
		return listaUsers;
	}

	public static void setListaUsers(ArrayList<Usuario> listaUsers) {
		ConexionSQLite.listaUsers = listaUsers;
	}

	// Conexión a la BBDD
	private static Connection getConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			System.err.println("Error con el Driver: " + e.getMessage());
		}
		return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME + ".db");
	}

	// Creación de tablas
	private void createTableUser(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS User (" + "id TEXT PRIMARY KEY," + "name TEXT NOT NULL UNIQUE,"
				+ "password TEXT NOT NULL," + "accessToken TEXT," + "email TEXT," + "htmlURL TEXT," + "avatarURL TEXT,"
				+ "createdAt TEXT," + "lastLoginAt TEXT" + ",since TEXT" + ");";
		try (PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.executeUpdate();
		}
	}

	private void createTableRoom(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Room (" + "id TEXT PRIMARY KEY," + "alias TEXT UNIQUE,"
				+ "name TEXT NOT NULL," + "icon TEXT,"
				+ "type TEXT NOT NULL CHECK(type IN ('PROYECTO','COMUNIDAD','CANAL'))," + "parentRoomId TEXT,"
				+ "categoria TEXT," + "descripcion TEXT," + "estado TEXT," + "reglas TEXT," + "propuesta TEXT,"
				+ "tecnologias TEXT," + "userCreator TEXT NOT NULL," + "createdAt TEXT NOT NULL,"
				+ "FOREIGN KEY(userCreator) REFERENCES User(id)" + ");";
		try (PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.executeUpdate();
		}
	}

	private void createTableRoomMembers(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS RoomMembers (" + "userId TEXT NOT NULL," + "roomId TEXT NOT NULL,"
				+ "PRIMARY KEY(userId, roomId)," + "FOREIGN KEY(userId) REFERENCES User(id),"
				+ "FOREIGN KEY(roomId) REFERENCES Room(id)" + ");";
		try (PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.executeUpdate();
		}
	}

	private void createTableMessage(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS Message (" + "id TEXT PRIMARY KEY ," + "roomId TEXT NOT NULL,"
				+ "userId TEXT NOT NULL," + "body TEXT NOT NULL," + "createdAt TEXT NOT NULL,"
				+ "FOREIGN KEY(roomId) REFERENCES Room(id)," + "FOREIGN KEY(userId) REFERENCES User(id)" + ");";
		try (PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.executeUpdate();
		}
	}

	private static boolean searchUser(String userId) {
		String sql = "SELECT COUNT(*) FROM User WHERE id=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {

			prst.setString(1, userId.trim());

			try (ResultSet rs = prst.executeQuery();) {
				if (rs.next()) {
					if (rs.getInt(1) > 0) {
						return true;
					} else {
						return false;
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al buscar un usuario: " + e.getMessage());
			System.out.println(e.getStackTrace());
			return false;
		}
		return false;
	}

	// Inserción en tablas
	public static void insertUser(Usuario user) {

		if (!searchUser(user.getId())) {
			if (listaUsers != null) {
				listaUsers.add(user);
			}
			String sql = "INSERT INTO User (id,name,password,accessToken,email,htmlURL,avatarURL,createdAt,lastLoginAt)"
					+ " VALUES (?,?,?,?,?,?,?,?,?);";
			try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {

				prst.setString(1, user.getId());
				prst.setString(2, user.getName());
				prst.setString(3, user.getPassword());
				prst.setString(4, user.getAccessToken());
				prst.setString(5, user.getEmail());
				prst.setString(6, user.getHtmlURL());
				prst.setString(7, user.getMxcUrl());
				prst.setString(8, user.getCreatedAt());
				prst.setString(9, user.getLastLoginAt());
				prst.executeUpdate();
				System.out.println("-Usuario " + user.getName() + " añadido a la bbdd!");
			} catch (SQLException e) {
				System.err.println("--Error al registrar un usuario: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static boolean insertRoom(Room rm) {

		// Esto lo hacemos para que no pueda agregar una room que ya se agregó
		for (Room sala : Main.getUserMain().getListaRooms()) {
			if (rm.getId() == sala.getId()) {
				return false;
			}
		}

		String sql = "INSERT INTO Room (id,alias,name,icon,type,parentRoomId,categoria,descripcion,"
				+ "estado,reglas,propuesta,tecnologias,userCreator,createdAt)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {

			prst.setString(1, rm.getId());
			prst.setString(2, rm.getAlias());
			prst.setString(3, rm.getName());
			prst.setString(4, rm.getMxcUrl());
			prst.setString(5, rm.getType());
			prst.setString(6, rm.getParentRoomId());
			prst.setString(7, rm.getCategoria());
			prst.setString(8, rm.getDescripcion());
			prst.setString(9, rm.getEstado());
			prst.setString(10, rm.getReglas());
			prst.setString(11, rm.getPropuesta());
			prst.setString(12, rm.getTecnologias());
			prst.setString(13, rm.getUserCreator());
			prst.setString(14, rm.getCreatedAt());
			prst.executeUpdate();

			return true;
		} catch (SQLException e) {
			// La única forma en la que nos meta en el catch es que la room ya este en la
			// bbdd, pero no esta asociada al usuarioMain por lo que los asociamos
			if (insertRoomMember(Main.getUserMain().getId(), rm.getId())) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static void insertMensaje(Mensaje msj) {
		String sql = "INSERT INTO Message (id,roomId,userId,body,createdAt) VALUES (?,?,?,?,?);";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, msj.getId());
			prst.setString(2, msj.getRoomId());
			prst.setString(3, msj.getUser().getId());
			prst.setString(4, msj.getBody());
			prst.setString(5, msj.getCreatedAt() + " " + msj.getMinutes());// fecha y hora
			prst.executeUpdate();
		} catch (SQLException e) {
			System.err.println("--Error al insertar un mensaje: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static boolean mensajeExists(String idMsj) {
		String sql = "SELECT COUNT(*) FROM Message WHERE id=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, idMsj);

			try (ResultSet rs = prst.executeQuery()) {
				if (rs.next()) {
					if (rs.getInt(1) > 0) {
						return true;
					} else {
						return false;
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al insertar un mensaje: " + e.getMessage());
		}
		return false;
	}

	public static void cargarMensajesRoom(Room room) {
		ArrayList<Mensaje> listaMensajes = new ArrayList<Mensaje>();
		String sql = "SELECT * FROM Message WHERE roomId=?;";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, room.getId());

			try (ResultSet rs = prst.executeQuery()) {
				while (rs.next()) {
					rs.getString(1);// id
					rs.getString(2);// roomId
					rs.getString(3);// userId
					rs.getString(4);// body
					rs.getString(5);// createdAt

					Mensaje msj = new Mensaje(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
							rs.getString(5));
					listaMensajes.add(msj);
				}
				if (listaMensajes.size() > 0) {
					Component[] listaPaneles = Home.panel.getComponents();
					for (int j = 1; j < listaPaneles.length; j++) {
						if (listaPaneles[j] instanceof PanelChat) {// Hacemos otra validacion por si acaso
							PanelChat panel = (PanelChat) listaPaneles[j];
							if (panel.room.getId().equals(room.getId())) {// Si es true quiere
								// decir que en ese room
								for (Mensaje msj : listaMensajes) {
									if (!msj.getUser().getId().equals(Main.getUserMain().getId())) {
										panel.panelChat.add(msj, "align right");
										room.getListaMensajes().add(msj);
									} else {
										panel.panelChat.add(msj);
										room.getListaMensajes().add(msj);
									}
//									}
								} // hay un mensaje nuevo
								panel.revalidate(); // Recalcula layout
								panel.repaint(); // Redibuja visualmente
								break;
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al insertar un mensaje: " + e.getMessage());
		}
	}

	public static boolean insertRoomMember(String idUser, String idRoom) {
		// Para saber si el user ya esta en la tabla y no repetirlo dando error
		String sqlInsercion = "INSERT INTO RoomMembers (userId,roomId) VALUES (?,?);";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlInsercion)) {
			prst.setString(1, idUser);
			prst.setString(2, idRoom);
			prst.executeUpdate();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	// Consultas BBDD
	public static String buscarAccessTokenUsuario(Usuario user) {
		String sql = "SELECT id,avatarURL,accessToken FROM User WHERE name = ?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, user.getName());
			try (ResultSet rs = prst.executeQuery()) {
				if (rs.next()) {
					user.setId(rs.getString(1));
					user.setMxcUrl(rs.getString(2));
					user.setIcon(rs.getString(2));
					return rs.getString(3);
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al buscar al usuarioMain en la BBDD: " + e.getMessage());
		}
		return null;
	}

	public static ArrayList getUserRooms() {
		ArrayList<Room> roomList = new ArrayList<>();
		// Esto lo hacemos para que si el usuario inicia con otra cuenta no se carguen
		// las rooms de la otra cuenta tambien y den errores
		String sql = "SELECT * FROM Room AS room INNER JOIN RoomMembers AS member ON member.roomId = room.id WHERE member.userId = ?;";

		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, Main.getUserMain().getId());

			try (ResultSet rs = prst.executeQuery()) {
				while (rs.next()) {

					Room sala;
					if (rs.getString(5).equals("PROYECTO")) {
						// El Room es un Proyecto
						sala = new Proyecto(rs.getString(1), rs.getString(3), rs.getString(7), rs.getString(8),
								rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12), null,
								rs.getString(6), rs.getString(2), rs.getString(13), rs.getString(14), rs.getString(5));
						sala.setMxcUrl(rs.getString(4));
						sala.setIcon(rs.getString(4));
					} else if (rs.getString(5).equals("COMUNIDAD")) {
						// El Room es una Comunidad
						sala = new Comunidad(rs.getString(1), rs.getString(6), rs.getString(2), rs.getString(13),
								rs.getString(14), rs.getString(5), null, rs.getString(3), rs.getString(7),
								rs.getString(8), rs.getString(10), rs.getString(11));
						sala.setMxcUrl(rs.getString(4));
						sala.setIcon(rs.getString(4));
					} else {
						sala = new Canal(rs.getString(1), rs.getString(6), rs.getString(2), rs.getString(13),
								rs.getString(3), rs.getString(14), "Canal", null);
					}

					roomList.add(sala);
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al buscar las Rooms a las que pertenece el usuario: " + e.getMessage());
		}
		return roomList;
	}

	public static void deleteUser(String userId) {
		if (!userId.equals(Main.getUserMain().getId())) {
			String sqlBusqueda = "DELETE FROM User WHERE id = ?;";
			try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
				prst.setString(1, userId);
				prst.executeUpdate();

			} catch (SQLException e) {
				System.err.println(
						"--Error al borrar un usuario de la room (se salio de la room en matrix): " + e.getMessage());
			}
		}
	}

	public static ArrayList<Usuario> cargarUsuarios() {
		String sql = "SELECT * FROM User;";
		ArrayList<Usuario> listaUsers = new ArrayList<>();

		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			try (ResultSet rs = prst.executeQuery()) {
				while (rs.next()) {
					Usuario user = new Usuario.Builder().setId(rs.getString(1)).setName(rs.getString(2))
							.setAccessToken(rs.getString(4)).setEmail(rs.getString(5)).setHtmlURL(rs.getString(6))
							.setMxcUrl(rs.getString(7)).setCreatedAt(rs.getString(8)).setLastLoginAt(rs.getString(9))
							.build();

					listaUsers.add(user);
				}
				return listaUsers;
			}
		} catch (SQLException e) {
			System.err.println("--Error al cargar los usuario de la bbdd : " + e.getMessage());
			return listaUsers;
		}
	}

	public static void deleteRoomMember(Usuario user, String idRoom) {
		String sqlBusqueda = "DELETE FROM RoomMembers WHERE userId = ? AND roomId=?;";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, user.getId());
			prst.setString(2, idRoom);
			prst.executeUpdate();
			// Si el usuario ya no pertenece a ninguna sala en comun con la nuestra se
			// eliminara de nuestra bbdd
			int i = numeroDeRoomALasQuePerteneceUnUser(user.getId());
			if (i < 0 || i == 0) {// Si el usuario ya no pertenece a ningun room en el que coincida con el
									// userMain se elimina de la bbdd

				if (Main.getUserMain().getId() != user.getId()) {// Esto lo hacemos para evitar borrar nuestro propio
																	// user si
					listaUsers.remove(user); // no pertenecemos a ninguna room
					deleteUser(user.getId());
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al borrar un usuario de la tabla RoomMember " + e.getMessage());
		}
	}

	private static int numeroDeRoomALasQuePerteneceUnUser(String userId) {
		String sqlBusqueda = "SELECT COUNT(*) FROM RoomMembers WHERE userId=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, userId);

			try (ResultSet rs = prst.executeQuery();) {
				rs.next();
				return rs.getInt(1);

			}

		} catch (SQLException e) {
			System.err.println("--Error al comprobar el numero de rooms a las que pertenece el usuario " + userId + " "
					+ e.getMessage());
			return -1;
		}
	}

	// Esto lo hacemos para comprobar que
	// no esta ya en el room en el que
	// ya lo ha podido meter en el
	public static boolean isUserInRoom(String userId, String roomId) {
		String sqlBusqueda = "SELECT roomId FROM RoomMembers WHERE userId=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, userId);

			try (ResultSet rs = prst.executeQuery();) {
				while (rs.next()) {
					if (rs.getString(1).equals(roomId)) {
						return true;
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("--Error al comprobar el numero de rooms a las que pertenece el usuario " + userId + " "
					+ e.getMessage());
		}
		return false;
	}

	public static void borrarRastroRoom(Room room) {
		if (roomExists(room.getId())) {// si existe lo borramos
			ArrayList<Usuario> temp = new ArrayList<>(listaUsers);
			if (!room.getUserCreator().equals(Main.getUserMain().getId())) {// Esto es para los usuario que no sean el
																			// creador
				boolean isOtherUserMainInRoom = false;

				for (Usuario user : temp) {// verificamos que no haya otra sesion del usuario unida al room
					if (user.getAccessToken() != null && isUserInRoom(user.getId(), room.getId())
							&& !user.getId().equals(Main.getUserMain().getId())) {
						isOtherUserMainInRoom = true;
						break;
					}
				}
				if (isOtherUserMainInRoom) {// si hay otra sesion del usuario unida al room borramos solo el usuario de
											// esta sesion de la room para que cuando inice sesion con el otro usuario
											// tenga el room con todos sus usuario,mensajes,...
					deleteRoomMember(Main.getUserMain(), room.getId());
					return;
				}
				deleteMensajesRoom(room.getId());
				for (Usuario user : temp) {
					deleteRoomMember(user, room.getId());
				}
				deleteRoom(room.getId());
			} else {// Si el usuario es el creador borramos todo rastro del room
				deleteMensajesRoom(room.getId());
				for (Usuario user : temp) {
					deleteRoomMember(user, room.getId());
				}
				deleteRoom(room.getId());
			}
		}
	}

	private static boolean roomExists(String roomId) {
		String sqlBusqueda = "SELECT COUNT(*) FROM Room WHERE id=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, roomId);

			try (ResultSet rs = prst.executeQuery();) {
				if (rs.next()) {
					if (rs.getInt(1) > 0) {
						return true;
					} else {
						return false;
					}
				}
			}

		} catch (SQLException e) {
			System.err.println("--Error al buscar el Room " + roomId + " " + e.getMessage());
		}
		return false;
	}

	private static void deleteRoom(String roomId) {
		String sqlBusqueda = "DELETE FROM Room WHERE id = ?;";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, roomId);
			prst.executeUpdate();
		} catch (SQLException e) {
			System.err.println("--Error al borrar una Room " + e.getMessage());
		}
	}

	private static void deleteMensajesRoom(String roomId) {
		String sqlBusqueda = "DELETE FROM Message WHERE roomId = ?;";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sqlBusqueda)) {
			prst.setString(1, roomId);
			prst.executeUpdate();
		} catch (SQLException e) {
			System.err.println("--Error al borrar los mensajes de laRoom " + roomId + " " + e.getMessage());
		}
	}

	public static void actualizarSinceUser() {
		String sql = "UPDATE User SET since = ? WHERE id = ?";

		try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, VistaApp.getSince());
			pstmt.setString(2, Main.getUserMain().getId());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("--Error al actualizar el since del userMain" + e.getMessage());
		}
	}

	public static void cargarSinceUserMain() {
		String sql = "SELECT since FROM User WHERE id=?";
		try (Connection conn = getConnection(); PreparedStatement prst = conn.prepareStatement(sql)) {
			prst.setString(1, Main.getUserMain().getId());

			try (ResultSet rs = prst.executeQuery();) {
				if (rs.next()) {
					Main.getUserMain().setSince(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			System.err.println("--Error al cargar el since del userMain: " + e.getMessage());
			System.out.println(e.getStackTrace());
		}
	}
}
