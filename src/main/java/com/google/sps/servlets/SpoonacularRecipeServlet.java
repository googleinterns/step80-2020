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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;  
import com.google.gson.Gson;

/** Returns recipe information from recipe id */
@WebServlet("/recipe")
public class SpoonacularRecipeServlet extends HttpServlet {
  private static final String spoonacularPrefix = "https://api.spoonacular.com/recipes";
  private static final String spoonacularAPIKey = "cd2269d31cb94065ad1e73ce292374a5";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String numRecipe = request.getParameter("numRecipe");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(spoonacularPrefix + "/" + numRecipe + "/information?apiKey=" + spoonacularAPIKey + "&includeNutrition=true");

    try {
      String recipeInfo = target.request(MediaType.APPLICATION_JSON).get(String.class);
      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(recipeInfo));
    } catch(Exception e){
      System.out.println(e);
    }
  }
}