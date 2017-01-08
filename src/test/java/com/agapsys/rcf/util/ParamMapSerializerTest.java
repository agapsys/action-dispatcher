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

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse;
import com.agapsys.rcf.ActionRequest;
import com.agapsys.rcf.Controller;
import com.agapsys.rcf.ServletContainerBuilder;
import com.agapsys.rcf.WebAction;
import com.agapsys.rcf.WebController;
import com.agapsys.rcf.exceptions.BadRequestException;
import com.agapsys.rcf.util.ParamMapSerializer.SerializerException;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParamMapSerializerTest {
    // CLASS SCOPE =============================================================
    @BeforeClass
    public static void beforeClass() {
        System.out.println(String.format("=== %s ===", ParamMapSerializerTest.class.getSimpleName()));
    }

    @AfterClass
    public static void afterClass() {
        System.out.println();
    }

    // Classes -----------------------------------------------------------------
    public static class TestDto {
        public String     strField;
        public Boolean    booleanObjectField;
        public boolean    booleanField;
        public Short      shortObjectField;
        public short      shortField;
        public Integer    integerObjectField;
        public int        integerField;
        public Long       longObjectField;
        public long       longField;
        public Float      floatObjectField;
        public float      floatField;
        public Double     doubleObjectField;
        public double     doubleField;
        public Date       dateField;
        public BigDecimal bigDecimalfield;
        public UUID       uuidField;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + Objects.hashCode(this.strField);
            hash = 19 * hash + Objects.hashCode(this.booleanObjectField);
            hash = 19 * hash + (this.booleanField ? 1 : 0);
            hash = 19 * hash + Objects.hashCode(this.shortObjectField);
            hash = 19 * hash + this.shortField;
            hash = 19 * hash + Objects.hashCode(this.integerObjectField);
            hash = 19 * hash + this.integerField;
            hash = 19 * hash + Objects.hashCode(this.longObjectField);
            hash = 19 * hash + (int) (this.longField ^ (this.longField >>> 32));
            hash = 19 * hash + Objects.hashCode(this.floatObjectField);
            hash = 19 * hash + Float.floatToIntBits(this.floatField);
            hash = 19 * hash + Objects.hashCode(this.doubleObjectField);
            hash = 19 * hash + (int) (Double.doubleToLongBits(this.doubleField) ^ (Double.doubleToLongBits(this.doubleField) >>> 32));
            hash = 19 * hash + Objects.hashCode(this.dateField);
            hash = 19 * hash + Objects.hashCode(this.bigDecimalfield);
            hash = 19 * hash + Objects.hashCode(this.uuidField);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TestDto other = (TestDto) obj;
            if (!Objects.equals(this.strField, other.strField)) {
                return false;
            }
            if (!Objects.equals(this.booleanObjectField, other.booleanObjectField)) {
                return false;
            }
            if (this.booleanField != other.booleanField) {
                return false;
            }
            if (!Objects.equals(this.shortObjectField, other.shortObjectField)) {
                return false;
            }
            if (this.shortField != other.shortField) {
                return false;
            }
            if (!Objects.equals(this.integerObjectField, other.integerObjectField)) {
                return false;
            }
            if (this.integerField != other.integerField) {
                return false;
            }
            if (!Objects.equals(this.longObjectField, other.longObjectField)) {
                return false;
            }
            if (this.longField != other.longField) {
                return false;
            }
            if (!Objects.equals(this.floatObjectField, other.floatObjectField)) {
                return false;
            }
            if (Float.floatToIntBits(this.floatField) != Float.floatToIntBits(other.floatField)) {
                return false;
            }
            if (!Objects.equals(this.doubleObjectField, other.doubleObjectField)) {
                return false;
            }
            if (Double.doubleToLongBits(this.doubleField) != Double.doubleToLongBits(other.doubleField)) {
                return false;
            }
            if (!Objects.equals(this.dateField, other.dateField)) {
                return false;
            }
            if (!Objects.equals(this.bigDecimalfield, other.bigDecimalfield)) {
                return false;
            }
            if (!Objects.equals(this.uuidField, other.uuidField)) {
                return false;
            }
            return true;
        }
    }

    private static class UUIDFieldSerializer implements ParamMapSerializer.TypeSerializer<UUID> {

        @Override
        public String toString(UUID srcObject) {

            return String.format("%s|%s", srcObject.getLeastSignificantBits(), srcObject.getMostSignificantBits());
        }

        @Override
        public UUID getObject(String str) {
            String[] tokens = str.split(Pattern.quote("|"));
            return new UUID(Long.parseLong(tokens[1]), Long.parseLong(tokens[0]));
        }

    }

    private static class CustomMapSerializer extends ParamMapSerializer {

        public CustomMapSerializer() {
            super();
            try {
                registerTypeSerializer(UUID.class, new UUIDFieldSerializer());
                registerTypeSerializer(Date.class, new ParamMapSerializer.SimpleDateSerializer());
            } catch (SerializerException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String toString(Object obj) {
            if (obj == null)
                throw new IllegalArgumentException("Null object");

            Map<String, String> map = toParamMap(obj);

            StringBuilder sb = new StringBuilder();

            boolean first = true;
            for (Map.Entry<String, String> entry : map.entrySet()) {

                if (entry.getValue() != null) {

                    if (!first)
                        sb.append("&");

                    sb.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
                    first = false;
                }
            }

            return sb.toString();
        }

        public <T> T getObject(String str, Class<T> targetClass) throws SerializerException {
            Map<String, String> fieldMap = new LinkedHashMap<>();

            String[] tokens = str.split(Pattern.quote("&"));
            for (String token : tokens) {
                token = token.trim();
                if (token.isEmpty())
                    throw new RuntimeException("Invalid string");

                String[] tokenElements = token.split(Pattern.quote("="));
                if (tokenElements.length != 2)
                    throw new RuntimeException("Invalid string");

                fieldMap.put(tokenElements[0].trim(), tokenElements[1].trim());
            }

            return getObject(fieldMap, targetClass);
        }
    }

    private static final CustomMapSerializer DEFAULT_SERIALIZER = new CustomMapSerializer();

    @WebController("test")
    public static class TestController extends Controller {

        @WebAction(mapping = "/get")
        public TestDto onGet(ActionRequest request) throws IOException, BadRequestException {
            try {
                return DEFAULT_SERIALIZER.getObject(request.getParameterMap(), TestDto.class);
            } catch (SerializerException ex) {
                throw new BadRequestException(ex.getMessage());
            }
        }
    }
    // -------------------------------------------------------------------------

    // Utility methods ---------------------------------------------------------
    public static Date getSimpleDate(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return sdf.parse(str);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    // -------------------------------------------------------------------------
    // =========================================================================

    // INSTANCE SCOPE ==========================================================
    @Test
    public void customSerializerTest() throws SerializerException {
        TestDto dto = new TestDto();
        dto.uuidField = new UUID(1, 2);
        String serialized = DEFAULT_SERIALIZER.toString(dto);
        Assert.assertEquals("booleanField=false&shortField=0&integerField=0&longField=0&floatField=0.0&doubleField=0.0&uuidField=2|1", serialized);
        Assert.assertEquals(dto, DEFAULT_SERIALIZER.getObject(serialized, TestDto.class));

        serialized = "dateField=2015-11-28";
        dto = DEFAULT_SERIALIZER.getObject(serialized, TestDto.class);
        Assert.assertEquals(getSimpleDate("2015-11-28"), dto.dateField);
    }

    @Test
    public void stringSerializationTest() throws SerializerException {
        CustomMapSerializer mapSerializer = new CustomMapSerializer();
        TestDto dto = new TestDto();
        dto.strField = "Hello World! áéíóú";
        String serialized = mapSerializer.toString(dto);
        Assert.assertEquals("strField=Hello+World%21+%C3%A1%C3%A9%C3%AD%C3%B3%C3%BA&booleanField=false&shortField=0&integerField=0&longField=0&floatField=0.0&doubleField=0.0", serialized);
        Assert.assertEquals(dto, mapSerializer.getObject(serialized, TestDto.class));
    }

    @Test
    public void testServlet () {

        ServletContainer sc = new ServletContainerBuilder().registerController(TestController.class).setErrorHandler(new StacktraceErrorHandler()).build();

        sc.startServer();

        HttpResponse.StringResponse resp = sc.doRequest(new HttpGet("/test/get?uuidField=%s&dateField=%s&strField=%s", "2|1", "2015-11-28", "Hello+World áéíóú"));
        Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
        Assert.assertEquals("{\"strField\":\"Hello World áéíóú\",\"booleanField\":false,\"shortField\":0,\"integerField\":0,\"longField\":0,\"floatField\":0.0,\"doubleField\":0.0,\"dateField\":\"2015-11-28T00:00:00.000Z\",\"uuidField\":\"00000000-0000-0001-0000-000000000002\"}", resp.getContentString());
        sc.stopServer();
    }
    // =========================================================================
}
