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
import com.google.sps.data.Profile;
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
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/** Servlet that gets and returns the list of recipes favorited by a user's friends */
@WebServlet("/feed")
public class FeedServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();
    ArrayList<FavoriteRecipe> recipeList = new ArrayList();

    if (!userService.isUserLoggedIn()) {
      String errorMessage = "User needs to log in.";
      responseMap.put("error", errorMessage);
    } else {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Profile")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();

      String email = (String) entity.getProperty("email");
      ArrayList<String> friendList = (ArrayList<String>) entity.getProperty("friendList");
      friendList = nullToArrayList(friendList);

      System.out.println(friendList);

      for (String friend: friendList) {
        Query emailQuery = new Query("Profile")
          .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, friend));
        PreparedQuery emailResults = datastore.prepare(emailQuery);
        Entity emailEntity = emailResults.asSingleEntity();
        String userId = (String) emailEntity.getProperty("id");

        Query userQuery = new Query("FavoriteRecipe")
          .setFilter(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
        PreparedQuery userResults = datastore.prepare(userQuery);
        for (Entity userEntity : userResults.asIterable()) {
          long entityFavoriteId = userEntity.getKey().getId();
          String entityUserId = (String) friend;
          long entityRecipeId = (long) userEntity.getProperty("recipeId");
          String entityDate = (String) userEntity.getProperty("dateFavorited");

          FavoriteRecipe newRecipe = new FavoriteRecipe (entityFavoriteId, friend, entityRecipeId, entityDate);
          recipeList.add(newRecipe);
        }
      }
      System.out.println(recipeList);

      Collections.sort(recipeList, (o1, o2) -> o2.getFavoriteDate().compareTo(o1.getFavoriteDate()));

      System.out.println(recipeList);

      responseMap.put("recipeList", recipeList);
      responseMap.put("email", email);
    } 
    
    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }

  public ArrayList<String> nullToArrayList(ArrayList<String> valuesList) {
    if (valuesList == null) {
      return new ArrayList<>();
    }
    return valuesList;
  }
}
