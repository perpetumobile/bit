package com.perpetumobile.bit.orm.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.perpetumobile.bit.util.VelocityScript;


/**
 * @author Zoran Dukic
 *
 */
public class DBScriptRunner extends VelocityScript {
	protected boolean isAllYes = false;

	public DBScriptRunner(String scriptName) throws Exception {
		super(scriptName, "DBScriptRunner");
		context.put("DBUtil", new DBUtil());
	}
	
	public DBScriptRunner(String scriptName, boolean isAllYes) throws Exception {
		super(scriptName, "DBScriptRunner");
		context.put("DBUtil", new DBUtil());
		this.isAllYes = isAllYes;
	}

	private boolean doExecute(DBConnection dbConnection, String script) 
	throws IOException {
		boolean result = false;
		System.out.println("");
		System.out.println("About to execute script: " + script);
		System.out.println("Config: " + dbConnection.getConfigName());
		System.out.println("Database: " + dbConnection.getUrl() + dbConnection.getDatabaseName());
		System.out.print("Continue (A/Y/N) ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String answer = reader.readLine();
		if (answer.toLowerCase().equals("y")) {
			result = true;
		} else if (answer.toLowerCase().equals("a")) {
			isAllYes = true;
			result = true;
		}
		return result;
	}

	public void run(String configName, String scriptName) 
	throws SQLException, Exception {
		DBConnection dbConnection = null;
		try {
			dbConnection = DBConnectionManager.getInstance().getConnection(configName);
			if (isAllYes || doExecute(dbConnection, scriptName)) {
				DBScript dbScript = new DBScript(dbConnection, scriptName, velocity, context);
				dbScript.execute();
			} else {
				System.out.println("Stopped!");
				System.exit(1);
			}
		} catch (SQLException e) {
			System.out.println("Error running script: " + scriptName);
			System.out.println("Config: " + configName);
			System.out.println("SQL Exception " + e.getLocalizedMessage());
			System.out.println("SQL Code " + e.getErrorCode());
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
			throw e;
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("DBScriptRunner <script>");
			System.exit(1);
		}

		String scriptName = args[0];
		try {
			DBScriptRunner script = new DBScriptRunner(scriptName);
			script.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
