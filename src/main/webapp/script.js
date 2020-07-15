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

/** Fetches information returned from Spoonacular (after the image has been classified appropriately) */
function getRecipeInfo() {
  const image = document.getElementById('image').files[0];
  const params = new FormData();
  params.append('image', image);
  const request = new Request('/dishAnalysis', {method: "POST", body: params});
  fetch(request).then(response => response.json()).then((recipeListInfoJson) => {
    //set top options to radio buttons
    document.getElementById("first-option-label").innerText = recipeListInfoJson[0];
    document.getElementById("second-option-label").innerText = recipeListInfoJson[1];
    //set radio button values
    document.getElementById("first-option").value = recipeListInfoJson[0];
    document.getElementById("second-option").value = recipeListInfoJson[1];
  });
}

/** Fetches and then populates nutrition information section of display page with average fat, calories, etc. */
function createNutritionElements() {
  dishName = sessionStorage.dishName;
  fetch('/dishNutrition?dishName='+dishName).then(response => response.json()).then((dish) => {
    dish = JSON.parse(dish);
    var title = document.getElementById("dish");
    title.setAttribute("data-rotate", dishName);

    var period = title.getAttribute('data-period');
    new TxtRotate(title, dishName, period);

    // Populate nutrition element
    var nutritionElement = document.getElementById("nutrition-info");
    Object.keys(dish).forEach(function(key) {
      if (key != "recipesUsed") {
        var node = document.createElement('div');
        node.className = 'nutrition-element';
        node.innerText = 'Average ' + key + ': ' + dish[key]['value'] + ' ' + dish[key]['unit'];
        nutritionElement.appendChild(node);
      }
    });
  });
  displayRecipes();
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

TxtRotate.prototype.tick = function() {
  var i = this.loopNum % this.toRotate.length;
  var fullTxt = this.toRotate;

  if (this.isDeleting) {
    this.txt = fullTxt.substring(0, this.txt.length - 1);
  } else {
    this.txt = fullTxt.substring(0, this.txt.length + 1);
  }

  this.el.innerHTML = '<span class="wrap">' + this.txt + '</span>';

  var that = this;
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
    that.tick();
  }, delta);
}

/** Display recipeList json stored in session storage */
function displayRecipes() {
  var recipeList = sessionStorage.recipeList;
  appendToDisplayElement(JSON.parse(recipeList));
}

/** Display saved recipes by tag name */
function savedRecipes() {
  const tagName = document.getElementById('select-tag').value;
  const displayRecipesElement = document.getElementById("display-recipes");
  displayRecipesElement.innerHTML = "";

  fetch('/tag?tagName=' + tagName).then(response => response.json()).then((tagJson) => {
    // get list of unique recipes from tagList
    const tagList = tagJson.filteredList;
    const recipeIdList = new Set(tagList.map(tag => tag.recipeId));

    const selectElement = document.getElementById("select-tag");
    selectElement.innerHTML = "<option value=''>All tags</option>";
    // get list of unique tag names from tagList
    tagJson.tagNames.forEach(tagName => {
      selectElement.appendChild(addTagOption(tagName));
    });
    
    // display recipes as cards
    recipeIdList.forEach(recipeId => { 
      getSavedRecipe(displayRecipesElement, recipeId);
    });
  });
}

function addTagOption(tag) {
  const optionElement = document.createElement('option');
  optionElement.value = tag;
  optionElement.innerHTML = tag;
  return optionElement;
}

/** Helper function to display recipe cards in display-recipes element */
function appendToDisplayElement(recipeList) {
  const displayRecipeElement = document.getElementById('display-recipes');
  displayRecipeElement.innerHTML = "";
  for (recipe of recipeList) {
    var pictureWrap = document.createElement('div');
    pictureWrap.className = 'dish-image-wrap';

    var picture = document.createElement('img');
    picture.className = 'dish-image';
    picture.src = recipe["image"];
    
    createRecipeElement(recipe, pictureWrap);

    var pictureText = document.createElement('button');
    pictureText.className = 'dish-image-text';
    pictureText.innerHTML = recipe["title"];

    displayRecipeElement.appendChild(pictureWrap);
    pictureWrap.appendChild(picture);
    pictureWrap.appendChild(pictureText);
  }
}

/*
// helper to create picture wrap for gallery display
function createPictureWrap(displayRecipeElement, recipe) {
  var recipeCard = createRecipeElement(recipe);
    recipeCard.className ='dish-recipe';
    receipeCard.style.display = 'none';

    var pictureWrap = document.createElement('div');
    pictureWrap.className = 'dish-image-wrap';

    var picture = document.createElement('img');
    picture.className = 'dish-image';
    picture.src = recipe["image"];

    var pictureText = document.createElement('button');
    pictureText.className = 'dish-image-text';
    pictureText.innerHTML = recipe["title"];
    pictureText.onclick = function() {
      recipeCard.style.display = "block";
    }

    displayRecipeElement.appendChild(pictureWrap);
    pictureWrap.appendChild(picture);
    pictureWrap.appendChild(pictureText);
    pictureWrap.appendChild(recipeCard);
}
*/

