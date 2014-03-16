package com.perpetumobile.bit.servlet.actions.validators;

import com.perpetumobile.bit.servlet.actions.ValidatorConfig;

public class EmailAddressValidator extends RegularExpressionValidator {
	
	public EmailAddressValidator() {
	}
	
	public boolean validate(ValidatorConfig vc) throws Exception {
		String[] args = { ".+@.+\\.[a-z]+" }; 
		vc.setArgs(args);
		return super.validate(vc);
	}
}