package ar.com.ada3d.utilidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.openntf.domino.*;
import org.openntf.domino.xsp.XspOpenLogUtil;

import ar.com.ada3d.connect.GetQueryAS400;
import ar.com.ada3d.data.Edificio;

public class DocUsr implements Serializable{
	private static final long serialVersionUID = 1L;
	private final HashMap<String, String> _map;
	private Vector usrSelected;

	public ArrayList<String> edificiosLista;
	public HashMap<String, String> edificiosListaMapa;
	private Vector<Object> edificiosNoAccessLista;
	private ArrayList<String> edificiosMyLista;


	public DocUsr() {
		System.out.println("Constructor DocUsr");
		this._map = new HashMap<String, String>();
		
		updateDocUsr();
	}

	private void updateDocUsr() {
		try {
			Session session = JSFUtil.getSession();
			Database currentDB = JSFUtil.getCurrentDatabase();

			Document docUsuario = null;
			docUsuario = getUserDocument(session.getEffectiveUserName(),
					currentDB);
			if (docUsuario != null) {
				synchronized (this._map) {
					this._map.put("userName", docUsuario
							.getItemValueString("usr_UserName_des"));
					this._map
							.put(
									"nombreyApellido",
									docUsuario
											.getItemValueString("usr_Nombre_des")
											+ " "
											+ docUsuario
													.getItemValueString("usr_Apellido_des"));
					this._map.put("userMaskName", docUsuario
							.getItemValueString("usr_UserMaskName_des"));
					this._map.put("userSequential", docUsuario
							.getItemValueString("usr_UserSequential_nro"));
					this._map.put("userStatus", docUsuario
							.getItemValueString("usr_Status_des"));
					this._map.put("userSeg", docUsuario
							.getItemValueString("usr_USRSEG_opt"));
					this._map.put("userDB", currentDB.getFileName().substring(
							currentDB.getFileName().length() - 8, 5));

					this.edificiosNoAccessLista = docUsuario
							.getItemValue("usr_EdificiosSinAcceso_cod");

					GetQueryAS400 getQueryAS400 = new ar.com.ada3d.connect.GetQueryAS400();
					this.edificiosLista = getQueryAS400.getSelectAS("PH_E01",
							null);
										
					updateListaEdificiosMapa(); 
					
					updateMyListaEdificios();
					System.out.println("updateMyListaEdificios para scope:"
							+ getEdificiosMyLista().toString());
				}
				/* getItemValue definirlo como Vector=import java.util.Vector */
				setUsrSelected(docUsuario.getItemValue("usr_MenuSelected_cod"));

			} else {
				synchronized (this._map) {
					this._map.put("userName", session.getEffectiveUserName());

					this._map.put("userDB", currentDB.getFileName().substring(
							currentDB.getFileName().length() - 8, 5));
				}

			}
		} catch (Exception e) {
			XspOpenLogUtil.logError(e);

		}
	}

	/*
	 * Actualizo la lista de edificios permitidas a el usuario logueado
	 * */
	private void updateMyListaEdificios() {
		this.setEdificiosMyLista(diffEdificios());
	}

	/*
	 * 
	 * */
	private ArrayList<String> diffEdificios() {
		
		ArrayList<String> arrRet = new ArrayList<String>();
		if (edificiosNoAccessLista == null | edificiosNoAccessLista.isEmpty())
			return edificiosLista;
		
		for(String currentKey : edificiosListaMapa.keySet()){
			if (!edificiosNoAccessLista.contains(currentKey))
				arrRet.add(edificiosListaMapa.get(currentKey));
		}
		return arrRet;
	}
	
	/*
	 * 
	 * */
	private void updateListaEdificiosMapa(){
		HashMap<String, String> tempMapa = new HashMap<String, String>();
		for (String strEdificio : edificiosLista) {
			tempMapa.put(strEdificio.split("\\|")[1].trim(), strEdificio.split("\\|")[0]);
		}
		this.edificiosListaMapa = tempMapa;
	}

	
	
	
	
	/*
	 * Devuelve el documento de usuario de currentDatabase (base de
	 * administracion)
	 */
	private Document getUserDocument(String userName, Database currentDB) {
		Document docUsuario;
		View viewMenuPorUsuario;
		String nombreVista = JSFUtil
				.getOpcionesClave("VIEW_USUARIOS_POR_ADMINISTRACION");

		if (nombreVista == "") {
			return null;
		}
		viewMenuPorUsuario = currentDB.getView(nombreVista);
		docUsuario = viewMenuPorUsuario.getFirstDocumentByKey(userName, true);
		if (docUsuario != null) {
			return docUsuario;
		}
		return null;

	}

	
	// ******** Getters and Setters ***************
	public String getUser() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userName");
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private String getNombre() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("nombreyApellido");
		}
		return ret;
	}

	public String getUserMask() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userMaskName");
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private String getUserSec() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userSequential");
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private String getStatus() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userStatus");
		}
		return ret;
	}

	@SuppressWarnings("unused")
	private boolean isUsrSeg() {
		boolean ret = false;
		synchronized (this._map) {
			ret = this._map.get("userSeg").equals("1");
		}
		return ret;
	}

	public String getUserDB() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userDB");
		}
		return ret;
	}

	private void setUsrSelected(Vector usrSelected) {
		this.usrSelected = usrSelected;
	}

	private Vector getUsrSelected() {
		return usrSelected;
	}

	public ArrayList<String> getEdificiosLista() {
		return edificiosLista;
	}

	public void setEdificiosMyLista(ArrayList<String> edificiosMyLista) {
		this.edificiosMyLista = edificiosMyLista;
	}

	public ArrayList<String> getEdificiosMyLista() {
		return edificiosMyLista;
	}

	public HashMap<String, String> getEdificiosListaMapa() {
		return edificiosListaMapa;
	}

	public void setEdificiosListaMapa(HashMap<String, String> edificiosListaMapa) {
		this.edificiosListaMapa = edificiosListaMapa;
	}

}
