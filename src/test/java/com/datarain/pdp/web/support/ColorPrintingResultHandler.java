package com.datarain.pdp.web.support;

import com.datarain.pdp.support.AnsiColors;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

public class ColorPrintingResultHandler implements ResultHandler {

    @Override
    public void handle(MvcResult result) throws Exception {
        MockHttpServletRequest request = result.getRequest();

        String requestColor = AnsiColors.CYAN;
        String statusColor = statusColor(result.getResponse().getStatus());
        String responseColor = statusColor;

        StringBuilder sb = new StringBuilder();
        sb.append(requestColor)
                .append("\n=== REQUEST ===\n")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURI());

        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }

        sb.append("\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name).append(": ").append(request.getHeader(name)).append("\n");
        }

        byte[] content = request.getContentAsByteArray();
        if (content != null && content.length > 0) {
            String requestBody = request.getCharacterEncoding() == null
                    ? new String(content, StandardCharsets.UTF_8)
                    : new String(content, request.getCharacterEncoding());
            if (!requestBody.isBlank()) {
                sb.append("\n").append(requestBody).append("\n");
            }
        }

        sb.append(AnsiColors.RESET)
                .append(responseColor)
                .append("\n=== RESPONSE ===\n")
                .append("Status: ")
                .append(result.getResponse().getStatus())
                .append("\n");

        result.getResponse().getHeaderNames()
                .forEach(name -> sb.append(name).append(": ")
                        .append(result.getResponse().getHeader(name))
                        .append("\n"));

        String responseBody = result.getResponse().getContentAsString();
        if (!responseBody.isBlank()) {
            sb.append("\n").append(responseBody).append("\n");
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
}
