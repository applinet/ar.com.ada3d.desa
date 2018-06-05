package ar.com.ada3d.controller;

import java.math.BigDecimal;
import java.util.*;

import org.openntf.domino.Document;

import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Gasto;
import ar.com.ada3d.model.Prorrateo;
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
		String idGasto = "";
		Vector<String> tempTextoFactura = null;
		Gasto myGasto = null;
		Integer count = 0;
		for (String strLinea : nl) {
			//Voy a generar una linea por comprobante pero recordar que una factura puede tener muchas lineas de texto
			Gasto miGasto = null;
			if(!idGasto.equals(strLinea.split("\\|")[1].trim())){
				idGasto = strLinea.split("\\|")[1].trim();
				tempTextoFactura = new Vector<String>();
				myGasto = new Gasto();
				myGasto.setIdGasto(idGasto);
				myGasto.setCodigoEdificio(strLinea.split("\\|")[0].trim());
				myGasto.setNumeroComprobante(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[1].trim(), Locale.UK, 0)));
				
				tempTextoFactura.add(strLinea.split("\\|")[2].trim());
				myGasto.setTextoDetalleFactura(tempTextoFactura);
				myGasto.setListaProrrateos(cargaProrrateo(strLinea));
				
				myGasto.setIsReadMode(true);
				listaGastos.add(myGasto);
				count = count + 1;
			}else{//Es igual al anterior solo agregar el texto
				tempTextoFactura.add(strLinea.split("\\|")[2].trim());
				if(miGasto == null){
					miGasto = listaGastos.get(count - 1);
				}
				miGasto.setTextoDetalleFactura(tempTextoFactura);					
			}
		}
	}

	/**
	 * Al cargar una factura en la misma consulta al AS400 tambien cargo los importes de prorrateo
	 * @param strLinea de AS400 que estoy procesando
	 * @return la lista de prorrateos
	 */
	private List<Prorrateo> cargaProrrateo(String strLinea){
		List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
		for(int i=1; i<5; i++){ //Son 4 porcentuales por ahora
			
		}
		//strLinea.split("\\|")[3].trim() IMPOR1
		/*
		Prorrateo myProrrateo;
		myProrrateo = new Prorrateo();
		myProrrateo.setPrt_posicion(i);
		myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strValorCuotaFija, Locale.UK, 2)));
		listaProrrateos.add(myProrrateo);
		 */
		return listaProrrateos;
	}
	
	
	/**
	 * Al ingresar en un registro de la lista de gastos hago un nuevo update del gasto con los datos que faltan
	 * Recordar que cada gasto puede tener mas de una linea, pero existe un unico importe por factura
	 */
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
