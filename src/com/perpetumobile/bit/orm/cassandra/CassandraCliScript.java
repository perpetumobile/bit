package com.perpetumobile.bit.orm.cassandra;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

// import org.apache.cassandra.cli.CliMain;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.perpetumobile.bit.orm.record.RecordScript;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


public class CassandraCliScript extends RecordScript {
	static private Logger logger = new Logger(CassandraCliScript.class);
	
	protected CassandraConnection cassandraConnection;
	protected CliClient cli = null;

	public CassandraCliScript(CassandraConnection cassanddraConnection, String scriptName)
	throws Exception {
		super(scriptName, "CassandraScript");
		this.cassandraConnection = cassanddraConnection;
		cli = new CliClient(cassandraConnection.getCliSessionState(), cassandraConnection.getConnection());
		cli.setKeySpace(cassandraConnection.getKeyspace());
	}
	
	public CassandraCliScript(CassandraConnection cassanddraConnection, String scriptName, VelocityEngine velocity, VelocityContext context)
	throws Exception {
		super(scriptName, "CassandraScript", velocity, context);
		this.cassandraConnection = cassanddraConnection;
		cli = new CliClient(cassandraConnection.getCliSessionState(), cassandraConnection.getConnection());
		cli.setKeySpace(cassandraConnection.getKeyspace());
	}
	
	public void run(String scriptName) 
	throws Exception {
		try {
			CassandraCliScript CassandraScript = new CassandraCliScript(cassandraConnection, scriptName, velocity, context);
			CassandraScript.execute();
		} catch (Exception e) {
			logger.error("Error running script: " + scriptName);
			logger.error("Cassandra Exception " + e.getLocalizedMessage());
			logger.error("Exception at CassandraScript.run", e);
			throw e;
		}
		// cassanddraConnection doesn't need to be returned/invalidated since it belongs to this
	}
	
	public void execute(String statement, boolean ignoreExceptions) 
	throws Exception {
		try {
			System.out.print(statement);
			System.out.println(";");
			cli.executeCLIStatement(statement);
		} catch (Exception e) {
			if(!ignoreExceptions) {
				throw e;		
			}
		}
	}
	
	public void execute() throws Exception {
		try {
			String all = generate();
			
			String delimiter = (String)context.get("StatementDelimiter");
			if(Util.nullOrEmptyString(delimiter)) {
				delimiter = ";";
			}
			
			// CliMain.sessionState = cassandraConnection.getCliSessionState();
			// CliMain.connect(cassandraConnection.host, cassandraConnection.port);
			
			String[] statements = all.split(delimiter);
			for (String statement : statements) {
				statement = statement.trim();
				if (statement == null || statement.length() == 0)
					continue;

				if(logger.isInfoEnabled()) {
					System.out.print(statement);
					System.out.println(";");
				}
				
				// CliMain.processStatement(statement);
				cli.executeCLIStatement(statement);
				System.out.println();
			}
		} finally {
			CliMain.disconnect();
		}
	}
	
	static public CommandLine processCommandLine(String[] args)
	throws org.apache.commons.cli.ParseException {
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		
		options.addOption("a", "auto", false, "run CassandraScript in auto mode");
		
		return parser.parse(options, args);	
	}

	public static void main(String[] args) {
		String configName = null;
		String script = null;
		CassandraConnection cassanddraConnection = null;
		String keySpace = null;
		
		try {
			CommandLine cmd = processCommandLine(args);
			String[] rArgs = cmd.getArgs();
			if (rArgs.length != 2) {
				System.out.println("CassandraScript <configName> <script>");
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
						
			cassanddraConnection = CassandraConnectionManager.getInstance().getConnection(configName);
			keySpace = cassanddraConnection.getKeyspace();

			if(!cmd.hasOption("a")) {
				System.out.println("About to execute script: " + script);
				System.out.println("Config: " + cassanddraConnection.getConfigName());
				System.out.println("KeySpace: " + cassanddraConnection.getUrl() + keySpace);
				System.out.print("Continue (Y/N) ");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String answer = reader.readLine();
				if (answer == null || !answer.toLowerCase().equals("y")) {
					System.out.println("Stopped!");
					System.exit(1);
				}
			}
			
			CassandraCliScript CassandraScript = new CassandraCliScript(cassanddraConnection, script);
			CassandraScript.execute();
			
		} catch (Exception e) {
			logger.error("Exception running script: " + script);
			logger.error("KeySpace: " + keySpace + " Message: "+ e.getLocalizedMessage());
			CassandraConnectionManager.getInstance().invalidateConnection(cassanddraConnection);
			cassanddraConnection = null;			
		} finally {
			CassandraConnectionManager.getInstance().returnConnection(cassanddraConnection);
		}
	}
}
