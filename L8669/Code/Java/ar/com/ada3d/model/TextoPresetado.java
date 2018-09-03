package ar.com.ada3d.model;

import java.util.List;

public class TextoPresetado {
	private String id;
	private String label;
	private String[] lineas;
	private List<TextoPreseteadoEdificio> edificios;
	
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String[] getLineas() {
		return lineas;
	}

	public void setLineas(String[] lineas) {
		this.lineas = lineas;
	}

	public List<TextoPreseteadoEdificio> getEdificios() {
		return edificios;
	}

	public void setEdificios(List<TextoPreseteadoEdificio> edificios) {
		this.edificios = edificios;
	}

	
}
