package com.perpetumobile.bit.orm.cassandra;
	
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.Deletion;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.thrift.Mutation;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.IndexClause;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SuperColumn;

import com.perpetumobile.bit.orm.cassandra.exception.CassandraUnsupportedOperationException;
import com.perpetumobile.bit.orm.record.StatementLog;
import com.perpetumobile.bit.orm.record.StatementLogger;
import com.perpetumobile.bit.orm.record.field.ByteBufferField;
import com.perpetumobile.bit.orm.record.field.Field;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
public class CassandraStatement<T extends CassandraRecord> {
	
	protected CassandraRecordConfig cassandraRecordConfig = null;
	
	protected ConsistencyLevel readConsistencyLevel = ConsistencyLevel.ONE;
	protected ConsistencyLevel writeConsistencyLevel = ConsistencyLevel.ONE;
	
	protected CassandraKey startKey = CassandraKey.empty();
	protected CassandraKey endKey = CassandraKey.empty();
	
	protected IndexClause indexClause = null;
	protected CassandraKey superColumn = null;
	
	protected int limit = 0;
	
	protected StatementLogger stmtLogger = null;
	
	public CassandraStatement(String configName) {
		cassandraRecordConfig = CassandraRecordConfigFactory.getInstance().getRecordConfig(configName);
	}
	
	public CassandraStatement(String configName, StatementLogger stmtLogger) {
		cassandraRecordConfig = CassandraRecordConfigFactory.getInstance().getRecordConfig(configName);
		this.stmtLogger = stmtLogger;
	}
	
	public void reset() {
		readConsistencyLevel = ConsistencyLevel.ONE;
		writeConsistencyLevel = ConsistencyLevel.ONE;
		startKey = CassandraKey.empty();
		endKey = CassandraKey.empty();
		indexClause = null;
		superColumn = null;
		limit = 0;
	}
	
	@SuppressWarnings("unchecked")
	public T createCassandraRecord() throws Exception {
		return (cassandraRecordConfig != null ? (T)cassandraRecordConfig.createRecord() : null);
	}
	
	public String getConfigName() {
		return (cassandraRecordConfig != null ? cassandraRecordConfig.getConfigName() : null);
	}
	
	public String getColumnFamily() {
		return (cassandraRecordConfig != null ? cassandraRecordConfig.getColumnFamily() : null);
	}
	
	public CassandraKey getSuperColumn(T rec) {
		if(rec != null && rec.getSuperColumn() != null) {
			return rec.getSuperColumn();
		}
		if(superColumn != null) {
			return superColumn;
		}
		return (cassandraRecordConfig != null ? cassandraRecordConfig.getSuperColumn() : null);
	}
	
	public CassandraKey getSuperColumn() {
		return getSuperColumn(null);	
	}
	
	public boolean isSuperColumn() {
		return (cassandraRecordConfig != null ? cassandraRecordConfig.isSuperColumn() : false);
	}
	
	public ColumnParent getColumnParent() {
		ColumnParent cp = new ColumnParent(getColumnFamily());
		CassandraKey ck = getSuperColumn();
		if(ck !=  null) {
			cp.setSuper_column(Util.toBytes(ck.getByteBufferFieldValue()));
		}
		return cp;
	}
	
	public SlicePredicate getSlicePredicate() throws UnsupportedEncodingException {		
		return (cassandraRecordConfig != null ? cassandraRecordConfig.getSlicePredicate(getSuperColumn()) : null);
	}
		
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setReadConsistencyLevel(ConsistencyLevel level) {
		this.readConsistencyLevel = level;
	}
	
	public void setWriteConsistencyLevel(ConsistencyLevel level) {
		this.writeConsistencyLevel = level;
	}
	
	public void setStartKey(CassandraKey key) {
		startKey = key;
	}
	
	public void setEndKey(CassandraKey key) {
		endKey = key;
	}
	
	public void addIndexExpression(IndexExpression ie) {
		if(indexClause == null) {
			indexClause = new IndexClause();
		}
		indexClause.addToExpressions(ie);
	}
	
	public void addIndexExpression(String columnName, IndexOperator io, String value) {
		ByteBufferField field = new ByteBufferField(columnName);
		field.setFieldValue(value);
		addIndexExpression(new IndexExpression(field.getByteBufferFieldName(), io, field.getByteBufferFieldValue()));	
	}
	
