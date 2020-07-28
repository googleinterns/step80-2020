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

import com.google.sps.data.FavoriteRecipe;
import com.google.gson.Gson;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import org.json.simple.JSONObject;
import java.util.Set;
import java.util.HashSet;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

/** Servlet that returns and keeps track of favorite recipes in Datastore */
@WebServlet("/favorite")
public class FavoriteRecipeServlet extends HttpServlet {
  private static final String AUTHORIZATION_ERROR = "User needs to login";
  
  /** Return all or one of user's favorite recipes */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    JSONObject responseMap = new JSONObject();
    Gson gson = new Gson();  
    String json;

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Long recipeId = strToLong(request.getParameter("recipeId"));
    String email = request.getParameter("email");
    String userId;

    if (userService.isUserLoggedIn()) {
      Query query = new Query("FavoriteRecipe");

      //see if a specific user's favorites are supposed to be returned or if just the current user
      if (email != null) {
        Query userQuery = new Query("Profile")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email));
        PreparedQuery results = datastore.prepare(userQuery);
        Entity entity = results.asSingleEntity();
        userId = (String) entity.getProperty("id");
      } else {
        userId = userService.getCurrentUser().getUserId();
      }

      if (recipeId != null) {
        // see if specific recipe has been favorited
        List<Query.Filter> filterList = new ArrayList<>();
        filterList.add(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        filterList.add(new Query.FilterPredicate("recipeId", Query.FilterOperator.EQUAL, recipeId));
        query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filterList));
        PreparedQuery results = datastore.prepare(query);
      
        Entity entity = results.asSingleEntity();
        if (entity == null) {
          responseMap.put("isFavorite", false);
        } else {
          responseMap.put("isFavorite", true);
          long favoriteId = entity.getKey().getId();
          String dateFavorited = (String) entity.getProperty("dateFavorited");
          responseMap.put("favoriteId", favoriteId);
          responseMap.put("dateFavorited", dateFavorited);
        }
      } else {
        // give back list of favorited recipes
        query.setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        PreparedQuery results = datastore.prepare(query);

        ArrayList<FavoriteRecipe> recipeList = new ArrayList();
        for (Entity entity : results.asIterable()) {
          long entityFavoriteId = entity.getKey().getId();
          String entityUserId = (String) entity.getProperty("userId");
          long entityRecipeId = (long) entity.getProperty("recipeId");
          String entityDate = (String) entity.getProperty("dateFavorited");

          recipeList.add(new FavoriteRecipe (entityFavoriteId, entityUserId, entityRecipeId, entityDate));
        }
        responseMap.put("recipeList", recipeList);
      }

    } else {
      responseMap.put("error", AUTHORIZATION_ERROR); 
    }
    
    json = gson.toJson(responseMap);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /** Add/Delete a FavoriteRecipe to datastore which represents a user's favorited recipe */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    JSONObject responseMap = new JSONObject();

    if (!userService.isUserLoggedIn()) {
      responseMap.put("error", AUTHORIZATION_ERROR);
    } else {
      String favoriteIdString = request.getParameter("favorite-id");
      if (favoriteIdString != null && favoriteIdString != "0") {
        // delete entity
        long id = Long.parseLong(favoriteIdString);

        Key tagEntityKey = KeyFactory.createKey("FavoriteRecipe", id);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.delete(tagEntityKey);
      } else {
        // create entity
        Entity entity = new Entity("FavoriteRecipe");
      
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userId = userService.getCurrentUser().getUserId();
        entity.setProperty("userId", userId);
      
        Long recipeId = strToLong(request.getParameter("recipe-id"));
        entity.setProperty("recipeId", recipeId);
        
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        String dateFavorited = dateFormat.format(now);
        entity.setProperty("dateFavorited", dateFavorited);  

        if (recipeId != null) {
          datastore.put(entity);

          long favoriteId = entity.getKey().getId();
          responseMap.put("favoriteId", favoriteId);
        }
      }
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  public Long strToLong(String str) {
    if (str == null) {
      return null;
    } else {
      return Long.parseLong(str);
    }
  }
}
