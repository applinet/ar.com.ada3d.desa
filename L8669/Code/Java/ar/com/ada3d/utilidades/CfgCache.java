package ar.com.ada3d.utilidades;

import java.util.*;


public class CfgCache {
	/**Esta clase va a tener las opciones clave de notes que no varian 
	 * y se mantienen como applicationScope, para eliminarlas usar el debugToolbar
	 * */
	private static final long serialVersionUID = 1L;
	
		
	private LinkedHashMap<String, String> opcionesGastoOrdenDatosProveedor;
	
	public CfgCache() {
		System.out.println("FPR_constructor cache bean");
		this.opcionesGastoOrdenDatosProveedor = ar.com.ada3d.utilidades.JSFUtil.getOpcionesClaveMap("opcionesGastoOrdenDatosProveedor");
		// Empty constructor
	}

	
	
	public LinkedHashMap<String, String> getOpcionesGastoOrdenDatosProveedor() {
		return opcionesGastoOrdenDatosProveedor;
	}
	
	
}
