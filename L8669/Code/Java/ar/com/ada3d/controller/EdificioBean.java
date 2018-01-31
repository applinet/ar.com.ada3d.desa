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
	@SuppressWarnings({ "static-access" })
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
			String tempDireccionLocalidad = strLinea.split("\\|")[2].trim();
			if(!tempDireccionLocalidad.contains("-")){
				throw new java.lang.Error("La direccion del edificio " + myEdificio.getEdf_codigo() + " no tiene un guión.");
				//throw new java.lang.RuntimeException("La direccion del edificio " + myEdificio.getEdf_codigo() + " no tiene un guión.");
			}
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim().split("-")[0]);
			myEdificio.setEdf_localidad(strLinea.split("\\|")[2].trim().split("-")[1]);
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			myEdificio.setEdf_fechaUltimaLiquidacion(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[4].trim()));
			myEdificio.setEdf_frecuenciaLiquidacion  (Integer.parseInt(strLinea.split("\\|")[5].trim()));
									
			Calendar cal = Calendar.getInstance();
			cal.setTime(myEdificio.getEdf_fechaUltimaLiquidacion());
			cal.add(Calendar.MONTH, myEdificio.getEdf_frecuenciaLiquidacion());
			cal.set(cal.DATE, cal.getActualMaximum(cal.DAY_OF_MONTH));
			myEdificio.setEdf_fechaProximaLiquidacion(cal.getTime());

			
			//Defino el tipo de prorrateo			
			String strTipo = strLinea.split("\\|")[14].trim();
			String strTempTipo;
			if(strTipo.equals("") || strTipo.equals("N")){
			    strTempTipo = "C";
			}else if (strTipo.equals("P")){//Presupuesto
				strTempTipo = "P";	
			}else{
				strTempTipo = "G";	
			}	
		    
			myEdificio.setListaProrrateos(cargaProrrateoEdificio(strLinea, strTempTipo));
			//Creo el combo de fecha prorrateo si es cuota fija
			if(strTipo.equals("") || strTipo.equals("N")){ //blanco puede ser por gastos
				for(Prorrateo myProrrateo : myEdificio.getListaProrrateos()){
					if(myProrrateo.getPrt_tipo().equals("C")){
						List<SelectItem> options = new ArrayList<SelectItem>();
					    SelectItem optionMesLiquedacion = new SelectItem();
					    optionMesLiquedacion.setLabel(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "dd/MM/yyyy"));
					    optionMesLiquedacion.setValue("N");
					    options.add(optionMesLiquedacion);

					    if(strTipo.equals("")){//CF y fecha prorrateo le agrego 1 dia para que sea el dia 1 del mes siguiente
					    	cal.add(Calendar.DATE, 1);
							myEdificio.setEdf_cuotaFijaDia("B");
					    }else{//CF y fecha prorrateo ídem liquidación
					    	myEdificio.setEdf_cuotaFijaDia("N");
					    	cal.add(Calendar.DATE, 1);
					    }
					    
					    SelectItem optionMesSiguiente = new SelectItem();
					    optionMesSiguiente.setLabel(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "dd/MM/yyyy"));
					    optionMesSiguiente.setValue("B");
					    options.add(optionMesSiguiente);
					    myEdificio.setEdf_cuotaFijaDiaOpcionesCombo(options);
						break;
					}
				}
			}
			
			
			myEdificio.setEdf_imprimeTitulosEnLiquidacion("0"); //Falta definir el campo
			
			myEdificio.setEdf_interesPunitorioDeudores( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[15].trim(), Locale.US, 1)));
			myEdificio.setEdf_recargoSegundoVencimiento( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[16].trim(), Locale.UK, 1)));
			myEdificio.setEdf_fechaPrimerVencimientoRecibos(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[17].trim()));
			myEdificio.setEdf_fechaSegundoVencimientoRecibos(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[18].trim()));
			myEdificio.setEdf_modalidadInteresesPunitorios(strLinea.split("\\|")[19].trim());
			myEdificio.setEdf_cuit(strLinea.split("\\|")[20].trim());		
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
	private List<Prorrateo> cargaProrrateoEdificio(String strLinea, String strTipo){
		Prorrateo myProrrateo;
		List<Prorrateo> listaPorcentualesEdificio = new ArrayList<Prorrateo>();
		int posicionPorcentaje = 5;
		int posicionCuotaFija = 9;
		int tempPorcentaje = 0;
		String strValorCuotaFija;
		
		for(int i=1; i<5; i++){ //Son 4 prorrateos por edificio
			//variables que recorren 4 valores a prorratear
			posicionPorcentaje = posicionPorcentaje + 1;
			posicionCuotaFija = posicionCuotaFija + 1;
			
			//Define si se crea o no el objeto Prorrateo
			tempPorcentaje = Integer.parseInt(strLinea.split("\\|")[posicionPorcentaje].trim());
			if(tempPorcentaje != 0){
				myProrrateo = new Prorrateo();
				myProrrateo.setPrt_posicion(i);
				myProrrateo.setPrt_titulo("Porcentual # " + i);
				myProrrateo.setPrt_porcentaje(tempPorcentaje);
				
				//valor que va a tener la cuota fija o el presupuesto
				strValorCuotaFija = strLinea.split("\\|")[posicionCuotaFija].trim();

				//Defino el tipo individual de prorrateo, pero si el valor es 0 es gasto
				if (strValorCuotaFija.equals("0")){
					myProrrateo.setPrt_tipo("G");
				}else if (strTipo.equals("P")){
					myProrrateo.setPrt_tipo("P");
					myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strValorCuotaFija, Locale.UK, 2)));
				}else if(strTipo.equals("C")){
					myProrrateo.setPrt_tipo("C");
					myProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strValorCuotaFija, Locale.UK, 2)));
				}else{
					myProrrateo.setPrt_tipo("G");
				}
				
				listaPorcentualesEdificio.add(myProrrateo);
			}
		}
		return listaPorcentualesEdificio;
	}
	
	public void onChangeProrrateos(Edificio prm_edificio){
		for(Prorrateo myProrrateo : prm_edificio.getListaProrrateos()){
			System.out.println("tipo: " + myProrrateo.getPrt_tipo());
			System.out.println("importe: " + myProrrateo.getPrt_importe());
			System.out.println("cod Cuota F: " + prm_edificio.getEdf_cuotaFijaDia());
			prm_edificio.setEdf_cuotaFijaDia("B");
		}
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
			myEdificio = getEdificioMap(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			
		
		}
	}

	
	/*
	 * Update de tablas AS400 con los datos del edificio
	 */
	public void saveEdificio(Edificio prm_edificio) {
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Codigo", prm_edificio.getEdf_codigo());
		docDummy.appendItemValue("DIRECC", prm_edificio.getEdf_direccion() + "-" + prm_edificio.getEdf_localidad());
		docDummy.appendItemValue("CodigoVisual", prm_edificio.getEdf_codigoVisual());
		
		QueryAS400 query = new QueryAS400();
		
		if (query.updateAS("updateEdificios", docDummy)) {
			System.out.println("FPR UpdateQuery OK_Codigo: " + prm_edificio.getEdf_codigo());
		}
	}
	

	/*
	 * Chequea los datos del edificio recibido por parámetros
	 * 
	 * @usedIn: Boton save edificio
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 */
	public ArrayList<String> strValidacionEdificio(Edificio prm_edificio){
		/*FacesMessage message = new FacesMessage("No es válido");
			// Throw exception so that it prevents document from being saved
			throw new ValidatorException(message);
		 */
		ArrayList<String> listAcumulaErrores = new ArrayList<String>();
		String strTemp = "";
		
		/*INI - Codigo Visual(reemplazo)
		 * - la consulta debe ser por SQL  
		 * - Puede ser = al codigo del edificio actual
		 * - No puede ser = a un codigo visual existente
		 * - No puede ser = a un codigo sasa que no sea el que estoy 
		*/
		QueryAS400 query = new QueryAS400();
		try {
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("CodigoVisual", prm_edificio.getEdf_codigoVisual());
			ArrayList<String> nl = query.getSelectAS("edificiosValidacionCodigoReemplazo", docDummy, false);
			for (String strLinea : nl) {
				if(!strLinea.split("\\|")[0].trim().equals(prm_edificio.getEdf_codigo())){
					listAcumulaErrores.add("edf_codigoVisual~Código de reemplazo duplicado." + " El código " + prm_edificio.getEdf_codigoVisual() + " está siendo utilizado en otro edificio.");
				}
			}
		} catch (NotesException e) {
			e.printStackTrace();
		}
		//FIN - Codigo Visual(reemplazo)

		
		//La direccion + localidad max. 27 long.
		strTemp = prm_edificio.getEdf_direccion() + "-" + prm_edificio.getEdf_localidad();
		if(strTemp.length() > 27){
			listAcumulaErrores.add("edf_direccion~El domicilio con la localidad, no deben superar los 26 caracteres.");
			listAcumulaErrores.add("edf_localidad~Los siguientes caracteres exceden el largo permitido: " + strTemp.substring(0, 27) + " --> " + strTemp.substring(27));
		}
		
		//El título de cada valor a prorratear max. 11 long. 

		
		return listAcumulaErrores;
		
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
