package ar.com.ada3d.model;

import java.math.BigDecimal;
import java.util.*;

public class Gasto {
	static final String codigoRegistro = "G";
	private String codigoEdificio;
	
	private Date fechaFactura;
	private BigDecimal numeroComprobante; 
	private BigDecimal numeroFactura;
	private Vector<String> textoDetalleFactura;
	//columnas en AS: IMPOR1, IMPOR2, IMPOR3, IMPOR4 
	List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
	private Integer numeroRenglon;
	private Integer cantidadRenglones;
	
	private String agrupamiento; //o rubro --> tabla
	private String codigoEspecial; // --> tabla (es el estado)
	private String cuitProveedor;
	
	private Date fechaLiquidacion; //MMAAAA
	private String estadoDadoDeBaja; // es una 'B'
	private String menuOrigenDatos;
	private Date fechaCreacion;
	private String userSequentialCreacion;
	private Date fechaUltimaModificacion;
	private String userSequentialUltimaModificacion;
	
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


	public BigDecimal getNumeroFactura() {
		return numeroFactura;
	}


	public void setNumeroFactura(BigDecimal numeroFactura) {
		this.numeroFactura = numeroFactura;
	}


	public Vector<String> getTextoDetalleFactura() {
		return textoDetalleFactura;
	}


	public void setTextoDetalleFactura(Vector<String> textoDetalleFactura) {
		this.textoDetalleFactura = textoDetalleFactura;
	}

	
	
	public List<Prorrateo> getListaProrrateos() {
		return listaProrrateos;
	}


	public void setListaProrrateos(
			List<Prorrateo> listaProrrateos) {
		this.listaProrrateos = listaProrrateos;
	}


	public Integer getNumeroRenglon() {
		return numeroRenglon;
	}


	public void setNumeroRenglon(Integer numeroRenglon) {
		this.numeroRenglon = numeroRenglon;
	}


	public Integer getCantidadRenglones() {
		return cantidadRenglones;
	}


	public void setCantidadRenglones(Integer cantidadRenglones) {
		this.cantidadRenglones = cantidadRenglones;
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


	public Date getFechaLiquidacion() {
		return fechaLiquidacion;
	}


	public void setFechaLiquidacion(Date fechaLiquidacion) {
		this.fechaLiquidacion = fechaLiquidacion;
	}


	public String getEstadoDadoDeBaja() {
		return estadoDadoDeBaja;
	}


	public void setEstadoDadoDeBaja(String estadoDadoDeBaja) {
		this.estadoDadoDeBaja = estadoDadoDeBaja;
	}


	public String getMenuOrigenDatos() {
		return menuOrigenDatos;
	}


	public void setMenuOrigenDatos(String menuOrigenDatos) {
		this.menuOrigenDatos = menuOrigenDatos;
	}


	public Date getFechaCreacion() {
		return fechaCreacion;
	}


	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}


	public String getUserSequentialCreacion() {
		return userSequentialCreacion;
	}


	public void setUserSequentialCreacion(String userSequentialCreacion) {
		this.userSequentialCreacion = userSequentialCreacion;
	}


	public Date getFechaUltimaModificacion() {
		return fechaUltimaModificacion;
	}


	public void setFechaUltimaModificacion(Date fechaUltimaModificacion) {
		this.fechaUltimaModificacion = fechaUltimaModificacion;
	}


	public String getUserSequentialUltimaModificacion() {
		return userSequentialUltimaModificacion;
	}


	public void setUserSequentialUltimaModificacion(
			String userSequentialUltimaModificacion) {
		this.userSequentialUltimaModificacion = userSequentialUltimaModificacion;
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
