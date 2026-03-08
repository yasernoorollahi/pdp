package com.datarain.pdp.exception.errors;


import java.util.List;


public class ValidationErrorResponse {


    private String code; // VALIDATION_ERROR
    private List<FieldValidationError> errors;


    public ValidationErrorResponse() {}


    public ValidationErrorResponse(String code, List<FieldValidationError> errors) {
        this.code = code;
        this.errors = errors;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public List<FieldValidationError> getErrors() {
        return errors;
    }


    public void setErrors(List<FieldValidationError> errors) {
        this.errors = errors;
    }
}
