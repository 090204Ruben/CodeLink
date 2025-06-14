package login.Authentification;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import conexionBD.ConexionSQLite;
import login.VistaLogin;
import modelo.MatrixManager;
import modelo.Room;
import modelo.Usuario;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vista.VistaApp;

@RestController
public class HomeController {
	public static Map<String, Object> datosUser;

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;

	@GetMapping("/secured")
	public String getUserInfo(Authentication authentication) {
		if (authentication.getPrincipal() instanceof OAuth2User) {
			OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

			// Obtener todos los atributos del usuario
			datosUser = oauthUser.getAttributes();
			for (String key : datosUser.keySet()) {
				System.out.println(key + ": " + datosUser.get(key));
			}
			Usuario user = new Usuario.Builder().setEmail((String) datosUser.get("email"))
					.setLogin(((String) datosUser.get("login")).trim())
					.setAvatarURL(((String) datosUser.get("avatar_url")))
					.setHtmlURL(((String) datosUser.get("html_url"))).setPosition(((String) datosUser.get("position")))
					.setState(((String) datosUser.get("state"))).setType(((String) datosUser.get("type")))
					.setCreatedAt(((String) datosUser.get("created_at")))
					.setLastLoginAt(((String) datosUser.get("updated_at"))).build();

			if (user.getEmail() == null) {
				String email = fetchVerifiedEmail((OAuth2AuthenticationToken) authentication); // Intentar obtenerlo
																								// manualmente
				user.setName(email.replace("@gmail.com", "").toLowerCase());
			} else {
				user.setName(user.getEmail().replace("@gmail.com", "").toLowerCase());
			}
			// Cogemos el access token de la bbdd local
			user.setAccessToken(ConexionSQLite.buscarAccessTokenUsuario(user));
			// Iniciar sesión o registrarse en matrix para permitir al usuario interactuar
			// con la app(enviar/recibir mensajes,crear y unirse canales)
			// Ponemos el password hardcodeado porque no podemos saber el password de
			// GitHub, y no hay problemas de seguridad porque si el usuario no inicia sesión
			// con GitHub no accede a Matrix y cada usuario tendra su email único.

			// ., _, =, -, /
			try {
				// Si el access token es null quiere decir que el usuario no se a registrado
				// nunca, por lo que lo creamos
				if (user.getAccessToken() == null) {
					Main.setUserMain(user);
					MatrixManager.registerUser(user.getName(), user.getPassword(), user.getImageUser(), false);
					Main.getUserMain().setListaRooms(ConexionSQLite.getUserRooms());
				} else {
					Main.setUserMain(user);
					Main.getUserMain().setListaRooms(ConexionSQLite.getUserRooms());
				}
				System.out.println(Main.getUserMain().getListaRooms());
				// Hacemos esto para borrar los rooms que han sido borrados por el creator en
				// ausencia del usuario cliente
				ConexionSQLite.setListaUsers(ConexionSQLite.cargarUsuarios());
				
				Iterator<Room> iterator = Main.getUserMain().getListaRooms().iterator();
				while (iterator.hasNext()) {
				    Room room = iterator.next();
				    Room sala = MatrixManager.getRoomByAliasLiteral("#" + room.getAlias() + ":matrixserver1.duckdns.org");
				    if (sala == null) {
				        ConexionSQLite.borrarRastroRoom(room);
				        iterator.remove(); // Elimina de forma segura el elemento actual
				    }
				}

				ConexionSQLite.cargarSinceUserMain();
				for (Usuario users : ConexionSQLite.getListaUsers()) {// asignamos a cada usuario su room
					for (Room rooms : Main.getUserMain().getListaRooms()) {
						if (ConexionSQLite.isUserInRoom(users.getId(), rooms.getId())) {
							rooms.getRoomMembers().add(users);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			VistaLogin.getInstance().dispose();
			VistaApp.getInstance().setVisible(true);

			return "<!DOCTYPE html>\r\n" + "<html lang=\"es\">\r\n" + "<head>\r\n" + "    <meta charset=\"UTF-8\">\r\n"
					+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
					+ "    <title>CodeLink</title>\r\n" + "    <style>\r\n" + "        body {\r\n"
					+ "            background-color: #7f8185;\r\n" + "            display: flex;\r\n"
					+ "            flex-direction: column;\r\n" + "            justify-content: center;\r\n"
					+ "            align-items: center;\r\n" + "            height: 50vh;\r\n"
					+ "            margin: 0;\r\n" + "            font-family: Arial, sans-serif;\r\n" + "        }\r\n"
					+ "        .container {\r\n" + "            text-align: center;\r\n" + "        }\r\n"
					+ "        .logo {\r\n" + "            width: 100px;\r\n" + "            height: 100px;\r\n"
					+ "        }\r\n" + "        h1 {\r\n" + "            font-size: 24px;\r\n"
					+ "            color: #333;\r\n" + "        }\r\n" + "    </style>\r\n" + "</head>\r\n"
					+ "<body>\r\n" + "    <div class=\"container\">\r\n"
					+ "        <img class=\"logo\" src=\"/imgs/logo4.png\" alt=\"Logo de CodeLink\">\r\n"
					+ "	<h1>BIENVENIDO A CODELINK</h1>\r\n" + "        <p>Ahora puedes volver a la aplicación</p>\r\n"
					+ "    </div>\r\n" + "</body>\r\n" + "</html>\r\n" + "";
		}
		return "No OAuth2 User found";
	}

	private String fetchVerifiedEmail(OAuth2AuthenticationToken auth) {
		OAuth2AuthorizedClient client = authorizedClientService
				.loadAuthorizedClient(auth.getAuthorizedClientRegistrationId(), auth.getName());

		String accessToken = client.getAccessToken().getTokenValue();

		OkHttpClient httpClient = new OkHttpClient();
		Request request = new Request.Builder().url("https://api.github.com/user/emails")
				.addHeader("Authorization", "Bearer " + accessToken)
				.addHeader("Accept", "application/vnd.github.v3+json").build();

		try (Response response = httpClient.newCall(request).execute()) {
			System.out.println("Email API response code: " + response.code());

			if (response.isSuccessful() && response.body() != null) {
				String body = response.body().string(); // consume aquí solo 1 vez
				System.out.println("Email API response body: " + body);

				Gson gson = new Gson();
				JsonArray emails = JsonParser.parseString(body).getAsJsonArray();

				for (JsonElement emailElement : emails) {
					JsonObject emailObj = emailElement.getAsJsonObject();
					if (emailObj.get("primary").getAsBoolean() && emailObj.get("verified").getAsBoolean()) {
						return emailObj.get("email").getAsString();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
