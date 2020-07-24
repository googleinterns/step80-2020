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
import com.google.gson.Gson;

/** Servlet that deals with login and logout requests */
@WebServlet("/login")
public class UserServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    JSONObject responseMap = new JSONObject();

    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/profile.html";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      
      responseMap.put("email", userEmail);
      responseMap.put("isLoggedIn", true);
      responseMap.put("logoutUrl", logoutUrl);

      String userName = getUserProfileName(userService.getCurrentUser().getUserId());
      if (userName == null) {
        responseMap.put("hasProfile", false);
      } else {
        responseMap.put("hasProfile", true);
        responseMap.put("userName", userName);
      }
    } else {
      String urlToRedirectToAfterUserLogsIn = "/profile.html";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      responseMap.put("isLoggedIn", false);
      responseMap.put("loginUrl", loginUrl);
    }

    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /** Returns the name of the user with id, or null if the user has no profile. */
  private String getUserProfileName(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("Profile")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return null;
    }
    String userName = (String) entity.getProperty("userName");
    return userName;
  }
}