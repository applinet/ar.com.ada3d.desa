package ar.com.ada3d.model;

import java.math.BigDecimal;
import java.util.*;

public class GastoDesdoblado{
	
	private int numeroCuotaInicial;
	private int cantidadCuotas;
	private int frecuencia;
	private String primerMes;
	private BigDecimal importeCuota;
	private List<GastoDesdobladoCuota> listaCuotas;
	
	
	public GastoDesdoblado() {
		super();
		// Empty constructor
		
	}


	//Getters & Setters
	
	public int getNumeroCuotaInicial() {
		return numeroCuotaInicial;
	}
	public void setNumeroCuotaInicial(int numeroCuotaInicial) {
		this.numeroCuotaInicial = numeroCuotaInicial;
	}
	public int getCantidadCuotas() {
		return cantidadCuotas;
	}
	public void setCantidadCuotas(int cantidadCuotas) {
		this.cantidadCuotas = cantidadCuotas;
	}

	public int getFrecuencia() {
		return frecuencia;
	}
	public void setFrecuencia(int frecuencia) {
		this.frecuencia = frecuencia;
	}

	public String getPrimerMes() {
		return primerMes;
	}
	public void setPrimerMes(String primerMes) {
		this.primerMes = primerMes;
	}

	public BigDecimal getImporteCuota() {
		return importeCuota;
	}
	public void setImporteCuota(BigDecimal importeCuota) {
		this.importeCuota = importeCuota;
	}

	public List<GastoDesdobladoCuota> getListaCuotas() {
		return listaCuotas;
	}

	public void setListaCuotas(List<GastoDesdobladoCuota> listaCuotas) {
		this.listaCuotas = listaCuotas;
	}
	
	
}
