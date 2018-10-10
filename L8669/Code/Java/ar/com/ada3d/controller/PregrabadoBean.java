package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import org.openntf.jsonbeanx.J2BConverter;
import org.openntf.jsonbeanx.J2BException;


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

	
	public void fillPregrabado(TextoPregrabado prm_textoPregrabado, Gasto prm_gasto) {
		this.myGasto = prm_gasto;
		this.textoPregrabado = prm_textoPregrabado;
		this.myGasto.setTextoDetalleFactura(prm_textoPregrabado.getTextoDetalle());
		//Cargo al gasto lo del primer edificio
		this.myGasto.setCuitProveedor(prm_textoPregrabado.getEdificios().get(0).getPrv_cuit());
		this.myGasto.setAgrupamiento(prm_textoPregrabado.getEdificios().get(0).getAgrupamiento());
		this.myGasto.setCodigoEspecial(prm_textoPregrabado.getEdificios().get(0).getCodigoEspecial());
		this.myGasto.setTipoGasto(prm_textoPregrabado.getEdificios().get(0).getTipoGasto());
		this.myGasto.setAplicarMes(prm_textoPregrabado.getEdificios().get(0).getMes());
		/*
		for (TextoPregrabadoEdificio edificio : prm_textoPregrabado.getEdificios()){
			edificio.getAgrupamiento()
		}*/
		
		
		
		System.out.println("edif:" + prm_textoPregrabado.getListaEdificios());
		System.out.println("id:" + prm_textoPregrabado.getId());
		
	}
	
	
	/**Cuando presiona btnConfirmar grabo en esta db
	 * @usedIn: Boton Confirmar 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 * @throws NotesException 

	 */
	public ArrayList<String> savePregrabado() {
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
		//TODO: Agregar control de errores --> listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el código de error: " + errCode);
		Session session = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess();
		Database thisdb = null;
		Document doc = null;
		try {
			thisdb = session.getCurrentDatabase();
			doc = thisdb.createDocument();
			doc.appendItemValue("Form", "f.ClaveJson");
			doc.save(); //para tener un id
			doc.appendItemValue("conf_clave", doc.getUniversalID()); //Texto para identificarlo en la vista no es necesario
			textoPregrabado.setId(doc.getUniversalID());
			doc.appendItemValue("conf_edf", new Vector<String>(textoPregrabado.getListaEdificios())); //Array con lista de edificios
			final String json = J2BConverter.beanToJson(textoPregrabado);
			doc.appendItemValue("conf_json", json);
			doc.save();
			
		} catch (NotesException e) {
			e.printStackTrace();
		} catch (J2BException e) {
			try {
				doc.remove(true);
			} catch (NotesException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally{
			//recyle domino objects here
			if(doc!=null)
				try {
					doc.recycle();
				} catch (NotesException e) {
					e.printStackTrace();
				}
			if(thisdb != null)
				try {
					thisdb.recycle();
				} catch (NotesException e) {
					e.printStackTrace();
				}
			
		}
		return listAcumulaErroresAS400;
	}

	
	/** Inicio el objeto: luego de cargar el gasto y asignar los edificios 
	 * @param prm_edificio lista de edificios seleccionados
	 * @param prm_gasto
	 * @usedIn: btnContinuarEdificios
	 */
	public void initPregrabado(List<String> prm_edificios, Gasto prm_gasto) {
		myGasto = prm_gasto;
		if(textoPregrabado == null)
			setTextoPregrabado(new TextoPregrabado());
		textoPregrabado.setListaEdificios(prm_edificios);
		textoPregrabado.setTextoDetalle(prm_gasto.getTextoDetalleFactura());
		textoPregrabado.setEdificios(fillListaJsonEdificios(prm_edificios));	
	}
	
	
	/**Carga de lista de pregrabados
	 * @param prm_edificio lista de edificios seleccionados
	 * @return lista de TextoPregrabadoEdificio
	 */
	private List<TextoPregrabadoEdificio> fillListaJsonEdificios(List<String> prm_edificios){
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
		myTextoPregrabadoEdificio.setPrv_cuit(myGasto.getCuitProveedor());
		myTextoPregrabadoEdificio.setMes(myGasto.getAplicarMes());
		myTextoPregrabadoEdificio.setTipoGasto(myGasto.getTipoGasto());
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
			por.setPorc_importeHonorarios(null); //nuleo el importe
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
