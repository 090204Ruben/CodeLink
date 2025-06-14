package modelo;

import javax.swing.ImageIcon;

public class Comunidad extends Room {
	private String id;
	private String nombre, categoria, descripcion, reglas, propuesta;
	private ImageIcon img;
	
	public Comunidad(String id, String parentRoomId, String alias, String userCreator, String createdAt,
			String type, ImageIcon icon, String nombre, String categoria, String descripcion, String reglas,
			String propuesta) {
		super(id, parentRoomId, alias, userCreator, nombre, createdAt, type, icon,null);

		this.id = id;
		this.img=icon;
		this.nombre = nombre;
		this.categoria = categoria;
		this.descripcion = descripcion;
		this.reglas = reglas;
		this.propuesta = propuesta;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public void setReglas(String reglas) {
		this.reglas = reglas;
	}

	public void setPropuesta(String propuesta) {
		this.propuesta = propuesta;
	}

	// Getters Room
	@Override
	public String getCategoria() {
		return categoria;
	}

	@Override
	public String getDescripcion() {
		return descripcion;
	}

	@Override
	public String getReglas() {
		return reglas;
	}

	@Override
	public String getPropuesta() {
		return propuesta;
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
	public String toString() {
		return "Comunidad [nombre=" + nombre + ", alias=" + alias +", categoria=" + categoria + ", descripcion=" + descripcion + ", reglas="
				+ reglas + ", propuesta=" + propuesta + "]";
	}

	@Override
	public ImageIcon getImage() {
		return img;
	}
	
	public void setImg(ImageIcon img) {
		this.img = img;
	}
}
