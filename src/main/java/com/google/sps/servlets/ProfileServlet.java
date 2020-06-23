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

import com.google.sps.data.Profile;
import com.google.gson.Gson;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/** Servlet that posts and gets user profiles in Datastore */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();
    
    if (userService.isUserLoggedIn()) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Profile")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
      PreparedQuery results = datastore.prepare(query);
      Entity entity = results.asSingleEntity();
      if (entity == null) {
        responseMap.put("hasProfile", false);
      } else {
        long id = (long) entity.getProperty("id");
        String userName = (String) entity.getProperty("userName");
        boolean vegetarian = (boolean) entity.getProperty("vegetarian");
        boolean vegan = (boolean) entity.getProperty("vegan");
        boolean glutenFree = (boolean) entity.getProperty("glutenFree");
        boolean dairyFree = (boolean) entity.getProperty("dairyFree");
        String[] allergies = (String[]) entity.getProperty("allergies");

        Profile profileObject = new Profile(id, userName, vegetarian, vegan, glutenFree, dairyFree, allergies);
        responseMap.put("profile", profileObject);
        responseMap.put("hasProfile", true);
      }

    } else {
      String errorMessage = "User needs to log in to see profile.";
      responseMap.put("error", errorMessage);
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();

    if (!userService.isUserLoggedIn()) {
      String errorMessage = "User needs to log in to change profile.";
      responseMap.put("error", errorMessage);
      
    } else {
      String id = userService.getCurrentUser().getUserId();
      String userName = request.getParameter("userName");
      boolean vegetarian = Boolean.parseBoolean(request.getParameter("vegetarian"));
      boolean vegan = Boolean.parseBoolean(request.getParameter("vegan"));
      boolean glutenFree = Boolean.parseBoolean(request.getParameter("glutenFree"));
      boolean dairyFree = Boolean.parseBoolean(request.getParameter("dairyFree"));
      String[] allergies = request.getParameterValues("allergies");

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity entity = new Entity("Profile", id);
      entity.setProperty("id", id);
      entity.setProperty("userName", userName);
      entity.setProperty("vegetarian", vegetarian);
      entity.setProperty("vegan", vegan);
      entity.setProperty("glutenFree", glutenFree);
      entity.setProperty("dairyFree", dairyFree);
      entity.setProperty("allergies", allergies);
      
      // The put() function automatically inserts new data or updates existing data based on id
      datastore.put(entity);
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }
}
