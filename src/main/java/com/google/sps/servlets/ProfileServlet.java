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
        String id = (String) entity.getProperty("id");
        String userName = (String) entity.getProperty("userName");
        ArrayList<String> dietaryNeedsStrings = (ArrayList<String>) entity.getProperty("dietaryNeeds");
        ArrayList<String> allergies = (ArrayList<String>) entity.getProperty("allergies");

        ArrayList<Profile.Diet> dietaryNeeds = new ArrayList<>();
        for (String dietString: dietaryNeedsStrings) {
          switch(dietString) {
            case "VEGETARIAN":
              dietaryNeeds.add(Profile.Diet.VEGETARIAN);
              break;
            case "VEGAN":
              dietaryNeeds.add(Profile.Diet.VEGAN);
              break;
            case "GLUTENFREE":
              dietaryNeeds.add(Profile.Diet.GLUTENFREE);
              break;
            case "DAIRYFREE":
              dietaryNeeds.add(Profile.Diet.DAIRYFREE);
              break;
            default: break;
          }
        }
        Profile profileObject = Profile.builder().setId(id).setUserName(userName).
          setDietaryNeeds(dietaryNeeds).setAllergies(allergies).build();
          
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
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      String id = userService.getCurrentUser().getUserId();
      Entity entity = new Entity("Profile", id);
      entity.setProperty("id", id);
      
      String userName = request.getParameter("userName");
      if (userName != null) {
        entity.setProperty("userName", request.getParameter("userName"));
        String[] dietaryNeeds = request.getParameterValues("dietary-needs");
        entity.setProperty("dietaryNeeds", Arrays.asList(dietaryNeeds));
        // entity.setProperty("vegetarian", Boolean.parseBoolean(request.getParameter("vegetarian")));
        // entity.setProperty("vegan", Boolean.parseBoolean(request.getParameter("vegan")));
        // entity.setProperty("glutenFree", Boolean.parseBoolean(request.getParameter("glutenFree")));
        // entity.setProperty("dairyFree", Boolean.parseBoolean(request.getParameter("dairyFree")));
        
        String[] allergies = (request.getParameter("allergies")).split(",");
        if (allergies == null) {
          allergies = new String[0];
        }
        entity.setProperty("allergies", Arrays.asList(allergies));
      
        // The put() function automatically inserts new data or updates existing data based on id
        datastore.put(entity);
      } else {
        String errorMessage = "User needs to input username.";
      responseMap.put("error", errorMessage);
      }
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }
}
