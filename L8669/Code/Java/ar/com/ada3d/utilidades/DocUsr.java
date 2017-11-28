package ar.com.ada3d.utilidades;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.openntf.domino.*;
import org.openntf.domino.xsp.XspOpenLogUtil;

import ar.com.ada3d.connect.GetQueryAS400;

public class DocUsr {
	private static final long serialVersionUID = 1L;
	private final HashMap<String, String> _map;
	private Vector usrSelected;
	public Vector edificiosSelected;
	public ArrayList<String> edificiosLista;
	
	
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
					
					GetQueryAS400 getQueryAS400  = new ar.com.ada3d.connect.GetQueryAS400();				
					this.edificiosLista = getQueryAS400.getSelectAS("PH_E01", null);	
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

	public String getUser() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userName");
		}
		return ret;
	}

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

	private String getUserSec() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userSequential");
		}
		return ret;
	}

	private String getStatus() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userStatus");
		}
		return ret;
	}

	private boolean isUsrSeg() {
		boolean ret = false;
		synchronized (this._map) {
			ret = this._map.get("userSeg").equals("1");
		}
		return ret;
	}

	private String getUserDB() {
		String ret;
		synchronized (this._map) {
			ret = this._map.get("userDB");
		}
		return ret;
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

	// Getters y Setteres
	private void setUsrSelected(Vector usrSelected) {
		this.usrSelected = usrSelected;
	}

	private Vector getUsrSelected() {
		return usrSelected;
	}
	
	public ArrayList<String> getEdificiosLista() {
		return edificiosLista;
	}

	public void setEdificiosLista(ArrayList<String> aa) {
		this.edificiosLista = aa;
	}

}
