package com.school.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class RequestUtil {

    /**
     * 获取请求真实IP地址
     */
    public static String getRequestIp(HttpServletRequest request) {
        //通过HTTP代理服务器转发时添加
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    log.error("請求IP 解析異常");
                }
                if (inetAddress != null) {
                    ipAddress = inetAddress.getHostAddress();
                }
            }
        }

        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }

        if (StringUtils.isBlank(ipAddress)) {
            ipAddress = StringPool.DASH;
        }
        return ipAddress;
    }

    /**
     * request parameter to map
     *
     * @param request http request
     * @return Map
     * @throws Exception exception
     */
    public static Map<String, String> getResMap(HttpServletRequest request) throws Exception {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Map<String, String[]> params = request.getParameterMap();

        // form 交由 Servlet 解碼，不能再把原始 key=value 內容當 JSON 解析。
        if (request.getContentType() != null && request.getContentType().toLowerCase(java.util.Locale.ROOT)
                .startsWith("application/x-www-form-urlencoded")) {
            Map<String, String> form = new LinkedHashMap<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getValue() == null || entry.getValue().length != 1) {
                    throw new IllegalArgumentException("Duplicate or invalid form field: " + entry.getKey());
                }
                form.put(entry.getKey(), entry.getValue()[0]);
            }
            return form;
        }

        StringBuilder requestBodyStream = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBodyStream.append(line);
            }
        }

        Map<String, String> resMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(requestBodyStream.toString())) {
            ObjectMapper mapper = new ObjectMapper();
            resMap = mapper.readValue(requestBodyStream.toString(), new TypeReference<Map<String, String>>() {
            });
        }

        for (Map.Entry<String, String[]> each : params.entrySet()) {
            resMap.put(each.getKey(), each.getValue()[0]);
        }

        return resMap;
    }

    /**
     * request header to map
     *
     * @param request http request
     * @return map
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            headerMap.put(key, value);
        }
        return headerMap;
    }

}
