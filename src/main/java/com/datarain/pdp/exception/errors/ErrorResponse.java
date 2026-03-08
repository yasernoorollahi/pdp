package com.datarain.pdp.exception.errors;


import java.time.Instant;


public class ErrorResponse {


    private Instant timestamp;
    private int status;
    private String error; // NOT_FOUND, BAD_REQUEST, ...
    private String code; // ITEM_NOT_FOUND, VALIDATION_ERROR, ...
    private String message;
    private String path;
    private String traceId; // برای آینده (ELK / Zipkin)


    public ErrorResponse() {}


    public ErrorResponse(Instant timestamp, int status, String error,
                         String code, String message, String path, String traceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
    }


// getters & setters


    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }


    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }


    public String getError() { return error; }
    public void setError(String error) { this.error = error; }


    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }


    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }


    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }


    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}