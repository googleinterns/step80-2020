// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;
import com.google.sps.servlets.UserServlet;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import com.google.common.collect.ImmutableMap;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/** Tests for UserServlet */
// maybe move user tests to ProfileServletTest to allow profile to be created before IsLoggedInHasProfile() runs
// to remove dependency on having "mvn test" run at least once before test passes
@RunWith(MockitoJUnitRunner.class)
public final class UserServletTest {
  // helper to mimic user authentication
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvAuthDomain("gmail.com")
      .setEnvEmail("test@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
  
  @Mock private UserServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws ServletException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servlet = new UserServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void IsNotLoggedIn() throws IOException, ServletException, ParseException {
    // User is not logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertFalse((boolean) json.get("isLoggedIn"));
  }
  
  @Test
  public void IsLoggedInHasProfile() throws IOException, ServletException, ParseException {
    // User is logged in and has profile
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue((boolean) json.get("isLoggedIn"));
    assertEquals("test@gmail.com", json.get("email"));
    assertTrue((boolean) json.get("hasProfile"));
    assertEquals("testUserName", json.get("userName"));
  }

  @Test
  public void IsLoggedInHasNoProfile() throws IOException, ServletException, ParseException {
    // User is logged in but doesn't have profile
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail("test2@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "111"));
    helper.setUp();

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertTrue((boolean) json.get("isLoggedIn"));
    assertEquals("test2@gmail.com", json.get("email"));
    assertFalse((boolean) json.get("hasProfile"));
  }
}