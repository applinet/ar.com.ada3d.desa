package ar.com.ada3d.model;

import java.util.List;

public class TextoPreseteadoEdificio {
	private String edif;
	private String mes;
	private String agrupamiento;
	private String codigoEspecial;
	private List<Porcentual> porcentuales;
	
	
	public TextoPreseteadoEdificio() {
		// Empty constructor
	}
	
	//Getters & Setters
	
	public String getEdif() {
		return edif;
	}

	public void setEdif(String edif) {
		this.edif = edif;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	public String getAgrupamiento() {
		return agrupamiento;
	}

	public void setAgrupamiento(String agrupamiento) {
		this.agrupamiento = agrupamiento;
	}

	public String getCodigoEspecial() {
		return codigoEspecial;
	}

	public void setCodigoEspecial(String codigoEspecial) {
		this.codigoEspecial = codigoEspecial;
	}
	
	public List<Porcentual> getPorcentuales() {
		return porcentuales;
	}

	public void setPorcentuales(List<Porcentual> porcentuales) {
		this.porcentuales = porcentuales;
	}

}
