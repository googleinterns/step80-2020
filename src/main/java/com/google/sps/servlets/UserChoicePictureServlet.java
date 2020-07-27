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

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/** Servlet to take in dish name and return dish picture */
@WebServlet("/dishChoicePictures")
public class UserChoicePictureServlet extends HttpServlet {
  private static final String PREFIX_KNOWLEDGE_GRAPH = "https://kgsearch.googleapis.com/v1/entities:search";
  private static final String IMAGE_NOT_FOUND = "https://cdn.iconscout.com/icon/premium/png-512-thumb/no-data-found-1965030-1662565.png";

  /** Uses Knowledge Graph API to get images from two queried dish names */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("apiKey")
        .setFilter(new Query.FilterPredicate("keyName", Query.FilterOperator.EQUAL, "knowledgeGraph"));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    String knowledgeGraphKey = (String) entity.getProperty("key");
    String firstOption = (String) request.getParameter("firstOption");
    String secondOption = (String) request.getParameter("secondOption");
    String firstImageURL = getImageUrl(firstOption, knowledgeGraphKey);
    String secondImageURL = getImageUrl(secondOption, knowledgeGraphKey);
    String imageURLs = String.format("{\"firstURL\": \"%s\", \"secondURL\": \"%s\"}", firstImageURL, secondImageURL);
    System.out.println(imageURLs);
    response.setContentType("application/json");
    response.getWriter().println(imageURLs);
  }

  String getImageUrl(String dishName, String knowledgeGraphKey){
    Client client = ClientBuilder.newClient();
    try {
      dishName = URLEncoder.encode(dishName);
    } catch (Exception e) {
      dishName = "";
      System.out.println(e);
    }
    WebTarget target = client.target(String.format("%s?query=%s&key=%s&types=image", PREFIX_KNOWLEDGE_GRAPH, dishName, knowledgeGraphKey));
    try {
      String searchInfoJSON = target.request(MediaType.APPLICATION_JSON).get(String.class);
      JSONObject searchInfo = new JSONObject(searchInfoJSON);
      System.out.println(searchInfo);
      JSONArray searchResultsArray = new JSONArray(searchInfo.get("itemListElement").toString());
      for(int i = 0; i < searchResultsArray.length(); i++) {
        JSONObject searchResult = new JSONObject(searchResultsArray.get(i).toString());
        JSONObject searchResultResult = new JSONObject(searchResult.get("result").toString());
        System.out.println(searchResultResult);
        if(searchResultResult.has("image")){
          JSONObject imageInfo = new JSONObject(searchResultResult.get("image").toString());
          String imageURL = (String) imageInfo.get("contentUrl");
          System.out.println(imageURL);
          return imageURL;
        }
      }
      return IMAGE_NOT_FOUND;
    } catch(Exception e){
      System.out.println(e);
      return "";
    }
  }
}