package ar.com.ada3d.model;

import java.io.Serializable;
import java.math.*;

public class Porcentual implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public Porcentual() {
		
	}

	private String edf_porcentualCodigo;
	private String edf_porcentualTitulo;
	private BigDecimal edf_porcentualImporte;
	
	
	
	//Getters and Setters

	public String getEdf_porcentualCodigo() {
		return edf_porcentualCodigo;
	}
	public void setEdf_porcentualCodigo(String edf_porcentualCodigo) {
		this.edf_porcentualCodigo = edf_porcentualCodigo;
	}
	public void setEdf_porcentualTitulo(String edf_porcentualTitulo) {
		this.edf_porcentualTitulo = edf_porcentualTitulo;
	}
	public String getEdf_porcentualTitulo() {
		return edf_porcentualTitulo;
	}
	public void setEdf_porcentualImporte(BigDecimal edf_porcentualImporte) {
		this.edf_porcentualImporte = edf_porcentualImporte;
	}
	public BigDecimal getEdf_porcentualImporte() {
		return edf_porcentualImporte;
	}


	
	
	
}
