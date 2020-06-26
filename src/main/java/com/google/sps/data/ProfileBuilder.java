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

package com.google.sps.data;
import java.util.ArrayList;

/** class to build profile */
public class ProfileBuilder {
  // required
  public String id;
  public String userName;

  // optional
  public boolean vegetarian;
  public boolean vegan;
  public boolean glutenFree;
  public boolean dairyFree;
  public ArrayList<String> allergies;

  public ProfileBuilder(String id, String userName) {
    this.id = id;
    this.userName = userName;
  }

  public void setVegetarian(boolean vegetarian) {
    this.vegetarian = vegetarian;
  }

  public void setVegan(boolean vegan) {
    this.vegan = vegan;
  }

  public void setGlutenFree(boolean glutenFree) {
    this.glutenFree = glutenFree;
  }

  public void setDairyFree(boolean dairyFree) {
    this.dairyFree = dairyFree;
  }

  public void setAllergies(ArrayList<String> allergies) {
    this.allergies = allergies;
  }

  public Profile build() {
    Profile profile = new Profile(this);
    return profile;
  }
}