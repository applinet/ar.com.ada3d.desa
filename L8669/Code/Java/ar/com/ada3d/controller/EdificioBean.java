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
import org.openntf.domino.Session;


import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Porcentual;
import ar.com.ada3d.model.Prorrateo;
import ar.com.ada3d.utilidades.*;
import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.utilidades.DocUsr;
import java.math.BigDecimal;
import java.util.*;

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
	 * @usedIn: layout tiene una propiedad para mostrar o no en cada XPage
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
		Edificio myEdificio = null;
		
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
					
			myEdificio = actualizoUnEdificioAs400( myEdificio, strLinea, true);
			
			listaEdificios.add(myEdificio);
			if(!docUsuario.getEdificiosNoAccessLista().contains(strLinea.split("\\|")[0].trim())){
				listaEdificiosTrabajo.add(myEdificio);
			}
			AddEdificioMap(myEdificio); // Lo agrego al mapa por código
		}
	}
	
	private List<SelectItem> opcionesDiaCuotaFija(Calendar cal){
		List<SelectItem> options = new ArrayList<SelectItem>();
	    SelectItem optionMesLiquedacion = new SelectItem();
	    optionMesLiquedacion.setLabel(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "dd/MM/yyyy"));
	    optionMesLiquedacion.setValue("N");
	    options.add(optionMesLiquedacion);
	    
	    cal.add(Calendar.DATE, 1);
	    SelectItem optionMesSiguiente = new SelectItem();
	    optionMesSiguiente.setLabel(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "dd/MM/yyyy"));
	    optionMesSiguiente.setValue("B");
	    options.add(optionMesSiguiente);
	    return options;

	}
	
	/*
	 * Al cargar un edificio en la misma consulta al AS400 tambien cargo los datos de prorrateo
	 */
	private List<Prorrateo> cargaProrrateoEdificio(String strLinea, String strTipo){
		Prorrateo myProrrateo;
		List<Prorrateo> listaProrrateosEdificio = new ArrayList<Prorrateo>();
		int posicionPorcentaje = 5;
		int posicionCuotaFija = 9;
		int tempPorcentaje = 0;
		int tempPosicionEnGrilla = 0;
		String strValorCuotaFija;
		
		for(int i=1; i<5; i++){ //Son 4 porcentuales por ahora
			//variables que recorren 4 valores a prorratear
			posicionPorcentaje = posicionPorcentaje + 1;
			posicionCuotaFija = posicionCuotaFija + 1;
			
			//Define si se crea o no el objeto Prorrateo
			tempPorcentaje = Integer.parseInt(strLinea.split("\\|")[posicionPorcentaje].trim()); 
			if(tempPorcentaje != 0){
				myProrrateo = new Prorrateo();
				myProrrateo.setPrt_posicion(i);
				myProrrateo.setPrt_posicionEnGrilla(tempPosicionEnGrilla);
				tempPosicionEnGrilla = tempPosicionEnGrilla + 1;
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
				
				listaProrrateosEdificio.add(myProrrateo);
			}
		}
		return listaProrrateosEdificio;
	}
	
	/*
	 * Al cargar un edificio en la misma consulta al AS400 tambien cargo los datos de porcentuales
	 */
	
	private List<Porcentual> cargaPorcentualEdificio(String strLinea){
		Porcentual myPorcentual;
		List<Porcentual> listaPorcentualesEdificio = new ArrayList<Porcentual>();
		int posicionPorcentaje = 6;
		int posicionHonorarios = 22;
		int posicionEnGrilla = 0;
		for(int i=1; i<5; i++){ //Son 4 porcentuales por ahora
			myPorcentual = new Porcentual();
			myPorcentual.setPorc_posicion(i);
			myPorcentual.setPorc_titulo("Honorarios % " + i);
			myPorcentual.setPorc_porcentaje(Integer.parseInt(strLinea.split("\\|")[posicionPorcentaje].trim()));
			myPorcentual.setPorc_importeHonorarios(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[posicionHonorarios].trim(), Locale.UK, 2)));
			posicionPorcentaje = posicionPorcentaje + 1;
			posicionHonorarios = posicionHonorarios + 1;
			posicionEnGrilla = posicionEnGrilla + 1;
			listaPorcentualesEdificio.add(myPorcentual);
		}
		return listaPorcentualesEdificio;
		
	}
	/*
	 * Oculta o visualiza otros campos al cambiar la opción
	 * 
	 * @param: 
	 * -El edificio que estoy modificando
	 * -La posición que estoy modificando en la XPage (es un repeat)
	 * 
	 * @usedIn: en el combo de valores a prorratear
	 */
	public String onChangeProrrateos(Edificio prm_edificio, Integer prm_idxRptProrrateoTipo){
		BigDecimal tempSumatoriaImportes = new BigDecimal(0);
		ArrayList<String> tempArrayCuotaFijaDia = new ArrayList<String>();
		String tempCuotaFijaDia = "";
		for(Prorrateo myProrrateo : prm_edificio.getListaProrrateos()){
			tempArrayCuotaFijaDia.add(myProrrateo.getPrt_tipo());
			if (prm_idxRptProrrateoTipo.equals(myProrrateo.getPrt_posicionEnGrilla())){
				//Solo modifico la linea del repeat que ha cambiado
				if (myProrrateo.getPrt_tipo().equals("G")){
					myProrrateo.setPrt_importe(null);
					
				}else{//CUOTA FIJA o Presupuesto
					if(myProrrateo.getPrt_importe() == null){
						myProrrateo.setPrt_importe(new BigDecimal(1));						
					}
					if (myProrrateo.getPrt_tipo().equals("C")){	//CF cargo el combo
						prm_edificio.setEdf_cuotaFijaDiaOpcionesCombo(opcionesDiaCuotaFija(ar.com.ada3d.utilidades.Conversores.dateToCalendar(prm_edificio.getEdf_fechaProximaLiquidacion())));
						tempCuotaFijaDia = "B";
					}else{
						tempCuotaFijaDia = "P";
					}
				}
			}
			
			//Hago una sumatoria si da cero tengo que enviar un *
			if(myProrrateo.getPrt_tipo().equals("G")){
				tempSumatoriaImportes = tempSumatoriaImportes.add(new BigDecimal(0));
			}else{
				tempSumatoriaImportes = tempSumatoriaImportes.add(myProrrateo.getPrt_importe());
			}
		}
		
		//Al final chequeo todo antes de confirmar la cuotaFijaDia
		if(tempSumatoriaImportes.equals(new BigDecimal(0))){
			//Son todos gastos
			prm_edificio.setEdf_cuotaFijaDia("*");
		}else{
			if(tempCuotaFijaDia.equals("P")){//Chequeo si cambio realmente a P
				if(tempArrayCuotaFijaDia.contains("C") || tempArrayCuotaFijaDia.contains("B")){
					return "prt_tipo~No puede seleccionar PRESUPUESTO si existe una CUOTA FIJA.";
				}else{
					prm_edificio.setEdf_cuotaFijaDia("P");
				}
			}else if(tempCuotaFijaDia.equals("C") || tempCuotaFijaDia.equals("B")){//Chequeo si cambio realmente a C
				if(tempArrayCuotaFijaDia.contains("P")){
					return "prt_tipo~No puede seleccionar CUOTA FIJA si existe un PRESUPUESTO .";
				}else{
					prm_edificio.setEdf_cuotaFijaDia(tempCuotaFijaDia);
				}
			}else if(tempCuotaFijaDia.equals("*")){//Chequeo si cambio realmente a *
				if(tempArrayCuotaFijaDia.contains("P") || tempArrayCuotaFijaDia.contains("C") || tempArrayCuotaFijaDia.contains("B")){
					prm_edificio.setEdf_cuotaFijaDia(tempCuotaFijaDia);
				}
			}
			
		}
		return "";
	}
	
	/*
	 * Modifica los siguientes datos:
	 * -Fecha próxima liquidacion
	 * -Mes de prorrateo y recibos (solo si es cuota fija)
	 * -Fecha 1er vto en recibos
	 * -Fecha 2do vto en recibos
	 * 
	 * @param: 
	 * -El edificio que estoy modificando
	 * 
	 * @usedIn: en el slider de frecuencia
	 */
	@SuppressWarnings("static-access")
	public void onClickFrecuencia(Edificio prm_edificio){			
		Calendar cal = Calendar.getInstance();
		cal.setTime(prm_edificio.getEdf_fechaUltimaLiquidacion());
		cal.add(Calendar.MONTH, prm_edificio.getEdf_frecuenciaLiquidacion());
		cal.set(cal.DATE, cal.getActualMaximum(cal.DAY_OF_MONTH));
		prm_edificio.setEdf_fechaProximaLiquidacion(cal.getTime());
		if(prm_edificio.getEdf_cuotaFijaDia().equals("N") || prm_edificio.getEdf_cuotaFijaDia().equals("B")){
			prm_edificio.setEdf_cuotaFijaDiaOpcionesCombo(opcionesDiaCuotaFija(ar.com.ada3d.utilidades.Conversores.dateToCalendar(prm_edificio.getEdf_fechaProximaLiquidacion())));
		}
		
		
		/*
		cal.setTime(sessionScope.edfObj.edf_fechaPrimerVencimientoRecibos)
		cal.add(java.util.Calendar.MONTH, frecuencia);
		sessionScope.edfObj.edf_fechaPrimerVencimientoRecibos = cal.getTime();
		*/
		
	}

	
	private Edificio actualizoUnEdificioAs400(Edificio myEdificio, String strLinea, boolean isNew) {
		DocLock lockeos = (DocLock) JSFUtil.resolveVariable("DocLock");
		if(isNew){
			myEdificio = new Edificio();
			myEdificio.setEdf_codigo(strLinea.split("\\|")[0].trim());
		}else{
			//Es una actualizacion
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("Codigo", myEdificio.getEdf_codigo());
			QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
			ArrayList<String> nl = null;
			try {
				nl = query.getSelectAS("controllerUnEdificio", docDummy, false);
			} catch (NotesException e) {
				e.printStackTrace();
			}
			
			strLinea = nl.get(0);
				
		}
		//Agrego el usuario si el edificio está lockeado
		if(lockeos.isLocked("edf_" + myEdificio.getEdf_codigo()))
			myEdificio.setEdf_lockedBy(lockeos.getLock("edf_" + myEdificio.getEdf_codigo()));

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
		
		
		myEdificio.setEdf_importeInteresPunitorioDeudores( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[15].trim(), Locale.US, 1)));
		myEdificio.setEdf_importeRecargoSegundoVencimiento( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[16].trim(), Locale.UK, 1)));
		
		myEdificio.setEdf_fechaPrimerVencimientoRecibos(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[17].trim()));
		myEdificio.setEdf_fechaSegundoVencimientoRecibos(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[18].trim()));
		
		myEdificio.setEdf_modalidadInteresesPunitorios(strLinea.split("\\|")[19].trim());
		myEdificio.setEdf_cuit(strLinea.split("\\|")[20].trim());
		myEdificio.setEdf_imprimeTitulosEnLiquidacion(strLinea.split("\\|")[21].trim().equals("1") ? "1":"0");
		
		//Voy a cargar todos los porcentuales, los utilizados y los no utilizados
		myEdificio.setListaPorcentuales(cargaPorcentualEdificio(strLinea));
		
		//TODO: falta saber que campo tomar del AS400
		myEdificio.setEdf_importeFranqueo( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[15].trim(), Locale.US, 2)));
		myEdificio.setEdf_importeMultaDeudores( new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[15].trim(), Locale.US, 2)));
		
		myEdificio.setEdf_isReadMode(true);
		return myEdificio;
	}


	/*
	 * Actualizo solo el edificio que voy a modificar
	 * Lock del edificio
	 */
	public void editEdificio(Edificio prm_edificio) {
		actualizoUnEdificioAs400(prm_edificio, "", false);
		Session session = JSFUtil.getSession();
		DocLock lock = (DocLock) JSFUtil.resolveVariable("DocLock");
		if (lock.isLocked("edf_" + prm_edificio.getEdf_codigo())){
			if (!lock.getLock("edf_" + prm_edificio.getEdf_codigo()).equals(session.getEffectiveUserName()))
				return;
		}
		lock.addLock("edf_" + prm_edificio.getEdf_codigo(), session.getEffectiveUserName());
		prm_edificio.setEdf_isReadMode(false);
		prm_edificio.setEdf_lockedBy(session.getEffectiveUserName());
		
	}
	
	/*
	 * Update de tablas AS400 con los datos del edificio
	 */
	public void saveEdificio(Edificio prm_edificio) {
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Codigo", prm_edificio.getEdf_codigo());
		docDummy.appendItemValue("DIRECC", prm_edificio.getEdf_direccion() + "-" + prm_edificio.getEdf_localidad());
		docDummy.appendItemValue("CodigoVisual", prm_edificio.getEdf_codigoVisual());
		docDummy.appendItemValue("frecuencia", prm_edificio.getEdf_frecuenciaLiquidacion());
		docDummy.appendItemValue("imprimirTitulos", prm_edificio.getEdf_imprimeTitulosEnLiquidacion());
		docDummy.appendItemValue("CTFJ1", "0");
		docDummy.appendItemValue("CTFJ2", "0");
		docDummy.appendItemValue("CTFJ3", "0");
		docDummy.appendItemValue("CTFJ4", "0");
		docDummy.appendItemValue("E12", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(prm_edificio.getEdf_importeInteresPunitorioDeudores(),1));
		docDummy.appendItemValue("E08A", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(prm_edificio.getEdf_importeRecargoSegundoVencimiento(),1));
		docDummy.appendItemValue("VTOEX1", ar.com.ada3d.utilidades.Conversores.DateToString(prm_edificio.getEdf_fechaPrimerVencimientoRecibos(), "ddMMyy"));
		docDummy.appendItemValue("VTOEX2", ar.com.ada3d.utilidades.Conversores.DateToString(prm_edificio.getEdf_fechaSegundoVencimientoRecibos(), "ddMMyy"));
		
		
		//Recorro los prorrateos para cargar Cuota Fija o Presupuesto
		for(Prorrateo myProrrateo: prm_edificio.getListaProrrateos()){
			if (myProrrateo.getPrt_importe() != null){
				docDummy.replaceItemValue("CTFJ" + myProrrateo.getPrt_posicion(), ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myProrrateo.getPrt_importe(), 2));
			}
		}
		//Estado blanco de AS400 yo lo tengo como B. Se reemplaza
		docDummy.appendItemValue("ESTADO2", prm_edificio.getEdf_cuotaFijaDia().equals("B")? "" : prm_edificio.getEdf_cuotaFijaDia());
		docDummy.appendItemValue("E13A", prm_edificio.getEdf_modalidadInteresesPunitorios());
		
		for (Porcentual myPorcentual : prm_edificio.getListaPorcentuales()){
			switch (myPorcentual.getPorc_posicion()){
			case 1:
				docDummy.replaceItemValue("E391", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myPorcentual.getPorc_importeHonorarios(), 2));
				break; 
			case 2:
				docDummy.replaceItemValue("E392", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myPorcentual.getPorc_importeHonorarios(), 2));
				break; 
			case 3:
				docDummy.replaceItemValue("E441", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myPorcentual.getPorc_importeHonorarios(), 2));
				break; 
			case 4:
				docDummy.replaceItemValue("E442", ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myPorcentual.getPorc_importeHonorarios(), 2));
				break; 
			}
		}
		
		QueryAS400 query = new QueryAS400();
		
		if (query.updateAS("updateEdificiosPH_E01", docDummy)) {
			if (!query.updateAS("updateEdificiosValoresCTFJ", docDummy)) {
				if (!query.updateAS("updateEdificiosValoresCTFJ_insert", docDummy))						
					throw new java.lang.Error("No se pudo actualizar la tabla PH_CTFJ.");
			}
			
			if (!query.updateAS("updateEdificiosDIFED", docDummy)) {
				throw new java.lang.Error("No se pudo actualizar la tabla PH_DIFED.");
			}
		}else{
			throw new java.lang.Error("No se pudo actualizar la tabla PH_E01.");
		}
		DocLock lock = (DocLock) JSFUtil.resolveVariable("DocLock");
		lock.removeLock("edf_" + prm_edificio.getEdf_codigo());
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
		
		//Prorrateos
		ArrayList<String> tempArrayCuotaFijaDia = new ArrayList<String>();
		for(Prorrateo myProrrateo : prm_edificio.getListaProrrateos()){
			tempArrayCuotaFijaDia.add(myProrrateo.getPrt_tipo());
		}
		String tempCuotaFijaDia = prm_edificio.getEdf_cuotaFijaDia();
		if(tempCuotaFijaDia.equals("P")){//Chequeo si cambio realmente a P
			if(tempArrayCuotaFijaDia.contains("C") || tempArrayCuotaFijaDia.contains("B")){
				listAcumulaErrores.add("prt_tipo~No puede seleccionar PRESUPUESTO si existe una CUOTA FIJA.");
			}
		}else if(tempCuotaFijaDia.equals("C") || tempCuotaFijaDia.equals("B")){//Chequeo si cambio realmente a C
			if(tempArrayCuotaFijaDia.contains("P")){
				listAcumulaErrores.add("prt_tipo~No puede seleccionar CUOTA FIJA si existe un PRESUPUESTO .");
			}
		}
		
		//El título de cada valor a prorratear max. 11 long. 
		
		
		//Fecha primer y segundo vencimiento
		if(prm_edificio.getEdf_importeInteresPunitorioDeudores().compareTo(BigDecimal.ZERO) > 0){
			Calendar calMin = Calendar.getInstance();
			calMin.setTime(prm_edificio.getEdf_fechaProximaLiquidacion());
			calMin.add(Calendar.DATE, 1);
			
			Calendar calNew = Calendar.getInstance();
			calNew.setTime(prm_edificio.getEdf_fechaPrimerVencimientoRecibos());
			
			if (calNew.before(calMin)) {
				listAcumulaErrores.add("edf_fechaPrimerVencimientoRecibos~La fecha de primer vto. no puede ser menor a " + ar.com.ada3d.utilidades.Conversores.DateToString(calMin.getTime(), "dd/MM/yyyy" ));
			}
			
			if(prm_edificio.getEdf_importeRecargoSegundoVencimiento().compareTo(BigDecimal.ZERO) > 0){
				calMin.setTime(prm_edificio.getEdf_fechaPrimerVencimientoRecibos());
				calMin.add(Calendar.DATE, 1);
				calNew.setTime(prm_edificio.getEdf_fechaSegundoVencimientoRecibos());
				if (calNew.before(calMin)) {
					listAcumulaErrores.add("edf_fechaSegundoVencimientoRecibos~La fecha de 2° vto. debe ser mayor al 1° vto ");
				}
			}
			
			
		}
		
		
		return listAcumulaErrores;
		
	}

	public ArrayList<String> strValidacionMasivoEdificios(String prm_campo, Object prm_valor){
		ArrayList<String> listAcumulaErrores = new ArrayList<String>();
		if(prm_valor instanceof String){
			if(prm_valor.equals("") || prm_valor.equals("0")){
				listAcumulaErrores.add(prm_campo + "~Debe ingresar un valor.");
				
			}else{
				System.out.println("w:" + prm_valor);
			}
		}else if(prm_valor instanceof Double || prm_valor instanceof Long){
			
		
			
			
			BigDecimal valor = new BigDecimal(prm_valor.toString());
			if(valor.compareTo(new BigDecimal(9999)) == 1){
				listAcumulaErrores.add(prm_campo + "~El % de interés no puede superar el 9999 %" );
				return listAcumulaErrores;
			}
				
			valor = valor.setScale(1, RoundingMode.HALF_EVEN);
			for(Edificio myEdificio : listaEdificiosTrabajo){
				//Primero valido que no este lockeado
				if(myEdificio.getEdf_lockedBy() == null || myEdificio.getEdf_lockedBy() == JSFUtil.getSession().getEffectiveUserName()){
					if (prm_campo.equals("importeInteresPunitorioDeudoresMasivo"))
						myEdificio.setEdf_importeInteresPunitorioDeudores(valor);
					if (prm_campo.equals("recargoSegundoVencimientoMasivo"))
						myEdificio.setEdf_importeRecargoSegundoVencimiento(valor);
				}else{
					listAcumulaErrores.add(prm_campo + "~El edificio " + myEdificio.getEdf_codigo() + " no se pudo actualizar ya que estaba siendo modificado por otro usuario." );
				}
					
			}
		}else if(prm_valor instanceof Date){
			for(Edificio myEdificio : listaEdificiosTrabajo){
				//Primero valido que no este lockeado
				if(myEdificio.getEdf_lockedBy() == null || myEdificio.getEdf_lockedBy() == JSFUtil.getSession().getEffectiveUserName()){
					//Voy a validar las fechas ingresadas en cada edificio

					Calendar calMin = Calendar.getInstance();
					calMin.setTime(myEdificio.getEdf_fechaProximaLiquidacion());
					calMin.add(Calendar.DATE, 1);
					
					if(myEdificio.getEdf_importeInteresPunitorioDeudores().compareTo(BigDecimal.ZERO) > 0 && prm_campo.equals("fechaPrimerVtoMasivo")){
						
						Calendar calNew = Calendar.getInstance();
						calNew.setTime((Date) prm_valor);
						
						if (calNew.before(calMin)) {
							listAcumulaErrores.add(prm_campo + "~Edificio " + myEdificio.getEdf_codigo() + " la fecha de primer vto. no puede ser menor a " + ar.com.ada3d.utilidades.Conversores.DateToString(calMin.getTime(), "dd/MM/yyyy" ));
						}else{
							myEdificio.setEdf_fechaPrimerVencimientoRecibos((Date) prm_valor);
						}
					}
					
					if(myEdificio.getEdf_importeRecargoSegundoVencimiento().compareTo(BigDecimal.ZERO) > 0 && prm_campo.equals("fechaSegundoVtoMasivo")){
						calMin.setTime(myEdificio.getEdf_fechaPrimerVencimientoRecibos());
						calMin.add(Calendar.DATE, 1);
						Calendar calNew = Calendar.getInstance();
						calNew.setTime(myEdificio.getEdf_fechaSegundoVencimientoRecibos());
						if (calNew.before(calMin)) {
							listAcumulaErrores.add(prm_campo + "~Edificio " + myEdificio.getEdf_codigo() + " la fecha de 2° vto. debe ser mayor al 1° vto ");
						}else{
							myEdificio.setEdf_fechaSegundoVencimientoRecibos((Date) prm_valor);
						}
					}
					
				}else{
					listAcumulaErrores.add(prm_campo + "~El edificio " + myEdificio.getEdf_codigo() + " no se pudo actualizar ya que estaba siendo modificado por otro usuario." );
				}
			}
		
		}else{
			listAcumulaErrores.add(prm_campo + "~No es un número.");
		}
		
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
