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

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType; 
import com.google.gson.Gson;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import com.google.common.collect.ImmutableSet;

/**
* Servlet that completes a post request by accepting image input, classifying that image using 
* the Vision API, and then returns json recipe/nutritional data through the Spoonacular API
*/

/** Servlet that uses VisionAPI to analyze uploaded images */

@MultipartConfig
@WebServlet("/dishAnalysis")
public class DishAnalysisServlet extends HttpServlet {
  
  // Initialize blocked catagories
  private static final int MAX_RESULT = 7;
  private final ImmutableSet<String> BLOCKED_CATAGORIES = ImmutableSet.of("Cuisine", "Dish", "Food", "Ingredient", "Salad", "Fried food");

  // Initialize blocked catagories
  private static Set<String> blockedCatagories = ImmutableSet.copyOf(new HashSet<String>(Arrays.asList("Cuisine", "Dish", "Food", "Ingredient", "Salad", "Fried food")));
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // Initialize client used to send requests.
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
      // Get the image file
      Part filePart = request.getPart("image");
      InputStream fileContent = filePart.getInputStream();
      ByteString imgBytes = ByteString.readFrom(fileContent);

      // Builds the image annotation request
      List<AnnotateImageRequest> requests = new ArrayList<>();
      Image img = Image.newBuilder().setContent(imgBytes).build();
      Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).setMaxResults(MAX_RESULT).build();
      AnnotateImageRequest new_request =
          AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
      requests.add(new_request);

      // Performs label detection on the image file
      BatchAnnotateImagesResponse new_response = vision.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = new_response.getResponsesList();

      //Save and sort the descriptors
      List<String> descriptors = new ArrayList<>();
      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          continue;
        }

        for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
          String label = annotation.getDescription();
          if (!BLOCKED_CATAGORIES.contains(label)) {
            descriptors.add(annotation.getDescription());
          }
        }
      }

      Gson gson = new Gson();
      response.setContentType("application/json");
      response.getWriter().println(gson.toJson(descriptors));
      //Saving this block just in case - should be replaced with a fetch in js
      /*
      // Make Spoonacular 'GET' request
      String query = descriptors.get(0);
      Client client = ClientBuilder.newClient();
      WebTarget target = client.target("https://recipe-search-step-2020.appspot.com/recipeInfo?dishName=" + query);

      try {
        String recipeInfo = target.request(MediaType.APPLICATION_JSON).get(String.class);
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.getWriter().println(gson.toJson(recipeInfo));
      } catch(Exception e){
        System.out.println(e);
      }
      */
    }
    
  }

  /* Returns parameter value given its name */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    return value;
  }
}