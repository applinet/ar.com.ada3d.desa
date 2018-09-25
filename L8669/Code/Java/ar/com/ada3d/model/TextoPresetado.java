package ar.com.ada3d.model;

import java.util.List;

public class TextoPresetado {
	private String id;
	private String referencia;
	private List<String> textoDetalle;
	private List<TextoPreseteadoEdificio> edificios;
	private List<String> listaEdificios;
	
	public TextoPresetado() {
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

	public List<TextoPreseteadoEdificio> getEdificios() {
		return edificios;
	}

	public void setEdificios(List<TextoPreseteadoEdificio> edificios) {
		this.edificios = edificios;
	}

	public List<String> getListaEdificios() {
		return listaEdificios;
	}

	public void setListaEdificios(List<String> listaEdificios) {
		this.listaEdificios = listaEdificios;
	}

}
