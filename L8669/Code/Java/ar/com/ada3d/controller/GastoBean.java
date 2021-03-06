package ar.com.ada3d.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import javax.faces.model.SelectItem;
import org.openntf.domino.Document;
import org.openntf.domino.Session;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewEntryCollection;
import org.openntf.jsonbeanx.J2BConverter;
import org.openntf.jsonbeanx.J2BException;

import lotus.domino.Database;
import lotus.domino.NotesException;
import ar.com.ada3d.connect.QueryAS400;
import ar.com.ada3d.model.Edificio;
import ar.com.ada3d.model.Gasto;
import ar.com.ada3d.model.GastoDesdoblado;
import ar.com.ada3d.model.GastoDesdobladoCuota;
import ar.com.ada3d.model.GastoOpciones;
import ar.com.ada3d.model.Porcentual;
import ar.com.ada3d.model.Prorrateo;
import ar.com.ada3d.model.TextoPregrabado;
import ar.com.ada3d.utilidades.DocLock;
import ar.com.ada3d.utilidades.DocUsr;
import ar.com.ada3d.utilidades.JSFUtil;

public class GastoBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<Gasto> listaGastos;
	private List<Gasto> listaGastosLiquidacionSiguiente;
	private List<Gasto> listaGastosLiquidacionesFuturas;
	private List<Gasto> listaGastosLiquidacionesPasadas;
	private List<TextoPregrabado> listaTextosPregrabados;
	private LinkedHashMap<String, String> agrupamientosGastosMap;
	private LinkedHashMap<String, String> agrupamientosNotasMap;
	
	
	
	@SuppressWarnings("unused")
	private BigDecimal importeTotalListaGastos;
	private Gasto gasto;
	
	public GastoBean() {
		// Empty constructor
	}
	
	
	//***** INI BOTONES ****
	
	/**Cuando presiona btnNewGasto en grabar y nuevo
	 * Creo un objeto vacio
	 */
	public void createNewGasto() {
		setGasto(new Gasto());
		Edificio prm_edificio = (Edificio) JSFUtil.resolveVariable("edfObj");
		this.gasto.setListaProrrateos(cargaProrrateo("", prm_edificio));
		this.gasto.setGastoRepetitivo("0");
	}
	
	
	/**Cuando presiona btnNewGastoDesdoblado
	 * Creo un objeto vacio
	 */
	public void createNewDesdoblado() {
		setGasto(new Gasto());
		this.gasto.setDesdoblado(new GastoDesdoblado());
		Edificio myEdificio = (Edificio) JSFUtil.resolveVariable("edfObj");
		this.gasto.getDesdoblado().setPrimerMes(ar.com.ada3d.utilidades.Conversores.DateToString(myEdificio.getEdf_fechaProximaLiquidacion(), "MMyyyy"));
		this.gasto.getDesdoblado().setNumeroCuotaInicial(1);
		this.gasto.getDesdoblado().setCantidadCuotas(6);
		this.gasto.getDesdoblado().setFrecuencia(1);
		
	}
	
	
	/**Cuando presiona btnNewNota en grabar y nuevo
	 * Creo un objeto vacio
	 */
	public void createNewNota() {
		setGasto(new Gasto());
	}
	
	
	/**
	 * Funcion en el boton editar
	 * Actualizo el gasto y lockeo
	 * @usedIn: frmGasto
	 */
	public void editFormulario(Edificio prm_edificio){
		//TODO: Actualizar el gasto al presionar el btnEdit
		Session session = JSFUtil.getSession();
		DocLock lock = (DocLock) JSFUtil.resolveVariable("DocLock");
		/*
		if (lock.isLocked("edf_" + prm_edificio.getEdf_codigo())){
			if (!lock.getLock("edf_" + prm_edificio.getEdf_codigo()).equals(session.getEffectiveUserName()))
				return;
		}*/
		if (lock.isLocked("gts_" + this.gasto.getIdGasto())){
			if (!lock.getLock("gts_" + this.gasto.getIdGasto()).equals(session.getEffectiveUserName()))
				return;
		}
		//lock.addLock("edf_" + prm_edificio.getEdf_codigo(), session.getEffectiveUserName());
		lock.addLock("gts_" + this.gasto.getIdGasto(), session.getEffectiveUserName());
		//log de las actividades en la session
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		docUsuario.setUltimaActividad(lock.setLog("Ha editado el gasto " + this.gasto.getTextoDetalleFactura().get(0) + "... del edificio: " + prm_edificio.getEdf_codigo()));
		prm_edificio.setEdf_lockedBy(session.getEffectiveUserName());
		this.gasto.setLockedBy(session.getEffectiveUserName());
		this.gasto.setIsReadMode(false);
		
		//Detalle factura para edicion
		Vector<String> tempTextoFactura = new Vector<String>();		
		List<String> detalleCompleto = this.gasto.getTextoDetalleFactura();
		StringBuilder sb = new StringBuilder();
		for (String s : detalleCompleto){
		    sb.append(s);
		    sb.append("\n");
		}
		sb.delete(0, this.gasto.getFilaColumnaTextoDetalleFactura());
		tempTextoFactura.add(sb.toString());
		this.gasto.setTextoDetalleFactura(tempTextoFactura);
	}
	
	
	/**
	 * Desde la vista de gastos elimino
	 * @usedIn: viewGasto
	 * @param prm_gasto: el gasto que voy a eliminar
	 */
	public void deleteGasto(Gasto prm_gasto){
		//TODO: Validar que no est� tomado y que se pueda eliminar
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("NCTROL", prm_gasto.getIdGasto());
		docDummy.appendItemValue("ESTADO", "B");
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		query.updateAS("gastosCambiarEstado", docDummy);
		
	}
	

	/**
	 * Chequea los datos del gasto antes de guardarlos
	 * @usedIn: Boton save 
	 * @param prm_edificio: del edificio necesito la configuracion del gasto 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 */
	public ArrayList<String> strValidacionGasto(Edificio prm_edificio){
		//TODO: que vamos a validar de la factura, si es nueva es otra validaci�n?
		ArrayList<String> listAcumulaErrores = new ArrayList<String>();
		List<String> acumulaDetalle = new ArrayList<String>();
		//Textos puedo tener hasta 99 lineas de 72 caracteres; las lineas de mas de 72 caracteres las divido en un nuevo array (acumulaDetalle)
		acumulaDetalle = getPreviewDetalleGastos(this.gasto, new ArrayList<String>(Arrays.asList(prm_edificio.getEdf_ConfigOrdenDetalleGasto().split(""))));
		if(acumulaDetalle.size() > 99){
			listAcumulaErrores.add("btnSave~El detalle de la factura es demasiado largo excede las 99 lineas y no puede ser grabado.");
		}
		//Validacion de importes que carguen al menos uno
		boolean importeCero = true;
		for (Prorrateo myProrrateo : this.gasto.getListaProrrateos()){
			if(myProrrateo.getPrt_importe().compareTo(BigDecimal.ZERO) != 0){
				importeCero = false;
			}
		}
		if(importeCero)
			listAcumulaErrores.add("prt_importe~Debe cargar al menos un importe para el gasto.");
		
		if(this.gasto.getSucursalFactura().length() > 4)
			listAcumulaErrores.add("numeroFactura~El punto de venta de la factura debe ser de 4 digitos");
		
		if(this.gasto.getNumeroFactura().length() > 8)
			listAcumulaErrores.add("numeroFactura~El n�mero de la factura debe ser de 8 digitos");
		
		if(this.gasto.getAgrupamiento().equals("--")){//El combo agrupamiento tiene un valor 'Seleccione' por defecto
			listAcumulaErrores.add("djComboAgrupamiento~Por favor seleccione un agrupamiento.");
		}
		
		//Datos de proveedor
		if(this.gasto.getFechaFactura() != null && this.gasto.getCuitProveedor().equals("0"))
			listAcumulaErrores.add("djComboMyProveedores~Si ingresa la fecha de factura debe seleccionar un proveedor.");
		
		if(!this.gasto.getSucursalFactura().equals("")){
			if(!this.gasto.getSucursalFactura().equals("0000") && this.gasto.getCuitProveedor().equals("0"))
				listAcumulaErrores.add("djComboMyProveedores~Si ingresa el punto de venta de la factura debe seleccionar un proveedor.");
		}

		if(!this.gasto.getNumeroFactura().equals("")){
			if(!this.gasto.getNumeroFactura().equals("00000000") && this.gasto.getCuitProveedor().equals("0"))
				listAcumulaErrores.add("djComboMyProveedores~Si ingresa el n�mero de la factura debe seleccionar un proveedor.");
		}

		if(this.gasto.getGastoRepetitivo().equals("3")){
			if (this.gasto.getGastoRepetitivoTexto() == null){
				listAcumulaErrores.add("divGastoRepetitivoTexto~Al seleccionar repetir en cuotas debe ingresar el numero de cuota actual (ej.: 01 de 03).");
			}else{
				 if(this.gasto.getGastoRepetitivoTexto().trim().equals("")){
					 listAcumulaErrores.add("divGastoRepetitivoTexto~Al seleccionar repetir en cuotas debe ingresar el numero de cuota actual (ej.: 01 de 03).");
				 }else if(this.gasto.getGastoRepetitivoTexto().length() != 8)
					 listAcumulaErrores.add("divGastoRepetitivoTexto~Al seleccionar repetir en cuotas debe ingresar el numero de cuota actual (ej.: 01 de 03).");
			}
		}
		return listAcumulaErrores;
	}
	

	
	
	
	/**Cuando presiona btnSave Gasto actualizo el AS400
	 * Esto ya tiene que venir validado
	 * @usedIn: Boton save 
	 * @param prm_edificio: del edificio necesito la configuracion del gasto 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla

	 */
	public ArrayList<String> saveGasto(Edificio prm_edificio) {
		boolean printConsole = false;
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
		boolean isNew = false;
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Form", "docDummy");
		Session session = JSFUtil.getSession();
		session.getCurrentDatabase().getAgent("a.ObtCorr").runWithDocumentContext(docDummy);

		if(printConsole) System.out.println("FPR_1");
		if(this.gasto.getIdGasto() == null){//Es un gasto nuevo
			this.gasto.setIdGasto(ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), "yyMMddHHmm" + docDummy.getItemValueString("nroSecuencial")));
			this.gasto.setCodigoEdificio(prm_edificio.getEdf_codigo());
			//TODO: que mensaje grabamos en origen de los datos cuando es nuevo?
			this.gasto.setOrigenDatos("QUE PONEMOS?");
			isNew = true;
		}
		if(printConsole) System.out.println("FPR_2");
		docDummy.appendItemValue("NCTROL", this.gasto.getIdGasto());
		docDummy.appendItemValue("EDIF", this.gasto.getCodigoEdificio());
		docDummy.appendItemValue("FECLIQ", this.gasto.getFechaLiquidacion());
		docDummy.appendItemValue("CUITPR", this.gasto.getCuitProveedor());
		if(printConsole) System.out.println("FPR_3");
		docDummy.appendItemValue("NFACT", (this.gasto.getSucursalFactura().equals("") ? "0000" : this.gasto.getSucursalFactura()) + "-" + (this.gasto.getNumeroFactura().equals("") ? "00000000" : this.gasto.getNumeroFactura()));
		if(this.gasto.getFechaFactura()== null){
			docDummy.appendItemValue("FFACT", "0");
		}else{
			docDummy.appendItemValue("FFACT", ar.com.ada3d.utilidades.Conversores.DateToString(this.gasto.getFechaFactura(), "ddMMyy"));
		}
		if(printConsole) System.out.println("FPR_4");
		docDummy.appendItemValue("AGRUP", this.gasto.getAgrupamiento());
		docDummy.appendItemValue("SPECIAL", this.gasto.getCodigoEspecial());
		docDummy.appendItemValue("TIPGTS", this.gasto.getTipoGasto());
		//Importes del prorrateo
		docDummy.appendItemValue("IMPOR1", "0");
		docDummy.appendItemValue("IMPOR2", "0");
		docDummy.appendItemValue("IMPOR3", "0");
		docDummy.appendItemValue("IMPOR4", "0");
		for (Prorrateo myProrrateo : this.gasto.getListaProrrateos()){
			docDummy.replaceItemValue("IMPOR" + myProrrateo.getPrt_posicion(), ar.com.ada3d.utilidades.Conversores.bigDecimalToAS400(myProrrateo.getPrt_importe(), 2));
		}
		
		//Detalle del gasto
		List<String> acumulaDetalle = new ArrayList<String>();
		acumulaDetalle = getPreviewDetalleGastos(this.gasto, new ArrayList<String>(Arrays.asList(prm_edificio.getEdf_ConfigOrdenDetalleGasto().split(""))));
		docDummy.appendItemValue("FILCOL", this.gasto.getFilaColumnaTextoDetalleFactura().toString());
		
		docDummy.appendItemValue("ACUMULADETALLE", acumulaDetalle);
		docDummy.appendItemValue("TRENGL", acumulaDetalle.size()); //Total de renglones
		docDummy.appendItemValue("ORIGEN", this.gasto.getOrigenDatos());
		docDummy.appendItemValue("CMPREP", this.gasto.getGastoRepetitivo());
		docDummy.appendItemValue("TXTREP", this.gasto.getGastoRepetitivo().equals("0") || this.gasto.getGastoRepetitivo().equals("1") || this.gasto.getGastoRepetitivoTexto() == null ? "" : this.gasto.getGastoRepetitivoTexto());
		if(printConsole) System.out.println("FPR_5");
		
		// *** AS400 ***
		
		QueryAS400 query = new QueryAS400();
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
		
		if(isNew){ //es un nuevo gasto
			if(printConsole) System.out.println("FPR_6");
			if(prm_edificio.getEdf_ConfigTipoNumeracion().equals("1")){//Numeracion automatica
				if(printConsole) System.out.println("FPR_7");
				//Actualizo el numero de comprobante si corresponde
				session.getCurrentDatabase().getAgent("a.saveNewFrmGastosNumAutSimple").runWithDocumentContext(docDummy);
				if(docDummy.getItemValueString("COMPRO") != null){
					if(docDummy.getItemValueString("COMPRO").equals("ERROR")){
						listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
						System.out.println("ERROR: " + errCode + " METH:saveNewGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo obtener el n�mero correlativo para el gasto.");
						return listAcumulaErroresAS400;
					}
				}else{
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveNewGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo obtener el COMPROBANTE para el gasto.");
					return listAcumulaErroresAS400;
				}
			}else if(prm_edificio.getEdf_ConfigTipoNumeracion().equals("2")){//Numeracion automatica por rubros
				docDummy.appendItemValue("COMPRO", "0");
			}else{//Numeracion Manual o sin numeraci�n
				if(printConsole) System.out.println("FPR_8");
				docDummy.appendItemValue("COMPRO", "0");
			}
			
			if (!query.updateBatchGastos("gastosInsertBatchGTS01", docDummy, acumulaDetalle, true)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo insertar en la tabla PH_GTS01.");
			}
			
		}else{  // es un gasto existente
			/******************DETALLE DE FACTURA *************************************
			 * Cantidad de lineas que tengo ahora: acumulaDetalle.size()
			 * Cantidad de lineas que tenia al ingresar: this.gasto.getCantidadRenglones()
			 * Si la cantidad de lineas es = actualizo los datos en batch, sino elimino
			 * todo y vuelvo a insertar  
			*/
			if(printConsole) System.out.println("FPR_9");
			docDummy.appendItemValue("COMPRO", this.gasto.getNumeroComprobante().toString());
			if (acumulaDetalle.size() == this.gasto.getCantidadRenglones()){
				//Misma cantidad de lineas, solo actualizo 
				if (!query.updateBatchGastos("gastosUpdateBatchGTS01", docDummy, acumulaDetalle, true)) {
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo actualizar la tabla PH_GTS01.");
				}
						
			}else{	
				//La cantidad de lineas cambio elimino lineas y vuelvo a generar
				if(query.updateAS("gastosDelete", docDummy)){
					if (!query.updateBatchGastos("gastosInsertBatchGTS01", docDummy, acumulaDetalle, true)) {
						listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
						System.out.println("ERROR: " + errCode + " METH:saveNewGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo insertar en la tabla PH_GTS01.");
					}
				}else{
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveNewGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo eliminar en la tabla PH_GTS01.");
				}
			}
		}
		
		
		if(printConsole) System.out.println("FPR_10");
		//Actualizo los campos de logueo si no hubo errores
		if (listAcumulaErroresAS400.isEmpty()){
			Calendar ahora = Calendar.getInstance();
			if(isNew){ //es un nuevo gasto
				docDummy.appendItemValue("FECHAC", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAC", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "HHmm"));
				docDummy.appendItemValue("FECHAM", "0");
				docDummy.appendItemValue("HORAM", "0");
				docDummy.appendItemValue("USERC", docUsuario.getUserSec());
				docDummy.appendItemValue("USERM", "0");
			}else{
				docDummy.appendItemValue("FECHAC", ar.com.ada3d.utilidades.Conversores.DateToString(this.gasto.getFechaCreacion(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAC", ar.com.ada3d.utilidades.Conversores.DateToString(this.gasto.getFechaCreacion(), "HHmm"));
				docDummy.appendItemValue("USERC", this.gasto.getUsuarioCreacion());
				docDummy.appendItemValue("FECHAM", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAM", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "HHmm"));
				docDummy.appendItemValue("USERM", docUsuario.getUserSec());
			}
			
			if(!query.updateAS("gastosUpdateLog", docDummy)){
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo loguear en la tabla PH_GTS01.");
			}
		}
		
		
		DocLock lock = (DocLock) JSFUtil.resolveVariable("DocLock");
		if (listAcumulaErroresAS400.isEmpty()){
			//lock.removeLock("edf_" + prm_edificio.getEdf_codigo());
			if(!isNew)
				lock.removeLock("gts_" + this.gasto.getIdGasto());
			//TODO: Que mensaje ponemos al salvar el gasto ok?
			docUsuario.setUltimaActividad(lock.setLog("Ha guardado los cambios del la factura ???? " ));			
		}else{
			docUsuario.setUltimaActividad(lock.setLog("No se han guardado los cambios (ERROR: " + errCode + ")"));
		}
		
		return listAcumulaErroresAS400;
		
	}
	
	
	/**
	 * Chequea los datos de notas antes de guardarlas
	 * @usedIn: Boton save 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla
	 */
	public ArrayList<String> strValidacionNota(){
		ArrayList<String> listAcumulaErrores = new ArrayList<String>();
		List<String> acumulaDetalle = new ArrayList<String>();
		//Textos puedo tener hasta 99 lineas de 72 caracteres
		//Las lineas de mas de 72 caracteres las divido en un nuevo array (acumulaDetalle)
		for (String detalle : this.gasto.getTextoDetalleFactura()){
			acumulaDetalle.addAll(ar.com.ada3d.utilidades.Conversores.splitString(detalle, 72));
		}
		if(acumulaDetalle.size() > 99){
			listAcumulaErrores.add("btnSave~El detalle de la nota es demasiado largo excede las 99 lineas y no puede ser grabado.");
		}
		
		if(this.gasto.getAgrupamiento().equals("--")){//El combo agrupamiento tiene un valor 'Seleccione' por defecto
			listAcumulaErrores.add("djComboAgrupamiento~Por favor seleccione un agrupamiento.");
		}
		return listAcumulaErrores;
	}

	
	/**Cuando presiona btnSave en Nota actualizo el AS400
	 * Esto ya tiene que venir validado
	 * @usedIn: Boton save 
	 * @param prm_edificio: del edificio necesito la configuracion del gasto 
	 * @return: un texto con: idComponente con error ~ Mensaje a Mostrar en pantalla

	 */
	public ArrayList<String> saveNota(Edificio prm_edificio) {
		
		ArrayList<String> listAcumulaErroresAS400 = new ArrayList<String>();
		
		boolean isNew = false;
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Form", "docDummy");
		Session session = JSFUtil.getSession();
		session.getCurrentDatabase().getAgent("a.ObtCorr").runWithDocumentContext(docDummy);

		if(this.gasto.getIdGasto() == null){//Es un gasto nuevo
			this.gasto.setIdGasto(ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), "yyMMddHHmm" + docDummy.getItemValueString("nroSecuencial")));
			this.gasto.setCodigoEdificio(prm_edificio.getEdf_codigo());
			//TODO: que mensaje grabamos en origen de los datos cuando es nuevo?
			this.gasto.setOrigenDatos("QUE PONEMOS?");
			isNew = true;
		}
		
		docDummy.appendItemValue("NCTROL", this.gasto.getIdGasto());
		docDummy.appendItemValue("EDIF", this.gasto.getCodigoEdificio());
		docDummy.appendItemValue("FECLIQ", this.gasto.getFechaLiquidacion());
		docDummy.appendItemValue("AGRUP", this.gasto.getAgrupamiento());
				
		List<String> acumulaDetalle = new ArrayList<String>();
		for (String detalle : this.gasto.getTextoDetalleFactura()){
			acumulaDetalle.addAll(ar.com.ada3d.utilidades.Conversores.splitString(detalle, 72));
		}

		docDummy.appendItemValue("ACUMULADETALLE", acumulaDetalle);
		docDummy.appendItemValue("TRENGL", acumulaDetalle.size()); //Total de renglones
		docDummy.appendItemValue("ORIGEN", this.gasto.getOrigenDatos());
		
		
		// *** AS400 ***
		
		QueryAS400 query = new QueryAS400();
		DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
		String errCode = ar.com.ada3d.utilidades.Conversores.DateToString(Calendar.getInstance().getTime(), docUsuario.getUserSec() + "ddMMyyHHmmss" );
		
		if(isNew){ //es una nota nueva
			if (!query.updateBatchGastos("notasInsertBatchGTS01", docDummy, acumulaDetalle, true)) {
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveNewNota" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo insertar en la tabla PH_GTS01.");
			}
		}else{  // es una nota existente
			/******************DETALLE DE FACTURA *************************************
			 * Cantidad de lineas que tengo ahora: acumulaDetalle.size()
			 * Cantidad de lineas que tenia al ingresar: this.gasto.getCantidadRenglones()
			 * Si la cantidad de lineas es = actualizo los datos en batch, sino elimino
			 * todo y vuelvo a insertar  
			*/
			
			
			if (acumulaDetalle.size() == this.gasto.getCantidadRenglones()){
				//Misma cantidad de lineas, solo actualizo 
				if (!query.updateBatchGastos("notasUpdateBatchGTS01", docDummy, acumulaDetalle, true)) {
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveNota" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo actualizar la tabla PH_GTS01.");
				}
						
			}else{	
				//La cantidad de lineas cambio elimino lineas y vuelvo a generar
				if(query.updateAS("gastosDelete", docDummy)){
					if (!query.updateBatchGastos("notasInsertBatchGTS01", docDummy, acumulaDetalle, true)) {
						listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
						System.out.println("ERROR: " + errCode + " METH:saveNewNota" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo insertar en la tabla PH_GTS01.");
					}
				}else{
					listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
					System.out.println("ERROR: " + errCode + " METH:saveNewNota" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo eliminar en la tabla PH_GTS01.");
				}
			}
		}
		
		
		
		//Actualizo los campos de logueo si no hubo errores
		if (listAcumulaErroresAS400.isEmpty()){
			Calendar ahora = Calendar.getInstance();
			if(isNew){ //es un nuevo gasto
				docDummy.appendItemValue("FECHAC", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAC", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "HHmm"));
				docDummy.appendItemValue("FECHAM", "0");
				docDummy.appendItemValue("HORAM", "0");
				docDummy.appendItemValue("USERC", docUsuario.getUserSec());
				docDummy.appendItemValue("USERM", "0");
			}else{
				docDummy.appendItemValue("FECHAC", ar.com.ada3d.utilidades.Conversores.DateToString(this.gasto.getFechaCreacion(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAC", ar.com.ada3d.utilidades.Conversores.DateToString(this.gasto.getFechaCreacion(), "HHmm"));
				docDummy.appendItemValue("USERC", this.gasto.getUsuarioCreacion());
				docDummy.appendItemValue("FECHAM", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "ddMMyyyy"));
				docDummy.appendItemValue("HORAM", ar.com.ada3d.utilidades.Conversores.DateToString(ahora.getTime(), "HHmm"));
				docDummy.appendItemValue("USERM", docUsuario.getUserSec());
			}
			
			if(!query.updateAS("gastosUpdateLog", docDummy)){
				listAcumulaErroresAS400.add("btnSave~Por favor comuniquese con Sistemas Administrativos e informe el c�digo de error: " + errCode);
				System.out.println("ERROR: " + errCode + " METH:saveGasto" + "_ID:" + this.gasto.getIdGasto() + "_DESC: No se pudo loguear en la tabla PH_GTS01.");
			}
		}
		
		
		DocLock lock = (DocLock) JSFUtil.resolveVariable("DocLock");
		if (listAcumulaErroresAS400.isEmpty()){
			//lock.removeLock("edf_" + prm_edificio.getEdf_codigo());
			if(!isNew)
				lock.removeLock("gts_" + this.gasto.getIdGasto());
			//TODO: Que mensaje ponemos al salvar el gasto ok?
			docUsuario.setUltimaActividad(lock.setLog("Ha guardado los cambios del la nota ???? " ));			
		}else{
			docUsuario.setUltimaActividad(lock.setLog("No se han guardado los cambios (ERROR: " + errCode + ")"));
		}
		
		return listAcumulaErroresAS400;
		
	}
	
	//***** FIN BOTONES ****
	
	//***** INI VISTAS ****
	
	/**Cuando ingresa a la vista de Gastos
	 * @param prm_edificio: necesito los porcentuales del edificio 
	 */
	public void viewGastos(Edificio prm_edificio){
		fillListaGastos(prm_edificio, "G");
	}
	
	
	/**Cuando ingresa a la vista de Notas
	 * @param prm_edificio: necesito los porcentuales del edificio 
	 */
	public void viewNotas(Edificio prm_edificio){
		fillListaGastos(prm_edificio, "N");
	}
	
	
	
	/**
	 * Completo la variable listaGastos consultando As400, cada linea separa el dato por un pipe
	 * Cada gasto puede tener mas de una linea, pero existe un unico importe por factura.
	 * @param prm_edificio: necesito los porcentuales del edificio
	 * @param prm_tipoFormulario: N o G. Nota o gasto.
	 * @return la lista de facturas o notas de un edificio
	 */
	private void fillListaGastos(Edificio prm_edificio, String prm_tipoFormulario){
		if((prm_tipoFormulario.equals("N") && agrupamientosNotasMap == null) || (prm_tipoFormulario.equals("G") && agrupamientosGastosMap == null)){
			System.out.println("** ERROR - agrupamientosNotasMap o agrupamientosGastosMap son nulos **");
			return;
		}
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("edf_codigo", prm_edificio.getEdf_codigo());
			nl = query.getSelectAS("gasto_CONTROLLER", docDummy);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		listaGastos = new ArrayList<Gasto>();
		listaGastosLiquidacionSiguiente = new ArrayList<Gasto>();
		listaGastosLiquidacionesFuturas = new ArrayList<Gasto>();
		listaGastosLiquidacionesPasadas = new ArrayList<Gasto>();
		String idGasto = "";
		Vector<String> tempTextoFactura = null;
		Gasto myGasto = null;
		Integer count = 0;
		Calendar calGastoProxLiq = Calendar.getInstance();
		Calendar calEdificioProxLiq = Calendar.getInstance();
		for (String strLinea : nl) {
			//Estoy llenando notas solo si el codigo de agrupamiento es de notas o estoy llenando gastos solo si el codigo de agrupamiento es de gastos
			if((prm_tipoFormulario.equals("N") && agrupamientosNotasMap.containsKey(strLinea.split("\\|")[10].trim())) || (prm_tipoFormulario.equals("G") && agrupamientosGastosMap.containsKey(strLinea.split("\\|")[10].trim()))){
			
				if(!strLinea.split("\\|")[13].equals("B")){ //Si no es una baja
					//Voy a generar una linea por comprobante pero recordar que una factura puede tener muchas lineas de texto
					Gasto miGasto = null;
					if(!idGasto.equals(strLinea.split("\\|")[1].trim())){
						idGasto = strLinea.split("\\|")[1].trim();
						tempTextoFactura = new Vector<String>();
						myGasto = new Gasto();
						myGasto.setIdGasto(idGasto);
						myGasto.setCodigoEdificio(strLinea.split("\\|")[0].trim());
						
						if(prm_tipoFormulario.equals("G")){//Separacion notas de gastos
							myGasto.setNumeroComprobante(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[9].trim(), Locale.UK, 0)));
							myGasto.setCodigoEspecial(strLinea.split("\\|")[11].trim());
							myGasto.setCuitProveedor(strLinea.split("\\|")[14].trim());
							myGasto.setSucursalFactura(strLinea.split("\\|")[15].trim().split("-")[0]);
							myGasto.setNumeroFactura(strLinea.split("\\|")[15].trim().split("-")[1]);
							if(!strLinea.split("\\|")[16].trim().equals("0")) //la fecha si es nula viene un cero
								myGasto.setFechaFactura(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[16].trim()));
							
							//Si es la linea de importes (NRENGL = TRENGL)
							if(strLinea.split("\\|")[7].trim().equals(strLinea.split("\\|")[8].trim())){
								//Prorrateo para gastos
								myGasto.setListaProrrateos(cargaProrrateo(strLinea, prm_edificio));					
							}
						}
						myGasto.setNumeroRenglon(Integer.parseInt(strLinea.split("\\|")[7].trim()));
						myGasto.setCantidadRenglones(Integer.parseInt(strLinea.split("\\|")[8].trim()));
						myGasto.setAgrupamiento(strLinea.split("\\|")[10].trim());
						myGasto.setFechaLiquidacion(ar.com.ada3d.utilidades.Conversores.DateToString(ar.com.ada3d.utilidades.Conversores.StringToDate("Myyyy", strLinea.split("\\|")[12].trim()), "MMyyyy"));
						
						//Detalle factura
						tempTextoFactura.add(strLinea.split("\\|")[2].trim());
						myGasto.setTextoDetalleFactura(tempTextoFactura);
						
						
						myGasto.setIsReadMode(true);
						listaGastos.add(myGasto);
						
						//Defino calendar para comparar las fechas de liquidacion
						calGastoProxLiq.setTime(ar.com.ada3d.utilidades.Conversores.StringToDate("Myyyy", strLinea.split("\\|")[12].trim()));
						calGastoProxLiq.set(Calendar.DATE, calGastoProxLiq.getActualMaximum(Calendar.DAY_OF_MONTH));
						calEdificioProxLiq.setTime(prm_edificio.getEdf_fechaProximaLiquidacion());
						
						if(calEdificioProxLiq.before(calGastoProxLiq)){
							listaGastosLiquidacionesFuturas.add(myGasto);
						}else if(calEdificioProxLiq.after(calGastoProxLiq)){
							listaGastosLiquidacionesPasadas.add(myGasto);
						}else{
							listaGastosLiquidacionSiguiente.add(myGasto);
						}
						
						
						count = count + 1;
					}else{//Es igual al anterior solo agregar el texto
						tempTextoFactura.add(strLinea.split("\\|")[2].trim());
						if(miGasto == null){
							miGasto = listaGastos.get(count - 1);
						}
						//Si es la linea de importes (NRENGL = TRENGL)
						if(strLinea.split("\\|")[7].trim().equals(strLinea.split("\\|")[8].trim()) && prm_tipoFormulario.equals("G")){
							//Prorrateo para gastos
							miGasto.setListaProrrateos(cargaProrrateo(strLinea, prm_edificio));					
						}
		
						miGasto.setTextoDetalleFactura(tempTextoFactura);					
					}
				}//Fin if es una baja
			}//Fin si es nota y coincide codigo de agrupamiento (o gasto) 
		}
		
	}
	
	//***** FIN VISTAS ****

	/** UPDATE DEL GASTO
	 * Al ingresar en un registro de la lista de gastos hago un nuevo update del gasto con los datos que faltan
	 * Recordar que cada gasto puede tener mas de una linea, pero existe un unico importe por factura
	 * @param prm_Gasto: el gasto por actualizar
	 * @param prm_tipoFormulario: N o G. Nota o gasto.
	 */
	private void updateGasto(Gasto prm_Gasto, String prm_tipoFormulario){
		ArrayList<String> nl = null;
		Document docDummy = JSFUtil.getDocDummy();
		docDummy.appendItemValue("Codigo", prm_Gasto.getCodigoEdificio());
		docDummy.appendItemValue("Idgasto", prm_Gasto.getIdGasto());
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("gasto_Individual_CONTROLLER", docDummy);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		Date temp;
		List<String> detalleFactura = new ArrayList<String>();
		for (String strLinea : nl) {
			if(strLinea.split("\\|")[3].trim().equals("1")){ 
				// Solo leo el primer renglon
				temp = ar.com.ada3d.utilidades.Conversores.StringToDate("Myyyy", strLinea.split("\\|")[2].trim());
				prm_Gasto.setFechaLiquidacion(ar.com.ada3d.utilidades.Conversores.DateToString(temp, "MMyyyy"));
				prm_Gasto.setAgrupamiento(strLinea.split("\\|")[9].trim());
				prm_Gasto.setNumeroComprobante(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(strLinea.split("\\|")[5].trim(), Locale.UK, 0)));
				
				//Fecha de creacion del registro
				Calendar tempDate = ar.com.ada3d.utilidades.Conversores.dateToCalendar(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyyyy", strLinea.split("\\|")[11].trim()));
				tempDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strLinea.split("\\|")[12].trim().substring(0, 2)));
				tempDate.set(Calendar.MINUTE, Integer.parseInt(strLinea.split("\\|")[12].trim().substring(2)));
				prm_Gasto.setFechaCreacion(tempDate.getTime());
				
				prm_Gasto.setUsuarioCreacion(strLinea.split("\\|")[13].trim());//Usuario creacion	
				prm_Gasto.setOrigenDatos(strLinea.split("\\|")[14].trim());//Origen de los datos	
				prm_Gasto.setFilaColumnaTextoDetalleFactura(Integer.parseInt(strLinea.split("\\|")[16].trim()));//FilaColumna texto variable	
				
				if (prm_tipoFormulario.equals("G")){
					prm_Gasto.setCuitProveedor(strLinea.split("\\|")[6].trim());
					if(!strLinea.split("\\|")[8].trim().equals("0")) //la fecha si es nula viene un cero
						prm_Gasto.setFechaFactura(ar.com.ada3d.utilidades.Conversores.StringToDate("ddMMyy", strLinea.split("\\|")[8].trim()));
					prm_Gasto.setSucursalFactura(strLinea.split("\\|")[7].trim().split("-")[0]);
					prm_Gasto.setNumeroFactura(strLinea.split("\\|")[7].trim().split("-")[1]);
					prm_Gasto.setCodigoEspecial(strLinea.split("\\|")[10].trim());
					prm_Gasto.setTipoGasto(strLinea.split("\\|")[15].trim());
					prm_Gasto.setGastoRepetitivo(strLinea.split("\\|")[17].trim());
					prm_Gasto.setGastoRepetitivoTexto(strLinea.split("\\|")[18].trim() == "" ? null : strLinea.split("\\|")[18].trim());
					
				}

			}
			detalleFactura.add(strLinea.split("\\|")[1].trim());
		}
		prm_Gasto.setTextoDetalleFactura(detalleFactura);
		
	}

	
	
	
	/**
	 * Al cargar una factura en la misma consulta al AS400 tambien cargo los importes de prorrateo
	 * @param prm_strLinea de AS400 que estoy procesando, ��debe ser la linea que contiene los importes!!
	 * @param prm_edificio: necesito los porcentuales del edificio
	 * @return la lista de prorrateos
	 */
	private List<Prorrateo> cargaProrrateo(String prm_strLinea, Edificio prm_edificio){
		
		// ***** Esta funcion solo debe recibir la linea de importes !!! ***
		List<Prorrateo> listaProrrateos = new ArrayList<Prorrateo>();
		//Recorro el prorrateo de edificios y lo igualo al prorrateo de gastos
		for(Prorrateo edificioProrrateo : prm_edificio.getListaProrrateos()){
			Prorrateo gastoProrrateo = new Prorrateo();
			gastoProrrateo.setPrt_posicion(edificioProrrateo.getPrt_posicion());
			gastoProrrateo.setPrt_posicionEnGrilla(edificioProrrateo.getPrt_posicionEnGrilla());
			gastoProrrateo.setPrt_titulo(edificioProrrateo.getPrt_titulo());
			gastoProrrateo.setPrt_porcentaje(edificioProrrateo.getPrt_porcentaje());
			if(prm_strLinea.equals("")){
				gastoProrrateo.setPrt_importe(new BigDecimal(0));
			}else{
				gastoProrrateo.setPrt_importe(new BigDecimal(ar.com.ada3d.utilidades.Conversores.stringToStringDecimal(prm_strLinea.split("\\|")[(edificioProrrateo.getPrt_posicion()+ 2)].trim(), Locale.UK, 2)));
			}
			listaProrrateos.add(gastoProrrateo);	
		}
		return listaProrrateos;
	}

	
	/**
	 * Al cambiar el combo de edificios en un gasto voy a blanquear los porcentuales y regenerarlos 
	 * agregando el total de importes en el primer porcentual disponible
	 * @param prm_gasto: el gasto que estoy editando
	 * @param prm_edificio: el nuevo edificio cambiado en el combo
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
	
	/**
	 * En viewGastos cargo en un mapa el Agrupamiento sale de PH_$T (tanto de gastos como de notas)
	 * Recordar que existen 3 tipos de agrupamientos:
	 * 	-Fijos: el usuario no puede cambiar su descripcion o c�digo (10, 20, 30 y 40)
	 * 	-Variables: el usuario no puede crear una descripcion o c�digo (que no sean los fijos)
	 * 	-Nota: no son gastos solo texto en detalle
	 */
	public void fillAgrupamientosGastosMap(){
		agrupamientosGastosMap = new LinkedHashMap<String, String>();
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("gastosCodigoAgrupamiento", null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		for (String strLinea : nl) {
			if(!strLinea.split("\\|")[1].trim().equals("")){
				agrupamientosGastosMap.put(strLinea.split("\\|")[0].trim(), strLinea.split("\\|")[1].trim());
			}
		}		
	}
	
	
	/**
	 * En viewNotas cargo en un mapa el Agrupamiento sale de lista de configuracion ya que en PH_$T no tengo la descripcion de las notas
	 * Recordar que existen 3 tipos de agrupamientos:
	 * 	-Fijos: el usuario no puede cambiar su descripcion o c�digo (10, 20, 30 y 40)
	 * 	-Variables: el usuario no puede crear una descripcion o c�digo (que no sean los fijos)
	 * 	-Nota: no son gastos solo texto en detalle
	 */
	public void fillAgrupamientosNotasMap(){
		agrupamientosNotasMap = new LinkedHashMap<String, String>();
		agrupamientosNotasMap = ar.com.ada3d.utilidades.JSFUtil.getOpcionesClaveMap("agrupamientoNotas");	
	}
	
	
	/**
	 * Carga en listaTextosPregrabados los textos Pregrabados del edificio recibido 
	 * @param prm_edificio objeto del edificio que estoy trabajando
	 */
	@SuppressWarnings("deprecation")
	public void fillTextosPregrabados(Edificio prm_edificio) {
		//TODO: el combo de edificio no funciona y tampoco si elijo otro edificio. Tambien aparece siempre el tooltip al confirmar 
		listaTextosPregrabados = null;
		StringBuilder conf_json = new StringBuilder();
		View currentDbPregrabadosView = ar.com.ada3d.utilidades.JSFUtil.getCurrentDatabase().getView("(Clave Json por edificio)");
		if (currentDbPregrabadosView == null) return;
		ViewEntryCollection viewEntryColl = currentDbPregrabadosView.getAllEntriesByKey(prm_edificio.getEdf_codigo(), true);
		if (viewEntryColl == null) return;
		if (viewEntryColl.getCount() == 0) return; //Si no encuentra textos sale
		if (viewEntryColl.getCount() > 0) {
			ViewEntry entryEdificio = viewEntryColl.getFirstEntry();
			conf_json.append("[");
			while (entryEdificio != null) {
				conf_json.append(entryEdificio.getDocument().getItemValueString("conf_json"));
				ViewEntry tempEntry = viewEntryColl.getNextEntry();
				if(tempEntry != null)
					conf_json.append(",");
				entryEdificio = tempEntry;
			}
			conf_json.append("]");
		}
		try {
			listaTextosPregrabados = J2BConverter.jsonToBean(TextoPregrabado.class, conf_json.toString()); //Funcion para crear un bean tomando un json
            //System.out.println("FPR:" + listaTextosPregrabados.get(0).getPorcentuales().get(1).getUnadjusted());
			
			/* *** NO BORRAR ***** IMPORTANTE *****
			 * Para debug, te muestra como debe quedar el JSON de Texto preseteado por si lo eliminas de Notes
			  
			TextoPregrabadoEdificio textoPregrabadoEdificio1 = new TextoPregrabadoEdificio();
			textoPregrabadoEdificio1.setEdif("VEC1");
			
			LinkedHashMap<String, TextoPregrabadoEdificio> mapaEdificios = new LinkedHashMap<String, TextoPregrabadoEdificio>();
			mapaEdificios.put("VEC1", textoPregrabadoEdificio1);
						
			TextoPregrabado textoPregrabado = new TextoPregrabado();
			textoPregrabado.setEdificios(mapaEdificios);
			textoPregrabado.setReferencia("Referencia");
			List myList = new ArrayList();
			myList.add(textoPregrabado);
			final String json = J2BConverter.beanToJson(myList);//Crea un json tomando un bean
			System.out.println(json);
			*/ 
		} catch (J2BException e) {
			e.printStackTrace();
		}
					
	}
	
	
	/**
	 * Al clickear en un preseteado lo recibe y transfiere los datos al gasto 
	 * @param prm_Preseteado objeto TextoPregrabado para aplicar al gasto
	 * @param prm_edf_codigo codigo de efificio seleccionado
	 */
	public void aplicarPreseteadoEnGasto(TextoPregrabado prm_Preseteado, String prm_edf_codigo) {
		this.gasto.setTextoDetalleFactura(prm_Preseteado.getTextoDetalle());
		this.gasto.setIdPregrabado(prm_Preseteado.getId());
		//De listaEdificios obtengo el edificio del preseteado 
		int posicionEdificio = prm_Preseteado.getListaEdificios().indexOf(prm_edf_codigo);
		if (posicionEdificio > -1){
			this.gasto.setPregrabado(prm_Preseteado.getEdificios().get(posicionEdificio)); //Seteo el pregrabado como propiedad
			this.gasto.setCodigoEspecial(prm_Preseteado.getEdificios().get(posicionEdificio).getCodigoEspecial());
			this.gasto.setAgrupamiento(prm_Preseteado.getEdificios().get(posicionEdificio).getAgrupamiento());
			this.gasto.setTipoGasto(prm_Preseteado.getEdificios().get(posicionEdificio).getTipoGasto());
			this.gasto.setAplicarMes(prm_Preseteado.getEdificios().get(posicionEdificio).getMes());
			this.gasto.setCuitProveedor(prm_Preseteado.getEdificios().get(posicionEdificio).getPrv_cuit());
			
			for (Iterator<Prorrateo> i = this.gasto.getListaProrrateos().iterator(); i.hasNext();) {
				Prorrateo item = i.next();
				item.setPrt_importe(new BigDecimal(0));
				item.setPrt_disabled(true);
			}
			
			for (int idxPorcentual = 0; idxPorcentual < prm_Preseteado.getEdificios().get(posicionEdificio).getPorcentuales().size(); idxPorcentual++) {
				Integer posi = prm_Preseteado.getEdificios().get(posicionEdificio).getPorcentuales().get(idxPorcentual).getPorc_posicion();
				double importe = prm_Preseteado.getEdificios().get(posicionEdificio).getPorcentuales().get(idxPorcentual).getPorc_importeJson();
				boolean esEditable = prm_Preseteado.getEdificios().get(posicionEdificio).getPorcentuales().get(idxPorcentual).getPorc_CheckBoxImporteJson();
				if(this.gasto.getUnProrrateo(posi) != null){
					this.gasto.getUnProrrateo(posi).setPrt_importe(new BigDecimal(importe));
					if(esEditable) this.gasto.getUnProrrateo(posi).setPrt_disabled(false);
				}
			}
		}
	}
	
	
	/** Chequea si hicieron cambios en el gasto que involucran el pregrabado 
	 * @param prm_gasto
	 * @return un arregle de textos con las modificaciones que se van a aplicar
	 */
	public ArrayList<String> modificacionesEnGastoAfectanPregrabado(Gasto prm_gasto){
		ArrayList<String> ret = new ArrayList<String>();
		if(prm_gasto.getCodigoEdificio().equals(prm_gasto.getPregrabado().getEdif())){ //Solo si no cambi� el edificio
			if(!prm_gasto.getPregrabado().getPrv_cuit().equals(""))
				if(!prm_gasto.getCuitProveedor().equals(prm_gasto.getPregrabado().getPrv_cuit())) 
					ret.add("El proveedor seleccionado no corresponde con el del pregrabado, se actualizar� con los datos del proveedor del gasto.");
			if(!prm_gasto.getPregrabado().getAgrupamiento().equals(""))	
				if(!prm_gasto.getAgrupamiento().equals(prm_gasto.getPregrabado().getAgrupamiento())) 
					ret.add("El c�digo de agrupamiento del pregrabado es: " + prm_gasto.getPregrabado().getAgrupamiento()  + ", se actualizar� por: " + prm_gasto.getAgrupamiento() + ".");
			
			if(!prm_gasto.getCodigoEspecial().equals(prm_gasto.getPregrabado().getCodigoEspecial())) 
				ret.add("El c�digo especial del pregrabado es: " + prm_gasto.getPregrabado().getCodigoEspecial() + ", se actualizar� por: " + prm_gasto.getCodigoEspecial() + ".");
			if(!prm_gasto.getTipoGasto().equals(prm_gasto.getPregrabado().getTipoGasto())) 
				ret.add("El tipo de gasto del pregrabado es: " + prm_gasto.getPregrabado().getTipoGasto()  + ", se actualizar� por: " + prm_gasto.getTipoGasto() + ".");
			if(!prm_gasto.getAplicarMes().equals(prm_gasto.getPregrabado().getMes())) 
				ret.add("Agregar mes en texto del pregrabado es: " + prm_gasto.getPregrabado().getMes()  + ", se actualizar� por: " + prm_gasto.getAplicarMes() + ".");
			
			for (Iterator<Porcentual> i = prm_gasto.getPregrabado().getPorcentuales().iterator() ; i.hasNext();) {
				Porcentual item = i.next();
				
				if (item.getPorc_importeJson() > BigDecimal.ZERO.doubleValue()){
					for (Iterator<Prorrateo> p = prm_gasto.getListaProrrateos().iterator() ; p.hasNext();) {
						Prorrateo prt = p.next();
						if(prt.getPrt_posicion() == item.getPorc_posicion())
							if(prt.getPrt_importe().compareTo(BigDecimal.valueOf(item.getPorc_importeJson())) != 0)
								ret.add("El Porcentual # " + item.getPorc_posicion() + " del pregrabado es: " + BigDecimal.valueOf(item.getPorc_importeJson()).setScale(2, BigDecimal.ROUND_HALF_UP) + ", se actualizar� por: " + prt.getPrt_importe().setScale(2, BigDecimal.ROUND_HALF_UP) + ".");
					}
				}
			}
		}
		return ret;
	}
	
	
	/** Guarda pregrabado con los cambios realizados en el gasto
	 * @param prm_gasto
	 */
	public void updatePregrabado(Gasto prm_gasto){
		TextoPregrabado textoPregrabado = null; 
		Session session = JSFUtil.getSession();
		Database thisdb = null;
		Document doc = null;
		try {
			thisdb = session.getCurrentDatabase();
			doc = (Document) thisdb.getDocumentByUNID(prm_gasto.getIdPregrabado());
			textoPregrabado = J2BConverter.jsonToBean(TextoPregrabado.class, doc.getItemValueString("conf_json")).get(0);
			//Actualizo los valores
			int posicionEdificio = textoPregrabado.getListaEdificios().indexOf(prm_gasto.getCodigoEdificio());
			if (posicionEdificio > -1){
				textoPregrabado.getEdificios().get(posicionEdificio).setPrv_cuit(prm_gasto.getCuitProveedor());
				textoPregrabado.getEdificios().get(posicionEdificio).setAgrupamiento(prm_gasto.getAgrupamiento());
				textoPregrabado.getEdificios().get(posicionEdificio).setCodigoEspecial(prm_gasto.getCodigoEspecial());
				textoPregrabado.getEdificios().get(posicionEdificio).setTipoGasto(prm_gasto.getTipoGasto());
				textoPregrabado.getEdificios().get(posicionEdificio).setMes(prm_gasto.getAplicarMes());
				
				for (Iterator<Porcentual> i = textoPregrabado.getEdificios().get(posicionEdificio).getPorcentuales().iterator() ; i.hasNext();) {
					Porcentual item = i.next();
					if (item.getPorc_importeJson() > BigDecimal.ZERO.doubleValue()){
						for (Iterator<Prorrateo> p = prm_gasto.getListaProrrateos().iterator() ; p.hasNext();) {
							Prorrateo prt = p.next();
							if(prt.getPrt_posicion() == item.getPorc_posicion())
								if(prt.getPrt_importe().compareTo(BigDecimal.valueOf(item.getPorc_importeJson())) != 0)
									item.setPorc_importeJson(prt.getPrt_importe().doubleValue());
						}
					}
				}
				
			}
			
			final String json = J2BConverter.beanToJson(textoPregrabado);
			doc.replaceItemValue("conf_json", json);
			doc.save();
			
		} catch (NotesException e) {
			e.printStackTrace();
		} catch (J2BException e) {
			doc.remove(true);
			e.printStackTrace();
		}finally{
			//recyle domino objects here
			if(thisdb != null)
				try {
					thisdb.recycle();
				} catch (NotesException e) {
					e.printStackTrace();
				}
			
		}
	}
	
	
	/**
	 * En frmGastos el combo de Agrupamiento sale de PH_$T
	 * @return selector con codigos de agrupamiento de gastos
	 */
	public List<SelectItem> getComboboxAgrupamientoGastos() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		SelectItem optionDefault = new SelectItem();
		optionDefault.setLabel("" );
		optionDefault.setValue("--");
		options.add(optionDefault);
		for (Map.Entry<String,String> entry : agrupamientosGastosMap.entrySet()) {
	    	SelectItem option = new SelectItem();
			option.setLabel(entry.getKey() + " " + entry.getValue());
			option.setValue(entry.getKey());
			options.add(option);
		}
		return options;
	}
	
	
	
	/**
	 * En frmNotas el combo de Agrupamiento sale de PH_$T
	 * @return selector con codigos de agrupamiento de notas
	 */
	public List<SelectItem> getComboboxAgrupamientoNotas() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		SelectItem optionDefault = new SelectItem();
		optionDefault.setLabel("-- Seleccionar --" );
		optionDefault.setValue("--");
		options.add(optionDefault);
		for (Map.Entry<String,String> entry : agrupamientosNotasMap.entrySet()) {
	    	SelectItem option = new SelectItem();
			option.setLabel(entry.getKey() + " " + entry.getValue());
			option.setValue(entry.getKey());
			options.add(option);
		}
		return options;
	}
	
	
	/**
	 * En frmGastos el combo de Codigo Especial sale de configuracion de Notes
	 * @return selector con codigos especiales
	 */
	public List<SelectItem> getComboboxCodigoEspecial() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		
		
		for (Map.Entry<String,String> entry : ar.com.ada3d.utilidades.JSFUtil.getCacheApp().getOpcionesCodigoEspecial().entrySet()) {
			SelectItem option = new SelectItem();
			option.setLabel(entry.getValue());
			option.setValue(entry.getKey());
			options.add(option);
		}
		return options;
	}
	
	
			
	/**
	 * Devuelve las opciones para diferentes combos segun la clave a buscar
	 * @param clave lookup que hace en Notes
	 * @return selector con codigos de Opciones Clave de Notes
	 */
	public List<SelectItem> getComboboxGasto(String clave) {
		List<SelectItem> options = new ArrayList<SelectItem>();		
		for (Map.Entry<String,String> entry : ar.com.ada3d.utilidades.JSFUtil.getOpcionesClaveMap(clave).entrySet()) {
			SelectItem option = new SelectItem();
			option.setLabel(entry.getValue());
			option.setValue(entry.getKey());
			options.add(option);
		}
		return options;
	}
	
	/**
	 * En frmGastos el combo de Liquidacion son 12 meses y default proxima liquidaci�n
	 * @param prm_edificio: necesito la frecuencia de liquidacion del edificio actual
	 * @param prm_mesesParaAdelante cantidad de meses a mostrar 
	 * @return texto MMyyyy de liquidacion
	 */
	public List<SelectItem> getComboboxFechaLiquidacion(Edificio prm_edificio, int prm_mesesParaAdelante) {
		List<SelectItem> options = new ArrayList<SelectItem>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(prm_edificio.getEdf_fechaUltimaLiquidacion());
		for (int i=0; i < prm_mesesParaAdelante; i++) {
			cal.add(Calendar.MONTH, prm_edificio.getEdf_frecuenciaLiquidacion());
			SelectItem option = new SelectItem();
			option.setLabel(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "MMMM yyyy"));
            option.setValue(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "MMyyyy"));
			options.add(option);
		}
		return options;
	}
	
	
	/**
	 * Junta el detalle de los gastos y los devuelve en un array
	 * @return array con el texto (detalle) de cada gasto 
	 * @usedIn: TypeAhead de busqueda de gastos
	 */
	public ArrayList<String> getArrayDetallesGastos() {
		ArrayList<String> result = new ArrayList<String>();
		String concatenarDetalle;
		for (Gasto myGasto : listaGastos) {
			concatenarDetalle = "";
			for(String detalle : myGasto.getTextoDetalleFactura()){
				if(concatenarDetalle.equals("")){
					concatenarDetalle = detalle;
				}else{
					concatenarDetalle = concatenarDetalle + "<br />" + detalle;
				}
			}
			for(Prorrateo gastoProrrateo : myGasto.getListaProrrateos()){
				if(concatenarDetalle.equals("")){
					concatenarDetalle = gastoProrrateo.getPrt_titulo() + ": " + gastoProrrateo.getPrt_importe().toString();
				}else{
					concatenarDetalle = concatenarDetalle + "<br />" + gastoProrrateo.getPrt_titulo() + ": " + gastoProrrateo.getPrt_importe().toString(); 
				}
			}
			result.add(concatenarDetalle + "|" + myGasto.getIdGasto());
		}
		return result;
	}
	
	
	/**
	 * Junta el detalle de los gastos con los datos del Proveedor  y los devuelve en un array
	 * Los datos por agregar son: Proveedor y cuit - Fecha de la factura - N�mero de la factura - Direcci�n proveedor
	 * @param prm_gasto del que muestro el detalle
	 * @param prm_OrdenDetalleGasto: el orden de los datos 
	 * @param prm_beanProveedor : el bean para obtener el proveedor
	 * @return array con datos de proveedor + el texto (detalle) de cada gasto 
	 * @usedIn: viewGastos, frmGastos en modo lectura
	 */
	
	public ArrayList<String> getPreviewDetalleGastos(Gasto prm_gasto, HashMap<String, String> prm_OrdenDetalleGasto, ProveedorBean prm_beanProveedor) {
		ArrayList<String> result = new ArrayList<String>();
		if(prm_OrdenDetalleGasto == null){
			result.addAll(prm_gasto.getTextoDetalleFactura());
		}else if(prm_OrdenDetalleGasto.containsValue(null)){
			result.addAll(prm_gasto.getTextoDetalleFactura());
		}else{
			//Puede que los datos del proveedor vengan nulos si cambiaron la configuracion y ya habian cargado previamente datos vacios
			final String datonulo = "***"; 
			StringBuilder texto = new java.lang.StringBuilder();
			int count = 0;
			for (String orden : prm_OrdenDetalleGasto.values()) {
				if(orden.equals("Proveedor y cuit")){
					if(prm_gasto.getCuitProveedor().equals("0")){
						texto.append(datonulo + " - " + datonulo);
					}else{
						String strCuit = prm_gasto.getCuitProveedor();
						texto.append((prm_gasto.getCuitProveedor() == null) ? datonulo + " - " + datonulo : prm_beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "razonSocial") + " - " + strCuit.substring(0,2) + "-" + strCuit.substring(2,10) + "-" + strCuit.substring(strCuit.length() - 1));
					}
				}	
				if(orden.equals("Fecha de la factura")){
					texto.append((prm_gasto.getFechaFactura() == null) ? datonulo : ar.com.ada3d.utilidades.Conversores.DateToString(prm_gasto.getFechaFactura(), "dd/MM/yyyy"));
				}
				
				if(orden.equals("N�mero de la factura")){
					texto.append((prm_gasto.getSucursalFactura() == null) ? datonulo : prm_gasto.getSucursalFactura());
					texto.append("-");
					texto.append((prm_gasto.getNumeroFactura() == null) ? datonulo : prm_gasto.getNumeroFactura());
				}
				if(orden.equals("Direcci�n proveedor")){
					texto.append(prm_beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "domicilio"));
				}
				count += count;
			    if(count < 4){
			    	texto.append(" - ");
			    }				
			}
			//Agrego el detalle del gasto
			for (String detalle : prm_gasto.getTextoDetalleFactura()){
				texto.append(detalle);
			}
			//Divido el string hasta 72 caracteres
			result.addAll(ar.com.ada3d.utilidades.Conversores.splitString(texto.toString(), 72));
		}
		return result;
	}

	
	/**
	 * Junta el detalle de los gastos con los datos del Proveedor  y los devuelve en un array
	 * Tambien completa FilaColumnaTextoDetalleFactura del prm_gasto
	 * Los datos por agregar son: Proveedor y cuit - Fecha de la factura - N�mero de la factura - Direcci�n proveedor
	 * @param prm_gasto del que muestro el detalle
	 * @param prm_OrdenDetalleGasto: el orden de los datos 
	 * @param prm_beanProveedor : el bean para obtener el proveedor
	 * @return array con datos de proveedor + el texto (detalle) de cada gasto 
	 * @usedIn: viewGastos, frmGastos en modo lectura
	 */
	
	public ArrayList<String> getPreviewDetalleGastos(Gasto prm_gasto, ArrayList<String> prm_OrdenDetalleGasto) {
		ArrayList<String> result = new ArrayList<String>();
		if(prm_OrdenDetalleGasto == null){
			result.addAll(prm_gasto.getTextoDetalleFactura());
			if(!prm_gasto.getAplicarMes().equals("0")) result.add(getAgregarMesEnTexto(prm_gasto.getAplicarMes(), prm_gasto.getFechaLiquidacion())); //Voy a incluir el mes autom�tico en el texto
			prm_gasto.setFilaColumnaTextoDetalleFactura(1);
		}else if(prm_OrdenDetalleGasto.contains(null)){
			result.addAll(prm_gasto.getTextoDetalleFactura());
			if(!prm_gasto.getAplicarMes().equals("0")) result.add(getAgregarMesEnTexto(prm_gasto.getAplicarMes(), prm_gasto.getFechaLiquidacion())); //Voy a incluir el mes autom�tico en el texto
			prm_gasto.setFilaColumnaTextoDetalleFactura(1);
		}else{
			//Puede que los datos del proveedor vengan nulos si cambiaron la configuracion y ya habian cargado previamente datos vacios
			boolean appendGuion = false; 
			StringBuilder texto = new java.lang.StringBuilder();
			if (prm_gasto.getCuitProveedor() != null && !prm_gasto.getCuitProveedor().equals("0")){
				ProveedorBean beanProveedor = new ProveedorBean();
				String strCuit = prm_gasto.getCuitProveedor();
				for (String orden : prm_OrdenDetalleGasto) {
					if(orden.equals("1")){//Raz�n social proveedor
						texto.append(beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "razonSocial"));
						appendGuion = true;
					}	
					if(orden.equals("2")){//CUIT proveedor
						texto.append(strCuit.substring(0,2) + "-" + strCuit.substring(2,10) + "-" + strCuit.substring(strCuit.length() - 1));
						appendGuion = true;
					}	
					if(orden.equals("3") && prm_gasto.getFechaFactura() != null){//Fecha factura
						texto.append(ar.com.ada3d.utilidades.Conversores.DateToString(prm_gasto.getFechaFactura(), "dd/MM/yyyy"));
						appendGuion = true;
					}
					
					if(orden.equals("4")){//N�mero factura
						if(!prm_gasto.getSucursalFactura().equals("") && !prm_gasto.getSucursalFactura().equals("0000") && !prm_gasto.getNumeroFactura().equals("") && !prm_gasto.getNumeroFactura().equals("00000000")){
							texto.append(prm_gasto.getSucursalFactura());
							texto.append("-");
							texto.append(prm_gasto.getNumeroFactura());
							appendGuion = true;
						}
					}
					if(orden.equals("5")){//Direcci�n proveedor
						texto.append(beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "domicilio"));
						appendGuion = true;
					}
					if(orden.equals("6")){//Matr�cula proveedor
						String matricula = beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "matricula");
						if(matricula != null && !matricula.equals("")){
							texto.append(beanProveedor.getDatoPorCuit(prm_gasto.getCuitProveedor(), "matricula"));
							appendGuion = true;
						}
					}

					if(appendGuion){
						texto.append(" - ");
						appendGuion = false;
					}				
				}
				//texto.setLength(texto.length() - 3);
				//texto.append("~^�");
			}

			//Agrego la fila columna
			prm_gasto.setFilaColumnaTextoDetalleFactura(texto.length());
			
			//Agrego el detalle del gasto
			for (String detalle : prm_gasto.getTextoDetalleFactura()){
				texto.append(detalle);
				texto.append("\n");
			}

			if(prm_gasto.getAplicarMes() != null && !prm_gasto.getAplicarMes().equals("0"))
				texto.append(getAgregarMesEnTexto(prm_gasto.getAplicarMes(), prm_gasto.getFechaLiquidacion())); //Voy a incluir el mes autom�tico en el texto
			
			//Divido el string hasta 72 caracteres
			result.addAll(ar.com.ada3d.utilidades.Conversores.splitString(texto.toString(), 72));
		}
		return result;
	}
	
	
	public String getAgregarMesEnTexto(String prm_aplicarMes, String prm_fechaLiquidacion){
		Date dtFechaLiquidacion = ar.com.ada3d.utilidades.Conversores.StringToDate("MMyyyy", prm_fechaLiquidacion);
		
		if(prm_aplicarMes.equals("1")){
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtFechaLiquidacion);
			cal.add(Calendar.MONTH, -1);
			return "MES: " + ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "MMMM/yy");
		}
		
		if(prm_aplicarMes.equals("2"))			
			return "MES: " + ar.com.ada3d.utilidades.Conversores.DateToString(dtFechaLiquidacion, "MMMM/yy");
		
		if(prm_aplicarMes.equals("3")){
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtFechaLiquidacion);
			cal.add(Calendar.MONTH, 1);
			return "MES: " + ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "MMMM/yy");
			
		}
		
		return "";
	}
	
	
	/**
	 * Devuelve un gasto que busco por id
	 * @param prm_id el id a devolver sino null
	 * @return objeto Gasto
	 */
	public Gasto getGastoPorId(String prm_id){
		for (Gasto myGasto : listaGastos) {
			if(myGasto.getIdGasto().equals(prm_id))
				return myGasto;
		}
		return null;
	}
	
	public static GastoOpciones getOpcionGastoMaestro(){
		ArrayList<String> nl = null;
		QueryAS400 query = new ar.com.ada3d.connect.QueryAS400();
		try {
			nl = query.getSelectAS("opcionesGastoMaestro", null);
		} catch (NotesException e) {
			e.printStackTrace();
		}
		GastoOpciones myGastoOpciones = null;
		for (String strLinea : nl) {
			myGastoOpciones = new GastoOpciones();
			myGastoOpciones.setCodigoEdificio(strLinea.split("\\|")[0].trim());
			myGastoOpciones.setTipoNumeracion(strLinea.split("\\|")[1].trim());
			myGastoOpciones.setNumerarSueldos(strLinea.split("\\|")[2].trim());
			myGastoOpciones.setOrdenDatosProveedorEnDetalleDelGasto(strLinea.split("\\|")[3].trim());
			myGastoOpciones.setNumeroProximoGasto(Integer.parseInt(strLinea.split("\\|")[4].trim()));
			myGastoOpciones.setConfiguracionUnica(true);
			myGastoOpciones.setIsReadMode(true);
			myGastoOpciones.setIsNew(false);
		}
		return myGastoOpciones;
	}
	
	
	/**
	 * Genera las cuotas de un gasto desdoblado
	 * @usedIn: frmGastoDesdoblado boton generar cuotas
	 * 
	 */
	public void generarCuotasDesdoblado(){
		List<GastoDesdobladoCuota> arrCuotas = new ArrayList<GastoDesdobladoCuota>();
		int nroCuota = this.gasto.getDesdoblado().getNumeroCuotaInicial();
		Calendar cal = Calendar.getInstance();
		cal.setTime(ar.com.ada3d.utilidades.Conversores.StringToDate("MMyyyy", this.gasto.getDesdoblado().getPrimerMes()));
		
		for (int i=this.gasto.getDesdoblado().getNumeroCuotaInicial(); i <= this.gasto.getDesdoblado().getCantidadCuotas(); i++){
			GastoDesdobladoCuota cuota = new GastoDesdobladoCuota();
			cuota.setNumeroCuota(nroCuota);
			cuota.setMesLiquidaCuota(ar.com.ada3d.utilidades.Conversores.DateToString(cal.getTime(), "MMyyyy"));
			cuota.setImporteCuota(this.gasto.getDesdoblado().getImporteCuota());
			cuota.setCodigoEspecialCuota(this.gasto.getCodigoEspecial());
			arrCuotas.add(cuota);
			nroCuota += 1;
			cal.add(Calendar.MONTH, this.gasto.getDesdoblado().getFrecuencia());
		}
		this.gasto.getDesdoblado().setListaCuotas(arrCuotas);
	}
	
	
	//Getters & Setters
	
	public List<Gasto> getListaGastos() {
		return listaGastos;
	}
	
	
	public List<Gasto> getListaGastosLiquidacionSiguiente() {
		return listaGastosLiquidacionSiguiente;
	}


	public List<Gasto> getListaGastosLiquidacionesFuturas() {
		return listaGastosLiquidacionesFuturas;
	}


	public List<Gasto> getListaGastosLiquidacionesPasadas() {
		return listaGastosLiquidacionesPasadas;
	}

	
	public List<TextoPregrabado> getListaTextosPregrabados() {
		return listaTextosPregrabados;
	}

	
	public Gasto getGasto() {
		return gasto;
	}


	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
		
	}

	public void setGasto(Gasto gasto, boolean updateAS400, String tipoFormulario) {
		updateGasto(gasto, tipoFormulario);
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


	public HashMap<String, String> getAgrupamientosGastosMap() {
		return agrupamientosGastosMap;
	}

}
