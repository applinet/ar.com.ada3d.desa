package ar.com.ada3d.model;

import java.util.List;

public class TextoPreseteadoEdificio {
	private String edif;
	private String mes;
	private String agrupamiento;
	private String codigoEspecial;
	private String tipoGasto;
	private List<Porcentual> porcentuales;
	private String prv_cuit;
	
	
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
	
	public String getTipoGasto() {
		return tipoGasto;
	}

	public void setTipoGasto(String tipoGasto) {
		this.tipoGasto = tipoGasto;
	}

	public List<Porcentual> getPorcentuales() {
		return porcentuales;
	}

	public void setPorcentuales(List<Porcentual> porcentuales) {
		this.porcentuales = porcentuales;
	}

	public String getPrv_cuit() {
		return prv_cuit;
	}

	public void setPrv_cuit(String prv_cuit) {
		this.prv_cuit = prv_cuit;
	}

}
