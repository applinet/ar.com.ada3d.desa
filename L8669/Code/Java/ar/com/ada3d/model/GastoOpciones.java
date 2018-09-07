package ar.com.ada3d.model;

public class GastoOpciones {
	private String codigoEdificio;
	private String numerarGastos;
	private String numerarSueldos;
	private String ordenDatosProveedorEnDetalleDelGasto;
	private boolean isReadMode;
	private boolean configuracionUnica;
	private Integer numeroProximoGasto;
	private boolean isNew;
	
	
	public GastoOpciones() {
		// Empty constructor
	}

	//Getters & Setters
	public String getCodigoEdificio() {
		return codigoEdificio;
	}

	public void setCodigoEdificio(String codigoEdificio) {
		this.codigoEdificio = codigoEdificio;
	}

	public String getNumerarGastos() {
		return numerarGastos;
	}

	public void setNumerarGastos(String numerarGastos) {
		this.numerarGastos = numerarGastos;
	}

	public String getNumerarSueldos() {
		return numerarSueldos;
	}

	public void setNumerarSueldos(String numerarSueldos) {
		this.numerarSueldos = numerarSueldos;
	}
	
	public String getOrdenDatosProveedorEnDetalleDelGasto() {
		return ordenDatosProveedorEnDetalleDelGasto;
	}

	public void setOrdenDatosProveedorEnDetalleDelGasto(
			String ordenDatosProveedorEnDetalleDelGasto) {
		this.ordenDatosProveedorEnDetalleDelGasto = ordenDatosProveedorEnDetalleDelGasto;
	}
	
	public boolean getIsReadMode() {
		return isReadMode;
	}

	public void setIsReadMode(boolean isReadMode) {
		this.isReadMode = isReadMode;
	}

	public boolean isConfiguracionUnica() {
		return configuracionUnica;
	}

	public void setConfiguracionUnica(boolean configuracionUnica) {
		this.configuracionUnica = configuracionUnica;
	}

	public Integer getNumeroProximoGasto() {
		return numeroProximoGasto;
	}

	public void setNumeroProximoGasto(Integer numeroProximoGasto) {
		this.numeroProximoGasto = numeroProximoGasto;
	}

	public boolean getIsNew() {
		return isNew;
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	
}
