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
import com.google.sps.servlets.ProfileServlet;
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
import java.util.Arrays;

/** */
@RunWith(MockitoJUnitRunner.class)
public final class ProfileServletTest {
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvAuthDomain("gmail.com")
      .setEnvEmail("test@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
  
  @Mock private ProfileServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws ServletException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servlet = new ProfileServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void postProfileNotLoggedIn() throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to log in to change profile.", json.get("error"));
  }
  
  @Test
  public void postProfileNoParameters() throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to input username.", json.get("error"));
  }

  @Test
  public void getProfileNotLoggedIn() throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(false);
    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertEquals("User needs to log in to see profile.", json.get("error"));
  }

  @Test
  public void getProfileHasNoProfile() throws IOException, ServletException, ParseException {
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

    assertFalse((boolean) json.get("hasProfile"));
  }

  @Test
  public void onlyUsernameProfile() throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true)
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
    helper.setUp();

    when(request.getParameter("userName")).thenReturn("testUserName");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    JSONObject profile = (JSONObject) json.get("profile");

    assertEquals("testUserName", profile.get("userName"));
  }

  @Test
  public void includeListInputsProfile() throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true);

    when(request.getParameter("userName")).thenReturn("testUserName");
    
    String[] dietList = {"VEGETARIAN","VEGAN"};
    when(request.getParameterValues("dietary-needs")).thenReturn(dietList);

    String[] allergyList = {"milk"};
    when(request.getParameterValues("allergies")).thenReturn(allergyList);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    JSONObject profile = (JSONObject) json.get("profile");
    
    assertEquals("testUserName", profile.get("userName"));
    assertEquals(Arrays.asList(dietList), profile.get("dietaryNeeds"));
    assertEquals(Arrays.asList(allergyList), profile.get("allergies"));
  }
}

