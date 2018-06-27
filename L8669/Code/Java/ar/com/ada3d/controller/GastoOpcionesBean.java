package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lotus.domino.NotesException;

import org.openntf.domino.Document;

import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.GastoOpciones;
import ar.com.ada3d.utilidades.JSFUtil;

public class GastoOpcionesBean implements Serializable {
	private static final long serialVersionUID = 1L;
	public List<GastoOpciones> listaOpcionesGastos;
	
	public GastoOpcionesBean() {
		// Empty constructor
	}
	
	
	
//***** INI VISTAS ****
	
	/**Cuando ingresa a la vista de opciones de Gastos
	 * @param prm_edificio:objeto edificio 
	 */
	public void viewOpcionesGastos(Edificio prm_edificio){
		fillListaOpcionesGastos(prm_edificio);
	}
	
	/**
	 * Completo la variable listaGastos consultando As400, cada linea separa el dato por un pipe
	 * Cada gasto puede tener mas de una linea, pero existe un unico importe por factura.
	 * @param prm_edificio:objeto edificio 
	 * @return la lista de facturas de un edificio
	 */
	private void fillListaOpcionesGastos(Edificio prm_edificio){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("edf_codigo", prm_edificio.getEdf_codigo());
			nl = query.getSelectAS("opcionesGasto_CONTROLLER", docDummy);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		listaOpcionesGastos = new ArrayList<GastoOpciones>();
		GastoOpciones myGastoOpciones = null;
		
		for (String strLinea : nl) {
			myGastoOpciones = new GastoOpciones();
			myGastoOpciones.setCodigoEdificio(strLinea.split("\\|")[0].trim());
			myGastoOpciones.setNumerarGastos(strLinea.split("\\|")[1].trim());
			myGastoOpciones.setNumerarSueldos(strLinea.split("\\|")[2].trim());
			myGastoOpciones.setAgregarDatosProveedorEnDetalleDelGasto(strLinea.split("\\|")[3].trim());
			myGastoOpciones.setOrdenDatosProveedorEnDetalleDelGasto(strLinea.split("\\|")[4].trim());
			
			myGastoOpciones.setIsReadMode(true);
			listaOpcionesGastos.add(myGastoOpciones);
					
		}
		
		
	}

	//***** FIN VISTAS ****

	
	//Getters & Setters
	
	public List<GastoOpciones> getListaOpcionesGastos() {
		return listaOpcionesGastos;
	}



	public void setListaOpcionesGastos(List<GastoOpciones> listaOpcionesGastos) {
		this.listaOpcionesGastos = listaOpcionesGastos;
	}
	
	
	
	

}
