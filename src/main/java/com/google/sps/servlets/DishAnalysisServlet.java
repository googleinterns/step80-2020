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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
<<<<<<< HEAD:src/main/java/com/google/sps/servlets/DishAnalysisServlet.java
=======
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType; 
import com.google.gson.Gson;

>>>>>>> Creates GET request to Spoonacular Server after detemining which label is most likely classification. Also adds lots of web design to home page:src/main/java/com/google/sps/servlets/VisionServlet.java

/** Servlet that uses VisionAPI to analyze uploaded images */
@WebServlet("/dishAnalysis")
public class DishAnalysisServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Initialize client used to send requests.
    try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
      // Get the file path from the form.
      String fileName = getParameter(request, "image", "");

      // Reads the image file into memory
      Path path = Paths.get(fileName);
      byte[] data = Files.readAllBytes(path);
      ByteString imgBytes = ByteString.copyFrom(data);

      // Builds the image annotation request
      List<AnnotateImageRequest> requests = new ArrayList<>();
      Image img = Image.newBuilder().setContent(imgBytes).build();
      Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).setMaxResults(7).build();
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
          return;
        }

        for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
          String label = annotation.getDescription();
          if (!label.equals("Cuisine") && !label.equals("Dish") && !label.equals("Food") && !label.equals("Ingredient") && !label.equals("Fried food")) {
            descriptors.add(annotation.getDescription());
          }
        }
      }
      
      // Make Spoonacular 'GET' request
      String query = descriptors.get(0);
      Client c = ClientBuilder.newClient();
      WebTarget target = c.target("https://recipe-search-step-2020.appspot.com/recipeInfo?dishName=" + query);

      try {
        String recipeInfo = target.request(MediaType.APPLICATION_JSON).get(String.class);
        Gson gson = new Gson();
        response.setContentType("application/json");
        response.getWriter().println(gson.toJson(recipeInfo));
      }
      catch(Exception e){
        System.out.println(e);
      }
    }
  }
  /* Returns parameter value given its name */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    return value;
  }
}

