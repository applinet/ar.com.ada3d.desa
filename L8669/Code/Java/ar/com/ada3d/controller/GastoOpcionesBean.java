package ar.com.ada3d.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
	private ArrayList<String> edificiosConOpcionesGastos;
	private GastoOpciones gastoOpciones;
	private HashMap<String, String> _mapOrdenDatosProveedorTarget;
	private HashMap<String, String> _mapOrdenDatosProveedorSource;
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
		this.gastoOpciones.setTipoNumeracion("1");
		this.gastoOpciones.setNumeroProximoGasto(Integer.parseInt("1"));
		this.gastoOpciones.setIsNew(true);
		crearMapaDefault();
		
	}

	/**Cuando presiona btnNewGasto
	 * Creo un objeto vacio en base al maestro
	 */
	public void createNewOpcionGasto(GastoOpciones maestroGastoOpciones) {
		setGastoOpciones(new GastoOpciones());
		this.gastoOpciones.setTipoNumeracion(maestroGastoOpciones.getTipoNumeracion());
		this.gastoOpciones.setNumeroProximoGasto(Integer.parseInt("1"));
		this.gastoOpciones.setNumerarSueldos(maestroGastoOpciones.getNumerarSueldos());
		this.gastoOpciones.setOrdenDatosProveedorEnDetalleDelGasto(maestroGastoOpciones.getOrdenDatosProveedorEnDetalleDelGasto());
		this.gastoOpciones.setIsNew(true);
		updateOpcionesGastos(this.gastoOpciones);
		
		
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
		docDummy.appendItemValue("NUMCMP", this.gastoOpciones.getTipoNumeracion());
		docDummy.appendItemValue("NROAUT", this.gastoOpciones.getTipoNumeracion().equals("0") ? "0" : this.gastoOpciones.getNumeroProximoGasto());
		docDummy.appendItemValue("NUMSLD", this.gastoOpciones.getNumerarSueldos().toString());
		if(!prm_ordenDatos.contains("1") && !prm_ordenDatos.equals(""))
			listAcumulaErroresAS400.add("ccModalMensajeConfirmacion~Advertencia!! Ha seleccionado datos del proveedor pero no ha incluido la raz�n social.");
		docDummy.appendItemValue("ORDTXT", prm_ordenDatos.equals("") ? "0" : prm_ordenDatos.replace("," , ""));
		QueryAS400 query = new QueryAS400();
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
		if(gastoOpciones.getCodigoEdificio() != null){ //es un update
			if (!query.updateBatchGastos("opcionesgastoUpdateBatch", docDummy, listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewOpcionGasto" + "_EDIF:" + (this.gastoOpciones.isConfiguracionUnica() ? "***" : prm_edificio.getEdf_codigo()) + "_DESC: No se pudo actualizar la tabla PH_OPGTS.");
			}
		}else{
			if (this.gastoOpciones.getIsNew()){
				for(GastoOpciones go : listaOpcionesGastos){
			        if(go.getCodigoEdificio() != null && go.getCodigoEdificio().contains(this.gastoOpciones.isConfiguracionUnica() ? "***" : prm_edificio.getEdf_codigo())){
			        	if(this.gastoOpciones.isConfiguracionUnica()){
			        		listAcumulaErroresAS400.add("btnSave~Ud. ya tiene cargada una configuraci�n �nica para todos sus edificios. Para modificarla por favor ingrese por opciones de gastos e ingrese en el edificio: ***");
			        	}else{
			        		listAcumulaErroresAS400.add("btnSave~El edificio " + go.getCodigoEdificio()  + " ya tiene una configuraci�n cargada. Para modificarla por favor ingrese por opciones de gastos");
			        	}
			        	return listAcumulaErroresAS400;
			        }
			    }
			}
			if (!query.updateBatchGastos("opcionesgastoInsertBatchOPGTS", docDummy, listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewOpcionGasto" + "_EDIF:" + (this.gastoOpciones.isConfiguracionUnica() ? "***" : prm_edificio.getEdf_codigo()) + "_DESC: No se pudo insertar en la tabla PH_OPGTS.");
			}
		}
		return listAcumulaErroresAS400;
	}	
	
	
	/**Cuando es edificio *** al presionar guardar presenta mensaje de copiar a todos los edificios
	 * @usedIn: ccModalMensajeConfirmacion
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 * @param prm_ordenDatos como debe ser el orden de los datos
	 * @param prm_listaEdificios los edificios a copiar del actual
	 * @param prm_instruccion I=insert / U=update 	
	 */
	public ArrayList<String> cargaAutomaticaOpcionGasto(String prm_ordenDatos, List<String> prm_listaEdificios, String prm_instruccion ) {
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
						
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("NUMCMP", this.gastoOpciones.getTipoNumeracion());
		docDummy.appendItemValue("NROAUT", this.gastoOpciones.getTipoNumeracion().equals("0") ? "0" : "1");
		docDummy.appendItemValue("NUMSLD", this.gastoOpciones.getNumerarSueldos().toString());
		docDummy.appendItemValue("ORDTXT", prm_ordenDatos.equals("") ? "0" : prm_ordenDatos.replace("," , ""));
		QueryAS400 query = new QueryAS400();
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
		
		if (prm_instruccion.equals("I")){
			if (!query.updateBatchGastos("opcionesgastoInsertBatchOPGTS", docDummy, prm_listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:cargaAutomaticaOpcionGasto" + "_DESC: No se pudo insertar en la tabla PH_OPGTS.");
			}
			
		}else if (prm_instruccion.equals("U")){
			if (!query.updateBatchGastos("opcionesgastoUpdateBatchSinNROAUT", docDummy, prm_listaEdificios, false)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:cargaAutomaticaOpcionGasto" + "_DESC: No se pudo actualizar la tabla PH_OPGTS.");
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
		edificiosConOpcionesGastos = new ArrayList<String>();
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
			edificiosConOpcionesGastos.add(strLinea.split("\\|")[0].trim());
			myGastoOpciones.setTipoNumeracion(strLinea.split("\\|")[1].trim());
			myGastoOpciones.setNumerarSueldos(strLinea.split("\\|")[2].trim());
			myGastoOpciones.setOrdenDatosProveedorEnDetalleDelGasto(strLinea.split("\\|")[3].trim());
			
			myGastoOpciones.setNumeroProximoGasto(Integer.parseInt(strLinea.split("\\|")[4].trim()));
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
		if(gastoOpciones.getOrdenDatosProveedorEnDetalleDelGasto().equals("0")){
			crearMapaDefault();
		}else{
			String newString = Arrays.toString(gastoOpciones.getOrdenDatosProveedorEnDetalleDelGasto().split(""));
            newString = newString.substring(1, newString.length()-1).replace(" ", ""); 
			this._mapOrdenDatosProveedorTarget = devolverMapa(newString.trim());
			this._mapOrdenDatosProveedorSource = new HashMap<String, String>();
			
			for (Entry<String, String> entry : _mapOrdenDatosProveedorOriginal.entrySet()) {
				if(!_mapOrdenDatosProveedorTarget.containsKey(entry.getKey())){
					_mapOrdenDatosProveedorSource.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}


	/**Orden de los datos de proveedores impresos en la descripcion del gasto
	 * @usedIn: ccListadoGastos y al updateOpcionesGastos
	 * @return: un nuevo mapa para asignar a mapOrdenDatosProveedor 
	 * @param prm_ordenDatos: el orden como quiero que devuelva ordenado mapOrdenDatosProveedor	
	 */

	public HashMap<String, String> devolverMapa(String prm_codigosDatos){
		if(this._mapOrdenDatosProveedorTarget == null){
			crearMapaDefault();
		}
		List<String> myList = new ArrayList<String>(Arrays.asList(prm_codigosDatos.split(",")));
		HashMap<String, String> tempMap = new HashMap<String, String>();
		
		for(String val : myList){
			for (Entry<String, String> entry : _mapOrdenDatosProveedorOriginal.entrySet()) {
				if(entry.getKey().equals(val)){
					tempMap.put(entry.getKey(), entry.getValue());
				}
			}
			
		}
		return tempMap;
	}

	
	
	/**Orden de los datos de proveedores impresos en la descripcion del gasto
	 * @usedIn: ***ESTO NO SE USA*** YA QUE ANTES TENIAMOS 4 POSICIONES Y SE ORDENABA POR ESAS POCICIONES
	 * @return: un nuevo mapa para asignar a mapOrdenDatosProveedor 
	 * @param prm_ordenDatos: el orden como quiero que devuelva ordenado mapOrdenDatosProveedor	
	 */

	public HashMap<String, String> devolverMapaOrdenado(String prm_ordenDatos){
		if(this._mapOrdenDatosProveedorTarget == null){
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
		this._mapOrdenDatosProveedorTarget = new HashMap<String, String>();
		this._mapOrdenDatosProveedorSource = new HashMap<String, String>();
		this._mapOrdenDatosProveedorSource.putAll(get_mapOrdenDatosProveedorOriginal());
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

	

	public ArrayList<String> getEdificiosConOpcionesGastos() {
		return edificiosConOpcionesGastos;
	}


	public HashMap<String, String> get_mapOrdenDatosProveedorTarget() {
		return _mapOrdenDatosProveedorTarget;
	}


	public void set_mapOrdenDatosProveedorTarget(
			HashMap<String, String> ordenDatosProveedor) {
		_mapOrdenDatosProveedorTarget = ordenDatosProveedor;
	}

	public HashMap<String, String> get_mapOrdenDatosProveedorSource() {
		return _mapOrdenDatosProveedorSource;
	}
	
	public HashMap<String, String> get_mapOrdenDatosProveedorOriginal() {
		this._mapOrdenDatosProveedorOriginal = ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getOpcionesGastoOrdenDatosProveedor();
		return _mapOrdenDatosProveedorOriginal;
	}
	
}
