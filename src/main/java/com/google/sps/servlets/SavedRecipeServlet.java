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

import com.google.sps.data.SavedRecipe;
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

/** Servlet that posts and gets saved recipe information in Datastore */
@WebServlet("/saved-recipe")
public class SavedRecipeServlet extends HttpServlet {
  
  /** Gets SavedRecipe using the recipe's id */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    JSONObject responseMap = new JSONObject();

    // get recipeId from request parameter
    Long recipeId = Long.parseLong(request.getParameter("recipeId"));
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("SavedRecipe")
      .setFilter(new Query.FilterPredicate("recipeId", Query.FilterOperator.EQUAL, recipeId));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      responseMap.put("recipeIsSaved", false);
    } else {
      String title = (String) entity.getProperty("title");
      String imageUrl = (String) entity.getProperty("imageUrl");
      String sourceUrl = (String) entity.getProperty("sourceUrl");
      Long servings = (Long) entity.getProperty("servings");
      Long readyInMinutes = (Long) entity.getProperty("readyInMinutes");
      ArrayList<String> dietaryNeedsStrings = (ArrayList<String>) entity.getProperty("dietaryNeeds");
      ArrayList<String> ingredientNamesStrings = (ArrayList<String>) entity.getProperty("ingredientNames");
      
      // convert string to Diet enum because datastore stores dietaryNeeds as a list of strings
      ArrayList<SavedRecipe.Diet> dietaryNeeds = new ArrayList<>();
      if(dietaryNeedsStrings == null) {
        dietaryNeedsStrings = new ArrayList<String>();
      }
      for (String dietString: dietaryNeedsStrings) {
        switch(dietString) {
          case "VEGETARIAN":
            dietaryNeeds.add(SavedRecipe.Diet.VEGETARIAN);
            break;
          case "VEGAN":
            dietaryNeeds.add(SavedRecipe.Diet.VEGAN);
            break;
          case "GLUTENFREE":
            dietaryNeeds.add(SavedRecipe.Diet.GLUTENFREE);
            break;
          case "DAIRYFREE":
            dietaryNeeds.add(SavedRecipe.Diet.DAIRYFREE);
            break;
          default: break;
        }
      }
      SavedRecipe savedRecipeObject = SavedRecipe.builder().setId(recipeId).setTitle(title).
        setImage(imageUrl).setSourceUrl(sourceUrl).setServings(servings)
        .setReadyInMinutes(readyInMinutes).setDietaryNeeds(dietaryNeeds)
        .setIngredientNames(ingredientNamesStrings).build();
        
      responseMap.put("savedRecipe", savedRecipeObject);
      responseMap.put("recipeIsSaved", true);
    }
    
    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /** saves recipe information in datastore */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String recipeIdStr = request.getParameter("recipe-id");
    Entity entity = new Entity("SavedRecipe", recipeIdStr);
    
    entity.setProperty("recipeId", Long.parseLong(recipeIdStr));

    String title = request.getParameter("recipe-title");
    entity.setProperty("title", title);

    String imageUrl = request.getParameter("image-url");
    entity.setProperty("imageUrl", imageUrl);

    String sourceUrl = request.getParameter("source-url");
    entity.setProperty("sourceUrl", sourceUrl);

    long servings = Long.parseLong(request.getParameter("servings"));
    entity.setProperty("servings", servings);

    long readyInMinutes = Long.parseLong(request.getParameter("ready-in-minutes"));
    entity.setProperty("readyInMinutes", readyInMinutes);

    String[] dietaryNeeds = retrieveLists(request, "dietary-needs");
    entity.setProperty("dietaryNeeds", Arrays.asList(dietaryNeeds));

    String[] ingredientNames = retrieveLists(request, "ingredient-names");
    entity.setProperty("ingredientNames", Arrays.asList(ingredientNames));
    
    datastore.put(entity);
  }
}

String[] retrieveLists(HttpServletRequest request, String query) {
  String[] tempList = request.getParameterValues(query);
  if(tempList == null) {
    tempList = new String[0];
  }
  return tempList;
}
