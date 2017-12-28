package ar.com.ada3d.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import javax.faces.model.SelectItem;
import ar.com.ada3d.model.Edificio;
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
public class Edificios implements Serializable {

	public Edificios() {
		System.out.println("Constructor Edificios y llamada AddEdificiosAS400");
		AddEdificiosAs400();
	}

	private Edificio edificio;
	private static final long serialVersionUID = 1L;
	HashMap<String, Edificio> hmEdificios = new HashMap<String, Edificio>();
	private static List<Edificio> listaEdificios;
	private static DocUsr docUsuario = (DocUsr) JSFUtil
			.resolveVariable("DocUsr");

	/*
	 * Esto devuelve para cada usuario el ComboBox de Edificios autorizados para
	 * trabajar
	 * 
	 * @return: etiqueta y valor para xp:comboBox
	 * 
	 * @usedIn: Combo principal en ccLayoutBootstrap, está asociado a una
	 * sessionScope(edificioSelected)
	 */
	public static List<SelectItem> getComboboxMyEdificios() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		boolean pepe = docUsuario == null ? true : false;
		System.out.println("FIXME: docUsuario es null: " + pepe );
		pepe = docUsuario.getEdificiosNoAccessLista() == null ? true : false;
		System.out.println("FIXME: docUsuario.getEdificiosNoAccessLista es null: " + pepe);
		for (Edificio miEdificio : listaEdificios) {
			
			if (!docUsuario.getEdificiosNoAccessLista().contains(
					miEdificio.getEdf_codigo())) { // Solo edificios autorizados
				SelectItem option = new SelectItem();

				option
						.setLabel(miEdificio.getEdf_codigoVisual().equals("") ? miEdificio
								.getEdf_codigo()
								: miEdificio.getEdf_codigoVisual() + " "
										+ miEdificio.getEdf_direccion() + " "
										+ miEdificio.getEdf_ultimaLiquidacion());
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
		if (!(listaEdificios == null)){
			listaEdificios.clear();
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
			myEdificio = new Edificio();
			myEdificio.setEdf_codigo(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			myEdificio.setEdf_codigoPostal(strLinea.split("\\|")[4].trim());
			listaEdificios.add(myEdificio);
			AddEdificioMap(myEdificio); // Lo agrego al mapa por código
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
			System.out.println("Ya ");
			myEdificio = getEdificioMap(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
		
		}
	}

	
	public void saveEdificio() {
		QueryAS400 pepe = new QueryAS400();

		if (pepe.updateAS("updateEdificios", null)) {
			System.out.println("save: " + edificio.getEdf_direccion());
		}
	}

	
	//*** Getters & Setters *****
	
	public List<Edificio> getListaEdificios() {
		return listaEdificios;
	}

	public void setListaEdificios(ArrayList<Edificio> edificios) {
		Edificios.listaEdificios = edificios;
	}

	public Edificio getEdificio() {
		return edificio;
	}

	public void setEdificio(Edificio edificio) {
		this.edificio = edificio;
	}

}
