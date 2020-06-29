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

/* Function gets recipe information from user input ID and displays the title on the page **/
function getRecipe(){
  var numRecipe = document.getElementById("num-recipe").value;
  fetch('/recipeInfo?numRecipe='+numRecipe).then(response => response.json()).then((recipeInfo) => {
    recipeInf = JSON.parse(recipeInfo);
    const recipeDisplayElement = document.getElementById('recipe-info');
    recipeDisplayElement.innerText = recipeInf["title"];
  });
}

/* Function gets recipe list from user input dish and displays the title of the first returned result on the page **/
function getRecipeId(){
  var dishName = document.getElementById("dish-name").value;
  fetch('/recipeInfo?dishName='+dishName).then(response => response.json()).then((recipeId) => {
    recipe = JSON.parse(recipeId);
    const recipeIdDisplayElement = document.getElementById('recipe-id-info');
    recipeIdDisplayElement.innerText = recipe[0]["title"] + "\n" + recipe[1]["title"];
  });
}