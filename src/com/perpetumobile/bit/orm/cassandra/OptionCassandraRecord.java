package com.perpetumobile.bit.orm.cassandra;

import com.perpetumobile.bit.util.OptionImpl;


/**
 * @author Zoran Dukic
 *
 */
public class OptionCassandraRecord extends CassandraRecord {
	private static final long serialVersionUID = 1L;

	protected OptionImpl option = null;
	
	public OptionCassandraRecord(String optionValue) {
		this.option = new OptionImpl(optionValue);
	}
	
	public OptionCassandraRecord(String optionValue, String optionText) {
		this.option = new OptionImpl(optionValue, optionText);
	}
		
	public String getOptionValue() {
		return option.getOptionValue();
	}
	
	public String getOptionText() {
		return option.getOptionText();
	}
}
