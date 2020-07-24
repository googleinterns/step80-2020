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
  const overlay = document.getElementById('overlay');
  overlay.style.display = 'block';
  const image = document.getElementById('image').files[0];
  const params = new FormData();
  params.append('image', image);
  const request = new Request('/dishAnalysis', {method: "POST", body: params});
  fetch(request).then(response => response.json()).then((recipeListInfoJson) => {
    if(recipeListInfoJson["length"] > 1) {
      sessionStorage.optionOne = recipeListInfoJson[0];
      sessionStorage.optionTwo = recipeListInfoJson[1];
      window.location.href = "/selection.html";
    } else {
      // TODO: Add alert of too few labels for image: should not trigger often
      window.location.href = "/index.html";
    }
  });
}

function setRadioButtonValues() {
  getLoginStatus();
  const firstOption = sessionStorage.optionOne;
  const secondOption = sessionStorage.optionTwo;
  const overlay = document.getElementById('overlay');
  overlay.style.display = 'block';
  fetch('/dishChoicePictures?firstOption='+firstOption+'&secondOption='+secondOption).then(response => response.json()).then((imageURLs) => {
    document.getElementById("first-option-image").src = imageURLs["firstURL"];
    document.getElementById("second-option-image").src = imageURLs["secondURL"];
    // set top options to radio buttons
    document.getElementById("first-option-label").innerText = firstOption;
    document.getElementById("second-option-label").innerText = secondOption;
    // set radio button values
    document.getElementById("first-option").value = firstOption;
    document.getElementById("second-option").value = secondOption;
    overlay.style.display = 'none';
  });
}

