package com.perpetumobile.bit.orm.cassandra;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;

import com.perpetumobile.bit.orm.record.field.ByteBufferField;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;


/**
 * @author Zoran Dukic
 *
 */
public class CassandraColumnCursor {
	
	protected CassandraKey key = null;
	protected ColumnParent columnParent = null;
	protected int fetchSize = 0;
	
	protected ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
	
	protected ByteBuffer startColumn = ByteBuffer.allocate(0);
	
	protected ArrayList<ByteBufferField> results = null;
	protected int position = 0;
	
	public CassandraColumnCursor(CassandraKey key, ColumnParent columnParent, int fetchSize) {
		this.key = key;
		this.columnParent = columnParent;
		this.fetchSize = fetchSize;
	}
	
	public void setConsistencyLevel(ConsistencyLevel level) {
		this.consistencyLevel = level;
	}
	
	private ByteBufferField getResult() {
		if(results != null && results.size() > position) {
			ByteBufferField r = results.get(position++);
			startColumn = r.getByteBufferFieldName();
			return r;
		}
		return null;
	}
	
	protected SlicePredicate getSlicePredicate(boolean isFirst) 
	throws UnsupportedEncodingException {
		SlicePredicate result = new SlicePredicate();
		SliceRange sliceRange = new SliceRange();
		sliceRange.setStart(startColumn);
		sliceRange.setFinish(ByteBuffer.allocate(0));
		sliceRange.setReversed(false);
		if(isFirst) {
			sliceRange.setCount(fetchSize);
		} else {
			sliceRange.setCount(fetchSize+1);
		}
		result.setSlice_range(sliceRange);
		return result;
	}
	
	protected ByteBufferField read(CassandraConnection cassandraConnection, boolean isFirst) throws Exception {
		results = null;
		if(isFirst) {
			position = 0;
		} else {
			position = 1;
		}
		List<ColumnOrSuperColumn> rs = cassandraConnection.getConnection().get_slice(key.getByteBufferFieldValue(), columnParent, getSlicePredicate(isFirst), consistencyLevel);
		if(rs != null && rs.size() > 0) {
			results = new ArrayList<ByteBufferField>();
			for(ColumnOrSuperColumn c : rs) {
				String fieldName = Util.toString(c.column.name, FieldConfig.CHARSET_NAME);
				ByteBufferField f = new ByteBufferField(fieldName);
				f.bind(c.column);
				results.add(f);
			}
		}
		return getResult();
	}
	
	/**
	 * Reads next result. Returns null if there is no next result.
	 */
	public ByteBufferField readNext(CassandraConnection cassandraConnection) throws Exception {
		if(results == null) {
			return read(cassandraConnection, true);
		}
		
		ByteBufferField result = getResult();
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
