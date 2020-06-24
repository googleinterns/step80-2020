<%--
Copyright 2019 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>

<%-- The Java code in this JSP file runs on the server when the user navigates
     to the homepage. This allows us to insert the Blobstore upload URL into the
     form without building the HTML using print statements in a servlet. --%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<% BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/vision"); %>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Image Upload</title>
    <link rel="stylesheet" href="style.css">
    <script src="script.js"></script>
  </head>
  <body class="home-body">
    <h2>Popup Form</h2>
    <button id="popup-button" class="open-button" onclick="openImageForm()">Analyze Your Dish</button>
    <div class="form-popup" id="popup">
      <form method="POST" action="<%= uploadUrl %>" id="form" class="form-container" enctype="multipart/form-data">
        <input type="file" id="image" name="image" accept="image/png, image/jpeg" multiple="false" onchange="preview(this);">
        <img class="image-preview" id="image-preview" >
        <br>
        <button type="submit" class="btn">Upload</button>
        <button type="submit" class="btn cancel" onclick="closeImageForm()">Cancel</button>
      </form>
    </div>
  </body>
</html>