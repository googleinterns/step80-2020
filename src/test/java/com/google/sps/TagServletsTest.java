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
import com.google.sps.servlets.TagServlet;
import com.google.sps.servlets.TagNamesServlet;
import com.google.sps.servlets.DeleteTagServlet;
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
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Arrays;

/** Tests for all tag servlets: TagServlet, TagNamesServlet, DeleteTagServlet */
@RunWith(MockitoJUnitRunner.class)
public final class TagServletsTest {
  // helper to mimic user authentication
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvAuthDomain("gmail.com")
      .setEnvEmail("test@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
  
  @Mock private TagServlet tagServlet;
  @Mock private TagNamesServlet tagNamesServlet;
  @Mock private DeleteTagServlet deleteTagServlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws ServletException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    tagServlet = new TagServlet();
    tagNamesServlet = new TagNamesServlet();
    deleteTagServlet = new DeleteTagServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void postTagNotLoggedIn() throws IOException, ServletException, ParseException {
    // User tries to post tag but is not logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    tagServlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to login", json.get("error"));
  }

  @Test
  public void postTagNoParameters() throws IOException, ServletException, ParseException {
    // User posts tag without filling in parameters
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    tagServlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(json.isEmpty());
  }

  // helper test for creating tag with known parameters
  public void postTagHasParameters(String tagName, String recipeId) throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("tag-name")).thenReturn(tagName);
    when(request.getParameter("recipe-id")).thenReturn(recipeId);
    
    tagServlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(json.isEmpty());
  }

  @Test
  public void getTagNotLoggedIn() throws IOException, ServletException, ParseException {
    // User tries to get tag without being logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    tagServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to login", json.get("error"));
  }

  @Test
  public void getTagNoParameters() throws IOException, ServletException, ParseException {
    // User gets tags without any filters
    helper.setEnvIsLoggedIn(true);

    postTagHasParameters("tagName", "1");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    tagServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(((JSONArray) json.get("filteredList")).size() > 0);
    assertEquals(null, json.get("error"));
  }

  @Test
  public void getTagHasTagParameter() throws IOException, ServletException, ParseException {
    // User gets tags with tag name filter
    helper.setEnvIsLoggedIn(true);

    postTagHasParameters("tagName1", "1");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("tagName")).thenReturn("tagName1");
    
    tagServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    JSONArray jsonArray = (JSONArray) json.get("filteredList");
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject tagObject = (JSONObject) jsonArray.get(i);
      assertEquals("tagName1", tagObject.get("tagName"));
    }
    assertEquals(null, json.get("error"));
  }

  @Test
  public void getTagHasRecipeIdParameter() throws IOException, ServletException, ParseException {
    // User gets tags with recipe id filter
    helper.setEnvIsLoggedIn(true);

    postTagHasParameters("tagName", "2");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("recipeId")).thenReturn("2");
    
    tagServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    JSONArray jsonArray = (JSONArray) json.get("filteredList");
    for (int i = 0; i < jsonArray.size(); i++) {
      JSONObject tagObject = (JSONObject) jsonArray.get(i);
      assertEquals(new Long(2), tagObject.get("recipeId"));
    }
    assertEquals(null, json.get("error"));
  }

  @Test
  public void getTagNamesNotLoggedIn() throws IOException, ServletException, ParseException {
    // User tries to get all tag names but isn't logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    tagNamesServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONArray json = (JSONArray) parser.parse(result);
    
    assertTrue(json.isEmpty());
  }
  
  @Test
  public void getTagNamesIsLoggedIn() throws IOException, ServletException, ParseException {
    // User is logged in and gets all tag names
    helper.setEnvIsLoggedIn(true);

    // add tags to server
    postTagHasParameters("tagName", "1");
    postTagHasParameters("tagName1", "2");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // get tagnames from server
    tagNamesServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONArray json = (JSONArray) parser.parse(result);
    JSONArray testArray = new JSONArray();
    testArray.add("tagName");
    testArray.add("tagName1");

    assertEquals(testArray, json);

    deleteTag("tagName", "1");
    deleteTag("tagName1", "2");
  }

  // helper test for deleting tags
  public void deleteTag(String tagName, String recipeId) throws IOException, ServletException, ParseException {
    // User wants to delete tag
    helper.setEnvIsLoggedIn(true);
    
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    when(request.getParameter("tagName")).thenReturn(tagName);
    when(request.getParameter("recipeId")).thenReturn(recipeId);
    
    // get specific tag from server
    tagServlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject getTagJson = (JSONObject) parser.parse(result);
    
    // get tag's id for deletion
    JSONArray getTagJsonArray = (JSONArray) getTagJson.get("filteredList");
    JSONObject tagObject = (JSONObject) getTagJsonArray.get(0);
    String tagId = ((Long) tagObject.get("tagId")).toString();

    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // delete tag using id as parameter
    when(request.getParameter("tag-id")).thenReturn(tagId);
    deleteTagServlet.doPost(request, response);
    
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    when(request.getParameter("tagName")).thenReturn(tagName);
    when(request.getParameter("recipeId")).thenReturn(recipeId);
    
    // check if tag has been deleted from server
    tagServlet.doGet(request, response);
    result = sw.getBuffer().toString().trim();

    getTagJson = (JSONObject) parser.parse(result);
    getTagJsonArray = (JSONArray) getTagJson.get("filteredList");
    assertTrue(getTagJsonArray.isEmpty());
  }
}