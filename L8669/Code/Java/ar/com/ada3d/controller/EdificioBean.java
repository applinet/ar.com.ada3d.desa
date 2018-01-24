package ar.com.ada3d.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.math.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.io.Serializable;
import javax.faces.model.SelectItem;

import org.openntf.domino.Document;

import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Prorrateo;
import ar.com.ada3d.utilidades.*;
import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.utilidades.DocUsr;

/*
 * Cuando hablo de edificios son todos los de la administracion.
 * Cuando hablo de MyEdificios son los edificios que puede ver el usuario.
 * Cuando hablo de MyEdificiosTrabajo son para trabajar el usuario es decir que no estan bloqueados
 * Tomo la scope de usuario para los edificios autorizados por usuario
 * */
public class EdificioBean implements Serializable {

	public EdificioBean() {
		//System.out.println("FPR - Constr. Edificios y llamada AS400");
		AddEdificiosAs400();
	}

	private Edificio edificio;
	private static final long serialVersionUID = 1L;
	HashMap<String, Edificio> hmEdificios = new HashMap<String, Edificio>();
	private static List<Edificio> listaEdificios;
	private static List<Edificio> listaEdificiosTrabajo;
	
	

	/*
	 * Esto devuelve para cada usuario el ComboBox de Edificios autorizados para
	 * trabajar
	 * 
	 * @return: etiqueta y valor para xp:comboBox
	 * 
	 * @usedIn: Combo principal en ccLayoutBootstrap, está asociado a una
	 * sessionScope(edificioSelected)
	 */
	public List<SelectItem> getComboboxMyEdificios() {
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		List<SelectItem> options = new ArrayList<SelectItem>();
		for (Edificio miEdificio : listaEdificios) {
			
			if (!docUsuario.getEdificiosNoAccessLista().contains(
					miEdificio.getEdf_codigo())) { // Solo edificios autorizados
				SelectItem option = new SelectItem();
				
				option
						.setLabel(miEdificio.getEdf_codigoVisual().equals("") ? miEdificio
								.getEdf_codigo()
								: miEdificio.getEdf_codigoVisual() + " "
										+ miEdificio.getEdf_direccion() + " "
										+ ar.com.ada3d.utilidades.Conversores.DateToString(miEdificio.getEdf_fechaUltimaLiquidacion(), "dd/MM/yyyy" ));
				option.setValue(miEdificio.getEdf_codigo());
				options.add(option);
			}
		}
		return options;
	}

	/*
	 * Esto devuelve para cada usuario el ComboBox de Edificios autorizados para
	 * trabajar y en estadoProceso=1
	 * 
	 * @return: etiqueta y valor para xp:comboBox
	 * 
	 * @usedIn: en el combo al lado del boton Save de un formulario
	 */
	public static List<SelectItem> getComboboxMyEdificiosTrabajo() {
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		List<SelectItem> options = new ArrayList<SelectItem>();
		for (Edificio miEdificio : listaEdificios) {
			if (miEdificio.getEdf_estadoProceso().equals("1")) { // solo
																	// estado=1
				if (!docUsuario.getEdificiosNoAccessLista().contains(
						miEdificio.getEdf_codigo())) { // Solo edificios
														// autorizados
					SelectItem option = new SelectItem();
					option.setLabel(miEdificio.getEdf_direccion());
					option.setValue(miEdificio.getEdf_codigo());
					options.add(option);
				}
			}
		}
		return options;
	}

		
	public ArrayList<String> getObjDataEdificiosPorUsuario() {
		ArrayList<String> result = new ArrayList<String>();
		for (Edificio miEdificio : listaEdificios) {
			result.add(miEdificio.getEdf_direccion() + "|"
					+ miEdificio.getEdf_codigo());
		}
		return result;
	}

	/*
	 * Agrego edificios de a uno
	 * 
	 * @Param: objeto Edificio
	 */
	public void AddEdificioMap(Edificio prmEdificio) {
		hmEdificios.put(prmEdificio.getEdf_codigo(), prmEdificio);
	}

	/*
	 * Obtengo un edificio por el codigo SASA
	 * 
	 * @Param: el codigo de edificio
	 * 
	 * @Return: un objeto Edificio o nulo
	 */
	public Edificio getEdificioMap(String prmCodigoEdificio) {
		if (hmEdificios.containsKey(prmCodigoEdificio)) {
			return hmEdificios.get(prmCodigoEdificio);
		}
		return null;
	}

