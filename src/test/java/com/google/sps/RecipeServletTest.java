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
import com.google.sps.servlets.SavedRecipeServlet;
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

/** Tests for SavedRecipeServlet */
@RunWith(MockitoJUnitRunner.class)
public final class RecipeServletTest {
  // helper used to register API environment
  private final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalUserServiceTestConfig()).setEnvAuthDomain("gmail.com")
      .setEnvEmail("test@gmail.com")
      .setEnvAttributes(ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", "123"));
  
  @Mock private SavedRecipeServlet servlet;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;

  @Before
  public void setUp() throws ServletException {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    servlet = new SavedRecipeServlet();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void postRecipeNoParameters() throws IOException, ServletException, ParseException {
    // User tries to post recipe with no parameters
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doPost(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    
    assertEquals("User needs to input recipeId.", json.get("error"));
  }

  @Test
  public void postOnlyRecipeId() throws IOException, ServletException, ParseException {
    // User posts only recipe id when posting recipe
    when(request.getParameter("recipe-id")).thenReturn("1");
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // set recipeId of recipe object
    servlet.doPost(request, response);
    
    when(request.getParameter("recipeId")).thenReturn("1");
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // get recipe from servlet, check if saved
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    JSONObject recipe = (JSONObject) json.get("savedRecipe");
    
    assertEquals(new Long(1), recipe.get("id"));
    assertTrue((boolean) json.get("recipeIsSaved"));
  }

  @Test
  public void recipeDoesNotExist() throws IOException, ServletException, ParseException {
    // User tries to get a recipe that does not exist
    when(request.getParameter("recipeId")).thenReturn("2");
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);

    assertFalse((boolean) json.get("recipeIsSaved"));
  }

  @Test
  public void postRecipeParameters() throws IOException, ServletException, ParseException {
    // User posts complete recipe information
    when(request.getParameter("recipe-id")).thenReturn("1");
    when(request.getParameter("recipe-title")).thenReturn("testTitle");
    when(request.getParameter("image-url")).thenReturn("testImageUrl");
    when(request.getParameter("source-url")).thenReturn("testSourceUrl");
    when(request.getParameter("servings")).thenReturn("2");
    when(request.getParameter("ready-in-minutes")).thenReturn("3");
    
    String[] dietList = {"VEGETARIAN","VEGAN"};
    when(request.getParameterValues("dietary-needs")).thenReturn(dietList);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // set recipeId of recipe object
    servlet.doPost(request, response);
    
    when(request.getParameter("recipeId")).thenReturn("1");
    sw = new StringWriter();
    pw = new PrintWriter(sw);
    when(response.getWriter()).thenReturn(pw);
    
    // get recipe from servlet, check all parameters
    servlet.doGet(request, response);
    String result = sw.getBuffer().toString().trim();

    JSONParser parser = new JSONParser();
    JSONObject json = (JSONObject) parser.parse(result);
    JSONObject recipe = (JSONObject) json.get("savedRecipe");
    
    assertTrue((boolean) json.get("recipeIsSaved"));
    assertEquals(new Long(1), recipe.get("id"));
    assertEquals("testTitle", recipe.get("title"));
    assertEquals("testImageUrl", recipe.get("image"));
    assertEquals("testSourceUrl", recipe.get("sourceUrl"));
    assertEquals(new Long(2), recipe.get("servings"));
    assertEquals(new Long(3), recipe.get("readyInMinutes"));
    assertEquals(Arrays.asList(dietList), recipe.get("dietaryNeeds"));
  }
}