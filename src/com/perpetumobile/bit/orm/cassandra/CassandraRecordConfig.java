package com.perpetumobile.bit.orm.cassandra;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.velocity.VelocityContext;

import com.perpetumobile.bit.config.Config;
import com.perpetumobile.bit.orm.record.RecordConfig;
import com.perpetumobile.bit.orm.record.field.FieldConfig;
import com.perpetumobile.bit.util.Util;

/**
 * 
 * @author Zoran Dukic
 */
public class CassandraRecordConfig extends RecordConfig {
	
	public static final String READ_ALL_ENABLE_CONFIG_KEY = "CassandraRecord.ReadAll.Enable";
	public static final String READ_ALL_COUNT_MAX_CONFIG_KEY = "CassandraRecord.ReadAll.Count.Max";
	
	public static final int READ_ALL_COUNT_MAX_DEFAULT = 1000;
	
	public static final String COLUMN_FAMILY_CONFIG_KEY = ".CassandraRecord.ColumnFamily";
	public static final String SUPER_COLUMN_CONFIG_KEY = ".CassandraRecord.SuperColumn";
	public static final String IS_SUPER_COLUMN_CONFIG_KEY = ".CassandraRecord.IsSuperColumn";
	
	protected boolean readAll = false;
	protected int readAllCountMax = READ_ALL_COUNT_MAX_DEFAULT;
	
	protected String columnFamily = null;
	protected CassandraKey superColumn = null;
	protected boolean isSuperColumn = false;
	
	public CassandraRecordConfig(String configName)
	throws ClassNotFoundException, UnsupportedEncodingException {
		init(configName, null);
	}
	
	public CassandraRecordConfig(String configName, VelocityContext vc)
	throws ClassNotFoundException, UnsupportedEncodingException {
		init(configName, vc);
	}
	
	protected void init(String configName, VelocityContext vc)
	throws ClassNotFoundException, UnsupportedEncodingException {
		super.init(configName, CassandraRecord.class, vc);
		
		columnFamily = Config.getInstance().getProperty(configName+COLUMN_FAMILY_CONFIG_KEY, "");
		String str = Config.getInstance().getProperty(configName+SUPER_COLUMN_CONFIG_KEY, "");
		if(!Util.nullOrEmptyString(str)) {
			superColumn = CassandraKey.create(str);
			isSuperColumn = true;
		} else {
			isSuperColumn = Config.getInstance().getBooleanProperty(configName+IS_SUPER_COLUMN_CONFIG_KEY, false);
		}
		readAll = Config.getInstance().getBooleanClassProperty(configName, READ_ALL_ENABLE_CONFIG_KEY, false);
		readAllCountMax = Config.getInstance().getIntClassProperty(configName, READ_ALL_COUNT_MAX_CONFIG_KEY, READ_ALL_COUNT_MAX_DEFAULT);
	}
	
	protected RecordConfig getRecordConfig(String configName, VelocityContext vc) {
		return CassandraRecordConfigFactory.getInstance().getRecordConfig(configName, vc);
	}
	
	public boolean isReadAll() {
		return readAll;
	}
	
	public String getColumnFamily() {
		return columnFamily;
	}

	public CassandraKey getSuperColumn() {
		return superColumn;
	}
	
	public boolean isSuperColumn() {
		return isSuperColumn;
	}

	public SlicePredicate getSlicePredicate(CassandraKey superColumn) 
	throws UnsupportedEncodingException {
		SlicePredicate result = new SlicePredicate();
		if(fields.size() > 0 && (superColumn != null || !isSuperColumn)) {
			for(FieldConfig field : fields) {
				result.addToColumn_names(field.getByteBufferFieldName());
			}
		} else if(isReadAll()) {
			SliceRange sliceRange = new SliceRange();
			ByteBuffer empty = ByteBuffer.allocate(0);
			sliceRange.setStart(empty);
			sliceRange.setFinish(empty);
			sliceRange.setReversed(false);
			sliceRange.setCount(readAllCountMax);
			result.setSlice_range(sliceRange);
		}
		return result;
	}
}
