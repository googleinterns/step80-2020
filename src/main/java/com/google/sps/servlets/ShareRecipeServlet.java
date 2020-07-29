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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;

/** Servlet that posts and gets user profiles in Datastore */
@WebServlet("/shareRecipe")
public class ShareRecipeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
     UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
    } else {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Profile")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
      PreparedQuery results = datastore.prepare(query);
      Entity userEntity = results.asSingleEntity();
      if(userEntity == null) {
        //no profile
      } else {
        query = new Query("userMessages")
          .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userService.getCurrentUser().getEmail()));
        results = datastore.prepare(query);
        Entity messagesEntity = results.asSingleEntity();
        if(messagesEntity == null) {
          System.out.println(userService.getCurrentUser().getEmail());
        } else {
          ArrayList<String> messages = convertToList(messagesEntity, "messages");
          ArrayList<String> recipeIds = convertToList(messagesEntity, "recipeIds");
          ArrayList<String> userEmails = convertToList(messagesEntity, "userEmails");
          System.out.println(messages);
          System.out.println(recipeIds);
          System.out.println(userEmails);
          JSONArray messageList = new JSONArray();
          for(int i = 0; i < messages.size(); i++) {
            JSONObject messageObject = new JSONObject();
            messageObject.put("messageContent", messages.get(i));
            messageObject.put("recipeId", recipeIds.get(i));
            messageObject.put("userEmail", userEmails.get(i));
            messageObject.put("num", i);
            System.out.println(messageObject);
            messageList.put(messageObject);
          }
          response.setContentType("application/json");
          System.out.println(messageList);
          response.getWriter().println(messageList);
        }
      }
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setContentType("application/html");
      response.getWriter().println("Please log in to Share recipes");
    } else {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Profile")
      .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, userService.getCurrentUser().getUserId()));
      PreparedQuery results = datastore.prepare(query);
      Entity userEntity = results.asSingleEntity();
      if(userEntity == null) {
        response.setContentType("application/html");
        response.getWriter().println("Please create your user profile before sharing recipes");
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
          query = new Query("userMessages")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, friendEmail));
          results = datastore.prepare(query);
          Entity friendMessagesEntity = results.asSingleEntity();
          ArrayList<String> messages = convertToList(friendMessagesEntity, "messages");
          ArrayList<String> recipeIds = convertToList(friendMessagesEntity, "recipeIds");
          ArrayList<String> userEmails = convertToList(friendMessagesEntity, "userEmails");
          messages.add(request.getParameter("message"));
          recipeIds.add(request.getParameter("recipeId"));
          userEmails.add(userService.getCurrentUser().getEmail());
          friendMessagesEntity.setProperty("userEmails", userEmails);          
          friendMessagesEntity.setProperty("messages", messages);
          friendMessagesEntity.setProperty("recipeIds", recipeIds);
          datastore.put(friendMessagesEntity);
          response.setContentType("application/html");
          response.getWriter().println("Recipe shared successfully");      
        }
      }
    }
  }

  public ArrayList<String> convertToList(Entity friendMessagesEntity, String messages) {
    Object valuesList = friendMessagesEntity.getProperty(messages);
    if (valuesList == null) {
      return new ArrayList<>();
    } else {
      return (ArrayList<String>) valuesList;
    }
  }
}