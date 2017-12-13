package ar.com.ada3d.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.io.Serializable;
import javax.faces.model.SelectItem;

import test.test;
import ar.com.ada3d.utilidades.*;

import lotus.domino.NotesException;

import ar.com.ada3d.connect.GetQueryAS400;
import ar.com.ada3d.utilidades.DocUsr;

/*
 * Cuando hablo de edificios son todos los de la administracion.
 * Cuando hablo de MyEdificios son los edificios que puede ver el usuario.
 * Cuando hablo de MyEdificiosTrabajo son para trabajar el usuario es decir que no estan bloqueados
 * Tomo la scope de usuario para los edificios autorizados por usuario
 * */
public class Edificios implements Serializable{
	private static final long serialVersionUID = 1L;
	HashMap<String, Edificio> hmEdificios = new HashMap<String,Edificio>();
	static ArrayList<Edificio> edificios = new ArrayList<Edificio>();
	private static DocUsr docUsuario = (DocUsr) JSFUtil.resolveVariable("DocUsr");
	
	public Edificios() {
		System.out.println("Constructor Edificios y llamada AddEdificiosAS400");
		AddEdificiosAs400();
		// Empty constructor
	}

	
	/*
	 * Esto devuelve para cada usuario el ComboBox de Edificios autorizados para trabajar
	 * @return: etiqueta y valor para xp:comboBox
	 * @usedIn: Combo principal de layout asociado a una sessionScope(edificioSelected) 
	 * */
	public static List<SelectItem> getComboboxMyEdificios() {
	    List<SelectItem> options = new ArrayList<SelectItem>();
	    for (Edificio miEdificio : edificios) {
	    		if(!docUsuario.getEdificiosNoAccessLista().contains(miEdificio.getEdf_codigo())){ //Solo edificios autorizados
	    			SelectItem option = new SelectItem();
	    			option.setLabel(miEdificio.getEdf_direccion());
	    			option.setValue(miEdificio.getEdf_codigo());
	    			options.add(option);
	    		}
	    }
	    return options;
	}

	
	
	/*
	 * Esto devuelve para cada usuario el ComboBox de Edificios autorizados para trabajar
	 * y en estadoProceso=1
	 * @return: etiqueta y valor para xp:comboBox
	 * @usedIn: en el combo al lado del boton Save de un formulario
	 * */
	public static List<SelectItem> getComboboxMyEdificiosTrabajo() {
	    List<SelectItem> options = new ArrayList<SelectItem>();
	    for (Edificio miEdificio : edificios) {
	    	if(miEdificio.getEdf_estadoProceso().equals("1")){ // solo estado=1
	    		if(!docUsuario.getEdificiosNoAccessLista().contains(miEdificio.getEdf_codigo())){ //Solo edificios autorizados
	    			SelectItem option = new SelectItem();
	    			option.setLabel(miEdificio.getEdf_direccion());
	    			option.setValue(miEdificio.getEdf_codigo());
	    			options.add(option);
	    		}
	    	}
	    }
	    return options;
	}
	
	
	
	/*
	 * @Return: un array con las direcciones de todos los edificios autorizados para este usuario
	 * */
	public ArrayList<String> getMyEdificios (){
		ArrayList<String> result = new ArrayList<String>();
		for (Edificio miEdificio : edificios) {
			if(!docUsuario.getEdificiosNoAccessLista().contains(miEdificio.getEdf_codigo())){ //Solo edificios autorizados
				result.add(miEdificio.getEdf_direccion());
			}
		}
		return result;
	}
	
	
	/*
	 * @Return: un array con las direcciones de todos los edificios de la administracion
	 * */
	public ArrayList<String> getTestDireccion (){
		ArrayList<String> result = new ArrayList<String>();
		for (Edificio miEdificio : edificios) {
				result.add(miEdificio.getEdf_direccion());
		}
		return result;
	}
	
	public Vector<HashMap<String, String>> getTest(){
		Vector<HashMap<String, String>> result = new Vector<HashMap<String, String>>(); 
		
		HashMap<String, String> temp1 = new HashMap<String,String>();
		temp1.put("codigoEdificio", "VE%");
		temp1.put("descripcionEdificio", "AV.DE LOS INCAS 3730/2-CAP.");
		temp1.put("selected", "0");
		result.add(temp1);
		
		HashMap<String, String> temp2 = new HashMap<String,String>();
		temp2.put("codigoEdificio", "VEÑ");
		temp2.put("descripcionEdificio", "ARRIBEÑOS 2350-CAPITAL FED.");
		temp2.put("selected", "0");
		result.add(temp2);
		
		HashMap<String, String> temp3 = new HashMap<String,String>();
		temp3.put("codigoEdificio", "VE4");
		temp3.put("descripcionEdificio", "ARCOS 2574-CAPITAL FEDERAL");
		temp3.put("selected", "1");
		result.add(temp3);
		
		HashMap<String, String> temp4 = new HashMap<String,String>();
		temp4.put("codigoEdificio", "VFD");
		temp4.put("descripcionEdificio", "AMENABAR 3371/73-CAPITAL");
		temp4.put("selected", "1");
		result.add(temp4);		
		return result;
		
	}
	

	

	/*
	 * Agrego edificios de a uno
	 * @Param: objeto Edificio
	 * */
	public void AddEdificioMap(Edificio prmEdificio){
		hmEdificios.put(prmEdificio.getEdf_codigo(), prmEdificio);
	}
	
	/*
	 * Obtengo un edificio por el codigo SASA
	 * @Param: el codigo de edificio
	 * @Return: un objeto Edificio o nulo
	 */
	public Edificio getEdificioMap(String prmCodigoEdificio){
		if(hmEdificios.containsKey(prmCodigoEdificio)){
			return hmEdificios.get(prmCodigoEdificio);
		}
		return null;
	}
	
	
	/*
	 * Agrego Edificios consultando As400, cada linea separa el dato por un pipe
	 * */
	public void AddEdificiosAs400(){
		GetQueryAS400 query = new ar.com.ada3d.connect.GetQueryAS400();
		ArrayList<String> nl = null;
		try {
			nl = query.getSelectAS("dataObjEdificio",
					null, false);
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (String strLinea : nl){
			Edificio myEdificio = new Edificio();
			myEdificio.setEdf_codigo(strLinea.split("\\|")[0].trim());
			myEdificio.setEdf_codigoVisual(strLinea.split("\\|")[1].trim());
			myEdificio.setEdf_direccion(strLinea.split("\\|")[2].trim());
			myEdificio.setEdf_estadoProceso(strLinea.split("\\|")[3].trim());
			edificios.add(myEdificio);
			AddEdificioMap(myEdificio); //Lo agrego al mapa por código
		}
	}


	public static ArrayList<Edificio> getEdificios() {
		return edificios;
	}


	public static void setEdificios(ArrayList<Edificio> edificios) {
		Edificios.edificios = edificios;
	}
	
	
}
