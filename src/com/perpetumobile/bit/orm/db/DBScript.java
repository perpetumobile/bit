package com.perpetumobile.bit.orm.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.perpetumobile.bit.orm.record.RecordScript;
import com.perpetumobile.bit.orm.record.RecordScriptData;
import com.perpetumobile.bit.orm.record.exception.RecordScriptDataException;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


public class DBScript extends RecordScript {
	static private Logger logger = new Logger(DBScript.class);
	
	protected DBConnection dbConnection;
	protected Statement stmt;

	public DBScript(DBConnection dbConnection, String scriptName)
	throws Exception {
		super(scriptName, "DBScript");
		this.dbConnection = dbConnection;
	}
	
	public DBScript(DBConnection dbConnection, String scriptName, VelocityEngine velocity, VelocityContext context)
	throws Exception {
		super(scriptName, "DBScript", velocity, context);
		this.dbConnection = dbConnection;
	}
	
	public DBConnection getDBConnection() {
		return dbConnection;
	}
	
	public void load(String directoryName, String fileName, String dbRecordConfigName, int batchSize)
	throws IOException, RecordScriptDataException, SQLException {
		DBStatement<DBRecord> stmt = createDBStatement(dbRecordConfigName);
		ArrayList<DBRecord> dbRecords = new ArrayList<DBRecord>();
		
		System.out.println("Load File: " + directoryName + fileName);
		BufferedReader in = new BufferedReader(new FileReader(new File(directoryName, fileName)));
		String[] columnNames = null;
		String line = null;
		while((line = in.readLine()) != null) {
			line = line.trim();
			if(!Util.nullOrEmptyString(line)) {
				if(columnNames == null) {
					columnNames = line.split("\t");
				} else {
					RecordScriptData rsd = new RecordScriptData(columnNames, line.split("\t"));
					DBRecord rec = new DBRecord(dbRecordConfigName);
					ArrayList<Field> fields = rec.getFields();
					for(Field f : fields) {
						String val = rsd.get(f.getFieldName());
						// all fields needs to be included for batch insert to work
						if(Util.nullOrEmptyString(val)) {	
							f.setFieldValue("");
						} else {
							f.setFieldValue(val);
						}
					}
					dbRecords.add(rec);
				}
			}
			
			if(dbRecords.size() >= batchSize) {
				stmt.insertDBRecords(dbConnection, dbRecords);
				dbRecords.clear();
			}
		}
		
		if(dbRecords.size() > 0) {
			stmt.insertDBRecords(dbConnection, dbRecords);
		}
		in.close();
	}	
	
	public void run(String scriptName) 
	throws SQLException, Exception {
		try {
			DBScript dbScript = new DBScript(dbConnection, scriptName, velocity, context);
			dbScript.execute();
		} catch (SQLException e) {
			logger.error("Error running script: " + scriptName);
			logger.error("SQL Exception " + e.getLocalizedMessage());
			logger.error("SQL Code " + e.getErrorCode());
			throw e;
		}
		// dbConnection doesn't need to be returned/invalidated since it belongs to this
	}
	
	public void execute() throws Exception {
		try {
			String allSql = generate();
			stmt = dbConnection.getConnection().createStatement();
			
			String delimiter = (String)context.get("StatementDelimiter");
			if(Util.nullOrEmptyString(delimiter)) {
				delimiter = ";";
			}
			
			String[] sqls = allSql.split(delimiter);
			for (String sql : sqls) {
				sql = sql.trim();
				if (sql == null || sql.length() == 0)
					continue;

				if(logger.isInfoEnabled()) {
					System.out.print(sql);
					System.out.println(";");
					System.out.println();
				}
				
				stmt.execute(sql);
			}
		} finally {
			DBUtil.close(stmt);
		}
	}
	
	static public CommandLine processCommandLine(String[] args)
	throws org.apache.commons.cli.ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		
		options.addOption("a", "auto", false, "run DBScript in auto mode");
		
		return parser.parse(options, args);	
	}

	public static void main(String[] args) {
		String configName = null;
		String script = null;
		DBConnection dbConnection = null;
		String dbName = null;
		
		long start = System.currentTimeMillis();
		
		try {
			CommandLine cmd = processCommandLine(args);
			String[] rArgs = cmd.getArgs();
			if (rArgs.length != 2) {
				System.out.println("DBScript <configName> <script>");
				System.exit(1);
			}
			
			configName = rArgs[0];
			script = rArgs[1];

			// check to see that the file exists
			File scriptFile = new File(script);
			if (!scriptFile.exists()) {
				System.out.println("Script file " + script + " doesn't exist");
				System.exit(1);
			}
						
			dbConnection = DBConnectionManager.getInstance().getConnection(configName);
			dbName = dbConnection.getSchema();

			if(!cmd.hasOption("a")) {
				System.out.println("About to execute script: " + script);
				System.out.println("Config: " + dbConnection.getConfigName());
				System.out.println("Database: " + dbConnection.getUrl() + dbConnection.getSchema());
				System.out.print("Continue (Y/N) ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String answer = reader.readLine();
				if (answer == null || !answer.toLowerCase().equals("y")) {
					System.out.println("Stopped!");
					System.exit(1);
				}
			}
			
			DBScript dbScript = new DBScript(dbConnection, script);
			dbScript.execute();
			
		} catch (SQLException e) {
			logger.error("Exception running script: " + script);
			logger.error("Database: " + dbName);
			logger.error("SQL Exception " + e.getLocalizedMessage());
			logger.error("SQL Code " + e.getErrorCode());
			DBConnectionManager.getInstance().invalidateConnection(dbConnection);
			dbConnection = null;
		} catch (Exception e) {
			logger.error("Exception at DBScript.main", e);
		} finally {
			DBConnectionManager.getInstance().returnConnection(dbConnection);
		}
		
		long elapsedMins = (System.currentTimeMillis() - start) / 1000 / 60;
		System.out.println("Elapsed Time: " + elapsedMins + " mins");
	}
}
