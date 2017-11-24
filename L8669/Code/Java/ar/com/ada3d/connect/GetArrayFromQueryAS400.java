package ar.com.ada3d.connect;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import lotus.domino.NotesException;
//import ar.com.hdi.autos.utilidades.DocProfile;
//import ar.com.hdi.autos.utilidades.LogError;
//import ar.com.hdi.autos.utilidades.ODBCProfile;

public class GetArrayFromQueryAS400 implements Serializable {

	private static final long serialVersionUID = 1L;

	public GetArrayFromQueryAS400() {
		// No Args
		
	}

	@SuppressWarnings("unused")
	private ArrayList<String> selectAS;

	public void setSelectAS(ArrayList<String> selectAS) {
		this.selectAS = selectAS;
	}

	
	// Nuevo Formato
	public ArrayList<String> getSelectAS(String clave) throws NotesException {
		Connection connection = null;
		String url = "jdbc:as400://DOMINO;naming=system;socket timeout=10000;thread used=false";
		String user = "DOMINO_R";
		String pwd = "A";
		System.out.println("LLEGO ACA: " +  clave);
		String strSQL = clave;
		ArrayList<String> selectAS = new ArrayList<String>();

		Vector<String> arrcampos = new Vector (0);
		
		arrcampos.addElement("DIRECC");
		try {
			System.out.println("0");
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
			System.out.println("1");
			System.out.println("2");
			//connection = createConnection(docConf.getUrlConexion(), docConf.getUserRead(), docConf.getPassRead());

			connection = DriverManager.getConnection(url, user, pwd);
			System.out.println("3");
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			System.out.println("4");
			int cont = 0;

			while (rs.next()) {
				cont++;
				String temp = "";
				for (int i = 0; i < arrcampos.size(); i++) {
					if (arrcampos.elementAt(i).contains("@text_")) {
						temp = temp + arrcampos.elementAt(i).substring(7);
					} else {
						temp = temp + rs.getString(arrcampos.elementAt(i)).trim();
					}
				}
				selectAS.add(temp);
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
		return selectAS;

	}

	/*
	public ArrayList<String> getSelectAS(String clave, lotus.domino.Document docProp) throws NotesException {
		Connection connection = null;
		try {
			ODBCProfile docConf = JSFUtil.getDatosConexionOdbc();
			DocProfile docProfile = JSFUtil.getDocProfile(clave);
			docProfile.setStrsSQL(docProp);
			String strSQL = docProfile.getStrsSQL();
			ArrayList<String> selectAS = new ArrayList<String>();
			Vector<String> arrcampos = docProfile.getResultado();
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());

			connection = createConnection(docConf.getUrlConexion(), docConf.getUserRead(), docConf.getPassRead());

			Statement stmt = connection.createStatement();
			if (docProfile.getMsgConsola().equals("2")) {
				LogError log = new LogError("ar.com.hdi.autos", clave + " - sql=" + strSQL);
				log.commitLog(log);
			} else if (docProfile.getMsgConsola().equals("1")) {
				System.out.println(clave + " - sql=" + strSQL);
			}
			ResultSet rs = stmt.executeQuery(strSQL);

			int cont = 0;

			while (rs.next()) {
				cont++;
				String temp = "";
				for (int i = 0; i < arrcampos.size(); i++) {
					if (arrcampos.elementAt(i).contains("@text_")) {
						temp = temp + arrcampos.elementAt(i).substring(7);
					} else {
						if (rs.getString(arrcampos.elementAt(i)) != null) {
							temp = temp + rs.getString(arrcampos.elementAt(i)).trim();
						}

					}
				}
				selectAS.add(temp);
			}
			return selectAS;
		}

		catch (Exception e) {
			System.out.println();
			System.out.println("ERROR: " + e.getMessage());
			return null;
		}

		finally {
			try {
				if (connection != null)
					connection.close();
				// docProp.recycle();
			} catch (SQLException e) {
			}
		}

	}

	*/
	////ACA
	
	
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