	public void addIndexExpression(String columnName, IndexOperator io, int value) {
		ByteBufferField field = new ByteBufferField(columnName);
		field.setIntFieldValue(value);
		addIndexExpression(new IndexExpression(field.getByteBufferFieldName(), io, field.getByteBufferFieldValue()));	
	}
	
	public void addIndexExpression(String columnName, IndexOperator io, long value) {
		ByteBufferField field = new ByteBufferField(columnName);
		field.setLongFieldValue(value);
		addIndexExpression(new IndexExpression(field.getByteBufferFieldName(), io, field.getByteBufferFieldValue()));	
	}
	
	public void addIndexExpression(String columnName, IndexOperator io, float value) {
		ByteBufferField field = new ByteBufferField(columnName);
		field.setFloatFieldValue(value);
		addIndexExpression(new IndexExpression(field.getByteBufferFieldName(), io, field.getByteBufferFieldValue()));	
	}
	
	public void addIndexExpression(String columnName, IndexOperator io, double value) {
		ByteBufferField field = new ByteBufferField(columnName);
		field.setDoubleFieldValue(value);
		addIndexExpression(new IndexExpression(field.getByteBufferFieldName(), io, field.getByteBufferFieldValue()));	
	}
	
	public void setSuperColumn(CassandraKey superColumn) {
		this.superColumn = superColumn;
	}
	
	protected T readCassandraRecord(CassandraConnection cassandraConnection, CassandraKey key, List<ColumnOrSuperColumn> rs)
	throws Exception {
		T result = null;
		if(rs != null && rs.size() > 0) {
			result = createCassandraRecord();
			result.readRecord(cassandraConnection, getSuperColumn(), key, rs, 1);
		}
		return result;
	}
	
	protected T readCassandraRecord(CassandraConnection cassandraConnection, KeySlice keySlice)
	throws Exception {
		return readCassandraRecord(cassandraConnection, CassandraKey.create(keySlice.key), keySlice.getColumns());
	}
	
	/**
	 * Calls get_slice.
	 */
	public T readCassandraRecord(CassandraConnection cassandraConnection, CassandraKey key) 
	throws Exception {	
		List<ColumnOrSuperColumn> rs = cassandraConnection.getConnection().get_slice(key.getByteBufferFieldValue(), getColumnParent(), getSlicePredicate(), readConsistencyLevel);
		return readCassandraRecord(cassandraConnection, key, rs);
	}
	
	/**
	 * Calls get_count.
	 */
	public int getCount(CassandraConnection cassandraConnection, CassandraKey key) 
	throws Exception {	
		return cassandraConnection.getConnection().get_count(key.getByteBufferFieldValue(), getColumnParent(), getSlicePredicate(), readConsistencyLevel);
	}
	
	/**
	 * Calls multiget_slice.
	 */
	protected Set<Entry<ByteBuffer, List<ColumnOrSuperColumn>>> multigetSliceSet(CassandraConnection cassandraConnection, ArrayList<CassandraKey> keys) 
	throws Exception {
		if(keys != null && keys.size() > 0) { 
			ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
			for(CassandraKey k : keys) {
				list.add(k.getByteBufferFieldValue());
			}
			Map<ByteBuffer, List<ColumnOrSuperColumn>> map = cassandraConnection.getConnection().multiget_slice(list, getColumnParent(), getSlicePredicate(), readConsistencyLevel);
			if(map != null && map.size() > 0) {
				return map.entrySet();
			}
		}
		return null;
	}
	
	/**
	 * Calls multiget_slice.
	 */
	public ArrayList<T> readCassandraRecords(CassandraConnection cassandraConnection, ArrayList<CassandraKey> keys) 
	throws Exception {
		ArrayList<T> result = null;	
		Set<Entry<ByteBuffer, List<ColumnOrSuperColumn>>> set = multigetSliceSet(cassandraConnection, keys);
		if(set != null) {
			result = new ArrayList<T>();
			for(Entry<ByteBuffer, List<ColumnOrSuperColumn>> e : set) {
				T r = readCassandraRecord(cassandraConnection, CassandraKey.create(e.getKey()), e.getValue());
				if(r != null) {
					result.add(r);
				}
			}
		}
		return result;
	}
	
