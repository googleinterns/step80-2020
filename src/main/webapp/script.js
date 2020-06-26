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

/** Fetches profile from server and displays the information to user */
function getProfile() {
  fetch('/profile').then(response => response.json()).then((message) => {
    if (message.error == null) {
      if (message.hasProfile) {
        const profile = message.profile;

        const userNameElement = document.getElementById('name-entry');
        const vegetarianElement = document.getElementById("vegetarian-checkbox");
        const veganElement = document.getElementById("vegan-checkbox");
        const glutenFreeElement = document.getElementById("gluten-checkbox");
        const dairyFreeElement = document.getElementById("dairy-checkbox");
        const allergiesStringElement = document.getElementById("allergies-entry");

        userNameElement.value = profile.userName;
        vegetarianElement.checked = profile.vegetarian;
        veganElement.checked = profile.vegan;
        glutenFreeElement.checked = profile.glutenFree;
        dairyFreeElement.checked = profile.dairyFree;
        allergiesStringElement.value = (profile.allergies).join(", ");
      }
      
    } else {
      alert(message.error);
    }
  });
}

/** Posts profile information from form to server */
function postProfile() {
  const userName = document.getElementById('name-entry').value;
  const vegetarian = document.getElementById("vegetarian-checkbox").checked;
  const vegan = document.getElementById("vegan-checkbox").checked;
  const glutenFree = document.getElementById("gluten-checkbox").checked;
  const dairyFree = document.getElementById("dairy-checkbox").checked;

  const allergiesString = document.getElementById("allergies-entry").value;
  const allergies = allergiesString.split(",").map(allergy => allergy.trim());

  const params = new URLSearchParams();
  params.append('userName', userName);
  params.append('vegetarian', vegetarian);
  params.append('vegan', vegan);
  params.append('glutenFree', glutenFree);
  params.append('dairyFree', dairyFree);
  params.append('allergies', allergies);

  fetch('/profile', {method: 'POST', body: params}).then(response => response.json()).then((message) => {
    const profileStatusElement = document.getElementById('saved-profile-status');
    if (message.error != null) {
      alert(message.error);
    } else {
      profileStatusElement.style.display = "block";
    }
  });
}

function clearSavedProfileStatus() {
  const profileStatusElement = document.getElementById('saved-profile-status');
  profileStatusElement.style.display = "none";
}

/**
  * Checks with server if user has logged in.
  * Display corresponding text and url in login section if login is true/false.
  */
function getLoginStatus() {
  fetch('/login').then(response => response.json()).then((userInfo) => {
    const loginStatusElement = document.getElementById('login-section');
    loginStatusElement.innerHTML = "";

    if (userInfo.isLoggedIn) {
      const logoutElement = document.createElement('a');
      logoutElement.innerHTML = "Logout";
      logoutElement.href = userInfo.logoutUrl;

      const textElement = document.createElement('p');
      if (userInfo.hasProfile) {
        textElement.innerHTML = "Welcome, <strong>" + userInfo.userName + "</strong>";
      } else {
        textElement.innerHTML = "Welcome! Remember to create a profile!";
      }

      loginStatusElement.appendChild(logoutElement);
      loginStatusElement.appendChild(textElement);
      
    } else {
      const loginElement = document.createElement('a');
      loginElement.innerHTML = "Login";
      loginElement.href = userInfo.loginUrl;

      const textElement = document.createElement('p');
      textElement.innerHTML = "Hello!";

      loginStatusElement.appendChild(loginElement);
      loginStatusElement.appendChild(textElement);
    }
  });

}

// test function for displaying recipes
function hardCodedRecipeCard() {
  const displayRecipeElement = document.getElementById('display-recipes');
  displayRecipeElement.innerHTML = "";
  // recipes.forEach(recipe => displayRecipeElement.appendChild(createRecipeElement(recipe)));
  displayRecipeElement.appendChild(createRecipeElement("Placeholder for recipe json"));
}

