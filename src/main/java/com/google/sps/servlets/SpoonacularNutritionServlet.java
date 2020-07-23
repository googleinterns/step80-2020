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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;  
import com.google.gson.Gson;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/** Servlet to take in dish name and return bulk recipe infomation */
@WebServlet("/dishNutrition")
public class SpoonacularNutritionServlet extends HttpServlet {
  private static String spoonacularPrefix = "https://api.spoonacular.com/recipes/guessNutrition";

  /** Takes in dishName parameter and uses spoonacular's guess nutrition to return nutrition information about the dish*/
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("apiKey")
        .setFilter(new Query.FilterPredicate("keyName", Query.FilterOperator.EQUAL, "spoonacular"));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    String spoonacularKey = (String) entity.getProperty("key");

    String title = (String) request.getParameter("dishName");
    Client client = ClientBuilder.newClient();
    try {
      title = URLEncoder.encode(title);
    }
    catch (Exception e) {
      System.out.println(e);
    }
    WebTarget target = client.target(String.format("%s?title=%s&apiKey=%s", spoonacularPrefix, title, spoonacularKey));
    try {
      String recipeListJSONString = target.request(MediaType.APPLICATION_JSON).get(String.class);
      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(recipeListJSONString));
    } catch(Exception e){
      System.out.println(e);
    }
  }
}