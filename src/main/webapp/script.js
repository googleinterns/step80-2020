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

// Fetches information returned from Spoonacular (after the image has been classified appropriately)
function getRecipeInfo() {
  const image = document.getElementById('image').files[0];
  const params = new FormData();
  params.append('image', image);
  const request = new Request('/dishAnalysis', {method: "POST", body: params});
  fetch(request).then(response => response.json()).then((recipeListInfoJson) => {
    sessionStorage.recipeList = JSON.parse(recipeListInfoJson);
    window.location.href = "/display.html";
  });
}

/** Fetches and then populates nutrition information section of display page with average fat, calories, etc. */
function createNutritionElements() {
  fetch('/dishNutrition?dishName='+dishName).then(response => response.json()).then((dish) => {
    // Get dish name
    var dishName = document.forms.dishFitChoice.elements.labelFitChoice.value;
    if (dishName != null) {
      title.setAttribute("data-rotate", dishName);
    }

    // Populate nutrition element
    var nutritionElement = document.getElementById(".nutrition-info");
    Object.keys(dish).forEach(function(key) {
      var node = document.createElement('div');
      node.className = 'nutrition-element';
      node.innerText = 'Average' + key + ': ' + dish[key]['value'] + ' ' + dish[key]['units'];
      nutritionElement.appendChild(node);
    });
  });
}

/** Creates text typing animation */
window.onload = function() {
  var text_element = document.getElementById('dish');
  var toRotate = text_element.getAttribute('data-rotate');
  console.log("---1.----" + toRotate);
  var period = text_element.getAttribute('data-period');
  if (toRotate != null) {
    new TxtRotate(text_element, toRotate, period);
  }
}

var TxtRotate = function(el, toRotate, period) {
  this.toRotate = toRotate;
  this.el = el;
  this.loopNum = 0;
  this.period = parseInt(period, 10) || 2000;
  this.txt = '';
  this.tick();
  this.isDeleting = false;
};

/* Print, wait the period, then delete */
TxtRotate.prototype.tick = function() {
  var i = this.loopNum % this.toRotate.length;
  var fullTxt = this.toRotate;

  if (this.isDeleting) {
    this.txt = fullTxt.substring(0, this.txt.length - 1);
  } else {
    this.txt = fullTxt.substring(0, this.txt.length + 1);
  }

  this.el.innerHTML = '<span class="wrap">' + this.txt + '</span>';

  var delta = 300 - Math.random() * 100;

  if (this.isDeleting) { delta /= 2; }

  if (!this.isDeleting && this.txt === fullTxt) {
    delta = this.period;
    this.isDeleting = true;
  } else if (this.isDeleting && this.txt === '') {
    this.isDeleting = false;
    this.loopNum++;
    delta = 400;
  }

  setTimeout(function() {
    this.tick();
  }, delta);
}

/** at display.html onload, display recipeList json stored in session storage */
function displayRecipes() {
  var recipeList = JSON.parse(sessionStorage.recipeList);

  const displayRecipeElement = document.getElementById('display-recipes');
  displayRecipeElement.innerHTML = "";
  for (recipe of recipeList) {
    displayRecipeElement.appendChild(createRecipeElement(recipe));
  }
}

/* Slideshow that rotates through different background images */
function startSlideshow() {
    var images = new Array('/images/redbgr.jpg','/images/greenbgr.jpg','/images/yellowbgr.jpg', '/images/purplebgr.jpg', '/images/orangebgr.jpg');
    var count = images.length;
    document.body.style.backgroundImage = 'url("' + images[Math.floor(Math.random() * count)] + '")';
    setTimeout(startSlideshow, 5000);
}

/* Opens form for user to submit image of dish for anlysis on home page */
function openImageForm() {
  document.getElementById("popup").style.display = "block";
  document.getElementById("popup-button").style.display = "none";
}

/* Closes form for user to submit image of dish */
function closeImageForm() {
  document.getElementById("popup").style.display = "none";
  document.getElementById("popup-button").style.display = "block";
}

/* Generates a preview of the user's uploaded image */
function previewImage(input) {
  if(input.files && input.files[0]) {
    var reader = new FileReader();
    reader.onload = function (e) {
      document.getElementById("image-preview").src = e.target.result;
    };
    reader.readAsDataURL(input.files[0]);
  }
}

