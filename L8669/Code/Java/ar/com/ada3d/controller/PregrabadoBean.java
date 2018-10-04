package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Gasto;
import ar.com.ada3d.model.Porcentual;
import ar.com.ada3d.model.TextoPregrabado;
import ar.com.ada3d.model.TextoPregrabadoEdificio;

public class PregrabadoBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private TextoPregrabado textoPregrabado;
	private Gasto myGasto;
	
	public PregrabadoBean() {
		// Empty constructor
	}

	
	public void initPregrabado(List<String> prm_edificios, Gasto prm_gasto) {
		myGasto = prm_gasto;
		if(textoPregrabado == null){
			System.out.println("FPR new TextoPregrabado");
			setTextoPregrabado(new TextoPregrabado());
		}else{
			System.out.println("FPR NO ES PRIMERA VEZ PUEDO NULEAR?=" );
			
		}
		textoPregrabado.setListaEdificios(prm_edificios);
		textoPregrabado.setEdificios(fillListaJsonEdificios(prm_edificios));	
	}
	
	
	/**
	 * @param prm_edificios
	 * @return lista de TextoPregrabadoEdificio
	 */
	private List<TextoPregrabadoEdificio> fillListaJsonEdificios(List<String> prm_edificios){
		System.out.println("FPR fillListaJsonEdificios:" + prm_edificios);
		Edificio myEdificio = null;
		List<TextoPregrabadoEdificio> ret = new ArrayList<TextoPregrabadoEdificio>();
		for (String edif : prm_edificios){
			
			myEdificio = ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getHmEdificios().get(edif);
			ret.add(fillJsonEdificio(myEdificio));
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
	private TextoPregrabadoEdificio fillJsonEdificio(Edificio prm_edificio){
		TextoPregrabadoEdificio myTextoPregrabadoEdificio = new TextoPregrabadoEdificio();
		myTextoPregrabadoEdificio.setEdif(prm_edificio.getEdf_codigo());
		//TODO: URGENTE obtener el cuit de algun lado textoPregrabadoEdificio.setPrv_cuit(prv_cuit);
		System.out.println("FPR agrupamiento de gasto=" + myGasto.getAgrupamiento() + " - edif=" + prm_edificio.getEdf_codigo());
		myTextoPregrabadoEdificio.setAgrupamiento(myGasto.getAgrupamiento());
		myTextoPregrabadoEdificio.setCodigoEspecial(myGasto.getCodigoEspecial());
		myTextoPregrabadoEdificio.setPorcentuales(fillListaJsonPorcentuales(prm_edificio));
		return myTextoPregrabadoEdificio;
	}

	
	/**Devuelve lista de porcentuales para pregrabado seteando el tilde para ingreso de importe
	 * 
	 * @param prm_edificio el objeto edificio que le voy a cargar los porcentuales
	 * @return lista de porcentuales a aplicar al pregrabadoEdificio
	 */
	private List<Porcentual> fillListaJsonPorcentuales(Edificio prm_edificio){
		List<Porcentual> listaPorcentuales = prm_edificio.getListaPorcentuales();
		for(Porcentual por : listaPorcentuales){
			if(!por.getPorc_porcentaje().equals(0))
				por.setPorc_CheckBoxImporteJson(true);
		}
		return listaPorcentuales;
	}
	

	
	
	//Getters & Setters
		
	public TextoPregrabado getTextoPregrabado() {
		return textoPregrabado;
	}

	public void setTextoPregrabado(TextoPregrabado textoPregrabado) {
		this.textoPregrabado = textoPregrabado;
	}

	public Gasto getMyGasto() {
		return myGasto;
	}

	public void setMyGasto(Gasto myGasto) {
		this.myGasto = myGasto;
	}
	
}
