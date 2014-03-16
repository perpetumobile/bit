package com.perpetumobile.bit.servlet.actions.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.perpetumobile.bit.servlet.actions.Validator;
import com.perpetumobile.bit.servlet.actions.ValidatorConfig;
import com.perpetumobile.bit.servlet.tools.ParamTool;


public class RegularExpressionValidator extends Validator {
	
	public RegularExpressionValidator() {
	}
	
	public boolean validate(ValidatorConfig vc) throws Exception {
		ParamTool params = getParamTool();
		String value = params.getString(vc.getFieldName());

		// fieldName must be in the form to be validated
		if (value == null) return true;
		
		String[] args = vc.getArgs();
		
		Pattern pattern = Pattern.compile(args[0]);
		Matcher matcher = pattern.matcher(value);
		
		if (!matcher.find()) {
			addError(vc.getFieldName(), vc.getErrorMessage());
			return false;
		}
		
		return true;
	}
}