package modelo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import vista.ButtonUser;

public class Usuario {
	private static ArrayList<Room> listaRooms = new ArrayList<>();
	private static HashMap<Room,ButtonUser> listaRoomsBtns = new HashMap<>();

	private String id;
	private String name;
	private String login;
	private String email;
	private String avatar_url;
	private String html_url;
	private String position;
	private final String PASSWORD = "He382$%@tuj";
	private String state;
	private String type;
	private String accessToken;
	private String lastLoginAt;
	private String createdAt;
	private String mxcUrl;
	private ImageIcon icon;
	private String since;

	private Usuario(String id, String name, String login, String email, String avatar_url, String html_url,
			String position, String state, String type, String accessToken, String lastLoginAt, String createdAt,
			String mxcUrl) {

		this.id = id;
		this.name = name;
		this.login = login;
		this.email = email;
		this.avatar_url = avatar_url;
		this.html_url = html_url;
		this.position = position;
		this.state = state;
		this.type = type;
		this.accessToken = accessToken;
		this.lastLoginAt = lastLoginAt;
		this.createdAt = createdAt;
		this.mxcUrl = mxcUrl;
		setIcon(mxcUrl);
	}
	
	public ImageIcon getIcon() {
		return icon;
	}

	public void setIcon(String uriStr) {
	    try {
	        if (uriStr != null && uriStr.startsWith("mxc://")) {
	            URI uri = new URI(uriStr);
	            String server = uri.getHost();
	            String mediaId = uri.getPath().substring(1);
	            String downloadUrl = "https://" 
	                + server 
	                + "/_matrix/media/r0/download/" 
	                + server 
	                + "/" 
	                + mediaId;
	            
	            // ← aquí usamos URL en lugar de String
	            URL url = new URL(downloadUrl);
	            this.icon = new ImageIcon(url);
	        } else {
	            this.icon = null;
	        }
	    } catch (URISyntaxException | MalformedURLException e) {
	        e.printStackTrace();
	        this.icon = null;
	    }
	}

	public void setImageUser(String avatar_url) {
		this.avatar_url = avatar_url;
	}

	public URL getImageUser() {
		try {
			return new URI(avatar_url).toURL();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getSince() {
		return since;
	}

	public void setSince(String since) {
		this.since = since;
	}

	public HashMap<Room,ButtonUser> getListaRoomsBtns() {
		return listaRoomsBtns;
	}

	public void setListaRoomsBtns(HashMap<Room,ButtonUser> listaRoomsBtns) {
		Usuario.listaRoomsBtns = listaRoomsBtns;
	}

	public String getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(String lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getMxcUrl() {
		return mxcUrl;
	}

	public void setMxcUrl(String mxcUrl) {
		this.mxcUrl = mxcUrl;
	}

	public String getHtmlURL() {
		return html_url;
	}

	public ArrayList<Room> getListaRooms() {
		return listaRooms;
	}

	public void setListaRooms(ArrayList<Room> listaRooms) {
		Usuario.listaRooms = listaRooms;
	}

	public void setHtmlURL(String html_url) {
		this.html_url = html_url;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getPassword() {
		return PASSWORD;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getId() {
		return id;
	}

	public void setId(String identifier) {
		id = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String d) {
		name = d;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String j) {
		email = j;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", name=" + name + ", login=" + login + ", email=" + email + ", avatar_url="
				+ avatar_url + ", html_url=" + html_url + ", position=" + position + ", PASSWORD=" + PASSWORD
				+ ", state=" + state + ", type=" + type + ", accessToken=" + accessToken + ", lastLoginAt="
				+ lastLoginAt + ", createdAt=" + createdAt + ", mxcUrl=" + mxcUrl + "]";
	}



	public static class Builder {
		private String id;
		private String name;
		private String login;
		private String email;
		private String avatar_url;
		private String html_url;
		private String position;
		private String password = "He382$%@tuj";
		private String state;
		private String type;
		private String accessToken;
		private String lastLoginAt;
		private String createdAt;
		private String mxcUrl;

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setLogin(String login) {
			this.login = login;
			return this;
		}

		public Builder setEmail(String email) {
			this.email = email;
			return this;
		}

		public Builder setAvatarURL(String avatar_url) {
			this.avatar_url = avatar_url;
			return this;
		}

		public Builder setHtmlURL(String html_url) {
			this.html_url = html_url;
			return this;
		}

		public Builder setPosition(String position) {
			this.position = position;
			return this;
		}

		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		public Builder setState(String state) {
			this.state = state;
			return this;
		}

		public Builder setType(String type) {
			this.type = type;
			return this;
		}

		public Builder setAccessToken(String accessToken) {
			this.accessToken = accessToken;
			return this;
		}

		public Builder setLastLoginAt(String lastLoginAt) {
			this.lastLoginAt = lastLoginAt;
			return this;
		}

		public Builder setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder setMxcUrl(String mxcUrl) {
			this.mxcUrl = mxcUrl;
			return this;
		}

		public Usuario build() {
			return new Usuario(id, name, login, email, avatar_url, html_url, position, state, type, accessToken,
					lastLoginAt, createdAt, mxcUrl);
		}

	}

}
