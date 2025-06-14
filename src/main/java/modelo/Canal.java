package modelo;

import javax.swing.ImageIcon;

public class Canal extends Room {
	String id, name;

	public Canal(String id, String parentRoomId, String alias, String userCreator, String name, String createdAt,
			String type, ImageIcon icon) {
		super(id, parentRoomId, alias, userCreator, name, createdAt, type, icon, null);
	}

	@Override
	public String getCategoria() {
		return null;
	}

	@Override
	public String getDescripcion() {
		return null;
	}

	@Override
	public String getReglas() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPropuesta() {
		return null;
	}

	@Override
	public String getTecnologias() {
		return null;
	}

	@Override
	public String getEstado() {
		return null;
	}

	@Override
	public ImageIcon getImage() {
		return null;
	}

}
