package ar.com.ada3d.model;

import java.util.List;

public class TextoPregrabado {
	private String id;
	private String referencia;
	private List<String> textoDetalle;
	private List<TextoPregrabadoEdificio> edificios;
	private List<String> listaEdificios;
	
	public TextoPregrabado() {
		// Empty constructor
	}

	//Getters & Setters
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	
	public List<String> getTextoDetalle() {
		return textoDetalle;
	}

	public void setTextoDetalle(List<String> textoDetalle) {
		this.textoDetalle = textoDetalle;
	}

	public List<TextoPregrabadoEdificio> getEdificios() {
		return edificios;
	}

	public void setEdificios(List<TextoPregrabadoEdificio> edificios) {
		this.edificios = edificios;
	}

	public List<String> getListaEdificios() {
		return listaEdificios;
	}

	public void setListaEdificios(List<String> listaEdificios) {
		this.listaEdificios = listaEdificios;
	}

}
