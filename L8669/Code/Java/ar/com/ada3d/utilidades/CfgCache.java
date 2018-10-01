package ar.com.ada3d.utilidades;

import java.util.*;

import ar.com.ada3d.model.Edificio;


public class CfgCache {
	/**Esta clase va a tener las opciones clave de notes que no varian 
	 * y se mantienen como applicationScope, para eliminarlas usar el debugToolbar
	 * */
	private static final long serialVersionUID = 1L;
		
	private LinkedHashMap<String, String> opcionesGastoOrdenDatosProveedor;
	private LinkedHashMap<String, String> opcionesCodigoEspecial;
	private HashMap<String, Edificio> hmEdificios;
	
	/**
	 * Constructor de cacheBean
	 */
	public CfgCache() {
		System.out.println("FPR_constructor cache bean");
		opcionesGastoOrdenDatosProveedor = ar.com.ada3d.utilidades.JSFUtil.getOpcionesClaveMap("opcionesGastoOrdenDatosProveedor");
		opcionesCodigoEspecial = ar.com.ada3d.utilidades.JSFUtil.getOpcionesClaveMap("codigoEspecial");
		hmEdificios = ar.com.ada3d.controller.EdificioBean.cacheHashMapEdificios();
	}
	
	
	public void initCacheBean() {
		
	}

	public LinkedHashMap<String, String> getOpcionesGastoOrdenDatosProveedor() {
		return opcionesGastoOrdenDatosProveedor;
	}

	public void setOpcionesGastoOrdenDatosProveedor(
			LinkedHashMap<String, String> opcionesGastoOrdenDatosProveedor) {
		this.opcionesGastoOrdenDatosProveedor = opcionesGastoOrdenDatosProveedor;
	}

	public LinkedHashMap<String, String> getOpcionesCodigoEspecial() {
		return opcionesCodigoEspecial;
	}


	public void setOpcionesCodigoEspecial(
			LinkedHashMap<String, String> opcionesCodigoEspecial) {
		this.opcionesCodigoEspecial = opcionesCodigoEspecial;
	}

	public HashMap<String, Edificio> getHmEdificios() {
		return hmEdificios;
	}


	public void setHmEdificios(HashMap<String, Edificio> hmEdificios) {
		this.hmEdificios = hmEdificios;
	}

	
	
}
