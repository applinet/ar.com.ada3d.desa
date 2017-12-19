package ar.com.ada3d.model;

import java.io.Serializable;

public class Edificio implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public Edificio() {
		// Empty constructor
	}

	private String edf_codigo;
	private String edf_codigoNumerico;
	private String edf_codigoVisual;
	private String edf_direccion;
	private String edf_codigoPostal;
	private String edf_localidad;
	private String edf_provincia;
	private String edf_dependiente;
	private String edf_estadoProceso;
	private String edf_ultimaLiquidacion;
	private String edf_porcentualTitulo;
	private String edf_cuit;
	
	//TODO: private String edf_lockeo --> DocLock

		
	public Edificio(String edf_codigo, String edf_codigoNumerico,
			String edf_codigoVisual, String edf_direccion,
			String edf_codigoPostal, String edf_provincia,
			String edf_dependiente, String edf_estadoProceso,
			String edf_ultimaLiquidacion) {
		super();
		this.edf_codigo = edf_codigo;
		this.edf_codigoNumerico = edf_codigoNumerico;
		this.edf_codigoVisual = edf_codigoVisual;
		this.edf_direccion = edf_direccion;
		this.edf_codigoPostal = edf_codigoPostal;
		this.edf_provincia = edf_provincia;
		this.edf_dependiente = edf_dependiente;
		this.edf_estadoProceso = edf_estadoProceso;
		this.edf_ultimaLiquidacion = edf_ultimaLiquidacion;
	}
	
	
	
	
	//Getters and Setters
	
	public String getEdf_codigo() {
		return edf_codigo;
	}
	public void setEdf_codigo(String edf_codigo) {
		this.edf_codigo = edf_codigo;
	}
	public String getEdf_codigoNumerico() {
		return edf_codigoNumerico;
	}
	public void setEdf_codigoNumerico(String edf_codigoNumerico) {
		this.edf_codigoNumerico = edf_codigoNumerico;
	}
	public String getEdf_codigoVisual() {
		return edf_codigoVisual;
	}
	public void setEdf_codigoVisual(String edf_codigoVisual) {
		this.edf_codigoVisual = edf_codigoVisual;
	}
	public String getEdf_direccion() {
		return edf_direccion;
	}
	public void setEdf_direccion(String edf_direccion) {
		this.edf_direccion = edf_direccion;
	}
	public String getEdf_codigoPostal() {
		return edf_codigoPostal;
	}
	public void setEdf_codigoPostal(String edf_codigoPostal) {
		this.edf_codigoPostal = edf_codigoPostal;
	}
	public String getEdf_localidad() {
		return edf_localidad;
	}
	public void setEdf_localidad(String edf_localidad) {
		this.edf_localidad = edf_localidad;
	}
	public String getEdf_provincia() {
		return edf_provincia;
	}
	public void setEdf_provincia(String edf_provincia) {
		this.edf_provincia = edf_provincia;
	}
	public String getEdf_adm_dependiente() {
		return edf_dependiente;
	}
	public void setEdf_adm_dependiente(String edf_adm_dependiente) {
		this.edf_dependiente = edf_adm_dependiente;
	}
	public String getEdf_estadoProceso() {
		return edf_estadoProceso;
	}
	public void setEdf_estadoProceso(String edf_estadoProceso) {
		this.edf_estadoProceso = edf_estadoProceso;
	}
	public String getEdf_ultimaLiquidacion() {
		return edf_ultimaLiquidacion;
	}
	public void setEdf_ultimaLiquidacion(String edf_ultimaLiquidacion) {
		this.edf_ultimaLiquidacion = edf_ultimaLiquidacion;
	}
	public String getEdf_porcentualTitulo() {
		return edf_porcentualTitulo;
	}
	public void setEdf_porcentualTitulo(String edf_porcentualTitulo) {
		this.edf_porcentualTitulo = edf_porcentualTitulo;
	}
	public String getEdf_cuit() {
		return edf_cuit;
	}
	public void setEdf_cuit(String edf_cuit) {
		this.edf_cuit = edf_cuit;
	}
	
}