/** Fetches profile from server and displays the information to user */
function getProfile() {
  getLoginStatus();
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
  const userName = document.getElementById('name-entry').value.trim();
  const vegetarian = document.getElementById("vegetarian-checkbox").checked;
  const vegan = document.getElementById("vegan-checkbox").checked;
  const glutenFree = document.getElementById("gluten-checkbox").checked;
  const dairyFree = document.getElementById("dairy-checkbox").checked;

  const allergiesString = document.getElementById("allergies-entry").value;
  const allergies = allergiesString.split(",").map(allergy => allergy.toLowerCase().trim());
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

/** Gets recipe id list from query string */
function getRecipeId(){
  var dishName = document.getElementById("dish-name").value;
  fetch('/dishId?dishName='+dishName).then(response => response.json()).then((recipeId) => {
    recipe = JSON.parse(recipeId);
    const recipeIdDisplayElement = document.getElementById('recipe-id-info');
    console.log(recipeId);
    recipeIdDisplayElement.innerText = recipe["results"];
  });
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
  
  const recipe = {}
  recipe['id'] = 1;
  recipe['title'] = "Title";
  recipe['image'] = "/images/salad.jpeg";
  recipe['sourceUrl'] = "https://css-tricks.com/snippets/css/a-guide-to-flexbox/";
  recipe['vegetarian'] = true;
  displayRecipeElement.appendChild(createRecipeElement(recipe));
}

/** Creates an element that represents a recipe card */
function createRecipeElement(recipe) {
  var temp = document.querySelector("#recipe-template");;
  var clone = temp.content.cloneNode(true);
  
  const titleElement = clone.querySelector(".recipe-card-title");
  titleElement.innerText = recipe["title"];

  const imageElement = clone.querySelector(".recipe-image");
  imageElement.src = recipe["image"];

  const linkElement = clone.querySelector('a');
  linkElement.href = recipe["sourceUrl"];
  linkElement.innerHTML = recipe["sourceUrl"];

  const alertElements = clone.querySelectorAll(".recipe-card-block")[1];
  createRecipeCardAlerts(recipe, alertElements);
  
  const tagElements = clone.querySelector(".recipe-card-tags");
  createRecipeCardTags(recipe, tagElements);

  const tagTextElement = clone.querySelector("textarea");

  const addTagElement = clone.querySelector(".add-tag-button");
  addTagElement.addEventListener('click', () => {
    const newTagName = (tagTextElement.value).trim();
    if (newTagName != "") {
      // TODO: will eventually have post request to add tags
      tagElements.appendChild(createTagElement(newTagName));
    }
  });

  return clone;
}

/** Get profile information to determine which alerts to create */
function createRecipeCardAlerts(recipe, alertElements) {
  fetch('/profile').then(response => response.json()).then((message) => {

    if (message.hasProfile) {
      const profile = message.profile;
      if (profile.vegetarian && !recipe["vegetarian"]) {
        alertElements.appendChild(createAlertElement("icon-leaf", "Non-Vegetarian Alert"));
      }
      if (profile.vegan && !recipe["vegan"]) {
        alertElements.appendChild(createAlertElement("icon-exclamation", "Non-Vegan Alert"));
      }
      if (profile.glutenFree && !recipe["glutenFree"]) {
        alertElements.appendChild(createAlertElement("icon-warning-sign", "Non-GlutenFree Alert"));
      }
      if (profile.dairyFree && !recipe["dairyFree"]) {
        alertElements.appendChild(createAlertElement("icon-coffee", "Non-DairyFree Alert"));
      }

      const allergyList = allergyAlertList(recipe['extendedIngredients'], profile.allergies);
      if (allergyList.length > 0) {
        alertElements.appendChild(createAlertElement("icon-food", "The following allergies have been seen: " + allergyList.join(", "))); 
      }
    }
  });
}

// Loop through recipe ingredients to find food allergies
function allergyAlertList(ingredients, allergies) {
  var allergyList = [];
  for (allergy of allergies) {
    for (ingredient of ingredients) {
      if (ingredient['name'].includes(allergy)) {
        allergyList.push(allergy);
        break;
      }
    }
  }
  return allergyList;
}

/** Creates an element that represents an alert */
function createAlertElement(iconName, innerText) {
  var temp = document.querySelector("#alert-template");;
  var clone = temp.content.cloneNode(true);

  const alertElement = clone.querySelector(".recipe-alert");

  const textElement = clone.querySelector('.alert-text');
  textElement.innerText = innerText;

  const iconElement = document.createElement('i');
  iconElement.className = iconName;
  alertElement.insertBefore(iconElement, textElement);
  
  return clone;
}

/** Get user's tags for recipe */
function createRecipeCardTags(recipe, tagElements) {
  // TODO: Will eventually have get request to server
  tagElements.appendChild(createTagElement("Favorite"));
  tagElements.appendChild(createTagElement("Dinner"));
}

/** Creates an element that represents a tag. */
// TODO: will eventually incldue recipe id as parameter for tag deletion
function createTagElement(tag) {
  var temp = document.querySelector("#tag-template");;
  var clone = temp.content.cloneNode(true);

  const tagElement = clone.querySelector(".recipe-tag");
  
  const titleElement = clone.querySelector('span');
  titleElement.innerText = tag;

  const deleteButtonElement = clone.querySelector('button');
  deleteButtonElement.addEventListener('click', () => {
    // Remove the tag from the DOM.
    tagElement.remove();
    // TODO: will eventually have post request to delete tags
  });

  return clone;
}

/** Add new tag associated with recipe to datastore */
function addTagForRecipe() {
  // TODO
}

/** Delete tag associated with recipe in datastore */
function deleteTagForRecipe() {
  // TODO
}
