package modelo;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import vista.Mensaje;

public abstract class Room {
	protected String id;
	protected String parentRoomId, alias, userCreator, name, createdAt, type, estado;
	protected ImageIcon icon;
	protected String mxcUrl;
	private ArrayList<Usuario> roomMembers = new ArrayList<>();
	private ArrayList<Mensaje> listaMensajes = new ArrayList<>();
	private boolean visible;

	public Room(String id, String parentRoomId, String alias, String userCreator, String name, String createdAt,
			String type, ImageIcon icon,String estado) {

		this.id = id;
		this.parentRoomId = parentRoomId;
		this.alias = alias;
		this.userCreator = userCreator;
		this.name = name;
		this.createdAt = createdAt;
		this.type = type;
		this.icon = icon;
		this.estado = estado;
	}

	public abstract String getCategoria();

	public abstract String getDescripcion();

	public abstract String getReglas();

	public abstract String getPropuesta();

	public abstract String getTecnologias();

	public abstract ImageIcon getImage();

	public ArrayList<Mensaje> getListaMensajes() {
		return listaMensajes;
	}

	public void setListaMensajes(ArrayList<Mensaje> listaMensajes) {
		this.listaMensajes = listaMensajes;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}
	
	public String getEstado() {
		return estado;
	}

	public ArrayList<Usuario> getRoomMembers() {
		return roomMembers;
	}

	public String getId() {
		return id;
	}

	public String getMxcUrl() {
		return mxcUrl;
	}

	public void setMxcUrl(String mxcUrl) {
		this.mxcUrl = mxcUrl;
		setIcon(mxcUrl);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentRoomId() {
		return parentRoomId;
	}

	public void setParentRoomId(String parentRoomId) {
		this.parentRoomId = parentRoomId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getUserCreator() {
		return userCreator;
	}

	public void setUserCreator(String userCreator) {
		this.userCreator = userCreator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
				String downloadUrl = "https://" + server + "/_matrix/media/r0/download/" + server + "/" + mediaId;

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

	public void setVisibility(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return this.visible;
	}
}