package com.perpetumobile.bit.orm.cassandra;

import org.apache.cassandra.cli.CliSessionState;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConnection;
import com.perpetumobile.bit.util.Logger;
import com.perpetumobile.bit.util.Util;


/**
 * 
 * @author  Zoran Dukic
 */
public class CassandraConnection extends RecordConnection<Cassandra.Client> {
	static private Logger logger = new Logger(CassandraConnection.class);
	
	final static public int DB_PORT_DEFAULT = 9160;
	
	//config file keys  
	final static public String DB_HOST_KEY = "Database.Host";
	final static public String DB_PORT_KEY = "Database.Port";
	final static public String DB_KEYSPACE_KEY = "Database.Keyspace";
	
	protected String host = null;
	protected int port = DB_PORT_DEFAULT;
	protected String keyspace = null;
	
	/**
	 * Creates a new instance of CassandraConnection
	 */
	public CassandraConnection(String configName) {
		super(configName);
		host = Config.getInstance().getClassProperty(configName, DB_HOST_KEY, "");
		port = Config.getInstance().getIntClassProperty(configName, DB_PORT_KEY, DB_PORT_DEFAULT);
		keyspace = Config.getInstance().getClassProperty(configName, DB_KEYSPACE_KEY, "");
		if(Util.nullOrEmptyString(user)) {
			user="default";
		}
	}
	
	public void connect() {
		if(connection == null) {
			try {
				TTransport tr = new TFramedTransport(new TSocket(host, port));
				TProtocol proto = new TBinaryProtocol(tr);
				connection = new Cassandra.Client(proto);
				tr.open();
				// connection.set_cql_version("2.0.0");
			} catch (Exception e) {
				logger.error("Cannot open database connection", e);
				connection = null;
			}
		}
	}
	
	/**
	 * Frees up connection resources.
	 * You should call this method before your DB object goes out of scope.
	 * This method explicitly frees up resources rather than waiting for
	 * garbage collection to do so.
	 *
	 * @param con Connection object
	 */
	public void disconnect() {
		try {
			if (connection != null) {
			    connection.getInputProtocol().getTransport().close();
			    connection.getOutputProtocol().getTransport().close();
			}
		} catch(Exception e) {
			logger.error("Cannot close database connection", e);
		}
		connection = null;
	}
	
	public boolean validate() throws Exception {
		// is describe_cluster_name() the cheapest way to validate the connection 
		// connection.describe_cluster_name();
		connection.set_keyspace(keyspace);
		return true;
	}
	
	public String getUrl() {
		StringBuilder buf = new StringBuilder(host);
		buf.append(":");
		buf.append(port);
		buf.append("/");
		return buf.toString();
	}
	
	public String getKeyspace(){
		return keyspace;
	}
	
	public CliSessionState getCliSessionState() {
		CliSessionState result = new CliSessionState();
		result.hostName = host;
		result.thriftPort = port;
		result.username = user;
		result.password = password;
		result.keyspace = keyspace;
		result.batch = true;
		result.schema_mwt = 10 * 1000;
		return result;
	}
}
