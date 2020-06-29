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

/**
 * Class representing user profile
 */
public class Profile {
  // required
  private String id;
  private String userName;

  // optional
  private boolean vegetarian;
  private boolean vegan;
  private boolean glutenFree;
  private boolean dairyFree;
  private ArrayList<String> allergies;

  /**
    * @param id The unique id of the profile.
    * @param userName The name of the user that the profile belongs to.
    * @param allergies The allergies of the user
    * @param vegetarian @param vegan @param glutenFree @param diaryFree The user's dietary options
    */
  public Profile(ProfileBuilder builder) {
    this.id = builder.getId();
    this.userName = builder.getUserName();
    this.vegetarian = builder.getVegetarian();
    this.vegan = builder.getVegan();
    this.glutenFree = builder.getGlutenFree();
    this.dairyFree = builder.getDairyFree();
    this.allergies = builder.getAllergies();
  }
}