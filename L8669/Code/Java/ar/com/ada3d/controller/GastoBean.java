package ar.com.ada3d.controller;

import java.math.BigDecimal;
import java.util.*;
import javax.faces.model.SelectItem;
import org.openntf.domino.Document;
import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Gasto;
import ar.com.ada3d.model.Prorrateo;
import ar.com.ada3d.utilidades.JSFUtil;

public class GastoBean {
	private static final long serialVersionUID = 1L;
	public List<Gasto> listaGastos;
	@SuppressWarnings("unused")
	private BigDecimal importeTotalListaGastos;
	private Gasto gasto;
	
	public GastoBean() {
		// Empty constructor
	}
	
	
	//***** INI BOTONES ****
	
	/**Cuando presiona btnNewGasto
	 * Creo un objeto vacio
	 */
	public void createNewGasto() {
		setGasto(new Gasto());
	}
	
	/**
	 * Funcion en el boton editar
	 * Actualizo el gasto y lockeo
	 * @usedIn: frmGasto
	 */
	public void editFormulario(){
		//TODO: Actualizar el gasto y loquearlo al editar
		this.gasto.setIsReadMode(false);
	}
	
	/**Cuando presiona btnSave Gasto
	 */
	public ArrayList<String> saveGasto() {
		return null;
		
	}
	
	//***** FIN BOTONES ****
	
	//***** INI VISTAS ****
	
	/**Cuando ingresa a la vista de Gastos
	 * @param prm_edificio:objeto edificio 
	 */
	public void viewGastos(Edificio prm_edificio){
		fillListaGastos(prm_edificio);
	}
	
	/**
	 * Completo la variable listaGastos consultando As400, cada linea separa el dato por un pipe
	 * Cada gasto puede tener mas de una linea, pero existe un unico importe por factura.
	 * @param prm_edificio:objeto edificio 
	 * @return la lista de facturas de un edificio
	 */
	private void fillListaGastos(Edificio prm_edificio){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("edf_codigo", prm_edificio.getEdf_codigo());
			nl = query.getSelectAS("controllerGastos", docDummy);
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
				
				//Detalle factura
				tempTextoFactura.add(strLinea.split("\\|")[2].trim());
				myGasto.setTextoDetalleFactura(tempTextoFactura);
				
				//Si es la linea de importes (NRENGL = TRENGL)
				if(strLinea.split("\\|")[7].trim().equals(strLinea.split("\\|")[8].trim())){
					//Prorrateo para gastos
					myGasto.setListaProrrateos(cargaProrrateo(strLinea, prm_edificio));					
				}
				
				myGasto.setIsReadMode(true);
				listaGastos.add(myGasto);
				count = count + 1;
			}else{//Es igual al anterior solo agregar el texto
				tempTextoFactura.add(strLinea.split("\\|")[2].trim());
				if(miGasto == null){
					miGasto = listaGastos.get(count - 1);
				}
				//Si es la linea de importes (NRENGL = TRENGL)
				if(strLinea.split("\\|")[7].trim().equals(strLinea.split("\\|")[8].trim())){
					//Prorrateo para gastos
					miGasto.setListaProrrateos(cargaProrrateo(strLinea, prm_edificio));					
				}

				miGasto.setTextoDetalleFactura(tempTextoFactura);					
			}
		}
	}
	
	//***** FIN VISTAS ****

	
	
	/**
	 * Al cargar una factura en la misma consulta al AS400 tambien cargo los importes de prorrateo
	 * @param strLinea de AS400 que estoy procesando, ¡¡debe ser la linea que contiene los importes!!
	 * @return la lista de prorrateos
	 */
	private List<Prorrateo> cargaProrrateo(String strLinea, Edificio prm_edificio){
		
		// ***** Esta funcion solo debe recibir la linea de importes !!! ***
		
		List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
		//Recorro el prorrateo de edificios y lo igualo al prorrateo de gastos
		for(Prorrateo edificioProrrateo : prm_edificio.getListaProrrateos()){
			Prorrateo gastoProrrateo = new Prorrateo();
			gastoProrrateo.setPrt_posicion(edificioProrrateo.getPrt_posicion());
			gastoProrrateo.setPrt_posicionEnGrilla(edificioProrrateo.getPrt_posicionEnGrilla());
			gastoProrrateo.setPrt_titulo(edificioProrrateo.getPrt_titulo());
			gastoProrrateo.setPrt_porcentaje(edificioProrrateo.getPrt_porcentaje());	
			gastoProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[(edificioProrrateo.getPrt_posicion()+ 2)].trim(), Locale.UK, 2)));
			listaProrrateos.add(gastoProrrateo);	
		}
		return listaProrrateos;
	}

	
	/**
	 * Al cambiar el combo de edificios en un gasto voy a blanquear los porcentuales y regenerarlos 
	 * agregando el total de importes en el primer porcentual disponible
	 * @param el gasto que estoy editando
	 * @param el nuevo edificio cambiado en el combo
	 */
	public void blanqueoProrrateosAlCambiarEdificio(Gasto prm_gasto, Edificio prm_edificio){
		if(prm_gasto.getListaProrrateos().size() == prm_edificio.getListaProrrateos().size())
			return;
		
		BigDecimal sumatoria = getImporteTotalListaGastos();
		prm_gasto.getListaProrrateos().clear(); //limpio lista
		List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
		for(Prorrateo edificioProrrateo : prm_edificio.getListaProrrateos()){
			//Recorro los prorrateos del edificio nuevo
			Prorrateo gastoProrrateo = new Prorrateo();
			gastoProrrateo.setPrt_posicion(edificioProrrateo.getPrt_posicion());
			gastoProrrateo.setPrt_posicionEnGrilla(edificioProrrateo.getPrt_posicionEnGrilla());
			gastoProrrateo.setPrt_titulo(edificioProrrateo.getPrt_titulo());
			gastoProrrateo.setPrt_porcentaje(edificioProrrateo.getPrt_porcentaje());
			if(edificioProrrateo.getPrt_posicionEnGrilla() == 0){
				gastoProrrateo.setPrt_importe(sumatoria);
			}else{
				gastoProrrateo.setPrt_importe(new BigDecimal(0));
			}
			listaProrrateos.add(gastoProrrateo);	
		}
		prm_gasto.setListaProrrateos(listaProrrateos);
	}
	
	
	public List<SelectItem> getComboboxAgrupamiento() {
		ArrayList<String> nl = null;
		List<SelectItem> options = new ArrayList<SelectItem>();
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("gastosCodigoAgrupamiento", null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		for (String strLinea : nl) {
			System.out.println();
			SelectItem option = new SelectItem();
			option.setLabel((strLinea.split("\\|")[0].trim() + " " + strLinea.split("\\|")[1].trim()).trim());
			option.setValue(strLinea.split("\\|")[0].trim());
			options.add(option);
				
		}
		return options;
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

	/**@return una sumatoria de importes prorrateados 
	 * */
	public BigDecimal getImporteTotalListaGastos() {
		BigDecimal bdResult = new BigDecimal("0");
		for(Prorrateo gastoProrrateo : gasto.getListaProrrateos()){
			bdResult = bdResult.add(gastoProrrateo.getPrt_importe());
		}
		return bdResult;
	}
	
	
}
