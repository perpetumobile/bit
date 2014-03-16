package com.perpetumobile.bit.orm.cassandra;

import java.util.ArrayList;

/**
 * @author Zoran Dukic
 *
 */
public class CassandraCursor<T extends CassandraRecord> {
	
	protected CassandraStatement<T> cassandraStatement = null;
	protected int fetchSize = 0;
	
	protected CassandraKey startKey = null;
	
	protected ArrayList<T> results = null;
	protected int position = 0;
	
	public CassandraCursor(CassandraStatement<T> cassandraStatement, int fetchSize) {
		this.cassandraStatement = cassandraStatement;
		this.fetchSize = fetchSize;
	}
	
	private T getResult() {
		if(results != null && results.size() > position) {
			T r = results.get(position++);
			startKey = r.getKey();
			return r;
		}
		return null;
	}
	
	protected T read(CassandraConnection cassandraConnection, boolean isFirst) throws Exception {
		if(isFirst) {
			startKey = CassandraKey.empty();
			cassandraStatement.setLimit(fetchSize);
			position = 0;
		} else {
			cassandraStatement.setLimit(fetchSize+1);
			position = 1;
		}
		cassandraStatement.setStartKey(startKey);
		results = cassandraStatement.readCassandraRecords(cassandraConnection);
		return getResult();
	}
	
	/**
	 * Reads next result. Returns null if there is no next result.
	 */
	public T readNext(CassandraConnection cassandraConnection) throws Exception {
		if(results == null) {
			return read(cassandraConnection, true);
		}
		
		T result = getResult();
		if(result == null) {	
			result = read(cassandraConnection, false);
		}
		return result;
	}
	
	/**
	 * Releses the resources. Must be called after the use.
	 */
	public void close() {
	}
}
