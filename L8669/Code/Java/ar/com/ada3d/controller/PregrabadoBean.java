package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
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


	/**
	 * Desde la vista de pregrabados al ingresar en un pregrabado, solo actualiza los datos del gasto
	 * @usedIn: viewPregrabados
	 * @param universalId del doc notes que estoy editando
	 * @param prm_gasto nuevo gasto
	 * @param listaEdificios todos los posibles
	 */
	public void fillPregrabado(String universalId, Gasto prm_gasto, List<Edificio> listaEdificios) {
		this.myGasto = prm_gasto;		
		setTextoPregrabado(universalId); //Cargo el notes document
		if(this.textoPregrabado.getId() == null) return;
		
		this.myGasto.setTextoDetalleFactura(this.textoPregrabado.getTextoDetalle());
		
		//Cargo al gasto lo del primer edificio
		this.myGasto.setCuitProveedor(this.textoPregrabado.getEdificios().get(0).getPrv_cuit());
		this.myGasto.setAgrupamiento(this.textoPregrabado.getEdificios().get(0).getAgrupamiento());
		this.myGasto.setCodigoEspecial(this.textoPregrabado.getEdificios().get(0).getCodigoEspecial());
		this.myGasto.setTipoGasto(this.textoPregrabado.getEdificios().get(0).getTipoGasto());
		this.myGasto.setAplicarMes(this.textoPregrabado.getEdificios().get(0).getMes());
		//Tilda los edificios que estan seleccionados 
		for (Iterator<Edificio> i = listaEdificios.iterator(); i.hasNext();) {
			Edificio edificio = i.next();
			if (textoPregrabado.getListaEdificios().contains(edificio.getEdf_codigo()))
				edificio.setCheckBoxSelected("1");
					
		}
				
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
			if (textoPregrabado.getId() == null){
				doc = thisdb.createDocument();
				doc.appendItemValue("Form", "f.ClaveJson");
				doc.save(); //para tener un id
			}else{
				doc = thisdb.getDocumentByUNID(textoPregrabado.getId());
			}
			doc.appendItemValue("conf_clave", doc.getUniversalID()); //Texto para identificarlo en la vista no es necesario
			textoPregrabado.setId(doc.getUniversalID());
			if(doc.getFirstItem("conf_edf") == null)
				doc.appendItemValue("conf_edf", new Vector<String>(textoPregrabado.getListaEdificios())); //Array con lista de edificios
			else
				doc.replaceItemValue("conf_edf", new Vector<String>(textoPregrabado.getListaEdificios())); //Array con lista de edificios
			
			final String json = J2BConverter.beanToJson(textoPregrabado);
			if(doc.getFirstItem("conf_json") == null)
				doc.appendItemValue("conf_json", json);
			else
				doc.replaceItemValue("conf_json", json);
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
		//textoPregrabado.setListaEdificios(prm_edificios);
		textoPregrabado.setTextoDetalle(prm_gasto.getTextoDetalleFactura());
		textoPregrabado.setEdificios(fillListaJsonEdificios(prm_edificios));
		
	}
	
	
	
	/**Carga de lista de pregrabados
	 * @param prm_edificio lista de edificios seleccionados
	 * @return lista de TextoPregrabadoEdificio
	 */
	private List<TextoPregrabadoEdificio> fillListaJsonEdificios(List<String> prm_edificios){
		Edificio myEdificio = null;
		List<String> temp_edificios = new ArrayList<String>();
		List<String> tempList = new ArrayList<String>(prm_edificios);
		List<TextoPregrabadoEdificio> ret = new ArrayList<TextoPregrabadoEdificio>();
		if(textoPregrabado.getEdificios() != null){
			List<TextoPregrabadoEdificio> listaActual = textoPregrabado.getEdificios();
			for (Iterator<TextoPregrabadoEdificio> i = listaActual.iterator(); i.hasNext();) {
				TextoPregrabadoEdificio edificio = i.next();
				if(tempList.contains(edificio.getEdif())){
					temp_edificios.add(edificio.getEdif());
					ret.add(edificio);
					tempList.remove(tempList.indexOf(edificio.getEdif()));
				}
			}
		}
		
		
		for (String edif : tempList){
			myEdificio = ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getHmEdificios().get(edif);
			temp_edificios.add(myEdificio.getEdf_codigo());
			ret.add(fillJsonEdificio(myEdificio));
			myEdificio = null;
		}
		textoPregrabado.setListaEdificios(temp_edificios); //reordeno la lista de edificios
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

	/** Setea tomando el Json de un doc Notes
	 * @param universalId el documento a buscar
	 */
	public void setTextoPregrabado(String universalId){
		Session session = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess();
		Database thisdb = null;
		Document doc = null;
		try {
			thisdb = session.getCurrentDatabase();
			doc = thisdb.getDocumentByUNID(universalId);
			if (doc != null)
				this.textoPregrabado = J2BConverter.jsonToBean(TextoPregrabado.class, doc.getItemValueString("conf_json")).get(0);
		} catch (NotesException e) {
			e.printStackTrace();
		}catch (J2BException e) {
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
	}
	
	public Gasto getMyGasto() {
		return myGasto;
	}

	public void setMyGasto(Gasto myGasto) {
		this.myGasto = myGasto;
	}
	
}
