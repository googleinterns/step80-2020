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

/** class to build profile */
public class ProfileBuilder {
  // required
  private long id;
  private String userName;

  // optional
  private boolean vegetarian;
  private boolean vegan;
  private boolean glutenFree;
  private boolean dairyFree;
  private String[] allergies;

  public ProfileBuilder(long id, String userName) {
    this.id = id;
    this.userName = userName;
  }

  public setVegetarian(boolean vegetarian) {
    this.vegetarian = vegetarian;
  }

  public setVegan(boolean vegan) {
    this.vegan = vegan;
  }

  public setGlutenFree(boolean glutenFree) {
    this.glutenFree = glutenFree;
  }

  public setDairyFree(boolean dairyFree) {
    this.dairyFree = dairyFree;
  }

  public setAllergies(String[] allergies) {
    this.allergies = allergies;
  }

  public Profile build() {
    Profile profile = new Profile(this);
    return profile;
  }
}