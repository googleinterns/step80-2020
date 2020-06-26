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