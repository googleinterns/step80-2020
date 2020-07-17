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
@WebServlet("/recipeInfo")
public class SpoonacularCombinedServlet extends HttpServlet {
  private static final String SPOONACULAR_API_PREFIX = "https://api.spoonacular.com/recipes";
  private static final String API_QUERY_NUMBER = "6";
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("apiKey")
        .setFilter(new Query.FilterPredicate("keyName", Query.FilterOperator.EQUAL, "spoonacular"));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    String spoonacularKey = (String) entity.getProperty("key");
    
    String query = request.getParameter("dishName");
    Client client = ClientBuilder.newClient();
    try {
      query = URLEncoder.encode(query);
    } catch (Exception e) {
      System.out.println(e);
    }
    String targetString = String.format("%s/search?query=%s&number=%s&includeNutririon=true&apiKey=%s", 
      SPOONACULAR_API_PREFIX, query, API_QUERY_NUMBER, spoonacularKey);
    WebTarget target = client.target(targetString);
    try {
      String recipeListJSONString = target.request(MediaType.APPLICATION_JSON).get(String.class);
      JSONObject recipeJson = new JSONObject(recipeListJSONString);
      JSONArray recipeListJson = new JSONArray(recipeJson.get("results").toString());
      String recipeList = "";
      Boolean isFirstinList = true;
      for(Object recipeInfoObject: recipeListJson) {
        JSONObject recipeInfoJson = (JSONObject)recipeInfoObject;
        if(isFirstinList) {
          recipeList = recipeList + recipeInfoJson.get("id"); 
          isFirstinList = false;
        } else {
          recipeList = recipeList + "," + recipeInfoJson.get("id"); 
        }
      }
      targetString = String.format("%s/informationBulk?includeNutrition=true&apiKey=%s&ids=%s", 
        SPOONACULAR_API_PREFIX, SPOONACULAR_API_KEY, recipeList);
      target = client.target(targetString);
      String recipeInformationString = target.request(MediaType.APPLICATION_JSON).get(String.class);
      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(recipeInformationString));
    } catch(Exception e){
      System.out.println(e);
    }
  }
}