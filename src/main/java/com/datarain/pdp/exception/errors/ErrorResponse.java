package com.datarain.pdp.exception.errors;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {


    private Instant timestamp;
    private int status;
    private String error; // UNAUTHORIZED, FORBIDDEN, BAD_REQUEST, ...
    private String message;
    private String path;
    private String traceId; // برای آینده (ELK / Zipkin)


    public ErrorResponse() {}


    public ErrorResponse(Instant timestamp, int status, String error,
                         String message, String path, String traceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
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


    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }


    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }


    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
}
