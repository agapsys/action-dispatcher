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

package com.agapsys.rcf;

import com.agapsys.rcf.UriBuilder.QueryParameters;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ProxyServlet extends HttpServlet {

	// STATIC SCOPE ============================================================
	public static final int DEFAULT_BUFFER_SIZE = 512;

	private static class Header {

		public final String name;
		public final String value;

		public Header(String name, String value) {
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Null/empty name");
			}

			this.name = name;

			if (value == null) {
				throw new IllegalArgumentException("Null value");
			}

			this.value = value;
		}

		@Override
		public String toString() {
			return String.format("%s: %s", name, value);
		}
	}

	private static List<Header> getHeaders(HttpServletRequest req) {
		List<Header> headerList = new LinkedList<>();

		Enumeration<String> names = req.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			Enumeration<String> values = req.getHeaders(name);
			while (values.hasMoreElements()) {
				headerList.add(new Header(name, values.nextElement()));
			}
		}

		return headerList;
	}

	private static void copy(InputStream is, OutputStream os, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = is.read(buffer)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
	}
	// =========================================================================

	// STATIC SCOPE ============================================================
	/**
	 * Returns the forwarding URI related to given path
	 *
	 * @param path path associated to forwarding URI
	 * @return forwarding URI
	 */
	protected abstract String getForwardUri(String path);

	/**
	 * Returns the size of internal buffer (in bytes) used to fetch data
	 *
	 * @return internal buffer size. Default implementation returns {@linkplain ProxyServlet#DEFAULT_BUFFER_SIZE}.
	 */
	protected int getBufferSize() {
		return DEFAULT_BUFFER_SIZE;
	}

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		QueryParameters queryParams = UriBuilder.getQueryParameters(req.getQueryString());

		UriBuilder uriBuilder = new UriBuilder(getForwardUri(path));
		for (Map.Entry<String, String> entry : queryParams.entrySet()) {
			uriBuilder.addParameter(entry.getKey(), entry.getValue());
		}

		URL url = new URL(uriBuilder.toString());
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(req.getMethod());
		for (Header header : getHeaders(req)) {
			con.setRequestProperty(header.name, header.value);
		}

		int bufferSize = getBufferSize();

		switch (req.getMethod()) {
			case "POST":
			case "PATCH":
			case "PUT":
				InputStream is = req.getInputStream();
				OutputStream os = con.getOutputStream();
				copy(is, os, bufferSize);
				os.flush();
				os.close();
				break;
		}

		resp.setStatus(con.getResponseCode());
		for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			for (String value : values) {
				resp.addHeader(name, value);
			}
		}

		InputStream is = con.getInputStream();
		OutputStream os = resp.getOutputStream();
		copy(is, os, bufferSize);
		os.flush();
		os.close();
	}
	// =========================================================================
}
