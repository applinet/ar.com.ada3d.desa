package ar.com.ada3d.model;


public class Proveedor {
	private String prv_razonSocial;
	private String prv_domicilio;
	private String prv_localidad;
	private String prv_cuit;
	private String prv_telefono;
	private String prv_tipoFactura;
	private String prv_estado;
	private String prv_codadm;
	private String prv_contacto;
	private boolean prv_isReadMode;
	private boolean prv_isNew;
	
	
	public Proveedor() {
		// Empty constructor
	}

	//Getters & Setters
	
	public String getPrv_razonSocial() {
		return prv_razonSocial;
	}

	public void setPrv_razonSocial(String prv_razonSocial) {
		this.prv_razonSocial = prv_razonSocial;
	}

	public String getPrv_domicilio() {
		return prv_domicilio;
	}

	public void setPrv_domicilio(String prv_domicilio) {
		this.prv_domicilio = prv_domicilio;
	}

	public String getPrv_localidad() {
		return prv_localidad;
	}

	public void setPrv_localidad(String prv_localidad) {
		this.prv_localidad = prv_localidad;
	}

	public String getPrv_cuit() {
		return prv_cuit;
	}

	public void setPrv_cuit(String prv_cuit) {
		this.prv_cuit = prv_cuit;
	}

	public String getPrv_telefono() {
		return prv_telefono;
	}

	public void setPrv_telefono(String prv_telefono) {
		this.prv_telefono = prv_telefono;
	}

	public String getPrv_tipoFactura() {
		return prv_tipoFactura;
	}

	public void setPrv_tipoFactura(String prv_tipoFactura) {
		this.prv_tipoFactura = prv_tipoFactura;
	}

	public String getPrv_estado() {
		return prv_estado;
	}

	public void setPrv_estado(String prv_estado) {
		this.prv_estado = prv_estado;
	}

	public String getPrv_codadm() {
		return prv_codadm;
	}

	public void setPrv_codadm(String prv_codadm) {
		this.prv_codadm = prv_codadm;
	}

	public String getPrv_contacto() {
		return prv_contacto;
	}

	public void setPrv_contacto(String prv_contacto) {
		this.prv_contacto = prv_contacto;
	}

	public boolean isPrv_isReadMode() {
		return prv_isReadMode;
	}

	public void setPrv_isReadMode(boolean prv_isReadMode) {
		this.prv_isReadMode = prv_isReadMode;
	}

	public boolean isPrv_isNew() {
		return prv_isNew;
	}

	public void setPrv_isNew(boolean prv_isNew) {
		this.prv_isNew = prv_isNew;
	}	
}
