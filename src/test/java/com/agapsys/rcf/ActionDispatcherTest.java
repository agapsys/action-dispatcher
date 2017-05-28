/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActionDispatcherTest {

    private class TestAction implements Action {

        private boolean called = false;

        @Override
        public void processRequest(ActionRequest request, ActionResponse response) throws ServletException, IOException {
            called = true;
        }

        public void assertCalled() {
            try {
                Assert.assertTrue(called);
            } catch (RuntimeException ex) {
                called = false;
                throw ex;
            }
        }
    }

    private ActionDispatcher dispatcher;

    @Before
    public void setUp() {
        dispatcher = new ActionDispatcher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPassNullAction() {
        dispatcher.registerAction(HttpMethod.POST, "/test", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPassNullMethod() {
        dispatcher.registerAction(null, "/test", new TestAction());
    }

    @Test
    public void testSameUrlDistinctMethods() {
        TestAction action = new TestAction();

        dispatcher.registerAction(HttpMethod.GET, "/test", action);
        dispatcher.registerAction(HttpMethod.POST, "/test", action);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSameUrlSameMethod() {
        TestAction action = new TestAction();

        dispatcher.registerAction(HttpMethod.GET, "/test", action);
        dispatcher.registerAction(HttpMethod.GET, "/test", action);
    }
    
    private static String __getRelativePath(String parent, String child) {
        try {
            Method method = ActionDispatcher.class.getDeclaredMethod("__getRelativePath", String.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, parent, child);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Test
    public void testRelativePaths() {
        String child;
        String parent;
        
        child = "/foo/path/to/resource";
        parent = "/foo/path";
        Assert.assertEquals("/to/resource", __getRelativePath(parent, child));

        child = "/foo/path";
        parent = "/bar/path";
        Assert.assertEquals("/foo/path", __getRelativePath(parent, child));

        child = "/abc";
        parent = "/";
        Assert.assertEquals("/abc", __getRelativePath(parent, child));

        child = "/";
        parent = "/";
        Assert.assertEquals("/", __getRelativePath(parent, child));

        child = "/abc/";
        parent = "/abc";
        Assert.assertEquals("/", __getRelativePath(parent, child));

        child = "/abc";
        parent = "/abc/";
        Assert.assertEquals("/", __getRelativePath(parent, child));
    }
    // =========================================================================
}