/** Creates an element that represents a recipe card */
// currently only has hardcoded values
function createRecipeElement(recipe) {
  const recipeElement = document.createElement('div');
  recipeElement.className = 'recipe-card';

  const titleElement = document.createElement('p');
  titleElement.innerText = "Title"; // replace with recipe title
  titleElement.className = "recipe-card-title";
  recipeElement.appendChild(titleElement);

  const recipeTableElement = document.createElement('p');
  recipeTableElement.className = "recipe-card-table";

  const infoElements = document.createElement('div');
  infoElements.className = 'recipe-card-block';

  const imageElement = document.createElement('img');
  imageElement.src = "/images/salad.jpeg"; // replace with recipe image url
  infoElements.appendChild(imageElement);

  const linkElement = document.createElement('a');
  linkElement.href = "https://www.w3schools.com/jsref/dom_obj_image.asp"; // replace with recipe instructions url
  linkElement.innerHTML = "<br/>random link";
  infoElements.appendChild(linkElement);

  recipeTableElement.appendChild(infoElements);

  recipeTableElement.appendChild(createRecipeCardAlerts(recipe));
  recipeElement.append(recipeTableElement);
  
  const tagElements = createRecipeCardTags(recipe);
  recipeElement.append(tagElements);

  const tagTableElement = document.createElement('p');
  tagTableElement.className = "recipe-card-table";

  const tagTextElement = document.createElement('textarea');
  tagTableElement.appendChild(tagTextElement);

  const addTagElement = document.createElement('button');
  addTagElement.innerText = '+';
  addTagElement.className = "add-tag-button";
  addTagElement.addEventListener('click', () => {
    const newTagName = (tagTextElement.value).trim();
    if (newTagName != "") {
      // TODO: will eventually have post request to add tags
      tagElements.appendChild(createTagElement(newTagName));
    }
  });
  tagTableElement.append(addTagElement);
  recipeElement.appendChild(tagTableElement);

  return recipeElement;
}

/** Get profile information to determine which alerts to create */
function createRecipeCardAlerts(recipe) {
  const alertElements = document.createElement('div');
  alertElements.className = 'recipe-card-block';
  
  fetch('/profile').then(response => response.json()).then((message) => {

    if (message.hasProfile) {
      const profile = message.profile;
      // TODO: need comparisons against recipe json
      // profile.vegetarian
      // profile.vegan
      // profile.glutenFree
      // profile.dairyFree
      // profile.allergies

      alertElements.appendChild(createAlertElement("icon-warning-sign", "Dietary Alert"));
      alertElements.appendChild(createAlertElement("icon-exclamation", "Dietary Alert"));
      alertElements.appendChild(createAlertElement("icon-leaf", "Non-Vegetarian Alert"));
      alertElements.appendChild(createAlertElement("icon-coffee", "Non-DairyFree Alert"));
      alertElements.appendChild(createAlertElement("icon-food", "Allergies Alert"));
    }
  });
  return alertElements;
}

/** Creates an element that represents an alert */
function createAlertElement(iconName, innerText) {
  const alertElement = document.createElement('p');
  alertElement.className = 'recipe-alert';

  const iconElement = document.createElement('i');
  iconElement.className = iconName;
  alertElement.appendChild(iconElement);

  const textElement = document.createElement('div');
  textElement.innerText = innerText;
  alertElement.appendChild(textElement);
  
  return alertElement;
}

/** Get user's tags for recipe */
function createRecipeCardTags(recipe) {
  // TODO: Will eventually have get request to server
  const tagElements = document.createElement('div');
  tagElements.className = "recipe-card-tags";
  tagElements.appendChild(createTagElement("Favorite"));
  tagElements.appendChild(createTagElement("Dinner"));
  return tagElements;
}

/** Creates an element that represents a tag. */
// TODO: will eventually incldue recipe id as parameter for tag deletion
function createTagElement(tag) {
  const tagElement = document.createElement('div');
  tagElement.className = 'recipe-tag';

  const titleElement = document.createElement('span');
  titleElement.innerText = tag;

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'X';
  deleteButtonElement.className = "delete-tag-button";
  deleteButtonElement.addEventListener('click', () => {
    // Remove the tag from the DOM.
    tagElement.remove();
    // TODO: will eventually have post request to delete tags
  });

  tagElement.appendChild(titleElement);
  tagElement.appendChild(deleteButtonElement);
  return tagElement;
}

/** Add new tag associated with recipe to datastore */
function addTagForRecipe() {
  // TODO
}

/** Delete tag associated with recipe in datastore */
function deleteTagForRecipe() {
  // TODO
}