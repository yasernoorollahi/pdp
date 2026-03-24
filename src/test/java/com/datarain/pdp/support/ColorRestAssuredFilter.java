package com.datarain.pdp.support;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.response.Response;

import io.restassured.http.Header;

public class ColorRestAssuredFilter implements Filter {

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext ctx
    ) {
        logRequest(requestSpec);
        Response response = ctx.next(requestSpec, responseSpec);
        logResponse(response);
        return response;
    }

    private void logRequest(FilterableRequestSpecification requestSpec) {
        StringBuilder sb = new StringBuilder();
        sb.append(AnsiColors.CYAN)
                .append("\n=== REQUEST ===\n")
                .append(requestSpec.getMethod())
                .append(" ")
                .append(requestSpec.getURI())
                .append("\n");

        for (Header header : requestSpec.getHeaders()) {
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }

        Object body = extractBody(requestSpec);
        if (body != null) {
            sb.append("\n").append(body).append("\n");
        }

        sb.append(AnsiColors.RESET);
        System.out.println(sb);
    }

    private void logResponse(Response response) {
        String statusColor = statusColor(response.getStatusCode());
        StringBuilder sb = new StringBuilder();
        sb.append(statusColor)
                .append("\n=== RESPONSE ===\n")
                .append("Status: ")
                .append(response.getStatusCode())
                .append("\n");

        response.getHeaders().asList()
                .forEach(h -> sb.append(h.getName()).append(": ").append(h.getValue()).append("\n"));

        String body = response.asString();
        if (!body.isBlank()) {
            sb.append("\n").append(body).append("\n");
        }

        sb.append(AnsiColors.RESET);
        System.out.println(sb);
    }

    private String statusColor(int status) {
        if (status >= 200 && status < 400) {
            return AnsiColors.GREEN;
        }
        if (status >= 400 && status < 500) {
            return AnsiColors.YELLOW;
        }
        return AnsiColors.RED;
    }

    private Object extractBody(FilterableRequestSpecification requestSpec) {
        try {
            return requestSpec.getClass().getMethod("getBody").invoke(requestSpec);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
