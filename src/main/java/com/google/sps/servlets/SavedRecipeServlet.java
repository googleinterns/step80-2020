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
import java.util.List;

/** Servlet that posts and gets saved recipe information in Datastore */
@WebServlet("/saved-recipe")
public class SavedRecipeServlet extends HttpServlet {
  /** Gets SavedRecipe using the recipe's id */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
      String title = checkIfStringNull((String) entity.getProperty("title"));
      String imageUrl = checkIfStringNull((String) entity.getProperty("imageUrl"));
      String sourceUrl = checkIfStringNull((String) entity.getProperty("sourceUrl"));
      Long servings = checkIfLongNull((Long) entity.getProperty("servings"));
      Long readyInMinutes = checkIfLongNull((Long) entity.getProperty("readyInMinutes"));
      ArrayList<String> dietaryNeedsStrings = (ArrayList<String>) entity.getProperty("dietaryNeeds");
      dietaryNeedsStrings = nullToArrayList(dietaryNeedsStrings);

      ArrayList<String> ingredientNamesStrings = (ArrayList<String>) entity.getProperty("ingredientNames");
      ingredientNamesStrings = nullToArrayList(ingredientNamesStrings);
      
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
    JSONObject responseMap = new JSONObject();
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String recipeIdStr = request.getParameter("recipe-id");
    if (recipeIdStr != null) {
      Entity entity = new Entity("SavedRecipe", recipeIdStr);
    
      entity.setProperty("recipeId", Long.parseLong(recipeIdStr));

      String title = request.getParameter("recipe-title");
      entity.setProperty("title", title);

      String imageUrl = request.getParameter("image-url");
      entity.setProperty("imageUrl", imageUrl);

      String sourceUrl = request.getParameter("source-url");
      entity.setProperty("sourceUrl", sourceUrl);

      Long servings = strToLong(request.getParameter("servings"));
      entity.setProperty("servings", servings);

      Long readyInMinutes = strToLong(request.getParameter("ready-in-minutes"));
      entity.setProperty("readyInMinutes", readyInMinutes);

      String[] dietaryNeeds = retrieveLists(request, "dietary-needs");
      entity.setProperty("dietaryNeeds", Arrays.asList(dietaryNeeds));

      String[] ingredientNames = retrieveLists(request, "ingredient-names");
      entity.setProperty("ingredientNames", Arrays.asList(ingredientNames));
    
      datastore.put(entity);
    } else {
      String errorMessage = "User needs to input recipeId.";
      responseMap.put("error", errorMessage);
    }
    Gson gson = new Gson();   
    String json = gson.toJson(responseMap);
    response.setContentType("application");
    response.getWriter().println(json);
  }

  public Long strToLong(String str) {
    if (str == null) {
      return null;
    } else {
      return Long.parseLong(str);
    }
  }

  public List<String> convertToList(String[] valuesList) {
    if (valuesList == null) {
      return new ArrayList<>();
    } else {
      return Arrays.asList(valuesList);
    }
  }

  public ArrayList<String> nullToArrayList(ArrayList<String> valuesList) {
    if (valuesList == null) {
      return new ArrayList<>();
    }
    return valuesList;
  }

  public String checkIfStringNull(String str) {
    if (str == null) {
      return "";
    }
    return str;
  }

  public Long checkIfLongNull(Long num) {
    if (num == null) {
      return new Long(0);
    }
    return num;
  }
  
  String[] retrieveLists(HttpServletRequest request, String query) {
    String[] tempList = request.getParameterValues(query);
    if(tempList == null) {
      tempList = new String[0];
    }
    return tempList;
  }
}