	/**
	 * Calls multiget_slice.
	 */
	public HashMap<ByteBuffer, T> readCassandraRecordMap(CassandraConnection cassandraConnection, ArrayList<CassandraKey> keys) 
	throws Exception {
		HashMap<ByteBuffer, T> result = null;			
		Set<Entry<ByteBuffer, List<ColumnOrSuperColumn>>> set = multigetSliceSet(cassandraConnection, keys);
		if(set != null) {
			result = new HashMap<ByteBuffer, T>();
			for(Entry<ByteBuffer, List<ColumnOrSuperColumn>> e : set) {
				T r = readCassandraRecord(cassandraConnection, CassandraKey.create(e.getKey()), e.getValue());
				if(r != null) {
					result.put(e.getKey(), r);
				}
			}
		}
		return result;
	}
	
	/**
	 * Calls multiget_count.
	 */
	public Map<ByteBuffer, Integer> multigetCount(CassandraConnection cassandraConnection, ArrayList<CassandraKey> keys) 
	throws Exception {	
		if(keys != null && keys.size() > 0) { 
			ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
			for(CassandraKey k : keys) {
				list.add(k.getByteBufferFieldValue());
			}
			return cassandraConnection.getConnection().multiget_count(list, getColumnParent(), getSlicePredicate(), readConsistencyLevel);
		}
		return null;
	}
	
	/**
	 * Calls get_indexed_slices if indexClause is set
	 * Calls get_range_slices otherwise
	 */
	protected List<KeySlice> getSlices(CassandraConnection cassandraConnection) 
	throws Exception {
		if(indexClause != null) {
			if(limit > 0) {
				indexClause.setCount(limit);
			}
			if(startKey != null) {
				indexClause.setStart_key(startKey.getByteBufferFieldValue());
			}
			return cassandraConnection.getConnection().get_indexed_slices(getColumnParent(), indexClause, getSlicePredicate(), readConsistencyLevel);
		}
		
		KeyRange keyRange = new KeyRange();
		if(limit > 0) {
			keyRange.setCount(limit);
		}
		keyRange.setStart_key(startKey.getByteBufferFieldValue());
		keyRange.setEnd_key(endKey.getByteBufferFieldValue());
		return cassandraConnection.getConnection().get_range_slices(getColumnParent(), getSlicePredicate(), keyRange, readConsistencyLevel);
	}
	
	/**
	 * Calls get_indexed_slices if indexClause is set
	 * Calls get_range_slices otherwise
	 */
	public ArrayList<T> readCassandraRecords(CassandraConnection cassandraConnection) 
	throws Exception {
		ArrayList<T> result = null;
		List<KeySlice> rs = getSlices(cassandraConnection);
		if(rs != null && rs.size() > 0) {
			result = new ArrayList<T>();
			for(KeySlice k : rs) {
				T r = readCassandraRecord(cassandraConnection, k);
				if(r != null) {
					result.add(r);
				}
			}
		}
		return result;
	}
	
	/**
	 * Calls get_indexed_slices if indexClause is set
	 * Calls get_range_slices otherwise
	 */
	public HashMap<ByteBuffer, T> readCassandraRecordMap(CassandraConnection cassandraConnection) 
	throws Exception {
		HashMap<ByteBuffer, T> result = null;
		List<KeySlice> rs = getSlices(cassandraConnection);
		if(rs != null && rs.size() > 0) {
			result = new HashMap<ByteBuffer, T>();
			for(KeySlice k : rs) {
				T r = readCassandraRecord(cassandraConnection, k);
				if(r != null) {
					result.put(k.key, r);
				}
			}
		}
		return result;
	}
	
	/**
	 * Calls remove
	 * @deprecated use {@link #batchDeleteCassandraRecord(CassandraConnection, T)}
	 */
	@Deprecated public int deleteCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		if(rec.isSuperColumnRecordList()) {
			throw new CassandraUnsupportedOperationException();
		}
		
