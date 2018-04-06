package ar.com.ada3d.model;


public class Proveedor {
	private String prv_razon;
	private String prv_domici;
	private String prv_locali;
	private String prv_cuit;
	private String prv_telef;
	private String prv_tipoFa;
	private String prv_estado;
	private String prv_codadm;
	private String prv_contac;
	private boolean prv_isReadMode;
	
	public Proveedor() {
		// Empty constructor
	}

	//Getters & Setters
	
	public String getPrv_razon() {
		return prv_razon;
	}

	public void setPrv_razon(String prv_razon) {
		this.prv_razon = prv_razon;
	}

	public String getPrv_domici() {
		return prv_domici;
	}

	public void setPrv_domici(String prv_domici) {
		this.prv_domici = prv_domici;
	}

	public String getPrv_locali() {
		return prv_locali;
	}

	public void setPrv_locali(String prv_locali) {
		this.prv_locali = prv_locali;
	}

	public String getPrv_cuit() {
		return prv_cuit;
	}

	public void setPrv_cuit(String prv_cuit) {
		this.prv_cuit = prv_cuit;
	}

	public String getPrv_telef() {
		return prv_telef;
	}

	public void setPrv_telef(String prv_telef) {
		this.prv_telef = prv_telef;
	}

	public String getPrv_tipoFa() {
		return prv_tipoFa;
	}

	public void setPrv_tipoFa(String prv_tipoFa) {
		this.prv_tipoFa = prv_tipoFa;
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

	public String getPrv_contac() {
		return prv_contac;
	}

	public void setPrv_contac(String prv_contac) {
		this.prv_contac = prv_contac;
	}

	public boolean isPrv_isReadMode() {
		return prv_isReadMode;
	}

	public void setPrv_isReadMode(boolean prv_isReadMode) {
		this.prv_isReadMode = prv_isReadMode;
	}
	
}
