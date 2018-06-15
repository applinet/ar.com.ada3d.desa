package ar.com.ada3d.model;

import java.math.BigDecimal;
import java.util.*;

public class Gasto {
	static final String codigoRegistro = "G";
	private String codigoEdificio;
	
	private Date fechaFactura;
	private BigDecimal numeroComprobante; 
	private String numeroFactura;
	private List<String> textoDetalleFactura;
	//columnas en AS: IMPOR1, IMPOR2, IMPOR3, IMPOR4 
	List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
	private Integer cantidadRenglones;
	private Integer numeroRenglon;
	
	private String agrupamiento; //o rubro --> tabla
	private String codigoEspecial; // --> tabla (es el estado)
	private String cuitProveedor;
	
	private String fechaLiquidacion; //MMMMAAAA
	private Date fechaCreacion;
	private String usuarioCreacion;
	private String origenDatos;
	
	private Date fechaOdp;
	private Integer numeroOdp;
	
	private Date fechaCajaDiaria;
	private Integer numeroCajaDiaria;
	
	private String idGasto; // NCTROL numero interno de control

	private boolean isReadMode;
	private String lockedBy;
	
	
	public Gasto() {
		// Empty constructor
	}


	//Getters & Setters
	
	public String getCodigoEdificio() {
		return codigoEdificio;
	}


	public void setCodigoEdificio(String codigoEdificio) {
		this.codigoEdificio = codigoEdificio;
	}


	public Date getFechaFactura() {
		return fechaFactura;
	}


	public void setFechaFactura(Date fechaFactura) {
		this.fechaFactura = fechaFactura;
	}


	public BigDecimal getNumeroComprobante() {
		return numeroComprobante;
	}


	public void setNumeroComprobante(BigDecimal numeroComprobante) {
		this.numeroComprobante = numeroComprobante;
	}


	public String getNumeroFactura() {
		return numeroFactura;
	}


	public void setNumeroFactura(String numeroFactura) {
		this.numeroFactura = numeroFactura;
	}
	
	
	public List<String> getTextoDetalleFactura() {
		return textoDetalleFactura;
	}


	public void setTextoDetalleFactura(List<String> textoDetalleFactura) {
		this.textoDetalleFactura = textoDetalleFactura;
	}




	public List<Prorrateo> getListaProrrateos() {
		return listaProrrateos;
	}


	public void setListaProrrateos(
			List<Prorrateo> listaProrrateos) {
		this.listaProrrateos = listaProrrateos;
	}


	public Integer getCantidadRenglones() {
		return cantidadRenglones;
	}


	public void setCantidadRenglones(Integer cantidadRenglones) {
		this.cantidadRenglones = cantidadRenglones;
	}

	
	public Integer getNumeroRenglon() {
		return numeroRenglon;
	}


	public void setNumeroRenglon(Integer numeroRenglon) {
		this.numeroRenglon = numeroRenglon;
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


	public String getCuitProveedor() {
		return cuitProveedor;
	}


	public void setCuitProveedor(String cuitProveedor) {
		this.cuitProveedor = cuitProveedor;
	}


	public String getFechaLiquidacion() {
		return fechaLiquidacion;
	}


	public void setFechaLiquidacion(String fechaLiquidacion) {
		this.fechaLiquidacion = fechaLiquidacion;
	}


	public Date getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	
	public String getUsuarioCreacion() {
		return usuarioCreacion;
	}


	public void setUsuarioCreacion(String usuarioCreacion) {
		this.usuarioCreacion = usuarioCreacion;
	}


	public String getOrigenDatos() {
		return origenDatos;
	}


	public void setOrigenDatos(String origenDatos) {
		this.origenDatos = origenDatos;
	}


	public Date getFechaOdp() {
		return fechaOdp;
	}


	public void setFechaOdp(Date fechaOdp) {
		this.fechaOdp = fechaOdp;
	}


	public Integer getNumeroOdp() {
		return numeroOdp;
	}


	public void setNumeroOdp(Integer numeroOdp) {
		this.numeroOdp = numeroOdp;
	}


	public Date getFechaCajaDiaria() {
		return fechaCajaDiaria;
	}


	public void setFechaCajaDiaria(Date fechaCajaDiaria) {
		this.fechaCajaDiaria = fechaCajaDiaria;
	}


	public Integer getNumeroCajaDiaria() {
		return numeroCajaDiaria;
	}


	public void setNumeroCajaDiaria(Integer numeroCajaDiaria) {
		this.numeroCajaDiaria = numeroCajaDiaria;
	}


	public String getIdGasto() {
		return idGasto;
	}


	public void setIdGasto(String idGasto) {
		this.idGasto = idGasto;
	}


	public boolean getIsReadMode() {
		return isReadMode;
	}


	public void setIsReadMode(boolean isReadMode) {
		this.isReadMode = isReadMode;
	}


	public String getLockedBy() {
		return lockedBy;
	}


	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}


	public static String getCodigoRegistro() {
		return codigoRegistro;
	}
	
}
