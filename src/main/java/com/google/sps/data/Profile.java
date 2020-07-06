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
import com.google.auto.value.AutoValue;
import java.util.ArrayList;

/**
 * Abstract class representing user profile
 */
@AutoValue
public abstract class Profile {
  public abstract String getId();
  public abstract String getUserName();
  public abstract boolean getVegetarian();
  public abstract boolean getVegan();
  public abstract boolean getGlutenFree();
  public abstract boolean getDairyFree();
  public abstract ArrayList<String> getAllergies();
  public static Builder builder() {
    return new AutoValue_Profile.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    /**
      * @param id The unique id of the profile.
      * @param userName The name of the user that the profile belongs to.
      * @param allergies The allergies of the user
      * @param vegetarian @param vegan @param glutenFree @param diaryFree The user's dietary options
      */
    public abstract Builder setId(String id);
    public abstract Builder setUserName(String userName);
    public abstract Builder setVegetarian(boolean vegetarian);
    public abstract Builder setVegan(boolean vegan);
    public abstract Builder setGlutenFree(boolean glutenFree);
    public abstract Builder setDairyFree(boolean dairyFree);
    public abstract Builder setAllergies(ArrayList<String> allergies);
    public abstract Profile build();
  } 
}