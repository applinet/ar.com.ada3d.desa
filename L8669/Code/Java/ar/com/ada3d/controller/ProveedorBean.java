package ar.com.ada3d.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.model.SelectItem;
import org.openntf.domino.Document;


import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Proveedor;
import ar.com.ada3d.utilidades.DocUsr;
import ar.com.ada3d.utilidades.JSFUtil;


public class ProveedorBean implements Serializable{
	private static final long serialVersionUID = 1L;
	public List<Proveedor> listaProveedores;
	private Proveedor proveedor;
	private String proveedorSelected = "";
	
	public ProveedorBean() {
		// Empty constructor
	}

	/**Cuando presiona btnNewProveedor
	 * Creo un objeto vacio
	 */
	public void createNewProveedor() {
		setProveedor(new Proveedor());
	}
	
	public void editFormulario(){
		this.proveedor.setPrv_isReadMode(false);
	}
	
	/**Cuando presiona btnSave Proveedor
	 */
	public ArrayList<String> saveProveedor() {
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
		try {
			//TODO: Validar datos del proveedor. Duplicidad?
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("razon", proveedor.getPrv_razonSocial());
			docDummy.appendItemValue("domici", proveedor.getPrv_domicilio());
			docDummy.appendItemValue("locali", proveedor.getPrv_localidad());
			if(proveedor.getPrv_codigoPostal() == null){
				docDummy.appendItemValue("cp1","");
				docDummy.appendItemValue("cp2","");
				docDummy.appendItemValue("cp3","");
			}else if(proveedor.getPrv_codigoPostal().matches("[0-9]+")){//Solo numeros
				docDummy.appendItemValue("cp1","");
				docDummy.appendItemValue("cp2", proveedor.getPrv_codigoPostal());
				docDummy.appendItemValue("cp3","");
			}else{
				docDummy.appendItemValue("cp1", proveedor.getPrv_codigoPostal().substring(0, 1));
				docDummy.appendItemValue("cp2", proveedor.getPrv_codigoPostal().substring(1, 5));
				docDummy.appendItemValue("cp3", proveedor.getPrv_codigoPostal().substring(5, 8));
				
			}
			docDummy.appendItemValue("cuit", proveedor.getPrv_cuit().replaceAll("-", ""));
			docDummy.appendItemValue("telef", proveedor.getPrv_telefono());
			docDummy.appendItemValue("tipofa", proveedor.getPrv_tipoFactura());
			docDummy.appendItemValue("comis", (proveedor.getPrv_comision() == null) ? "0.00" : ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(proveedor.getPrv_comision(), 2));				
			docDummy.appendItemValue("sldini", (proveedor.getPrv_saldoInicial() == null) ? "0.00" : ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(proveedor.getPrv_saldoInicial(), 2));				
			docDummy.appendItemValue("estado", "0");
			
			QueryAS400 query = new QueryAS400();
			DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
			String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
			docDummy.appendItemValue("codadm", docUsuario.getUserDB());
			if (proveedor.isPrv_isNew()){
				if (!query.updateAS("proveedorProveInsert", docDummy)){				
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveProveedor" + "_CUIT:" + proveedor.getPrv_cuit() + "_DESC: No se pudo insertar en la tabla PH_PROVE.");
				}
			}else{
				if (!query.updateAS("proveedorProveUpdate", docDummy)) {
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveProveedor" + "_CUIT:" + proveedor.getPrv_cuit() + "_DESC: No se pudo actualizar la tabla PH_PROVE.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listAcumulaErroresAS400;
	}
	
	
	/**Cuando ingresa a la vista de Proveedores
	 */
	public void viewProveedores(){
		fillListaProveedores();
	}
	
	
	/**
	 * Lleno listaProveedores consultando As400, cada linea separa el dato por un pipe
	 */
	private void fillListaProveedores(){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("proveedor_CONTROLLER", null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		listaProveedores = new ArrayList<Proveedor>();
		for (String strLinea : nl) {
			Proveedor myProveedor = new Proveedor();
			myProveedor.setPrv_razonSocial(strLinea.split("\\|")[0].trim());
			myProveedor.setPrv_domicilio(strLinea.split("\\|")[1].trim());
			myProveedor.setPrv_localidad(strLinea.split("\\|")[2].trim());
			myProveedor.setPrv_cuit(strLinea.split("\\|")[3].trim());
			myProveedor.setPrv_estado(strLinea.split("\\|")[4].trim());
			myProveedor.setPrv_codigoPostal((strLinea.split("\\|")[5] + strLinea.split("\\|")[6] + strLinea.split("\\|")[7]).trim());
			myProveedor.setPrv_telefono(strLinea.split("\\|")[8].trim());
			myProveedor.setPrv_tipoFactura(strLinea.split("\\|")[9].trim());
			myProveedor.setPrv_comision(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[10].trim(), Locale.UK, 2)));
			myProveedor.setPrv_saldoInicial(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[11].trim(), Locale.UK, 2)));
			myProveedor.setPrv_isReadMode(true);
			listaProveedores.add(myProveedor);
		}
	}

	
	/**
	 * Esto devuelve para cada usuario el ComboBox de Proveedores
	 * @return: etiqueta y valor para xp:comboBox
	 * @usedIn: Combo principal en ccLayoutBootstrap, est� asociado a una
	 * sessionScope(edificioWork)
	 */
	public List<SelectItem> getComboboxMyProveedores() {
		//if(listaProveedores == null)
			fillListaProveedores();
		List<SelectItem> options = new ArrayList<SelectItem>();
		for (Proveedor myProveedor : listaProveedores) {
			/*
			if (options.isEmpty() && proveedorSelected.equals("") ) //Primer pasada
				proveedorSelected = myProveedor.getPrv_cuit();
			*/
			
			if (options.isEmpty()){ //Primer pasada opcion sin proveedor
				SelectItem option = new SelectItem();
				option.setLabel("");
				option.setValue("0");
				options.add(option);
			}
			SelectItem option = new SelectItem();
			
			option.setLabel(myProveedor.getPrv_razonSocial() + " - " + myProveedor.getPrv_domicilio() +" - " + myProveedor.getPrv_cuit() );
			option.setValue(myProveedor.getPrv_cuit());
			options.add(option);
		}
		return options;
	}



