package com.consorciosenred.ada.connect;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import lotus.domino.NotesException;

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


	public ArrayList<String> getSelectAS(String strSQL, String campo) throws NotesException {
		Connection connection = null;
		String urlConexion =  "jdbc:as400://DOMINO;naming=system;socket timeout=10000;thread used=false";
		String userRead = "DOMINO_UDI";
		String passRead = "A";
		
		ArrayList<String> selectAS = new ArrayList<String>();
		try {
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());

			connection = createConnection(urlConexion, userRead, passRead);

			// connection = DriverManager.getConnection(url, user, pwd);
			Statement stmt = connection.createStatement();

			ResultSet rs = stmt.executeQuery(strSQL);
			// connection.close();
			int cont = 0;

			while (rs.next()) {
				cont++;
				selectAS.add(rs.getString(campo));						
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

	
	public static Connection createConnection(String url, String username, String password) throws SQLException {

		if ((username == null) || (password == null) || (username.trim().length() == 0)
				|| (password.trim().length() == 0)) {
			return DriverManager.getConnection(url);
		} else {
			return DriverManager.getConnection(url, username, password);
		}
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
