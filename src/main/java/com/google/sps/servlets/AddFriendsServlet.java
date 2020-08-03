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
import java.util.List;

/** Servlet that posts and gets user profiles in Datastore */
@WebServlet("/addFriend")
public class AddFriendsServlet extends HttpServlet {
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
      if (!userService.isUserLoggedIn()) {
        response.setContentType("application/html");
        response.getWriter().println("Please log in to add friends");
      } else {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Profile")
          .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
        PreparedQuery results = datastore.prepare(query);
        Entity userEntity = results.asSingleEntity();
        if(userEntity == null) {
          response.setContentType("application/html");
          response.getWriter().println("Please create your user profile before adding friends");
        } else {
          String friendEmail = request.getParameter("friendEmail");
          query = new Query("Profile")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, friendEmail));
          results = datastore.prepare(query);
          Entity friendEntity = results.asSingleEntity();
          if(friendEntity == null) {
            response.setContentType("application/html");
            response.getWriter().println("Sorry, email not found");      
          }
          else {
            ArrayList<String> friendList = nullToArrayList((ArrayList<String>) userEntity.getProperty("friendList"));
            if(friendList.contains(friendEmail)){
              response.setContentType("application/html");
              response.getWriter().println("You already have this user as a friend");
            } else {
              friendList.add(friendEmail);
              userEntity.setProperty("friendList", friendList);
              datastore.put(userEntity);
              response.setContentType("application/html");
              response.getWriter().println("Successfully added");
            }
          }
        }
      }
  }

  public ArrayList<String> nullToArrayList(ArrayList<String> valuesList) {
    if (valuesList == null) {
      return new ArrayList<>();
    }
    return valuesList;
  }
}