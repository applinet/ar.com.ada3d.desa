package ar.com.ada3d.model;

import java.math.BigDecimal;
import java.util.Date;

public class GastoDesdobladoCuota {

	private int numeroCuota;
	private String mesLiquidaCuota;
	private BigDecimal importeCuota;
	private String codigoEspecialCuota;
	private String numeroFacturaCuota;
	private Date fechaFacturaCuota;
	private Date fechaPagoCuota;
	private String estadoCuota;
	
	public GastoDesdobladoCuota() {
		// Empty constructor
	}

	
	//Getters & Setters
	
	public int getNumeroCuota() {
		return numeroCuota;
	}

	public void setNumeroCuota(int numeroCuota) {
		this.numeroCuota = numeroCuota;
	}

	public String getMesLiquidaCuota() {
		return mesLiquidaCuota;
	}


	public void setMesLiquidaCuota(String mesLiquidaCuota) {
		this.mesLiquidaCuota = mesLiquidaCuota;
	}


	public BigDecimal getImporteCuota() {
		return importeCuota;
	}

	public void setImporteCuota(BigDecimal importeCuota) {
		this.importeCuota = importeCuota;
	}

	public String getCodigoEspecialCuota() {
		return codigoEspecialCuota;
	}

	public void setCodigoEspecialCuota(String codigoEspecialCuota) {
		this.codigoEspecialCuota = codigoEspecialCuota;
	}

	public String getNumeroFacturaCuota() {
		return numeroFacturaCuota;
	}

	public void setNumeroFacturaCuota(String numeroFacturaCuota) {
		this.numeroFacturaCuota = numeroFacturaCuota;
	}

	public Date getFechaFacturaCuota() {
		return fechaFacturaCuota;
	}

	public void setFechaFacturaCuota(Date fechaFacturaCuota) {
		this.fechaFacturaCuota = fechaFacturaCuota;
	}

	public Date getFechaPagoCuota() {
		return fechaPagoCuota;
	}

	public void setFechaPagoCuota(Date fechaPagoCuota) {
		this.fechaPagoCuota = fechaPagoCuota;
	}

	public String getEstadoCuota() {
		return estadoCuota;
	}

	public void setEstadoCuota(String estadoCuota) {
		this.estadoCuota = estadoCuota;
	}
	
}
