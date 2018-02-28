package ar.com.ada3d.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ar.com.ada3d.model.Porcentual;

public class PorcentualMasivoBean implements Serializable {
	private static final long serialVersionUID = 1L;

	public PorcentualMasivoBean() {
		//contructor vacio;
	}

	
	private List<Porcentual> cargaPorcentualEdificio(String strLinea){
		Porcentual myPorcentual;
		List<Porcentual> listaPorcentualesEdificio = new ArrayList<Porcentual>();
		int posicionPorcentaje = 6;
		int posicionHonorarios = 22;
		int posicionEnGrilla = 0;
		for(int i=1; i<5; i++){ //Son 4 porcentuales por ahora
			myPorcentual = new Porcentual();
			myPorcentual.setPorc_posicion(i);
			myPorcentual.setPorc_titulo("Honorarios % " + i);
			myPorcentual.setPorc_porcentaje(Integer.parseInt(strLinea.split("\\|")[posicionPorcentaje].trim()));
			myPorcentual.setPorc_importeHonorarios(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[posicionHonorarios].trim(), Locale.UK, 2)));
			posicionPorcentaje = posicionPorcentaje + 1;
			posicionHonorarios = posicionHonorarios + 1;
			posicionEnGrilla = posicionEnGrilla + 1;
			listaPorcentualesEdificio.add(myPorcentual);
		}
		return listaPorcentualesEdificio;
		
	}
	
}
