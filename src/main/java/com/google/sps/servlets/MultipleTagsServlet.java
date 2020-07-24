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
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import org.json.simple.JSONObject;
import java.util.Set;
import java.util.HashSet;

/** Servlet that returns and adds tags in Datastore */
@WebServlet("/multiple-tags")
public class MultipleTagsServlet extends HttpServlet {
  private static final String AUTHORIZATION_ERROR = "User needs to login";
  
  /** Return all tags filtered by tagName and/or recipeId */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    JSONObject responseMap = new JSONObject();
    Gson gson = new Gson();  
    String json;

    String[] tagNames = request.getParameterValues("tag-names");
    if (userService.isUserLoggedIn()) {
      Query query = getQueryWithFilters(tagNames, userService);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      HashSet<Long> recipeList = new HashSet<>();
      for (Entity entity : results.asIterable()) {
        long recipeId = (long) entity.getProperty("recipeId");
        recipeList.add(recipeId);
      }
      responseMap.put("recipeList", recipeList);

    } else {
      responseMap.put("error", AUTHORIZATION_ERROR); 
    }
    
    json = gson.toJson(responseMap);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  // helper function for creating query with a combination of filters
  public Query getQueryWithFilters(String[] tagNames, UserService userService) {
    List<Query.Filter> filterList = new ArrayList<>();
    String userId = userService.getCurrentUser().getUserId();

    // set up filters for query
    filterList.add(new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId));
    
    List<Query.Filter> tagFilters = new ArrayList<>();
    if (tagNames != null) {
      if (tagNames.length > 1) {
        for (int i = 0; i < tagNames.length; i++) {
          tagFilters.add(new Query.FilterPredicate("tagName", Query.FilterOperator.EQUAL, tagNames[i]));
        }
        filterList.add(new Query.CompositeFilter(Query.CompositeFilterOperator.OR, tagFilters));
      } else if (tagNames.length == 1) {
        filterList.add(new Query.FilterPredicate("tagName", Query.FilterOperator.EQUAL, tagNames[0]));
      }
    }

    Query query = new Query("TagRecipePair");
    if (filterList.size() > 1) { // use composite for multiple filters
      query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filterList));
    } else {
      query.setFilter(filterList.get(0));
    }
    return query;
  }

  public Long strToLong(String str) {
    if (str == null) {
      return null;
    } else {
      return Long.parseLong(str);
    }
  }
}