	/*
	 * Agrego Edificios consultando As400, cada linea separa el dato por un pipe
	 */
	public void AddEdificiosAs400() {
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		if (!(listaEdificios == null)){
			listaEdificios.clear();
			listaEdificiosTrabajo.clear();
			hmEdificios.clear();
			
		}	
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		ArrayList<String> nl = null;
		Edificio myEdificio;
		
		try {
			nl = query.getSelectAS("controllerEdificios", null, false);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		for (String strLinea : nl) {
			if (listaEdificios == null) {
				listaEdificios = new ArrayList<Edificio>();
			}
			if (listaEdificiosTrabajo == null) {
				listaEdificiosTrabajo = new ArrayList<Edificio>();
			}
			myEdificio = new Edificio();
			myEdificio.setEdf_codigo(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			myEdificio.setEdf_fechaUltimaLiquidacion(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[4].trim()));
			myEdificio.setEdf_frecuenciaLiquidacion  (Integer.parseInt(strLinea.split("\\|")[5].trim()));
									
			myEdificio.setListaProrrateos(cargaProrrateoEdificio(strLinea));
			
			
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(myEdificio.getEdf_fechaUltimaLiquidacion());
			cal.add(Calendar.MONTH, myEdificio.getEdf_frecuenciaLiquidacion());
			cal.set(cal.DATE, cal.getActualMaximum(cal.DAY_OF_MONTH));
			myEdificio.setEdf_fechaProximaLiquidacion(cal.getTime());

			myEdificio.setEdf_isReadMode(true);
			listaEdificios.add(myEdificio);
			if(!docUsuario.getEdificiosNoAccessLista().contains(strLinea.split("\\|")[0].trim())){
				//System.out.println("FPR - " + docUsuario.getUser() + " - " + strLinea.split("\\|")[2].trim());
				listaEdificiosTrabajo.add(myEdificio);
			}
			AddEdificioMap(myEdificio); // Lo agrego al mapa por código
		}
	}

	
	/*
	 * Al cargar un edificio en la misma consulta al AS400 tambien cargo los datos de prorrateo
	 */
	private List<Prorrateo> cargaProrrateoEdificio(String strLinea){
		Prorrateo myProrrateo;
		List<Prorrateo> listaPorcentualesEdificio = new ArrayList<Prorrateo>();
		int posicionPorcentaje = 5;
		int posicionCuotaFija = 9;
		int posicionPresupuesto = 13;
		
		int tempPorcentaje = 0;
		String strCuotaFija;
		String strPresupuesto;
		String strTipo;
		
		for(int i=1; i<5; i++){ //Son 4 prorrateos por edificio
			//variables que recorren 4 valores a prorratear
			posicionPorcentaje = posicionPorcentaje + 1;
			posicionCuotaFija = posicionCuotaFija + 1;
			posicionPresupuesto = posicionPresupuesto + 1;
			
			//Define si se crea o no el objeto Prorrateo
			tempPorcentaje = Integer.parseInt(strLinea.split("\\|")[posicionPorcentaje].trim());
			if(tempPorcentaje != 0){
				myProrrateo = new Prorrateo();
				myProrrateo.setPrt_posicion(i);
				myProrrateo.setPrt_titulo("Porcentual # " + i);
				myProrrateo.setPrt_porcentaje(tempPorcentaje);
				
				strCuotaFija = strLinea.split("\\|")[posicionCuotaFija].trim();
				strPresupuesto = strLinea.split("\\|")[posicionPresupuesto].trim();
				//Defino el tipo de prorrateo
				strTipo = strCuotaFija.equals("0") ? "P" : "C";
				strTipo = strCuotaFija.equals("0") && strPresupuesto.equals("0") ? "G" : strTipo;
				myProrrateo.setPrt_tipo(strTipo);
				
				if (strTipo.equals("C")){
					myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strCuotaFija, Locale.UK, 2)));
				}else if (strTipo.equals("P")){
					myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strPresupuesto, Locale.UK, 2)));
				}else{
					//myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal("0", Locale.UK, 2)));
				}
				listaPorcentualesEdificio.add(myProrrateo);
			}
			
			
		}
		return listaPorcentualesEdificio;
	}
	
	
	public void updateEdificiosAs400() {
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		ArrayList<String> nl = null;
		Edificio myEdificio;
		try {
			nl = query.getSelectAS("controllerEdificios", null, false);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		for (String strLinea : nl) {
			System.out.println("Ya ");
			myEdificio = getEdificioMap(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			
		
		}
	}

	
	public void saveEdificio(Edificio edificio) {
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("EDIF", edificio.getEdf_codigo());
		docDummy.appendItemValue("DIRECC", edificio.getEdf_direccion());
		docDummy.appendItemValue("E20A", edificio.getEdf_codigoVisual());
		
		QueryAS400 query = new QueryAS400();

		if (query.updateAS("updateEdificios", docDummy)) {
			//System.out.println("FPR UpdateQuery OK_Codigo: " + edificio.getEdf_codigo());
		}
	}

	
	//*** Getters & Setters *****
	
	public List<Edificio> getListaEdificios() {
		return listaEdificios;
	}

	public void setListaEdificios(ArrayList<Edificio> edificios) {
		EdificioBean.listaEdificios = edificios;
	}
	
	public List<Edificio> getListaEdificiosTrabajo() {
		return listaEdificiosTrabajo;
	}
	
	public void setListaEdificiosTrabajo(ArrayList<Edificio> edificios) {
		EdificioBean.listaEdificiosTrabajo = edificios;
	}

	public Edificio getEdificio() {
		return edificio;
	}

	public void setEdificio(Edificio edificio) {
		this.edificio = edificio;
	}

}
