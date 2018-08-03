package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.openntf.domino.Document;

import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.GastoOpciones;
import ar.com.ada3d.utilidades.DocUsr;
import ar.com.ada3d.utilidades.JSFUtil;


public class GastoOpcionesBean implements Serializable {
	private static final long serialVersionUID = 1L;
	public List<GastoOpciones> listaOpcionesGastos;
	private GastoOpciones gastoOpciones;
	private HashMap<String, String> _mapOrdenDatosProveedor;
	private HashMap<String, String> _mapOrdenDatosProveedorOriginal;

	
	public GastoOpcionesBean() {
		// Empty constructor
	}
	
	
//***** INI BOTONES ****
	
	/**Cuando presiona btnNewGasto
	 * Creo un objeto vacio y completo el orden por default
	 */
	public void createNewOpcionGasto() {
		setGastoOpciones(new GastoOpciones());
		this.gastoOpciones.setNumerarGastos("1");
		this.gastoOpciones.setNumeroProximoGasto(Integer.parseInt("1"));
		this.gastoOpciones.setIsNew(true);
		crearMapaDefault();
	}
	
	/**
	 * Funcion en el boton editar
	 * Actualizo y lockeo
	 * @usedIn: frmOpcionesGastos
	 */
	public void editFormulario(Edificio prm_edificio){
		this.gastoOpciones.setIsReadMode(false);
	}
	

	/**Cuando presiona btnSave en opciones de gastos
	 * Esto ya tiene que venir validado
	 * @usedIn: Boton save 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 * @param prm_ordenDatos como debe ser el orden de los datos
	 * @param prm_edificio el edificio seleccionado 	
	 */
	public ArrayList<String> saveOpcionGasto(String prm_ordenDatos, Edificio prm_edificio) {
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
		List<String> listaEdificios = new ArrayList<String>();
		if(this.gastoOpciones.isConfiguracionUnica()){
			listaEdificios.add("***");
		}else{
			listaEdificios.add(prm_edificio.getEdf_codigo());
		}
				
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("NUMCMP", this.gastoOpciones.getNumerarGastos());
		docDummy.appendItemValue("NROAUT", this.gastoOpciones.getNumerarGastos().equals("0") ? "0" : this.gastoOpciones.getNumeroProximoGasto());
		docDummy.appendItemValue("NUMSLD", this.gastoOpciones.getNumerarSueldos().toString());
		docDummy.appendItemValue("OPCPRV", this.gastoOpciones.getAgregarDatosProveedorEnDetalleDelGasto());
		docDummy.appendItemValue("OPCTXT", this.gastoOpciones.getAgregarDatosProveedorEnDetalleDelGasto().equals("0") ? "0" : prm_ordenDatos.replace("," , ""));
		QueryAS400 query = new QueryAS400();
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );

		if(gastoOpciones.getCodigoEdificio() != null){ //es un update
			if (!query.updateBatchGastos("opcionesgastoUpdateBatch", docDummy, listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el código de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewOpcionGasto" + "_EDID:" + prm_edificio.getEdf_codigo() + "_DESC: No se pudo actualizar la tabla PH_OPGTS.");
			}
		}else{
			if (this.gastoOpciones.getIsNew()){
				for(GastoOpciones go : listaOpcionesGastos){
			        if(go.getCodigoEdificio() != null && go.getCodigoEdificio().contains(prm_edificio.getEdf_codigo())){
			        	listAcumulaErroresAS400.add("btnSave~El edificio " + go.getCodigoEdificio()  + " ya tiene una configuración cargada. Para modificarla por favor ingrese por opciones de gastos");
			        	return listAcumulaErroresAS400;
			        }
			    }
			}
			if (!query.updateBatchGastos("opcionesgastoInsertBatchOPGTS", docDummy, listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el código de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewOpcionGasto" + "_EDID:" + prm_edificio.getEdf_codigo() + "_DESC: No se pudo insertar en la tabla PH_OPGTS.");
			}
		}
		return listAcumulaErroresAS400;
	}	
	
	

	
//***** FIN BOTONES ****
	
//***** INI VISTAS ****
	
	/**Cuando ingresa a la vista de opciones de Gastos
	 */
	public boolean viewOpcionesGastos(){
		return fillListaOpcionesGastos();
	}
	
	/**
	 * Completo la variable listaOpcionesGastos consultando As400, cada linea separa el dato por un pipe
	 * @return true si tiene al menos un edificio configurado, sino falso para crearlas
	 */
	private boolean fillListaOpcionesGastos(){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("opcionesGasto_CONTROLLER", null);
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
			
			myGastoOpciones.setNumeroProximoGasto(Integer.parseInt(strLinea.split("\\|")[5].trim()));
			if(myGastoOpciones.getCodigoEdificio().equals("***"))
				myGastoOpciones.setConfiguracionUnica(true);
			myGastoOpciones.setIsReadMode(true);
			myGastoOpciones.setIsNew(false);
			listaOpcionesGastos.add(myGastoOpciones);
					
		}
		if (listaOpcionesGastos.isEmpty()) return false;
		return true;
		
	}

