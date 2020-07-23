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
import com.google.sps.servlets.FavoriteRecipeServlet;
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
import org.json.simple.JSONArray;

/** Tests for FavoriteRecipeServlet */
@RunWith(MockitoJUnitRunner.class)
public final class FavoriteServletTest {
  // helper to mimic user authentication
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvAuthDomain("gmail.com")
      .setEnvEmail("test@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
  
  @Mock private FavoriteRecipeServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws ServletException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servlet = new FavoriteRecipeServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void postFavoriteNotLoggedIn() throws IOException, ServletException, ParseException {
    // User tries to set favorite but not logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to login", json.get("error"));
  }

  @Test
  public void postFavoriteNoParameters() throws IOException, ServletException, ParseException {
    // User set favorite without setting any parameters
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(json.isEmpty());
  }

  // helper test for creating favorites with known parameters
  public Long createFavoriteWithRecipeId(String recipeId) throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("recipe-id")).thenReturn(recipeId);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(json.get("favoriteId") != null);
    return (Long) json.get("favoriteId");
  }

  // helper test for deleting favorites
  public void deleteFavoriteWithFavoriteId(Long favoriteId) throws IOException, ServletException, ParseException {
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("favorite-id")).thenReturn(Long.toString(favoriteId));
    
    // delete favorite
    servlet.doPost(request, response);
    
    // check if favorite does not exist due to deletion
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("recipeId")).thenReturn("1");

    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();
    
    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertFalse((boolean) json.get("isFavorite"));
  }

  @Test
  public void getFavoriteNotLoggedIn() throws IOException, ServletException, ParseException {
    // User tries to get favorites without being logged in
    helper.setEnvIsLoggedIn(false);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to login", json.get("error"));
  }

  @Test
  public void getFavoritesNoParameters() throws IOException, ServletException, ParseException {
    // User tries to get all favorites
    helper.setEnvIsLoggedIn(true);

    Long favoriteId1 = createFavoriteWithRecipeId("1");
    Long favoriteId2 = createFavoriteWithRecipeId("2");

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue(((JSONArray) json.get("recipeList")).size() == 2);
    assertEquals(null, json.get("error"));

    getFavoriteHasRecipeIdParameter("1", favoriteId1);
    
    deleteFavoriteWithFavoriteId(favoriteId1);
    deleteFavoriteWithFavoriteId(favoriteId2);
  }

  public void getFavoriteHasRecipeIdParameter(String recipeId, Long favoriteId) throws IOException, ServletException, ParseException {
    // User wants to know if recipe is favorite
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("recipeId")).thenReturn(recipeId);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertTrue((boolean) json.get("isFavorite"));
    assertEquals(favoriteId, json.get("favoriteId"));
  }

  @Test
  public void getFavoriteDoesNotExist() throws IOException, ServletException, ParseException {
    // User wants to know if recipe is a favorite but it isn't
    helper.setEnvIsLoggedIn(true);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    when(request.getParameter("recipeId")).thenReturn("3");
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertFalse((boolean) json.get("isFavorite"));
  }
}