/* Slideshow that rotates through different background images */
function startSlideshow() {
  var images = new Array('/images/redbgr.jpg','/images/greenbgr.jpg','/images/yellowbgr.jpg', '/images/purplebgr.jpg', '/images/orangebgr.jpg');
  var count = images.length;
  document.body.style.backgroundImage = 'url("' + images[Math.floor(Math.random() * count)] + '")';
  setTimeout(startSlideshow, 5000);
}

/** Opens form for user to submit image of dish for anlysis on home page */
function openImageForm() {
  document.getElementById("popup").style.display = "block";
  document.getElementById("popup-button").style.display = "none";
  document.getElementById("upload").style.display = "none";
  document.getElementById("image-preview").style.display = "none";
}

/** Closes form for user to submit image of dish */
function closeImageForm() {
  document.getElementById("popup").style.display = "none";
  document.getElementById("popup-button").style.display = "inline-block";
}

/** Generates a preview of the user's uploaded image */
function previewImage(input) {
  if(input.files && input.files[0]) {
    preview = document.getElementById("image-preview")
    var reader = new FileReader();
    reader.onload = function (e) {
      preview.src = e.target.result;
      preview.style.display = "inline-block";
      document.getElementById("upload").style.display = "inline-block";
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

        const dietaryNeeds = profile.dietaryNeeds;
        dietaryNeeds.forEach(dietaryNeed => {
          switch(dietaryNeed) {
            case "VEGETARIAN":
              vegetarianElement.checked = true;
              break;
            case "VEGAN":
              veganElement.checked = true;
              break;
            case "GLUTENFREE":
              glutenFreeElement.checked = true;
              break;
            case "DAIRYFREE":
              dairyFreeElement.checked = true;
              break;
            default: 
              break;
          }
        });
        userNameElement.value = profile.userName;
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
  if (vegetarian) {
    params.append('dietary-needs', "VEGETARIAN");
  }
  if (vegan) {
    params.append('dietary-needs', "VEGAN");
  }
  if (glutenFree) {
    params.append('dietary-needs', "GLUTENFREE");
  }
  if (dairyFree) {
    params.append('dietary-needs', "DAIRYFREE");
  }
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

/** Function gets recipe information from user input ID and displays the title on the page */
function getRecipe(){
  var idRecipe = document.getElementById("num-recipe").value;
  fetch('/recipeInfo?idRecipe='+idRecipe).then(response => response.json()).then((recipeInfo) => {
    recipeInf = JSON.parse(recipeInfo);
    const recipeDisplayElement = document.getElementById('recipe-info');
    recipeDisplayElement.innerText = recipeInf["title"];
  });
}

/** Function gets recipe list from user input dish and displays the title of the first two returned results on the page */
function getRecipeId(){
  var dishName = document.getElementById("dish-name").value;
  fetch('/dishId?dishName='+dishName).then(response => response.json()).then(recipeId => {
    recipe = JSON.parse(recipeId);
    const recipeIdDisplayElement = document.getElementById('recipe-id-info');
    recipeIdDisplayElement.innerText = recipe[0]["title"] + "\n" + recipe[1]["title"];
  });
}

/** Call the appropriate functions needed on-load of the body of home page */
function loadHomePage() {
  startSlideshow();
  getLoginStatus();
}

/** Call the appropriate functions needed on-load of the body of display page */
function loadDisplayPage() {
  createNutritionElements();
  getLoginStatus();
}

/**
  * Checks with server if user has logged in.
  * Display corresponding text and url in login section if login is true/false.
  */
function getLoginStatus() {
  fetch('/login').then(response => response.json()).then((userInfo) => {
    const loginStatusElement = document.getElementById('login-status');
    const hoverMenuElement = document.getElementById('dropdown');

    if (userInfo.isLoggedIn) {
      const myProfileLink = document.createElement("a");
      myProfileLink.innerText = "Edit My Profile";
      myProfileLink.href="/profile.html";

      const taggedLink = document.createElement("a");
      taggedLink.innerText = "My Tagged Recipes";
      taggedLink.href="/board.html";

      const logoutLink = document.createElement("a");
      logoutLink.innerHTML = "Logout";
      logoutLink.href = userInfo.logoutUrl;

      hoverMenuElement.appendChild(myProfileLink);
      hoverMenuElement.appendChild(taggedLink);
      hoverMenuElement.appendChild(logoutLink);
      
    } else {
      const loginLink = document.createElement("a");
      loginLink.innerHTML = "Login";
      loginLink.href = userInfo.loginUrl;
      hoverMenuElement.appendChild(loginLink);
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
  recipe['servings'] = 1;
  recipe['readyInMinutes'] = 10;
  recipe['vegetarian'] = true;
  displayRecipeElement.appendChild(createRecipeElement(recipe));

  const recipe1 = {};
  recipe1['id'] = 2;
  recipe1['title'] = "Title 1";
  recipe1['image'] = "/images/salad.jpeg";
  recipe1['sourceUrl'] = "https://css-tricks.com/snippets/css/a-guide-to-flexbox/";
  recipe1['servings'] = 1;
  recipe1['readyInMinutes'] = 10;
  recipe1['vegan'] = true;
  displayRecipeElement.appendChild(createRecipeElement(recipe1));
}

/** Creates an element that represents a recipe card */
function createRecipeElement(recipe, pictureWrap) {
  var temp = document.querySelector("#recipe-template");;
  var clone = temp.content.cloneNode(true);
  
  const hoverElement = clone.querySelector(".recipe-card");

  const titleElement = clone.querySelector(".recipe-card-title");
  titleElement.innerText = recipe["title"];
  titleElement.href = recipe["sourceUrl"];

  const closeElement = clone.querySelector(".icon-remove-sign");
  closeElement.onclick = function() {
      hoverElement.style.display = "none";
      document.getElementById("display-recipes").style.opacity = "1";
  }
  const imageElement = clone.querySelector(".recipe-image");
  imageElement.src = recipe["image"];

  const alertElements = clone.querySelectorAll(".recipe-card-block")[1];
  createRecipeCardAlerts(recipe, alertElements);

  const servingElement = clone.querySelector(".recipe-card-servings");
  servingElement.innerHTML = "Serving Size: " + recipe["servings"];

  const timeElement = clone.querySelector(".recipe-card-time");
  timeElement.innerHTML = "Preparation Time: " + recipe["readyInMinutes"] + " minutes";
  
  const tagElements = clone.querySelector(".recipe-card-tags");
  createRecipeCardTags(recipe['id'], tagElements);

  const tagTextElement = clone.querySelector("textarea");

  const addTagElement = clone.querySelector(".add-tag-button");
  addTagElement.addEventListener('click', () => {
    const newTagName = (tagTextElement.value).trim();
    if (newTagName != "") {
      const params = new URLSearchParams();
      params.append('tag-name', newTagName);
      params.append('recipe-id', recipe['id']);

      fetch('/tag', {method: 'POST', body: params}).then(response => response.json()).then((tagList) => {
        tagElements.innerHTML = "";
        createRecipeCardTags(recipe['id'], tagElements);
        postSavedRecipe(recipe);
      });
    }
  });
  document.getElementById("card-gallery").appendChild(clone);
  pictureWrap.onclick = function() {
      hoverElement.style.display = "block";
      document.getElementById("display-recipes").style.opacity = "0.2";
  }
}

/** Get profile information to determine which alerts to create */
function createRecipeCardAlerts(recipe, alertElements) {
  const dietList = ['vegetarian', 'vegan', 'glutenFree', 'dairyFree'];
  const iconMap = {
    'vegetarian': 'icon-leaf',
    'vegan': 'icon-exclamation',
    'glutenFree': 'icon-warning-sign',
    'dairyFree': 'icon-coffee'
  };
  const warningMap = {
    'vegetarian': 'Non-Vegetarian Alert',
    'vegan': 'Non-Vegan Alert',
    'glutenFree': 'Non-GlutenFree Alert',
    'dairyFree': 'Non-DairyFree Alert'
  };
  
  fetch('/profile').then(response => response.json()).then((message) => {
    if (message.hasProfile) {
      const profile = message.profile;
      const dietaryNeeds = profile.dietaryNeeds;
      if (dietaryNeeds != null) {
        const alertContainer = document.getElementById("recipe-alert-block");
        alertContainer.innerText = "Dietary Alerts";
      }
      dietaryNeeds.forEach(dietaryNeed => {
        switch(dietaryNeed) {
          case "VEGETARIAN":
            if (!recipe["vegetarian"]) {
              alertElements.appendChild(createAlertElement(iconMap['vegetarian'], warningMap['vegetarian']));
            }
            break;
          case "VEGAN":
            if (!recipe["vegan"]) {
              alertElements.appendChild(createAlertElement(iconMap['vegan'], warningMap['vegan']));
            }
            break;
          case "GLUTENFREE":
            if (!recipe["glutenFree"]) {
              alertElements.appendChild(createAlertElement(iconMap['glutenFree'], warningMap['glutenFree']));
            }
            break;
          case "DAIRYFREE":
            if (!recipe["dairyFree"]) {
              alertElements.appendChild(createAlertElement(iconMap['dairyFree'], warningMap['dairyFree']));
            }
            break;
          default: 
            break;
        }
      });

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
      if (ifStringMatch(ingredient['name'], allergy)) {
        allergyList.push(allergy);
        break;
      }
    }
  }
  return allergyList;
}

// return true if ingredient is a matching string to allergy
function ifStringMatch(ingredient, allergy) {
  const allergyWordBase = stripEnding(allergy);
  const pattern = '.*' + allergyWordBase + '.*';
  const regex = new RegExp(pattern);
  return regex.test(ingredient);
}

// strip common endings of input word
function stripEnding(str) {
  const strLength = str.length;
  if (str.substring(strLength-3, strLength) == "ies") {
    return str.substring(0, strLength-3);
  } else if (str.substring(strLength-1, strLength) == "s" || str.substring(strLength-1, strLength == "y")) {
    return str.substring(0, strLength-1);
  }
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
function createRecipeCardTags(recipeId, tagElements) {
  fetch('/tag?recipeId=' + recipeId).then(response => response.json()).then((tagJson) => {
    tagJson.filteredList.forEach(tag => tagElements.appendChild(createTagElement(tag)));
  });
}

/** Creates an element that represents a tag. */
function createTagElement(tag) {
  var temp = document.querySelector("#tag-template");;
  var clone = temp.content.cloneNode(true);

  const tagElement = clone.querySelector(".recipe-tag");
  
  const titleElement = clone.querySelector('span');
  titleElement.innerText = tag.tagName;

  const deleteButtonElement = clone.querySelector('button');
  deleteButtonElement.addEventListener('click', () => {
    const params = new URLSearchParams();
    params.append('tag-id', tag.tagId);

    fetch('/delete-tag', {method: 'POST', body: params}).then(() => {
      // Remove the tag from the DOM.
      tagElement.remove();
    });
  });

  return clone;
}

// post recipe information to servlet
function postSavedRecipe(recipe) {
  const params = new URLSearchParams();
  params.append('recipe-id', recipe['id']);
  params.append('recipe-title', recipe['title']);
  params.append('image-url', recipe['image']);
  params.append('source-url', recipe['sourceUrl']);
  params.append('servings', recipe['servings']);
  params.append("ready-in-minutes", recipe['readyInMinutes']);
  if (recipe['vegetarian']) {
    params.append('dietary-needs', "VEGETARIAN");
  }
  if (recipe['vegan']) {
    params.append('dietary-needs', "VEGAN");
  }
  if (recipe['glutenFree']) {
    params.append('dietary-needs', "GLUTENFREE");
  }
  if (recipe['dairyFree']) {
    params.append('dietary-needs', "DAIRYFREE");
  }
  fetch('/saved-recipe', {method: 'POST', body: params});
}

// display recipe card element with information if saved recipe exists
function getSavedRecipe(displayRecipesElement, recipeId) {
  fetch('/saved-recipe?recipeId=' + recipeId).then(response => response.json()).then((savedRecipeJson) => {
    // check if recipe information is saved in datastore
    if (savedRecipeJson.recipeIsSaved) {
      const savedRecipe = savedRecipeJson.savedRecipe;

      // modify json to allow dietary needs to be displayed on recipe card
      const dietaryNeeds = savedRecipe.dietaryNeeds;
      dietaryNeeds.forEach(dietaryNeed => {
        switch(dietaryNeed) {
          case "VEGETARIAN":
            savedRecipe['vegetarian'] = true;
            break;
          case "VEGAN":
            savedRecipe['vegan'] = true;
            break;
          case "GLUTENFREE":
            savedRecipe['glutenFree'] = true;
            break;
          case "DAIRYFREE":
            savedRecipe['dairyFree'] = true;
            break;
          default: 
            break;
        }
      });

      // display recipe card using the recipe's saved information
      displayRecipesElement.append(createRecipeElement(savedRecipe));
      // createPictureWrap(displayRecipesElement, savedRecipe);
    }
  });
}

/** Reads dishname, fetches recipe information, and stores both in serssionStorage to use in display.html */
function readUserDishChoice() {
  var dishName = (document.forms.dishFitChoice.elements.labelFitChoice.value).split(" ").join("+");
  if(dishName != null){
    fetch('/recipeInfo?dishName=' + dishName).then(response => response.json()).then((recipeListInfoJson) => {
      sessionStorage.dishName = dishName;
      sessionStorage.recipeList = recipeListInfoJson;
      window.location.href = "/display.html";
    });
  }
}