/** Fetches and then populates nutrition information section of display page with average fat, calories, etc. */
function createNutritionElements() {
  dishName = sessionStorage.dishName;
  fetch('/dishNutrition?dishName='+dishName).then(response => response.json()).then((dishNutrition) => {
    dishNutrition = JSON.parse(dishNutrition);
    var title = document.getElementById("dish");
    title.setAttribute("data-rotate", dishName);
    var period = title.getAttribute('data-period');
    new TxtRotate(title, dishName, period);

    // Populate nutrition element
    var nutritionElement = document.getElementById("nutrition-info");

    Object.keys(dishNutrition).forEach(function(key) {
      if (key != "recipesUsed") {
        var node = document.createElement('div');
        node.className = 'nutrition-element';
        node.innerText = 'Average ' + key + ': ' + dishNutrition[key]['value'] + ' ' + dishNutrition[key]['unit'];
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

/** Call the appropriate functions needed on-load of the body of the tagged recipes page */
function loadBoardPage() {
  savedRecipes("");
  refreshTagNameSelection();
  getLoginStatus('board.html');
}

/** Call the appropriate functions needed on-load of the body of the favorites page */
function loadFavoritesPage() {
  getLoginStatus("favorites.html");
  const displayRecipesElement = document.getElementById("display-recipes");
  displayRecipesElement.innerHTML = "";
  fetch('/favorite').then(response => response.json()).then((message) => {
    if (message.error == null) {
      const recipeIdList = message.recipeList;

      // display recipes as cards
      recipeIdList.forEach(recipeId => { 
        getSavedRecipe(displayRecipesElement, recipeId);
      });
    } else {
      alert(message.error);
    }
  });
}

/** Display saved recipes by tag names that are checked in tag selection */
function multipleSavedRecipes() {
  const checkedcheckboxes = document.querySelectorAll('input[name="tag-menu-checkbox"]:checked');
  var tagNames = [];
  checkedcheckboxes.forEach((checkbox) => {
    tagNames.push(checkbox.value);
  });
  var tagNameParameters = "";
  if (tagNames.length > 0) {
    tagNameParameters += "?";
    for (var i = 0; i < tagNames.length-1; i++) {
      tagNameParameters += "tag-names=" + tagNames[i] + "&";
    }
    tagNameParameters += "tag-names=" + tagNames[tagNames.length-1];
  }
  const displayRecipesElement = document.getElementById("display-recipes");
  displayRecipesElement.innerHTML = "";

  fetch('/multiple-tags' + tagNameParameters).then(response => response.json()).then((tagJson) => {
    // get list of unique recipes from tagJson
    if (tagJson.error == null) {
      const recipeList = tagJson.recipeList;
    
      // display recipes as cards
      recipeList.forEach(recipeId => { 
        getSavedRecipe(displayRecipesElement, recipeId);
      });
    } else {
      alert(tagJson.error);
    }  
  });
}

/** Display saved recipes by tag name */
function savedRecipes(tagName) {
  const displayRecipesElement = document.getElementById("display-recipes");
  displayRecipesElement.innerHTML = "";

  fetch('/tag?tagName=' + tagName).then(response => response.json()).then((tagJson) => {
    // get list of unique recipes from tagList
    if (tagJson.error == null) {
      const tagList = tagJson.filteredList;
      const recipeIdList = new Set(tagList.map(tag => tag.recipeId));
    
      // display recipes as cards
      recipeIdList.forEach(recipeId => { 
        getSavedRecipe(displayRecipesElement, recipeId);
      });
    } else {
      alert(tagJson.error);
    }
    
  });
}

const PAGE_LENGTH = 4;
// refresh the tag selection menu in case tag was deleted or added
function refreshTagNameSelection() {
  const tagMenuElement = document.getElementById("tag-menu");
  if (tagMenuElement != null) {
    fetch('/tag-names').then(response => response.json()).then((tagNames) => {
      const carouselLinks = document.getElementById("tag-indicators");
      carouselLinks.innerHTML = "";
      const carousel = document.getElementById("tag-inner");
      carousel.innerHTML = "";

      var pages = Math.floor(tagNames.length / PAGE_LENGTH);
      var remainder = tagNames.length % PAGE_LENGTH;
      if (remainder != 0) {
        pages++;
      }

      for (i = 0; i < pages; i++) {
        var slice = tagNames.slice(i*PAGE_LENGTH, (i+1)*PAGE_LENGTH);
        if (i == 0) {
          carouselLinks.appendChild(createElementFromHTML('<li data-target="#myTagCarousel" data-slide-to="' + i + '" class="active"></li>'));
          carousel.appendChild(createTagCarouselPage(slice, true));
        } else {
          carouselLinks.appendChild(createElementFromHTML('<li data-target="#myTagCarousel" data-slide-to="' + i + '"></li>'));
          carousel.appendChild(createTagCarouselPage(slice, false));
        }
      }
    });
  }
}

function createTagCarouselPage(slice, isFirst) {
  const page = document.createElement('div');
  if (isFirst) {
    page.className = 'item active';
  } else {
    page.className = 'item';
  }
  slice.forEach(tagName => page.appendChild(addTagMenuItem(tagName, tagName)));
  return page;
}

function createElementFromHTML(htmlString) {
  var div = document.createElement('div');
  div.innerHTML = htmlString.trim();

  // Change this to div.childNodes to support multiple top-level nodes
  return div.firstChild; 
}

// create tag menu item for selecting recipes by tag
function addTagMenuItem(value, tag) {
  var temp = document.querySelector("#tag-name-template");;
  var tagMenuItemClone = temp.content.cloneNode(true);

  const tagMenuItemElement = tagMenuItemClone.querySelector(".tag-menu-item");
  tagMenuItemElement.value = tag;

  // create button for deleting all tags with selected name
  const tagItemButton = tagMenuItemClone.querySelector('button');
  tagItemButton.addEventListener('click', () => {
    fetch('/tag?tagName=' + value).then(response => response.json()).then((tagJson) => {
      if (tagJson.error == null) {
        // get all tags with tag name
        const tagList = tagJson.filteredList;
        const tagIdList = new Set(tagList.map(tag => tag.tagId));
    
        // delete tags from recipe cards
        tagIdList.forEach(tagId => { 
          deleteTagFromRecipeCard(tagId);
        });
      } else {
        alert(tagJson.error);
      }
    });
  });

  // display text of tag, which when clicked displays the related recipes
  const tagItemElement = tagMenuItemClone.querySelector('span');
  tagItemElement.innerHTML = tag;
  tagItemElement.addEventListener('click', () => {
    savedRecipes(value);
  });

  const tagCheckbox = tagMenuItemClone.querySelector(".tag-menu-checkbox");
  tagCheckbox.value = value;
  tagCheckbox.onclick = function() {
    multipleSavedRecipes();
  }

  return tagMenuItemClone;
}

function deleteTagFromRecipeCard(tagId) {
  const params = new URLSearchParams();
  params.append('tag-id', tagId);

  fetch('/delete-tag', {method: 'POST', body: params}).then(() => {
    // Remove the tag from the DOM.
    const recipeCardTagList = document.querySelectorAll(".recipe-tag");
    recipeCardTagList.forEach(tag => {
      if (tag.value == tagId) {
        tag.remove();
      }
    });
    // Remove the tag from the menu list.
    refreshTagNameSelection();
  });
}

/** Helper function to display recipe cards in display-recipes element */
function appendToDisplayElement(recipeList) {
  const displayRecipeElement = document.getElementById('display-recipes');
  displayRecipeElement.innerHTML = "";
  for (recipe of recipeList) {
    createPictureWrap(displayRecipeElement, recipe);
  }
}


// helper to create picture wrap for gallery display
function createPictureWrap(displayRecipeElement, recipe) {
  var temp = document.querySelector("#picture-wrap-template");
  var clone = temp.content.cloneNode(true);

  const pictureWrap = clone.querySelector(".dish-image-wrap");

  const pictureElement = clone.querySelector(".dish-image");
  pictureElement.src = recipe['image'];
  
  createRecipeElement(recipe, pictureWrap);

  const pictureText = clone.querySelector(".dish-image-text");
  pictureText.innerHTML = recipe['title'];

  displayRecipeElement.appendChild(clone);
}


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
  getLoginStatus('profile.html');
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
  const params = new URLSearchParams();
  
  const userName = document.getElementById('name-entry').value.trim();
  const vegetarian = document.getElementById("vegetarian-checkbox").checked;
  const vegan = document.getElementById("vegan-checkbox").checked;
  const glutenFree = document.getElementById("gluten-checkbox").checked;
  const dairyFree = document.getElementById("dairy-checkbox").checked;

  const allergiesString = document.getElementById("allergies-entry").value;
  
  const params = new URLSearchParams();
  allergiesString.split(",").map(allergy => {
    params.append('allergies', allergy.toLowerCase().trim()); 
  });
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

  fetch('/profile', {method: 'POST', body: params}).then(response => response.json()).then((message) => {
    const profileStatusElement = document.getElementById('saved-profile-status');
    if (message.error != null) {
      alert(message.error);
    } else {
      profileStatusElement.style.display = "block";
      if (sessionStorage.redirectUrl != "") {
        window.location.href = "/" + sessionStorage.redirectUrl;
        sessionStorage.redirectUrl = "";
      }
    }
  });
}

function clearSavedProfileStatus() {
  const profileStatusElement = document.getElementById('saved-profile-status');
  profileStatusElement.style.display = "none";
}

/** Call the appropriate functions needed on-load of the body of home page */
function loadHomePage() {
  startSlideshow();
  getLoginStatus('index.html');
}

/** Call the appropriate functions needed on-load of the body of display page */
function loadDisplayPage() {
  createNutritionElements();
  getLoginStatus('display.html');
}

/**
  * Checks with server if user has logged in.
  * Display corresponding text and url in login section if login is true/false.
  */
function getLoginStatus(url) {
  fetch('/login?url=' + url).then(response => response.json()).then((userInfo) => {
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

      if (!userInfo.hasProfile && url != "profile.html") {
        sessionStorage.redirectUrl = url;
        window.location.href = "/profile.html";
      }
      
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
  createPictureWrap(displayRecipeElement, recipe);

  const recipe1 = {};
  recipe1['id'] = 2;
  recipe1['title'] = "Title 1";
  recipe1['image'] = "/images/salad.jpeg";
  recipe1['sourceUrl'] = "https://css-tricks.com/snippets/css/a-guide-to-flexbox/";
  recipe1['servings'] = 1;
  recipe1['readyInMinutes'] = 10;
  recipe1['vegan'] = true;
  createPictureWrap(displayRecipeElement, recipe1);
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
    const tagHeader = document.getElementById("tag-page-header");
    if (tagHeader != null) {
      tagHeader.style.opacity = "1";
    }
  }

  const favoriteElement = clone.querySelector(".icon-star");
  getFavorite(recipe['id'], favoriteElement);
  favoriteElement.onclick = function() {
    const params = new URLSearchParams();

    if (favoriteElement.value != null) {
      params.append('favorite-id', favoriteElement.value);
    } else {
      params.append('recipe-id', recipe['id']);
    }
    fetch('/favorite', {method: 'POST', body: params}).then(response => response.json()).then((message) => {
      if (message.favoriteId != null) {
        favoriteElement.value = message.favoriteId;
        favoriteElement.style.color = "yellow";
      } else {
        favoriteElement.value = null;
        favoriteElement.style.color = "transparent";
      }
    });
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

  const tagTextElement = clone.querySelector(".tag-input");

  const addTagElement = clone.querySelector(".add-tag-button");
  addTagElement.addEventListener('click', () => {
    const newTagName = (tagTextElement.value).toLowerCase().trim();
    if (newTagName != "") {
      const params = new URLSearchParams();
      params.append('tag-name', newTagName);
      params.append('recipe-id', recipe['id']);

      fetch('/tag', {method: 'POST', body: params}).then(response => response.json()).then((tagList) => {
        tagElements.innerHTML = "";
        createRecipeCardTags(recipe['id'], tagElements);
        postSavedRecipe(recipe);
        refreshTagNameSelection();
      });
    }
  });
  document.getElementById("card-gallery").appendChild(clone);
  pictureWrap.onclick = function() {
    hoverElement.style.display = "block";
    document.getElementById("display-recipes").style.opacity = "0.2";
    const tagHeader = document.getElementById("tag-page-header");
    if (tagHeader != null) {
      tagHeader.style.opacity = "0.2";
    }
  }
}

// create an element that represents if recipe is one of user's favorites
function getFavorite(recipeId, favoriteElement) {
  fetch('/favorite?recipeId=' + recipeId).then(response => response.json()).then((message) => {
    if (message.isFavorite) {
      favoriteElement.value = message.favoriteId;
      favoriteElement.style.color = "yellow";
    } else {
      favoriteElement.value = null;
      favoriteElement.style.color = "transparent";
    }
  });
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
    'vegetarian': 'Alert: Not vegetarian',
    'vegan': 'Alert: Not vegan',
    'glutenFree': 'Alert: Contains gluten',
    'dairyFree': 'Alert: Contains dairy'
  };
  
  fetch('/profile').then(response => response.json()).then((message) => {
    if (message.hasProfile) {
      const profile = message.profile;
      const dietaryNeeds = profile.dietaryNeeds;
      if (dietaryNeeds != null) {
        const alertHeading = document.createElement('h5');
        alertHeading.className = "recipe-alert-block";
        alertHeading.innerText = "Dietary Alerts";
        alertElements.appendChild(alertHeading);
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
  tagElement.value = tag.tagId;
  
  const titleElement = clone.querySelector('span');
  titleElement.innerText = tag.tagName;
  // change the displayed recipes when tag is clicked
  titleElement.addEventListener('click', () => {
    savedRecipes(tag.tagName);
  });

  const deleteButtonElement = clone.querySelector('button');
  deleteButtonElement.addEventListener('click', () => {
    const params = new URLSearchParams();
    params.append('tag-id', tag.tagId);

    fetch('/delete-tag', {method: 'POST', body: params}).then(() => {
      // Remove the tag from the DOM.
      tagElement.remove();
      refreshTagNameSelection();
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
  if (recipe['extendedIngredients'] != null) {
    for(recipeIngredient of recipe['extendedIngredients']){
      params.append('ingredient-names', recipeIngredient['name']);
    }
  }
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
      const ingredientNames = savedRecipe.ingredientNames;
      savedRecipe['extendedIngredients'] = [];
      ingredientNames.forEach(ingredient => {
        savedRecipe['extendedIngredients'].push({'name': ingredient});
      });

      // display recipe card using the recipe's saved information
      // displayRecipesElement.append(createRecipeElement(savedRecipe));
      createPictureWrap(displayRecipesElement, savedRecipe);
    }
  });
}

/** Reads dishname, fetches recipe information, and stores both in serssionStorage to use in display.html */
function readUserDishChoice() {
  const overlay = document.getElementById('overlay');
  overlay.style.display = 'block';
  var dishName = document.forms.dishFitChoice.elements.labelFitChoice.value;
  if(dishName != null){
    fetch('/recipeInfo?dishName='+dishName).then(response => response.json()).then((recipeListInfoJson) => {
      sessionStorage.dishName = dishName;
      sessionStorage.recipeList = recipeListInfoJson;
      window.location.href = "/display.html";
    });
  }
}

/** Adds friend from the form */
function addFriend() {
  const friendEmail = document.getElementById('friend-input').value;
  fetch('/addFriend?friendEmail='+friendEmail).then(response => response.text()).then((friendListResponse) => {
    const friendResponseElement = document.getElementById("add-friend-response");
    friendResponseElement.innerHTML = friendListResponse;
  });
}