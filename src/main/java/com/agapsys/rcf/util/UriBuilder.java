/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agapsys.rcf.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

class UriBuilder {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    static class QueryParameters extends LinkedHashMap<String, String> {}

    public static QueryParameters getQueryParameters(String queryString) {
        QueryParameters params = new QueryParameters();

        if (queryString == null)
            return params;

        queryString = queryString.trim();
        String paramArray[] = queryString.split(Pattern.quote("&"));
        for (String param : paramArray) {
            String[] pair = param.split(Pattern.quote("="));
            if (pair.length > 2)
                throw new IllegalArgumentException("Invalid query string");

            String key = UriBuilder.decode(pair[0]);
            String value = pair.length == 2 ? UriBuilder.decode(pair[1]) : "";
            params.put(key, value);
        }

        return params;
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    // =========================================================================
    // </editor-fold>

    private final String scheme;
    private final String userInfo;
    private final String host;
    private final int port;
    private final String path;
    private final QueryParameters params;

    public UriBuilder(String baseUri) throws IllegalArgumentException {
        URI uri = URI.create(baseUri);

        scheme = uri.getScheme();
        userInfo = uri.getUserInfo();
        host = uri.getHost();
        port = uri.getPort();
        path = uri.getPath();
        params = getQueryParameters(uri.getQuery());
    }

    public void addParameter(String key, Object value) {
        if (key == null || key.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty key");

        if (value == null)
            value = "";

        if (params.containsKey(key))
            throw new IllegalArgumentException("Duplicate parameter: " + key);

        params.put(encode(key), value != null ? encode(value.toString().trim()) : "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://");

        if (userInfo != null)
            sb.append(userInfo).append("@");

        sb.append(host);

        if (port != -1)
            sb.append(":").append(port);

        sb.append(path);
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i == 0) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            i++;
        }

        return sb.toString();
    }

}