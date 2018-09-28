package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Porcentual;
import ar.com.ada3d.model.TextoPregrabado;
import ar.com.ada3d.model.TextoPregrabadoEdificio;

public class PregrabadoBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private TextoPregrabado textoPregrabado;

	
	public PregrabadoBean() {
		// Empty constructor
	}
	
	
	public void cargoPregrabado(List<String> prm_edificios) {
		if(textoPregrabado == null)
			setTextoPregrabado(new TextoPregrabado());
		textoPregrabado.setListaEdificios(prm_edificios);
		textoPregrabado.setEdificios(cargaListaTextoPregrabadoEdificio(prm_edificios));	
			
		System.out.println("FPR seguir aca");	
		
	}
	
	
	/**
	 * @param prm_edificios
	 * @return lista de TextoPregrabadoEdificio
	 */
	private List<TextoPregrabadoEdificio> cargaListaTextoPregrabadoEdificio(List<String> prm_edificios){
		Edificio myEdificio = null;
		List<TextoPregrabadoEdificio> ret = new ArrayList<TextoPregrabadoEdificio>();
		for (String edif : prm_edificios){
			
			myEdificio = ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getHmEdificios().get(edif);
			ret.add(cargaTextoPregrabadoEdificio(myEdificio));
			myEdificio = null;
		}
		return ret;
	}
	
	
	
	
	/**Cargo el texto pregrabado de un edificio recibiendo el objeto edificio, estoy presionando Continuar en la pantalla 
	 * de edificios del wizard
	 * 
	 * @param prm_edificio el objeto edificio que le voy a cargar los porcentuales
	 * @return los datos particulares de los edificios, solo esto ya que el resto lo cargo en el avance de carga de datos
	 */
	private TextoPregrabadoEdificio cargaTextoPregrabadoEdificio(Edificio prm_edificio){
		final TextoPregrabadoEdificio textoPregrabadoEdificio = new TextoPregrabadoEdificio();
		textoPregrabadoEdificio.setEdif(prm_edificio.getEdf_codigo());
		//TODO: URGENTE obtener el cuit de algun lado textoPregrabadoEdificio.setPrv_cuit(prv_cuit);
		textoPregrabadoEdificio.setPorcentuales(cargaPorcentuales(prm_edificio));
		return textoPregrabadoEdificio;
	}

	
	/**Devuelve lista de porcentuales para pregrabado
	 * 
	 * @param prm_edificio el objeto edificio que le voy a cargar los porcentuales
	 * @return lista de porcentuales a aplicar al pregrabadoEdificio
	 */
	private List<Porcentual> cargaPorcentuales(Edificio prm_edificio){
		List<Porcentual> listaPorcentuales = prm_edificio.getListaPorcentuales();
		return listaPorcentuales;
	}
	
	
	//Getters & Setters
	
	
	public TextoPregrabado getTextoPregrabado() {
		return textoPregrabado;
	}


	public void setTextoPregrabado(TextoPregrabado textoPregrabado) {
		this.textoPregrabado = textoPregrabado;
		
	}
	
}
