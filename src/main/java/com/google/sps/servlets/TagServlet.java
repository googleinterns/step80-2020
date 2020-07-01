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

import com.google.sps.data.TagRecipePair;
import com.google.gson.Gson;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.json.simple.JSONObject;

/** Servlet that returns and adds tags in Datastore */
@WebServlet("/tag")
public class TagServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();
    Gson gson = new Gson();  
    String json;

    String inputTagName = request.getParameter("tagName");
    String recipeIdString = request.getParameter("recipeId");
    Integer inputRecipeId = null;
    if (recipeIdString != null) {
      inputRecipeId = Integer.parseInt(recipeIdString);
    }
    
    if (userService.isUserLoggedIn()) {
      Query query = new Query("TagRecipePair")
        .setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
      if (inputTagName != null && !inputTagName.equals("")) {
        query.setFilter(new Query.FilterPredicate("tagName", Query.FilterOperator.EQUAL, inputTagName));
      }
      if (inputRecipeId != null) {
        query.setFilter(new Query.FilterPredicate("recipeId", Query.FilterOperator.EQUAL, inputRecipeId));
      }
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      ArrayList<TagRecipePair> tagList = new ArrayList<TagRecipePair>();
      for (Entity entity : results.asIterable()) {
        long tagId = entity.getKey().getId();
        String userId = (String) entity.getProperty("userId");
        String tagName = (String) entity.getProperty("tagName");
        long recipeId = (long) entity.getProperty("recipeId");

        TagRecipePair tagObject = new TagRecipePair(tagId, userId, tagName, recipeId);
        tagList.add(tagObject);
      }
    
      json = gson.toJson(tagList);
      response.setContentType("application/json");

    } else {
      String errorMessage = "User needs to log in to see tags.";
      responseMap.put("error", errorMessage); 
      json = gson.toJson(responseMap);
    }
    
    response.setContentType("application");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();

    if (!userService.isUserLoggedIn()) {
      String errorMessage = "User needs to log in to add tag.";
      responseMap.put("error", errorMessage);
      
    } else {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      String userId = userService.getCurrentUser().getUserId();
      Entity entity = new Entity("TagRecipePair"); // also want to get key of entity if it exists
      entity.setProperty("userId", userId);
      
      String tagName = request.getParameter("tag-name");
      entity.setProperty("tagName", tagName);

      long recipeId = Long.parseLong(request.getParameter("recipe-id"));
      entity.setProperty("recipeId", recipeId);
      
      datastore.put(entity);
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }

}
