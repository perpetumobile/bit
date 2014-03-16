package com.perpetumobile.bit.servlet.actions.validators;

import com.perpetumobile.bit.servlet.actions.Validator;
import com.perpetumobile.bit.servlet.actions.ValidatorConfig;
import com.perpetumobile.bit.servlet.tools.ParamTool;
import com.perpetumobile.bit.util.Util;

public class StringLengthValidator extends Validator {
	
	public StringLengthValidator() {
	}
	
	public boolean validate(ValidatorConfig vc) throws Exception {
		ParamTool params = getParamTool();
		String value = params.getString(vc.getFieldName());

		// fieldName must be in the form to be validated
		if (value == null) return true;
		
		String[] args = vc.getArgs();
		if(!(value.length() >= Util.toInt(args[0].trim()) && value.length() <= Util.toInt(args[1].trim()))){
			addError(vc.getFieldName(), vc.getErrorMessage());
			return false;
		}
		return true;
	}
}