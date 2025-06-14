package modelo;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JOptionPane;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import conexionBD.ConexionSQLite;
import login.Authentification.Main;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import vista.ButtonUser;
import vista.Home;
import vista.PanelChat;

public class MatrixManager {
	public static final String SERVER_URL = "https://matrixserver1.duckdns.org";
	private static final String ADMIN_TOKEN = "syt_YWRtaW4_EZBtpinaLZzqLKpBBCSb_3fbfSf";

	private static final OkHttpClient client = new OkHttpClient.Builder()
			.protocols(Collections.singletonList(Protocol.HTTP_1_1)).retryOnConnectionFailure(true).build();

	private static final Gson gson = new Gson();
	private static final MediaType JSON = MediaType.parse("application/json");

	public static void registerUser(String username, String password, URL avatarUser, boolean isAdmin)
			throws IOException {

		String userId = "@" + username + ":matrixserver1.duckdns.org";
		String url = SERVER_URL + "/_synapse/admin/v2/users/" + userId;
		String json = gson
				.toJson(Map.of("password", password, "admin", isAdmin, "displayname", userId, "logout_devices", false));

		Request request = new Request.Builder().url(url)
				.put(RequestBody.create(MediaType.parse("application/json"), json))
				.addHeader("Authorization", "Bearer " + ADMIN_TOKEN).build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				// Alta OK: seguimos con login + avatar + html_url
				loginUser(userId, password, avatarUser);

			} else if (response.code() == 400 || response.code() == 409) {
				// Usuario ya existe o alias en uso: hacemos login de todos modos
				System.out.println("ℹ️ Usuario ya existente, procedo a login en su lugar.");
				loginUser(userId, password, avatarUser);

			} else {
				// Cualquier otro fallo lo propagamos
				throw new IOException("Error al registrar usuario: HTTP " + response.code());
			}

		} catch (Exception e) {
			System.out.println("Error al conectar al usuario a Matrix!!");
		}
	}

	/**
	 * Hace login de un usuario, sube su avatar, lo asigna al perfil y añade un
	 * campo account_data con su html_url.
	 */
	public static void loginUser(String userId, String password, URL avatarUser) {
		Usuario user = Main.getUserMain();
		user.setId(null);
		user.setAccessToken(null);

		String urlLogin = SERVER_URL + "/_matrix/client/r0/login";
		JsonObject bodyJson = new JsonObject();
		bodyJson.addProperty("type", "m.login.password");
		bodyJson.addProperty("user", userId);
		bodyJson.addProperty("password", password);

		RequestBody body = RequestBody.create(JSON, bodyJson.toString());
		Request request = new Request.Builder().url(urlLogin).post(body).addHeader("Content-Type", "application/json")
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Login fallido: " + response.code());
			}
			String resp = response.body().string();
			String accessToken = gson.fromJson(resp, LoginResponse.class).access_token;

			// 1) Guardamos token e id en Usuario y SQLite
			user.setAccessToken(accessToken);
			user.setId(userId);

			// 2) Subimos avatar y obtenemos MXC
			String mxcUrl = uploadUserIcon(avatarUser, accessToken);
			user.setMxcUrl(mxcUrl);

			// 3) Asignamos el avatar en el perfil
			setUserAvatar(userId, mxcUrl, accessToken);

			// 4) Guardamos el html_url en account_data
			try {
				setUserHtmlUrl(userId, user.getHtmlURL(), accessToken);
			} catch (IOException e) {
				System.err.println("❌ No se pudo guardar html_url del usuario:");
				e.printStackTrace();
			}

			// 5) Persistimos en BD local
			ConexionSQLite.insertUser(user);

		} catch (Exception e) {
			System.err.println("Error al iniciar sesión y asignar avatar/html_url:");
			e.printStackTrace();
		}
	}

	/**
	 * Crea o actualiza un evento de account_data personalizado con la URL HTML del
	 * usuario.
	 */
	public static void setUserHtmlUrl(String userId, String htmlUrl, String accessToken) throws IOException {
		// Definimos un tipo propio para almacenar el html_url
		String type = "com.miapp.user.html_url";
		String pathUser = URLEncoder.encode(userId, "UTF-8");
		String url = SERVER_URL + "/_matrix/client/r0/user/" + pathUser + "/account_data/" + type;

		JsonObject bodyJson = new JsonObject();
		bodyJson.addProperty("html_url", htmlUrl);

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), bodyJson.toString());
		Request req = new Request.Builder().url(url).put(body).addHeader("Authorization", "Bearer " + accessToken)
				.build();

		try (Response resp = client.newCall(req).execute()) {
			if (!resp.isSuccessful()) {
				throw new IOException("Error guardando html_url: " + resp.code() + " / " + resp.body().string());
			}
		}
	}

	public static String uploadUserIcon(URL avatarUrl, String accessToken) throws IOException {
		// 1) Abrir conexión para detectar Content-Type y leer bytes
		URLConnection urlConn = avatarUrl.openConnection();
		String contentType = urlConn.getContentType(); // e.g. "image/jpeg"
		MediaType mediaType = MediaType.parse(contentType != null ? contentType : "application/octet-stream");

		byte[] imageBytes;
		try (InputStream in = urlConn.getInputStream()) {
			imageBytes = in.readAllBytes();
		}

		// 2) Construye un filename a partir de la ruta de la URL
		String path = avatarUrl.getPath(); // e.g. "/u/12345/avatar.png"
		String filename = Paths.get(path).getFileName().toString(); // "avatar.png"
		String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name());

		// 3) Prepara endpoint de Matrix para subir media
		String uploadUrl = SERVER_URL + "/_matrix/media/r0/upload?filename=" + encodedFilename;

		RequestBody body = RequestBody.create(mediaType, imageBytes);
		Request request = new Request.Builder().url(uploadUrl).post(body)
				.addHeader("Authorization", "Bearer " + accessToken).build();

		// 4) Ejecuta la petición y parsea la respuesta
		try (Response resp = client.newCall(request).execute()) {
			if (!resp.isSuccessful()) {
				throw new IOException("Error subiendo avatar: " + resp.code() + " → " + resp.body().string());
			}
			JsonObject json = gson.fromJson(resp.body().string(), JsonObject.class);
			return json.get("content_uri").getAsString(); // e.g. "mxc://matrix.org/AbC123..."
		}
	}

	private static boolean setUserAvatar(String userId, String mxcUrl, String accessToken) throws IOException {
		String url = SERVER_URL + "/_matrix/client/r0/profile/" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
				+ "/avatar_url";

		JsonObject bodyJson = new JsonObject();
		bodyJson.addProperty("avatar_url", mxcUrl);

		RequestBody body = RequestBody.create(JSON, bodyJson.toString());
		Request request = new Request.Builder().url(url).put(body).addHeader("Authorization", "Bearer " + accessToken)
				.addHeader("Content-Type", "application/json").build();

		try (Response resp = client.newCall(request).execute()) {
			if (!resp.isSuccessful()) {
				throw new IOException("Error asignando avatar: " + resp.code() + " → " + resp.body().string());
			}
			return true;
		}
	}

	private static class LoginResponse {
		String access_token;
	}

	public static void hardDeleteUser(String username) throws IOException {
		String userId = "@" + username + ":matrixserver1.duckdns.org";

		// 1) Deactivate + erase
		System.out.println("→ Deactivating " + userId);
		Response r1 = sendAdminPostRaw("/_synapse/admin/v1/deactivate/" + userId, Map.of("erase", true));
		System.out.println("HTTP " + r1.code() + " → " + r1.body().string());

		// 2) Delete devices
		System.out.println("→ Deleting devices of " + userId);
		Response r2 = sendAdminPostRaw("/_synapse/admin/v2/users/" + userId + "/delete_devices",
				Map.of("devices", List.of()));
		System.out.println("HTTP " + r2.code() + " → " + r2.body().string());

		// 3) Delete media
		System.out.println("→ Deleting media of " + userId);
		Response r3 = sendAdminDeleteRaw("/_synapse/admin/v1/users/" + userId + "/media");
		System.out.println("HTTP " + r3.code() + " → " + r3.body().string());
	}

	private static Response sendAdminPostRaw(String path, Object bodyObj) throws IOException {
		String json = gson.toJson(bodyObj);
		Request req = new Request.Builder().url(SERVER_URL + path).addHeader("Authorization", "Bearer " + ADMIN_TOKEN)
				.post(RequestBody.create(JSON, json)).build();
		return client.newCall(req).execute();
	}

	private static Response sendAdminDeleteRaw(String path) throws IOException {
		Request req = new Request.Builder().url(SERVER_URL + path).addHeader("Authorization", "Bearer " + ADMIN_TOKEN)
				.delete().build();
		return client.newCall(req).execute();
	}

	// Para canales/subchatsl
	public String getSubchatIdByName(String parentRoomId, String subchatName) {
		// Asegura que el usuario esté autenticado
		if (Main.getUserMain().getAccessToken() == null) {
			System.err.println("❌ No hay token de acceso. Registra/inicia sesión primero.");
			return null;
		}

		// Codificar el nombre del subchat
		String encodedSubchatName = URLEncoder.encode(subchatName, StandardCharsets.UTF_8);
		String url = SERVER_URL + "/_matrix/client/r0/rooms/" + parentRoomId + "/directory/room/" + encodedSubchatName;

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet request = new HttpGet(url);
			request.setHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken()); // Autenticación con
																									// Bearer token
			request.setHeader("Accept", "application/json");

			String response = client.execute(request, resp -> EntityUtils.toString(resp.getEntity()));
			JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

			String subchatId = jsonResponse.get("room_id").getAsString();
			System.out.println("✅ Subchat encontrado con nombre: " + subchatName + ", ID: " + subchatId);
			return subchatId;
		} catch (Exception e) {
			System.err.println("❌ Error al obtener el subchat por nombre: " + e.getMessage());
			return null;
		}
	}

	public static String createRoomWithParent(String name, String parentRoomId, String accessToken) throws IOException {
		if (accessToken == null) {
			System.err.println("❌ No hay token de acceso. Registra/inicia sesión primero.");
			return null;
		}

		// Crear el nuevo sub-room
		String url = SERVER_URL + "/_matrix/client/r0/createRoom";
		JsonObject bodyJson = new JsonObject();
		bodyJson.addProperty("name", name);
		bodyJson.addProperty("preset", "public_chat");

		// Agregar relación con room padre (espacio)
		JsonObject parentEvent = new JsonObject();
		parentEvent.addProperty("via", "matrixserver1.duckdns.org");
		JsonObject creationContent = new JsonObject();
		creationContent.add("m.space.parent", parentEvent);
		bodyJson.add("creation_content", creationContent);

		Request request = new Request.Builder().url(url).post(RequestBody.create(JSON, bodyJson.toString()))
				.addHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken())
				.addHeader("Content-Type", "application/json").build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("❌ Error al crear la sala hija: " + response.body().string());
				return null;
			}
			String responseBody = response.body().string();
			JsonObject json = gson.fromJson(responseBody, JsonObject.class);
			String roomId = json.get("room_id").getAsString();
			System.out.println("✅ Sala hija creada con nombre: " + name + ", ID: " + roomId);

			// Relacionarla explícitamente con el espacio padre (opcional si no se usa
			// creation_content correctamente)
			linkRoomToParent(parentRoomId, roomId);
			return roomId;
		}
	}

	private static void linkRoomToParent(String parentRoomId, String childRoomId) throws IOException {
		String url = SERVER_URL + "/_matrix/client/v1/rooms/" + parentRoomId + "/send/m.space.child/"
				+ System.currentTimeMillis();
		JsonObject childContent = new JsonObject();
		childContent.addProperty("via", "matrixserver1.duckdns.org");
		childContent.addProperty("room_id", childRoomId);

		Request request = new Request.Builder().url(url).put(RequestBody.create(JSON, childContent.toString()))
				.addHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken())
				.addHeader("Content-Type", "application/json").build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				System.err.println("❌ Error al vincular el sub-room al room padre: " + response.body().string());
			} else {
				System.out.println("🔗 Sub-room vinculado correctamente al room padre.");
			}
		}
	}

	private static byte[] compressIfNeeded(File imageFile) throws IOException {
		BufferedImage img = ImageIO.read(imageFile);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(0.8f); // Ajusta la calidad según tus necesidades

		try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
			writer.setOutput(ios);
			writer.write(null, new IIOImage(img, null, null), param);
		}
		writer.dispose();

		byte[] compressed = baos.toByteArray();
		final int MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2MB

		if (compressed.length > MAX_SIZE_BYTES) {
			throw new IOException("Imagen demasiado grande tras compresión: " + (compressed.length / 1024) + " KB");
		}
		return compressed;
	}

	private static String uploadRoomIcon(File imageFile, String accessToken) throws IOException {
		String filename = imageFile.getName().toLowerCase();
		String mime;
		if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
			mime = "image/jpeg";
		} else if (filename.endsWith(".png")) {
			mime = "image/png";
		} else if (filename.endsWith(".ico")) {
			mime = "image/vnd.microsoft.icon";
		} else {
			mime = Files.probeContentType(imageFile.toPath());
			if (mime == null)
				mime = "application/octet-stream";
		}
		MediaType mediaType = MediaType.parse(mime);

		String encoded = URLEncoder.encode(imageFile.getName(), StandardCharsets.UTF_8.name());
		String uploadUrl = SERVER_URL + "/_matrix/media/r0/upload?filename=" + encoded;

		// Intentar comprimir si hace falta
		byte[] bytes;
		try {
			bytes = compressIfNeeded(imageFile);
		} catch (IOException e) {
			bytes = Files.readAllBytes(imageFile.toPath());
		}

		RequestBody body = RequestBody.create(mediaType, bytes);
		Request request = new Request.Builder().url(uploadUrl).addHeader("Authorization", "Bearer " + accessToken)
				.addHeader("Content-Type", mime).post(body).build();

		try (Response resp = client.newCall(request).execute()) {
			if (!resp.isSuccessful()) {
				int code = resp.code();
				String respBody = resp.body().string();
				if (code == 413 || respBody.contains("M_TOO_LARGE")) {
					// Mostrar diálogo en el EDT
					JOptionPane.showMessageDialog(null, "El icono de la sala debe pesar menos de 2 MB",
							"Icono demasiado grande", JOptionPane.WARNING_MESSAGE);

					return null; // indica a quien llame que no se subió
				}
				throw new IOException("Error subiendo imagen: " + code + " → " + respBody);
			}
			JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
			return json.get("content_uri").getAsString();
		}
	}

	public static ArrayList<Usuario> getRoomMembers(String roomId) throws Exception {
		// 1. Hacemos la petición HTTP
		URL url = new URL(SERVER_URL + "/_matrix/client/v3/rooms/" + roomId + "/joined_members");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Authorization", "Bearer " + Main.getUserMain().getAccessToken());

		// 2. Leemos la respuesta
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = in.readLine()) != null)
			sb.append(line);
		in.close();

		// 3. Parseamos con Gson
		JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
		JsonObject joined = root.getAsJsonObject("joined");

		// 4. Iteramos sobre los miembros
		ArrayList<Usuario> members = new ArrayList<>();
		for (Map.Entry<String, JsonElement> entry : joined.entrySet()) {
			String userId = entry.getKey();
			JsonObject data = entry.getValue().getAsJsonObject();

			// Usamos getAsString(), y if-check para valores opcionales
			String displayName = data.has("display_name") ? data.get("display_name").getAsString() : userId;
			String avatarUrl = data.has("avatar_url") ? data.get("avatar_url").getAsString() : null;

			members.add(new Usuario.Builder().setId(userId).setName(displayName).setAvatarURL(avatarUrl).build());
			ConexionSQLite.listaUsers
					.add(new Usuario.Builder().setId(userId).setName(displayName).setMxcUrl(avatarUrl).build());
		}
		// room.setRoomMembers(getRoomMembers(idRoom));
		return members;
	}

	public static ArrayList<Room> getRoomByAliasOrCategoria(String term, int limit) {
		// 1) Preparamos JSON para /publicRooms SIN filtro (o con término vacío)
		JsonObject filter = new JsonObject();
		// Al no añadir ninguna propiedad “generic_search_term”, el servidor devolverá
		// hasta `limit` salas públicas ordenadas (p. ej. por número de miembros).
		JsonObject bodyJson = new JsonObject();
		bodyJson.add("filter", filter);
		bodyJson.addProperty("limit", limit);

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), bodyJson.toString());

		// 2) Llamada a /publicRooms
		Request req = new Request.Builder().url(SERVER_URL + "/_matrix/client/r0/publicRooms").post(body)
				.addHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken())
				.addHeader("Accept", "application/json").addHeader("Content-Type", "application/json").build();

		ArrayList<Room> results = new ArrayList<>();

		try (Response resp = client.newCall(req).execute()) {
			String responseBody = resp.body() != null ? resp.body().string() : "";
			if (!resp.isSuccessful()) {
				throw new IOException("Error en publicRooms: HTTP " + resp.code() + " - " + responseBody);
			}

			JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
			JsonArray chunk = root.getAsJsonArray("chunk");

			for (JsonElement elem : chunk) {
				JsonObject o = elem.getAsJsonObject();
				String roomId = o.get("room_id").getAsString();
				String name = o.has("name") ? o.get("name").getAsString() : null;
				String avatar = o.has("avatar_url") ? o.get("avatar_url").getAsString() : null;
				String canonicalAlias = o.has("canonical_alias") ? o.get("canonical_alias").getAsString() : null;

				if (canonicalAlias != null) {
					// Quitamos el prefijo "#" y el dominio
					canonicalAlias = canonicalAlias.substring(1).replace(":matrixserver1.duckdns.org", "");
				}

				String creator = null;
				String fechaCorrecta = null;

				// Variables para atributos personalizados
				String propuesta = "";
				String reglas = "";
				String categoriaAttr = "";
				String descripcion = "";
				String estadoAttr = "";
				String tecnologiasAttr = "";

				// ------------ Obtenemos TODO el state de la sala vía Admin API ------------
				try {
					String urlAdminState = SERVER_URL + "/_synapse/admin/v1/rooms/" + roomId + "/state";
					Request reqAdmin = new Request.Builder().url(urlAdminState).get()
							.addHeader("Authorization", "Bearer " + ADMIN_TOKEN).addHeader("Accept", "application/json")
							.build();

					try (Response respAdmin = client.newCall(reqAdmin).execute()) {
						String bodyAdmin = respAdmin.body() != null ? respAdmin.body().string() : "";

						if (respAdmin.isSuccessful()) {
							JsonElement parsed = JsonParser.parseString(bodyAdmin);
							JsonArray stateArray;

							if (parsed.isJsonObject()) {
								JsonObject adminRoot = parsed.getAsJsonObject();
								if (adminRoot.has("state") && adminRoot.get("state").isJsonArray()) {
									stateArray = adminRoot.getAsJsonArray("state");
								} else {
									stateArray = new JsonArray();
								}
							} else if (parsed.isJsonArray()) {
								stateArray = parsed.getAsJsonArray();
							} else {
								stateArray = new JsonArray();
							}

							for (JsonElement stateElem : stateArray) {
								JsonObject stateEvent = stateElem.getAsJsonObject();
								String type = stateEvent.get("type").getAsString();
								JsonObject content = stateEvent.getAsJsonObject("content");

								// m.room.create → creator + fecha
								if ("m.room.create".equals(type)) {
									if (content.has("creator") && !content.get("creator").isJsonNull()) {
										creator = content.get("creator").getAsString();
									}
									if (stateEvent.has("origin_server_ts")) {
										long ts = stateEvent.get("origin_server_ts").getAsLong();
										Instant instant = Instant.ofEpochMilli(ts);
										LocalDateTime createdAt = LocalDateTime.ofInstant(instant,
												ZoneId.systemDefault());
										SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
										Date date = Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
										fechaCorrecta = formatter.format(date);
									}
								}
								// Evento com.miapp.room.propuesta
								else if ("com.miapp.room.propuesta".equals(type)) {
									if (content.has("texto") && !content.get("texto").isJsonNull()) {
										propuesta = content.get("texto").getAsString();
									}
								}
								// Evento com.miapp.room.reglas
								else if ("com.miapp.room.reglas".equals(type)) {
									if (content.has("texto") && !content.get("texto").isJsonNull()) {
										reglas = content.get("texto").getAsString();
									}
								}
								// Evento com.miapp.room.categoria ← aquí está tu categoría
								else if ("com.miapp.room.categoria".equals(type)) {
									if (content.has("nombre") && !content.get("nombre").isJsonNull()) {
										categoriaAttr = content.get("nombre").getAsString();
									}
								}
								// Evento com.miapp.room.descripcion
								else if ("com.miapp.room.descripcion".equals(type)) {
									if (content.has("nombre") && !content.get("nombre").isJsonNull()) {
										descripcion = content.get("nombre").getAsString();
									}
								}
								// Evento com.miapp.room.estado
								else if ("com.miapp.room.estado".equals(type)) {
									if (content.has("nombre") && !content.get("nombre").isJsonNull()) {
										estadoAttr = content.get("nombre").getAsString();
									}
								}
								// Evento com.miapp.room.tecnologias
								else if ("com.miapp.room.tecnologias".equals(type)) {
									if (content.has("nombre") && !content.get("nombre").isJsonNull()) {
										tecnologiasAttr = content.get("nombre").getAsString();
									}
								}
								// Ignoramos el resto de eventos de state
							}
						} else {
							System.err.println("Admin API /state devolvió HTTP " + respAdmin.code() + " para roomId "
									+ roomId + " → " + bodyAdmin);
						}
					}
				} catch (Exception e) {
					System.err.println("Error obteniendo state vía Admin API para " + roomId + ": " + e.getMessage());
				}

				// Si no obtuvimos creator o fecha, ponemos valores por defecto
				if (creator == null)
					creator = "";
				if (fechaCorrecta == null)
					fechaCorrecta = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

				// 3) A partir de aquí hacemos el filtrado “cliente‐lado”:
				boolean matchAlias = (canonicalAlias != null)
						&& canonicalAlias.toLowerCase().contains(term.toLowerCase());
				boolean matchCategory = (categoriaAttr != null)
						&& categoriaAttr.toLowerCase().contains(term.toLowerCase());

				// Solo queremos salas que:
				// • coincidan en alias (matchAlias) OR
				// • coincidan en el contenido de com.miapp.room.categoria (matchCategory)
				if ((matchAlias || matchCategory) && canonicalAlias != null) {
					if (canonicalAlias.startsWith("P_")) {
						// 4.a) Es un Proyecto
						Room p = new Proyecto(roomId, name, categoriaAttr, descripcion, estadoAttr, reglas, propuesta,
								tecnologiasAttr, null, null, canonicalAlias, creator, fechaCorrecta, "PROYECTO");
						p.setMxcUrl(avatar);
						p.setIcon(avatar);
						results.add(p);
					} else if (canonicalAlias.startsWith("C_")) {
						// 4.b) Es una Comunidad
						Room c = new Comunidad(roomId, null, canonicalAlias, creator, fechaCorrecta, "COMUNIDAD", null,
								name, categoriaAttr, descripcion, reglas, propuesta);
						c.setMxcUrl(avatar);
						c.setIcon(avatar);
						results.add(c);
					}
					// … Otras lógicas de prefijos si las necesitaras …
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return results;
	}

	/**
	 * Envía un mensaje de texto a una sala Matrix.
	 *
	 * @param roomId      El ID completo de la sala (ej:
	 *                    "!AbC123:matrixserver1.duckdns.org").
	 * @param message     El texto que quieres enviar.
	 * @param accessToken El token de acceso del usuario.
	 * @return El event_id del mensaje enviado, o null en caso de error.
	 * @throws IOException Si falla la petición HTTP.
	 */
	public static String sendMessageToRoom(String roomId, String message, String accessToken) throws IOException {
		if (accessToken == null || accessToken.isEmpty()) {
			System.err.println("❌ No hay token de acceso. Registra/inicia sesión primero.");
			return null;
		}

		// 1) Generar txnId único (aquí usamos el timestamp)
		String txnId = "m" + System.currentTimeMillis();

		// 2) Construir la URL
		String url = SERVER_URL + "/_matrix/client/r0/rooms/" + URLEncoder.encode(roomId, StandardCharsets.UTF_8)
				+ "/send/m.room.message/" + txnId;

		// 3) Crear el cuerpo JSON
		JsonObject bodyJson = new JsonObject();
		bodyJson.addProperty("msgtype", "m.text");
		bodyJson.addProperty("body", message);

		RequestBody body = RequestBody.create(JSON, bodyJson.toString());

		// 4) Construir la petición HTTP
		Request request = new Request.Builder().url(url).put(body).addHeader("Authorization", "Bearer " + accessToken)
				.addHeader("Content-Type", "application/json").build();

		// 5) Ejecutar y manejar respuesta
		try (Response response = client.newCall(request).execute()) {
			String respBody = response.body().string();
			if (!response.isSuccessful()) {
				System.err.println("❌ Error al enviar mensaje: " + response.code() + " → " + respBody);
				return null;
			}
			// 6) Parsear el event_id
			JsonObject json = gson.fromJson(respBody, JsonObject.class);
			String eventId = json.get("event_id").getAsString();
			System.out.println("✅ Mensaje enviado. event_id: " + eventId);

			return eventId;
		}
	}

	public static void cargarUsuariosRoom(Room room) {
		try {
			// 1) Construir la URL de miembros
			String url = SERVER_URL + "/_matrix/client/v3/rooms/" + room.getId() + "/members";

			// 2) Abrir conexión HTTP y añadir encabezados
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + Main.getUserMain().getAccessToken());
			conn.setRequestProperty("Content-Type", "application/json");

			// 3) Leer código de respuesta
			int code = conn.getResponseCode();
			if (code != 200) {
				throw new RuntimeException("Error al cargar miembros: HTTP " + code);
			}

			// 4) Leer cuerpo y parsear JSON
			String body = new BufferedReader(new InputStreamReader(conn.getInputStream())).lines()
					.collect(Collectors.joining("\n"));
			JSONObject json = new JSONObject(body);
			org.json.JSONArray members = json.getJSONArray("chunk");

			// 5) Procesar cada evento de miembro
			for (int i = 0; i < members.length(); i++) {
				JSONObject event = members.getJSONObject(i);
				JSONObject content = event.getJSONObject("content");

				String membership = content.optString("membership", "");
				if (!"join".equals(membership)) {
					continue; // solo nos interesan los que ya están dentro
				}

				String userId = event.getString("state_key");
				String userName = content.optString("displayname", userId);
				String avatarMxc = content.optString("avatar_url", "");

				// ↓ NUEVO BLOQUE: obtener html_url desde account_data ↓
				String htmlUrl = "";
				try {
					String type = "com.miapp.user.html_url";
					String path = URLEncoder.encode(userId, "UTF-8");
					String urlHtml = SERVER_URL + "/_matrix/client/r0/user/" + path + "/account_data/" + type;
					HttpURLConnection conn2 = (HttpURLConnection) new URL(urlHtml).openConnection();
					conn2.setRequestMethod("GET");
					conn2.setRequestProperty("Authorization", "Bearer " + Main.getUserMain().getAccessToken());
					conn2.setRequestProperty("Content-Type", "application/json");

					if (conn2.getResponseCode() == 200) {
						String body2 = new BufferedReader(new InputStreamReader(conn2.getInputStream())).lines()
								.collect(Collectors.joining("\n"));
						JSONObject json2 = new JSONObject(body2);
						htmlUrl = json2.optString("html_url", "");
					}
				} catch (Exception ignored) {
					// Si falla, dejamos htmlUrl vacío
				}
				// ↑ FIN BLOQUE NUEVO ↑

				// 6) Crear Usuario y guardarlo (ahora con htmlUrl)
				Usuario user = new Usuario.Builder().setId(userId)
						.setName(userName.substring(1).replace(":matrixserver1.duckdns.org", "")).setMxcUrl(avatarMxc)
						.setHtmlURL(htmlUrl) // ← incluimos el html_url
						.build();

				if (!user.getId().equals(Main.getUserMain().getId())) {
					// Evitar duplicados en BD y lista
					boolean isAlreadyAdded = false;
					for (Usuario userBD : ConexionSQLite.getListaUsers()) {
						if (userBD.getId().equals(user.getId())) {
							isAlreadyAdded = true;
							break;
						}
					}
					if (!isAlreadyAdded) {
						ConexionSQLite.insertUser(user);
						ConexionSQLite.getListaUsers().add(user);
					}

					if (!ConexionSQLite.isUserInRoom(user.getId(), room.getId())) {
						ConexionSQLite.insertRoomMember(user.getId(), room.getId());
						room.getRoomMembers().add(user);

						Component[] listaPaneles = Home.panel.getComponents();
						for (int j = 1; j < listaPaneles.length; j++) {
							if (listaPaneles[j] instanceof PanelChat) {
								PanelChat panel = (PanelChat) listaPaneles[j];
								if (panel.room.getId().equals(room.getId())) {
									ButtonUser btnUser = new ButtonUser(user);
									panel.panelRight.getPanelUsuariosRoom().add(btnUser,
											"growx");
									btnUser.addActionListener(e -> {
										try {
											Desktop.getDesktop().browse(
													new URI(user.getHtmlURL()));
										} catch (IOException | URISyntaxException e1) {
											System.err.println(
													"Error al abrir el perfil del usuario - "
															+ e1);
										}
									});
									panel.panelRight.getPanelUsuariosRoom().revalidate();
									panel.panelRight.getPanelUsuariosRoom().repaint();
										
									break;
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Borrar Proyecto/Comunidad/Canal
	public static void deleteRoom(Room room) throws IOException {

		// Si el usuario no es el creador no podra eliminar el room
		if (!room.getUserCreator().equals(Main.getUserMain().getId()))
			return;

		String endpoint = SERVER_URL + "/_synapse/admin/v1/rooms/" + room.getId();

		String json = "{ \"block\": true, \"purge\": true }";

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

		Request request = new Request.Builder().url(endpoint).delete(body)
				.addHeader("Authorization", "Bearer " + ADMIN_TOKEN).addHeader("Content-Type", "application/json")
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (response.isSuccessful()) {
				System.out.println("✅ Sala eliminada correctamente: " + room.getId());
			} else {
				System.err.println("❌ Error al eliminar sala: " + room.getId());
				System.err.println("Código: " + response.code() + ", Cuerpo: " + response.body().string());
			}
		}
	}

	public static boolean joinRoomById(String roomId) throws IOException {
		// Codificar el roomId para que caracteres como "!" y ":" no rompan la URL
		String encodedRoomId = URLEncoder.encode(roomId, StandardCharsets.UTF_8.toString());

		// Construir la URL: POST /_matrix/client/r0/join/{roomId}
		String url = SERVER_URL + "/_matrix/client/r0/join/" + encodedRoomId;

		// El cuerpo puede ir vacío ("{}")
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{}");

		Request request = new Request.Builder().url(url).post(body)
				.addHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken())
				.addHeader("Accept", "application/json").addHeader("Content-Type", "application/json").build();

		try (Response response = client.newCall(request).execute()) {
			String respBody = response.body() != null ? response.body().string() : "";

			if (response.isSuccessful()) {
				// JSON de respuesta incluye "room_id", pero ya conocemos el roomId.
				JsonObject json = JsonParser.parseString(respBody).getAsJsonObject();
				String joinedRoom = json.has("room_id") ? json.get("room_id").getAsString() : roomId;
				System.out.println("Te has unido correctamente a la sala: " + joinedRoom);
				return true;
			} else {
				System.err.println(
						"Error al unirse a la sala " + roomId + ": HTTP " + response.code() + " - " + respBody);
				return false;
			}
		}
	}

	public static void leaveRoomById(String roomId) {
		String accessToken = Main.getUserMain().getAccessToken();

		String url = SERVER_URL + "/_matrix/client/v3/rooms/" + roomId + "/leave";

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.getOutputStream().write("{}".getBytes());

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				System.out.println("Se salió correctamente del room: " + roomId);
			} else {
				System.err.println("Error al salir del room " + roomId + ". Código: " + responseCode);
			}

			connection.disconnect();
		} catch (IOException e) {
			System.err.println("Excepción al salir del room: " + e.getMessage());
		}
	}

	public static boolean createRoomWithAlias(Room room, String accessToken, File iconFile) {
		if (accessToken == null || accessToken.isEmpty()) {
			System.err.println("❌ No hay token de acceso.");
			return false;
		}

		// 1) Construir alias con prefijo según tipo de sala
		String prefix = room.getType().equals("COMUNIDAD") ? "C_" : "P_";
		room.setAlias(prefix + room.getAlias());

		try {
			// 2) Subir el ícono y obtener la URL MXC
			String mxcUrl = uploadRoomIcon(iconFile, accessToken);
			if (mxcUrl==null) {
				return false;
			}

			// 3) Crear el JSON principal para la petición createRoom
			JsonObject bodyJson = new JsonObject();
			bodyJson.addProperty("room_alias_name", room.getAlias());
			bodyJson.addProperty("name", room.getName());
			bodyJson.addProperty("preset", "public_chat");
			bodyJson.addProperty("visibility", "public");

			// 4) Construir el array initial_state
			JsonArray initialState = new JsonArray();

			// 4.a) Evento de avatar (ya existente en tu código):
			JsonObject avatarEvent = new JsonObject();
			avatarEvent.addProperty("type", "m.room.avatar");
			avatarEvent.addProperty("state_key", "");
			JsonObject contentAvatar = new JsonObject();
			contentAvatar.addProperty("url", mxcUrl);

			// Aquí se determina MIME y tamaño del archivo (igual que antes)
			JsonObject info = new JsonObject();
			String filenameLower = iconFile.getName().toLowerCase();
			String mimeInfo;
			if (filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg")) {
				mimeInfo = "image/jpeg";
			} else if (filenameLower.endsWith(".png")) {
				mimeInfo = "image/png";
			} else if (filenameLower.endsWith(".ico")) {
				mimeInfo = "image/vnd.microsoft.icon";
			} else {
				mimeInfo = Files.probeContentType(iconFile.toPath());
				if (mimeInfo == null)
					mimeInfo = "application/octet-stream";
			}
			info.addProperty("mimetype", mimeInfo);
			info.addProperty("size", Files.size(iconFile.toPath()));

			contentAvatar.add("info", info);
			avatarEvent.add("content", contentAvatar);
			initialState.add(avatarEvent);

			// 4.b) **Evento personalizado: “propuesta”**
			if (room.getPropuesta() != null && !room.getPropuesta().isEmpty()) {
				JsonObject propuestaEvent = new JsonObject();
				propuestaEvent.addProperty("type", "com.miapp.room.propuesta");
				propuestaEvent.addProperty("state_key", "");
				JsonObject contentPropuesta = new JsonObject();
				contentPropuesta.addProperty("texto", room.getPropuesta());
				propuestaEvent.add("content", contentPropuesta);
				initialState.add(propuestaEvent);
			}

			// 4.c) **Evento personalizado: “reglas”**
			if (room.getReglas() != null && !room.getReglas().isEmpty()) {
				JsonObject reglasEvent = new JsonObject();
				reglasEvent.addProperty("type", "com.miapp.room.reglas");
				reglasEvent.addProperty("state_key", "");
				JsonObject contentReglas = new JsonObject();
				contentReglas.addProperty("texto", room.getReglas());
				reglasEvent.add("content", contentReglas);
				initialState.add(reglasEvent);
			}

			// 4.d) **Evento personalizado: “categoria”**
			if (room.getCategoria() != null && !room.getCategoria().isEmpty()) {
				JsonObject categoriaEvent = new JsonObject();
				categoriaEvent.addProperty("type", "com.miapp.room.categoria");
				categoriaEvent.addProperty("state_key", "");
				JsonObject contentCategoria = new JsonObject();
				contentCategoria.addProperty("nombre", room.getCategoria());
				categoriaEvent.add("content", contentCategoria);
				initialState.add(categoriaEvent);
			}

			// 4.d) **Evento personalizado: “descripción”**
			if (room.getDescripcion() != null && !room.getDescripcion().isEmpty()) {
				JsonObject categoriaEvent = new JsonObject();
				categoriaEvent.addProperty("type", "com.miapp.room.descripcion");
				categoriaEvent.addProperty("state_key", "");
				JsonObject contentCategoria = new JsonObject();
				contentCategoria.addProperty("nombre", room.getDescripcion());
				categoriaEvent.add("content", contentCategoria);
				initialState.add(categoriaEvent);
			}

			// 4.d) **Evento personalizado: “estado”**
			if (room.getEstado() != null && !room.getEstado().isEmpty()) {
				JsonObject categoriaEvent = new JsonObject();
				categoriaEvent.addProperty("type", "com.miapp.room.estado");
				categoriaEvent.addProperty("state_key", "");
				JsonObject contentCategoria = new JsonObject();
				contentCategoria.addProperty("nombre", room.getEstado());
				categoriaEvent.add("content", contentCategoria);
				initialState.add(categoriaEvent);
			}

			// 4.d) **Evento personalizado: “tecnologías”**
			if (room.getTecnologias() != null && !room.getTecnologias().isEmpty()) {
				JsonObject categoriaEvent = new JsonObject();
				categoriaEvent.addProperty("type", "com.miapp.room.tecnologias");
				categoriaEvent.addProperty("state_key", "");
				JsonObject contentCategoria = new JsonObject();
				contentCategoria.addProperty("nombre", room.getTecnologias());
				categoriaEvent.add("content", contentCategoria);
				initialState.add(categoriaEvent);
			}

			// 5) Agregar el array initial_state al cuerpo de la petición
			bodyJson.add("initial_state", initialState);

			// 6) Preparar y ejecutar la petición HTTP
			RequestBody roomBody = RequestBody.create(JSON, bodyJson.toString());
			Request req = new Request.Builder().url(SERVER_URL + "/_matrix/client/r0/createRoom")
					.addHeader("Authorization", "Bearer " + accessToken).addHeader("Content-Type", "application/json")
					.post(roomBody).build();

			try (Response resp = client.newCall(req).execute()) {
				String respBody = resp.body().string();
				if (!resp.isSuccessful()) {
					// 7a) Alias ya existe (Matrix devuelve 409 o error M_ROOM_IN_USE)
					if (resp.code() == 409 || respBody.contains("M_ROOM_IN_USE")) {
						JOptionPane.showMessageDialog(null, "El nombre del Proyecto ya existe, seleccione otro",
								"Alias duplicado", JOptionPane.WARNING_MESSAGE);
					}

					if (resp.code() == 413 || respBody.contains("M_TOO_LARGE")) {
						JOptionPane.showMessageDialog(null, "El icono de la sala debe pesar menos de 2MB",
								"Icono demasiado grande", JOptionPane.WARNING_MESSAGE);
					}
					// 7b) Otro error genérico
					System.err.println("❌ Error al crear sala: " + resp.code() + "\n" + respBody);
					return false;
				}
				// 7) Leer la respuesta exitosa y guardar el room_id, icono, etc.
				JsonObject respJson = JsonParser.parseString(respBody).getAsJsonObject();
				room.setId(respJson.get("room_id").getAsString());
				room.setMxcUrl(mxcUrl);
				room.setIcon(mxcUrl);
				ConexionSQLite.insertRoom(room); // Guarda tu objeto Room en la BD local
				return true;
			}

		} catch (Exception e) {
			System.err.println("❌ Error en createRoomWithAlias: " + e.getMessage());
			return false;
		}
	}

	public static int getRoomMemberCount(String roomId) {
		// Mejor extrae este token de una variable de entorno o vault, no lo dejes
		// hardcodeado.
		try {
			// Endpoint correcto de Admin API para listar miembros:
			// /_synapse/admin/v1/rooms/{roomId}/members
			String url = MatrixManager.SERVER_URL + "/_synapse/admin/v1/rooms/" + URLEncoder.encode(roomId, "UTF-8")
					+ "/members";

			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Bearer " + ADMIN_TOKEN);
			conn.setRequestProperty("Content-Type", "application/json");

			int code = conn.getResponseCode();
			if (code != 200) {
				throw new IOException("Admin API returned HTTP " + code);
			}

			// Leer y parsear JSON
			String body = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
					.lines().collect(Collectors.joining("\n"));
			JSONObject json = new JSONObject(body);

			// "total" es el número de miembros; "members" el array de user_ids
			return json.optInt("total", json.optJSONArray("members").length());

		} catch (IOException e) {
			System.err.println("❌ Error al buscar número de participantes (admin): " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	public static boolean updateRoomData(Room room) {
		if (Main.getUserMain().getAccessToken().isEmpty()) {
			System.err.println("❌ No hay token de acceso.");
			return false;
		}

		try {
			// Lista de eventos personalizados y su contenido
			Map<String, JsonObject> customEvents = new HashMap<>();

			if (room.getPropuesta() != null && !room.getPropuesta().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("texto", room.getPropuesta());
				customEvents.put("com.miapp.room.propuesta", content);
			}
			if (room.getReglas() != null && !room.getReglas().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("texto", room.getReglas());
				customEvents.put("com.miapp.room.reglas", content);
			}
			if (room.getCategoria() != null && !room.getCategoria().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("nombre", room.getCategoria());
				customEvents.put("com.miapp.room.categoria", content);
			}
			if (room.getDescripcion() != null && !room.getDescripcion().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("nombre", room.getDescripcion());
				customEvents.put("com.miapp.room.descripcion", content);
			}
			if (room.getEstado() != null && !room.getEstado().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("nombre", room.getEstado());
				customEvents.put("com.miapp.room.estado", content);
			}
			if (room.getTecnologias() != null && !room.getTecnologias().isEmpty()) {
				JsonObject content = new JsonObject();
				content.addProperty("nombre", room.getTecnologias());
				customEvents.put("com.miapp.room.tecnologias", content);
			}

			// Ejecutar una petición PUT por cada evento
			for (Map.Entry<String, JsonObject> entry : customEvents.entrySet()) {
				String eventType = entry.getKey();
				JsonObject content = entry.getValue();

				RequestBody requestBody = RequestBody.create(JSON, content.toString());

				String url = SERVER_URL + "/_matrix/client/r0/rooms/" + URLEncoder.encode(room.getId(), "UTF-8")
						+ "/state/" + URLEncoder.encode(eventType, "UTF-8");

				Request req = new Request.Builder().url(url)
						.addHeader("Authorization", "Bearer " + Main.getUserMain().getAccessToken())
						.addHeader("Content-Type", "application/json").put(requestBody).build();

				try (Response response = client.newCall(req).execute()) {
					if (!response.isSuccessful()) {
						System.err.println("❌ Error actualizando evento: " + eventType + " (" + response.code() + ") "
								+ response.body().string());
						return false;
					}
				}
			}
			return true;

		} catch (Exception e) {
			System.err.println("❌ Error en updateCustomEvents: " + e.getMessage());
			return false;
		}
	}

	public static Room getRoomByAliasLiteral(String fullAlias) throws IOException {
		OkHttpClient client = new OkHttpClient();
		String homeserver = SERVER_URL; // p.ej. "https://matrixserver1.duckdns.org"
		String userToken = Main.getUserMain().getAccessToken();
		String adminToken = ADMIN_TOKEN; // tu token de Admin API

		// 1) Resolver alias → room_id
		HttpUrl url = HttpUrl.parse(homeserver).newBuilder().addPathSegments("_matrix/client/r0/directory/room")
				.addEncodedPathSegment(fullAlias) // <-- aquí usamos addEncodedPathSegment
				.build();

		Request resolveReq = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + userToken).get()
				.build();

		String roomId;
		try (Response resolveResp = client.newCall(resolveReq).execute()) {
			if (resolveResp.code() == 404) {
				return null; // alias no existe
			}
			if (!resolveResp.isSuccessful()) {
				throw new IOException(
						"Error al resolver alias: HTTP " + resolveResp.code() + " → " + resolveResp.body().string());
			}
			JsonObject body = JsonParser.parseString(resolveResp.body().string()).getAsJsonObject();
			roomId = body.get("room_id").getAsString();
		}

		// 2) Obtener TODO el state de la sala con Admin‐API
		Request stateReq = new Request.Builder().url(homeserver + "/_synapse/admin/v1/rooms/" + roomId + "/state")
				.addHeader("Authorization", "Bearer " + adminToken).get().build();

		JsonArray stateArray;
		try (Response stateResp = client.newCall(stateReq).execute()) {
			if (!stateResp.isSuccessful()) {
				throw new IOException("Error al obtener state: HTTP " + stateResp.code());
			}
			JsonObject root = JsonParser.parseString(stateResp.body().string()).getAsJsonObject();
			stateArray = root.getAsJsonArray("state");
		}

		// 3) Parsear eventos de state
		String creator = "";
		String createdAt = "";
		String name = null;
		String avatarMxc = null;
		String categoria = "";
		String descripcion = "";
		String reglas = "";
		String propuesta = "";
		String estado = "";
		String tecnologias = "";

		for (JsonElement elem : stateArray) {
			JsonObject ev = elem.getAsJsonObject();
			String type = ev.get("type").getAsString();
			JsonObject content = ev.getAsJsonObject("content");

			switch (type) {
			case "m.room.create":
				creator = content.has("creator") ? content.get("creator").getAsString() : "";
				if (ev.has("origin_server_ts")) {
					long ts = ev.get("origin_server_ts").getAsLong();
					createdAt = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ts));
				}
				break;
			case "m.room.name":
				name = content.has("name") ? content.get("name").getAsString() : null;
				break;
			case "m.room.avatar":
				avatarMxc = content.has("url") ? content.get("url").getAsString() : null;
				break;
			case "com.miapp.room.categoria":
				categoria = content.has("nombre") ? content.get("nombre").getAsString() : "";
				break;
			case "com.miapp.room.descripcion":
				descripcion = content.has("nombre") ? content.get("nombre").getAsString() : "";
				break;
			case "com.miapp.room.reglas":
				reglas = content.has("texto") ? content.get("texto").getAsString() : "";
				break;
			case "com.miapp.room.propuesta":
				propuesta = content.has("texto") ? content.get("texto").getAsString() : "";
				break;
			case "com.miapp.room.estado":
				estado = content.has("nombre") ? content.get("nombre").getAsString() : "";
				break;
			case "com.miapp.room.tecnologias":
				tecnologias = content.has("nombre") ? content.get("nombre").getAsString() : "";
				break;
			}
		}

		// 4) Construir y devolver el Room (Proyecto o Comunidad)
		if (fullAlias.startsWith("#P_")) {
			Proyecto p = new Proyecto(roomId, name, categoria, descripcion, estado, reglas, propuesta, tecnologias,
					null, null, fullAlias.substring(1), // quita "#"
					creator, createdAt, "PROYECTO");
			if (avatarMxc != null)
				p.setIcon(avatarMxc);
			return p;
		} else {
			Comunidad c = new Comunidad(roomId, null, fullAlias.substring(1), creator, createdAt, "COMUNIDAD", null,
					name, categoria, descripcion, reglas, propuesta);
			if (avatarMxc != null)
				c.setIcon(avatarMxc);
			return c;
		}
	}

	public static Usuario getUserData(String userId) throws IOException {
		String accessToken = Main.getUserMain().getAccessToken();

		// 1) Perfil: displayname
		String profileUrl = SERVER_URL + "/_matrix/client/r0/profile/"
				+ URLEncoder.encode(userId, StandardCharsets.UTF_8) + "/displayname";
		Request reqName = new Request.Builder().url(profileUrl).addHeader("Authorization", "Bearer " + accessToken)
				.get().build();
		String displayName;
		try (Response resp = client.newCall(reqName).execute()) {
			if (!resp.isSuccessful()) {
				throw new IOException("Error al obtener displayname: HTTP " + resp.code());
			}
			JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
			displayName = json.has("displayname") ? json.get("displayname").getAsString() : userId;
		}

		// 2) Perfil: avatar_url
		String avatarUrlEndpoint = SERVER_URL + "/_matrix/client/r0/profile/"
				+ URLEncoder.encode(userId, StandardCharsets.UTF_8) + "/avatar_url";
		Request reqAvatar = new Request.Builder().url(avatarUrlEndpoint)
				.addHeader("Authorization", "Bearer " + accessToken).get().build();
		String mxcUrl = "";
		try (Response resp = client.newCall(reqAvatar).execute()) {
			if (resp.isSuccessful()) {
				JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
				if (json.has("avatar_url")) {
					mxcUrl = json.get("avatar_url").getAsString();
				}
			}
		}

		// 3) Account_data: html_url
		String type = "com.miapp.user.html_url";
		String htmlDataUrl = SERVER_URL + "/_matrix/client/r0/user/" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
				+ "/account_data/" + type;
		Request reqHtml = new Request.Builder().url(htmlDataUrl).addHeader("Authorization", "Bearer " + accessToken)
				.get().build();
		String htmlUrl = "";
		try (Response resp = client.newCall(reqHtml).execute()) {
			if (resp.isSuccessful()) {
				JsonObject json = JsonParser.parseString(resp.body().string()).getAsJsonObject();
				if (json.has("html_url")) {
					htmlUrl = json.get("html_url").getAsString();
				}
			}
		}

		// 4) Construir Usuario
		Usuario usuario = new Usuario.Builder().setId(userId)
				.setName(displayName.substring(1, displayName.length()).replace(":matrixserver1.duckdns.org", ""))
				.setMxcUrl(mxcUrl).build();
		usuario.setHtmlURL(htmlUrl);
		return usuario;
	}

}