	//***** FIN VISTAS ****

	
	//***** INI FUNCIONES ****
	
	/** UPDATE DE LA OPCION AL INGRESAR DE LA VISTA
	 * Al ingresar en un registro de la lista de opciones hago un nuevo update del gasto con los datos que faltan
	 */
	private void updateOpcionesGastos(GastoOpciones gastoOpciones){
		if(gastoOpciones.getAgregarDatosProveedorEnDetalleDelGasto().equals("1")){
			String newString = Arrays.toString(gastoOpciones.getOrdenDatosProveedorEnDetalleDelGasto().split(""));
            newString = newString.substring(1, newString.length()-1).replace(" ", ""); 
			this._mapOrdenDatosProveedor = devolverMapaOrdenado(newString.trim()); 
		}else{
			crearMapaDefault();
		}
	}
	
	/**Orden de los datos de proveedores impresos en la descripcion del gasto
	 * @usedIn: 
	 * @return: un nuevo mapa para asignar a mapOrdenDatosProveedor 
	 * @param prm_ordenDatos: el orden como quiero que devuelva ordenado mapOrdenDatosProveedor	
	 */

	public HashMap<String, String> devolverMapaOrdenado(String prm_ordenDatos){
		if(this._mapOrdenDatosProveedor == null){
			crearMapaDefault();
		}
		List<String> myList = new ArrayList<String>(Arrays.asList(prm_ordenDatos.split(",")));
		HashMap<String, String> tempMap = new HashMap<String, String>();
		int count = 1;
		for(String val : myList){
			tempMap.put(String.valueOf(count), this._mapOrdenDatosProveedorOriginal.get(val));
			count+=1;
		}
		return tempMap;
	}
	
	
	/** Opciones por defecto del orden que imprime los datos del proveedor */
	private void crearMapaDefault(){
		this._mapOrdenDatosProveedor = new HashMap<String, String>();
		this._mapOrdenDatosProveedor.putAll(get_mapOrdenDatosProveedorOriginal());
	}
	
	//***** FIN FUNCIONES ****
	
	//Getters & Setters
	
	public List<GastoOpciones> getListaOpcionesGastos() {
		return listaOpcionesGastos;
	}


	public void setListaOpcionesGastos(List<GastoOpciones> listaOpcionesGastos) {
		this.listaOpcionesGastos = listaOpcionesGastos;
	}


	public GastoOpciones getGastoOpciones() {
		return gastoOpciones;
	}


	public void setGastoOpciones(GastoOpciones gastoOpciones) {
		if(gastoOpciones.getCodigoEdificio() != null) //no es una opcion nueva
			updateOpcionesGastos(gastoOpciones);
		this.gastoOpciones = gastoOpciones;
	}


	public HashMap<String, String> get_mapOrdenDatosProveedor() {
		return _mapOrdenDatosProveedor;
	}


	public void set_mapOrdenDatosProveedor(
			HashMap<String, String> ordenDatosProveedor) {
		_mapOrdenDatosProveedor = ordenDatosProveedor;
	}

	public HashMap<String, String> get_mapOrdenDatosProveedorOriginal() {
		this._mapOrdenDatosProveedorOriginal = new HashMap<String, String>();
		this._mapOrdenDatosProveedorOriginal.put("1", "Proveedor y cuit");
		this._mapOrdenDatosProveedorOriginal.put("2", "Fecha de la factura");
		this._mapOrdenDatosProveedorOriginal.put("3", "Número de la factura");
		this._mapOrdenDatosProveedorOriginal.put("4", "Dirección proveedor");
		return _mapOrdenDatosProveedorOriginal;
	}
	
}
