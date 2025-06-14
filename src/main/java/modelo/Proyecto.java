package modelo;

import javax.swing.ImageIcon;

public class Proyecto extends Room {
	private String id;
	private String nombre, categoria, estado, descripcion, reglas, propuesta, tecnologias;
	private ImageIcon img;

	public Proyecto(String id, String nombre, String categoria, String descripcion, String estado, String reglas,
			String propuesta, String tecnologias, ImageIcon icon, String parentRoomId, String alias, String userCreator,
			String createdAt, String type) {

		super(id, parentRoomId, alias, userCreator, nombre, createdAt, "PROYECTO", icon, estado);

		this.id = id;
		this.img = icon;
		this.nombre = nombre;
		this.categoria = categoria;
		this.descripcion = descripcion;
		this.reglas = reglas;
		this.propuesta = propuesta;
		this.estado = estado;
		this.tecnologias = tecnologias;
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

	public void setEstado(String estado) {
		this.estado = estado;
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

	public void setTecnologias(String tecnologias) {
		this.tecnologias = tecnologias;
	}

	// Getters de Room
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
		return tecnologias;
	}

	@Override
	public String toString() {
		return "Proyecto [id=" + id + ", nombre=" + nombre + ", alias=" + alias + ", categoria=" + categoria
				+ ", descripcion=" + descripcion + ", reglas=" + reglas + ", propuesta=" + propuesta + ", tecnologias="
				+ tecnologias + ", createdAt=" + getCreatedAt() + ", userCreator=" + getUserCreator() + "]";
	}

	// Hacemos esto para que cuando creemos un room por primera vez nos salga la
	// foto en el btnUser
	// Las demas veces las cargara de matrix (icon)
	@Override
	public ImageIcon getImage() {
		return img;
	}

	public void setImg(ImageIcon img) {
		this.img = img;
	}

}
