package ar.com.ada3d.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Porcentual implements Serializable{
	
	private static final long serialVersionUID = 1L;

	public Porcentual() {
	
	}
	
	private Integer porc_posicion;
	private String porc_titulo;
	private Integer porc_porcentaje;
	private BigDecimal porc_importeHonorarios;

	//Getters and Setters
	
	public Integer getPorc_posicion() {
		return porc_posicion;
	}
	public void setPorc_posicion(Integer porc_posicion) {
		this.porc_posicion = porc_posicion;
	}
	public String getPorc_titulo() {
		return porc_titulo;
	}
	public void setPorc_titulo(String porc_titulo) {
		this.porc_titulo = porc_titulo;
	}
	public Integer getPorc_porcentaje() {
		return porc_porcentaje;
	}
	public void setPorc_porcentaje(Integer porc_porcentaje) {
		this.porc_porcentaje = porc_porcentaje;
	}
	public BigDecimal getPorc_importeHonorarios() {
		return porc_importeHonorarios;
	}
	public void setPorc_importeHonorarios(BigDecimal porc_importeHonorarios) {
		this.porc_importeHonorarios = porc_importeHonorarios;
	}
}