	/**
	 * Devuelve un string con la lista de proveedores para el cc:TypeAhead
	 *  @param verdadero devuelve solo los que trabaja el usuario, sino todos
	 *  @return un string con la lista de proveedores
	 *  @usedin typeAhead
	 * */
		
	public ArrayList<String> getArraySrtringListaProveedores(boolean prm_isDeTrabajo) {
		ArrayList<String> result = new ArrayList<String>();
		if (listaProveedores == null)
			fillListaProveedores();
		List<Proveedor> lista = prm_isDeTrabajo ? listaProveedores : listaProveedores;
		for (Proveedor myProveedor : lista) {
			result.add(myProveedor.getPrv_razonSocial() + "|" + myProveedor.getPrv_domicilio() + "|" + myProveedor.getPrv_cuit());
		}
		return result;
	}
		
	
	/**
	 * Devuelve datos de un proveedor por cuit
	 * @param prm_cuit el numero de cuit a buscar
	 * @param prm_dato el dato a devolver: razonSocial, domicilio, localidad, tipoFactura
	 * @return el dato del proveedor solicitado por parametros
	 * */
	public String getDatoPorCuit(String prm_cuit, String prm_dato) {
		if (listaProveedores == null)
			fillListaProveedores();
		if(prm_cuit.contains("-"))
			prm_cuit = prm_cuit.replaceAll("-", "");
		if (listaProveedores == null)
			fillListaProveedores();
		for (Proveedor myProveedor : listaProveedores){
			if (myProveedor.getPrv_cuit().equals(prm_cuit)){
				if(prm_dato.equals("razonSocial"))
					return myProveedor.getPrv_razonSocial();				
				if(prm_dato.equals("domicilio"))
					return myProveedor.getPrv_domicilio();				
				if(prm_dato.equals("localidad"))
					return myProveedor.getPrv_localidad();			
				if(prm_dato.equals("tipoFactura"))
					return myProveedor.getPrv_tipoFactura();			
				if(prm_dato.equals("matricula"))
					return myProveedor.getPrv_matricula();			
			}
		}
		return "";
	}
	
	/**
	 * En ccModalAltaProveedor cargo en un mapa de Tipo de Factura sale de una lista notes
	 * @return hashMap con Tipos de Factura
	 */
	
	
	/**
	 * En frmGastos el combo de Codigo Especial sale de configuracion de Notes
	 * @return codigos especiales
	 */
	public List<SelectItem> getComboboxTipoFactura() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		for (Map.Entry<String,String> entry : ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getOpcionesFacturaTipo().entrySet()) {
			SelectItem option = new SelectItem();
			option.setLabel(entry.getValue());
			option.setValue(entry.getKey());
			options.add(option);
		}
		return options;
	}
	
	//Getters & Setters
	public List<Proveedor> getListaProveedores() {
		return listaProveedores;
	}
	
	public Proveedor getProveedorPorCuit(String cuit) {
		if (listaProveedores == null)
			fillListaProveedores();
		for (Proveedor myProveedor : listaProveedores){
			if (myProveedor.getPrv_cuit().equals(cuit)){
				return myProveedor;				
			}
		}
		return proveedor;
	}
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public String getProveedorSelected() {
		return proveedorSelected;
	}

	public void setProveedorSelected(String proveedorSelected) {
		this.proveedorSelected = proveedorSelected;
	}
	
}