		ArrayList<Field> fields = rec.getFields();
		for(Field f : fields)  {
			if(f.isSet()) {
				ColumnPath cp = new ColumnPath();
				cp.setColumn_family(getColumnFamily());
				CassandraKey ck = getSuperColumn(rec);
				if(ck != null) {
					cp.setSuper_column(Util.toBytes(ck.getByteBufferFieldValue()));
				}
				cp.setColumn(f.getByteBufferFieldName());
				cassandraConnection.getConnection().remove(rec.getByteBufferKey(), cp, f.getTimestamp(), writeConsistencyLevel);
			}
		}
		return 0;
	}
	
	/**
	 * Calls remove
	 */
	public int deleteCassandraKey(CassandraConnection cassandraConnection, CassandraKey key) throws Exception {
		ColumnPath cp = new ColumnPath();
		cp.setColumn_family(getColumnFamily());
		cassandraConnection.getConnection().remove(key.getByteBufferFieldValue(), cp, Util.currentTimeMicros(), writeConsistencyLevel);
		return 0;
	}
	
	/**
	 * Calls remove
	 * @deprecated use {@link #batchDeleteCassandraRecords(CassandraConnection, ArrayList)}
	 */
	@Deprecated public int deleteCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		for(T rec : recs) {
			deleteCassandraRecord(cassandraConnection, rec);
		}
		return 0;
	}
	
	/**
	 * Calls remove
	 */
	public int deleteCassandraKeys(CassandraConnection cassandraConnection, ArrayList<CassandraKey> keys) throws Exception {
		for(CassandraKey key : keys) {
			deleteCassandraKey(cassandraConnection, key);
		}
		return 0;
	}
	
	protected ArrayList<Mutation> createDeletionList(T rec) throws Exception {
		if(rec.isSuperColumnRecordList()) {
			throw new CassandraUnsupportedOperationException();
		}
		
		ArrayList<Mutation> result = new ArrayList<Mutation>();
		ArrayList<Field> fields = rec.getFields();
		for(Field f : fields)  {
			if(f.isSet()) {
				Deletion del = new Deletion();
				CassandraKey ck = getSuperColumn(rec);
				if(ck != null) {
					del.setSuper_column(Util.toBytes(ck.getByteBufferFieldValue()));
				}
				SlicePredicate sp = new SlicePredicate();
				sp.addToColumn_names(f.getByteBufferFieldName());
				del.setPredicate(sp);
				del.setTimestamp(f.getTimestamp());
				Mutation m = new Mutation();
				m.setDeletion(del);
				result.add(m);
			}
		}
		return result;
	}
	
	/**
	 * Calls batch_mutate
	 */
	public int batchDeleteCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		HashMap<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
		HashMap<String, List<Mutation>> keyMutationMap = new HashMap<String, List<Mutation>>();
		keyMutationMap.put(getColumnFamily(), createDeletionList(rec));
		mutationMap.put(rec.getByteBufferKey(), keyMutationMap);
		cassandraConnection.getConnection().batch_mutate(mutationMap, writeConsistencyLevel);
		return 0;
	}
	
	/**
	 * Calls batch_mutate
	 */
	public int batchDeleteCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		HashMap<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
		for(T rec : recs) {
			HashMap<String, List<Mutation>> keyMutationMap = new HashMap<String, List<Mutation>>();
			keyMutationMap.put(getColumnFamily(), createDeletionList(rec));
			mutationMap.put(rec.getByteBufferKey(), keyMutationMap);
		}
		cassandraConnection.getConnection().batch_mutate(mutationMap, writeConsistencyLevel);
		return 0;
	}
	
	/**
	 * Calls insert
	 * @deprecated {@link #batchInsertCassandraRecord(CassandraConnection, CassandraRecord)}
	 */
	@Deprecated public int insertCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		if(rec.isSuperColumnRecordList()) {
			throw new CassandraUnsupportedOperationException();
		}
		
		ArrayList<Field> fields = rec.getFields();
		for(Field f : fields)  {
			if(f.isSet()) {
				ColumnParent cp = new ColumnParent(getColumnFamily());
				CassandraKey ck = getSuperColumn(rec);
				if(ck != null) {
					cp.setSuper_column(Util.toBytes(ck.getByteBufferFieldValue()));
				}
				Column c = new Column();
				c.setName(f.getByteBufferFieldName());
				c.setValue(f.getByteBufferFieldValue());
				c.setTimestamp(f.getTimestamp());
				cassandraConnection.getConnection().insert(rec.getByteBufferKey(), cp, c, writeConsistencyLevel);
			}
		}
		return 0;
	}
	
	/**
	 * Calls insert
	 * @deprecated {@link #batchInsertCassandraRecords(CassandraConnection, ArrayList)
	 */
	@Deprecated public int insertCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		for(T rec : recs)  {
			insertCassandraRecord(cassandraConnection, rec);
		}
		return 0;
	}
	
	protected ArrayList<Mutation> createMutationList(T rec, long timestamp) throws Exception {
		if(rec.isSuperColumnRecordList()) {
			throw new CassandraUnsupportedOperationException();
		}
		
		ArrayList<Mutation> result = new ArrayList<Mutation>();
		
		CassandraKey ck = getSuperColumn(rec);
		if(ck !=  null) {
			SuperColumn sc = new SuperColumn();
			sc.setName(Util.toBytes(ck.getByteBufferFieldValue()));
			ArrayList<Field> fields  =  rec.getFields();
			for(Field f : fields)  {
				if(f.isSet()) {
					Column c = new Column();
					c.setName(f.getByteBufferFieldName());
					c.setValue(f.getByteBufferFieldValue());
					c.setTimestamp(timestamp);
					sc.addToColumns(c);
				}
			}
			ColumnOrSuperColumn mRec = new ColumnOrSuperColumn();
			mRec.setSuper_column(sc);
			Mutation m = new Mutation();
			m.setColumn_or_supercolumn(mRec);
			result.add(m);
		} else {
			ArrayList<Field> fields  =  rec.getFields();
			for(Field f : fields)  {
				if(f.isSet()) {
					Column c = new Column();
					c.setName(f.getByteBufferFieldName());
					c.setValue(f.getByteBufferFieldValue());
					c.setTimestamp(timestamp);
					ColumnOrSuperColumn mRec = new ColumnOrSuperColumn();
					mRec.setColumn(c);
					Mutation m = new Mutation();
					m.setColumn_or_supercolumn(mRec);
					result.add(m);
				}
			}
		}
		return result;
	}
	
	/**
	 * Calls batch_mutate
	 */
	public int batchInsertCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		HashMap<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
		HashMap<String, List<Mutation>> keyMutationMap = new HashMap<String, List<Mutation>>();
		long timestamp = Util.currentTimeMicros();
		keyMutationMap.put(getColumnFamily(), createMutationList(rec, timestamp));
		mutationMap.put(rec.getByteBufferKey(), keyMutationMap);
		cassandraConnection.getConnection().batch_mutate(mutationMap, writeConsistencyLevel);
		return 0;
	}

	/**
	 * Calls batch_mutate
	 */
	public int batchInsertCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		HashMap<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
		long timestamp = Util.currentTimeMicros();
		for(T rec : recs) {
			HashMap<String, List<Mutation>> keyMutationMap = new HashMap<String, List<Mutation>>();
			keyMutationMap.put(getColumnFamily(), createMutationList(rec, timestamp));
			mutationMap.put(rec.getByteBufferKey(), keyMutationMap);
		}
		cassandraConnection.getConnection().batch_mutate(mutationMap, writeConsistencyLevel);
		return 0;
	}
	
	/**
	 * Deletes existing record for a rec.getCassandraKey()
	 * Calls insertCassandraRecord(cassandraConnection, rec)
	 * @deprecated use {@link #batchCleanInsertCassandraRecord(CassandraConnection, CassandraRecord)}
	 */
	@Deprecated public int cleanInsertCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		deleteCassandraKey(cassandraConnection, rec.getKey());
		return insertCassandraRecord(cassandraConnection, rec);
	}
	
	/**
	 * Deletes existing records for a rec.getCassandraKey()
	 * Calls insertCassandraRecords(cassandraConnection, recs)
	 * @deprecated {@link #batchCleanInsertCassandraRecords(CassandraConnection, ArrayList)}
	 */
	@Deprecated public int cleanInsertCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		for(T r : recs) {
			deleteCassandraKey(cassandraConnection, r.getKey());
		}
		return insertCassandraRecords(cassandraConnection, recs);
	}
	
	/**
	 * Batch deletes existing record for a rec.getCassandraKey()
	 * Calls batchInsertCassandraRecord(cassandraConnection, rec)
	 */
	public int batchCleanInsertCassandraRecord(CassandraConnection cassandraConnection, T rec) throws Exception {
		deleteCassandraKey(cassandraConnection, rec.getKey());
		return batchInsertCassandraRecord(cassandraConnection, rec);
	}
	
	/**
	 * Batch deletes existing records for a rec.getCassandraKey()
	 * Calls batchInsertCassandraRecords(cassandraConnection, recs)
	 */
	public int batchCleanInsertCassandraRecords(CassandraConnection cassandraConnection, ArrayList<T> recs) throws Exception {
		for(T r : recs) {
			deleteCassandraKey(cassandraConnection, r.getKey());
		}
		return batchInsertCassandraRecords(cassandraConnection, recs);
	}

	
	public int startStatement(String stmt) {
		StatementLog stmtLog = new StatementLog(stmt);
		if (stmtLogger != null) {
			return stmtLogger.startStatement(stmtLog);
		}
		return -1;
	}

	public void endStatement(int index) {
		if (stmtLogger != null) {
			stmtLogger.endStatement(index);
		}
	}
}
