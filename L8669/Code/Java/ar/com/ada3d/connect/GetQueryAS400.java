package ar.com.ada3d.connect;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import org.openntf.domino.Document;

import ar.com.ada3d.utilidades.CfgDataSource;
import ar.com.ada3d.utilidades.CfgTablas;
import ar.com.ada3d.utilidades.JSFUtil;

import lotus.domino.NotesException;

public class GetQueryAS400 implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private CfgDataSource configDs;

	public GetQueryAS400() {
		// No Args
	}

	//Para iniciar la conexion con el AS400
	public Boolean initConexion(){
		Document docDS = JSFUtil.getDocConexiones_y_Tablas("BaseConexionAS400");
		if (docDS == null) return false;
		configDs = new CfgDataSource(docDS);
		return true;		
	}
	
	
	// Utilizado - Devuelvo un array por ejemlo para un combo
	public ArrayList<String> getSelectAS(String param_clave, Document param_doc) throws NotesException {
		if (!initConexion()) return null;
		
		Document docTabla = JSFUtil.getDocConexiones_y_Tablas(param_clave);
		if (docTabla == null) return null;
		CfgTablas configTabla = new CfgTablas(docTabla);

		//INI - Siempre hay que hacer esto para que complete la biblioteca en el sql
		if (param_doc == null){
			//Invento un doc y le paso la biblioteca de la base que estoy
			Document docDummy = JSFUtil.getDocDummy();
			docDummy.appendItemValue("biblioteca", JSFUtil.getBiblioteca("B"));
			configTabla.setStrsSQL(docDummy);
		}else{
			//Al doc que tengo le agrego la biblioteca
			param_doc.appendItemValue("biblioteca", JSFUtil.getBiblioteca("B"));
			configTabla.setStrsSQL(param_doc);
		}	 
		//FIN - Siempre hay que hacer esto para que complete la biblioteca en el sql
		
		Connection connection = null;
		
		ArrayList<String> returnArrlist = new ArrayList<String>(); //variable que devuelvo
		Vector<String> arrcampos = configTabla.getResultado();//Desde documento de configuracion
		
		try {
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
			connection = DriverManager.getConnection(configDs.getUrlConexion(), configDs.getUserRead(), configDs.getPassRead());
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(configTabla.getStrsSQL());
			int cont = 0;
			while (rs.next()) {
				cont++;
				String temp = "";
				for (String s : arrcampos) {
					if (s.contains("@text_")) {
						temp = temp + s.substring(6);
					} else {
						temp = temp + rs.getString(s).trim();
					}
				}
				returnArrlist.add(temp);
			}
		}

		catch (Exception e) {
			System.out.println();
			System.out.println("ERROR: " + e.getMessage());
		}

		finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
			}
		}
		return returnArrlist;

	}

		
	public static void close(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void close(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void close(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
