package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
			//TODO: Validar datos del proveedor. Duplicidad
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("razon", proveedor.getPrv_razon());
			docDummy.appendItemValue("domici", proveedor.getPrv_domici());
			docDummy.appendItemValue("locali", proveedor.getPrv_locali());
			docDummy.appendItemValue("cuit", proveedor.getPrv_cuit().replaceAll("-", ""));
			docDummy.appendItemValue("telef", proveedor.getPrv_telef());
			
			QueryAS400 query = new QueryAS400();
			DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
			String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
			docDummy.appendItemValue("codadm", docUsuario.getUserDB());
			
			if (!query.updateAS("updateProveedorPROVE", docDummy)) {
				if (!query.updateAS("updateProveedorPROVE_insert", docDummy)){				
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el código de error: " + errCode);
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
			nl = query.getSelectAS("controllerProveedores", null, false);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		listaProveedores = new ArrayList<Proveedor>();
		for (String strLinea : nl) {
			Proveedor myProveedor = new Proveedor();
			myProveedor.setPrv_razon(strLinea.split("\\|")[0].trim());
			myProveedor.setPrv_domici(strLinea.split("\\|")[1].trim());
			myProveedor.setPrv_locali(strLinea.split("\\|")[2].trim());
			myProveedor.setPrv_cuit(strLinea.split("\\|")[3].trim());
			myProveedor.setPrv_isReadMode(true);
			listaProveedores.add(myProveedor);
		}
	}

	
	/**
	 * Esto devuelve para cada usuario el ComboBox de Proveedores
	 * @return: etiqueta y valor para xp:comboBox
	 * @usedIn: Combo principal en ccLayoutBootstrap, está asociado a una
	 * sessionScope(edificioWork)
	 */
	public List<SelectItem> getComboboxMyProveedores() {
		if(listaProveedores == null)
			fillListaProveedores();
		List<SelectItem> options = new ArrayList<SelectItem>();
		for (Proveedor myProveedor : listaProveedores) {
			if (options.isEmpty() && proveedorSelected.equals("") ) //Primer pasada
				proveedorSelected = myProveedor.getPrv_cuit();
			SelectItem option = new SelectItem();
			
			option.setLabel(myProveedor.getPrv_razon());
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
			result.add(myProveedor.getPrv_razon() + "|" + myProveedor.getPrv_domici() +"|" + myProveedor.getPrv_cuit());
		}
		return result;
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
