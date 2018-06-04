package ar.com.ada3d.controller;

import java.math.BigDecimal;
import java.util.*;

import org.openntf.domino.Document;

import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Gasto;
import ar.com.ada3d.utilidades.JSFUtil;

public class GastoBean {
	private static final long serialVersionUID = 1L;
	public List<Gasto> listaGastos;
	private Gasto gasto;
	
	public GastoBean() {
		// Empty constructor
	}
	
	/**Cuando presiona btnNewGasto
	 * Creo un objeto vacio
	 */
	public void createNewGasto() {
		setGasto(new Gasto());
	}
	
	public void editFormulario(){
		this.gasto.setIsReadMode(false);
	}
	
	/**Cuando presiona btnSave Gasto
	 */
	public ArrayList<String> saveGasto() {
		return null;
		
	}
	
	/**Cuando ingresa a la vista de Gastos
	 */
	public void viewGastos(){
		fillListaGastos();
	}
	
	/**
	 * Lleno listaGastos consultando As400, cada linea separa el dato por un pipe
	 * Cada gasto puede tener mas de una linea, pero existe un unico importe por factura
	 */
	private void fillListaGastos(){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("controllerGastos", null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		listaGastos = new ArrayList<Gasto>();
		for (String strLinea : nl) {
			Gasto myGasto = new Gasto();
			myGasto.setCodigoEdificio(strLinea.split("\\|")[0].trim());
			myGasto.setNumeroComprobante(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[1].trim(), Locale.UK, 0)));
			myGasto.setCuitProveedor(strLinea.split("\\|")[3].trim());
			myGasto.setIdGasto(strLinea.split("\\|")[4].trim());
			myGasto.setIsReadMode(true);
			listaGastos.add(myGasto);
		}
	}

	private void updateGasto(Gasto myGasto){
		ArrayList<String> nl = null;
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Codigo", myGasto.getCodigoEdificio());
		docDummy.appendItemValue("Cuit", myGasto.getCuitProveedor());
		docDummy.appendItemValue("Idgasto", myGasto.getIdGasto());
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("controllerUnGasto", docDummy);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		Vector<String> arrTextos = new Vector<String>();
		for (String strLinea : nl) {
			arrTextos.add(strLinea.split("\\|")[0].trim());
		}
		myGasto.setTextoDetalleFactura(arrTextos);
		myGasto.setCuitProveedor("24036435");
	}


	//Getters & Setters
	
	public List<Gasto> getListaGastos() {
		return listaGastos;
	}
	
	public Gasto getGasto() {
		return gasto;
	}


	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
		
	}

	public void setGasto(Gasto gasto, boolean updateAS400) {
		System.out.println("Setter del gasto con update");
		updateGasto(gasto);
		this.gasto = gasto;
	}
	
}
