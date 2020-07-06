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
/**
 * Abstract class representing saved recipe
 */
@AutoValue
public abstract class SavedRecipe {
  public abstract long getRecipeId();
  public abstract String getTitle();
  public abstract String getImage();
  public abstract String getSource();
  public abstract boolean getVegetarian();
  public abstract boolean getVegan();
  public abstract boolean getGlutenFree();
  public abstract boolean getDairyFree();
  public static Builder builder() {
    return new AutoValue_SavedRecipe.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    /**
      * @param recipeId The ids of the recipe that the tag is attached to
      * @param title The title of the recipe
      * @param imageUrl The url of the recipe image
      * @param sourceUrl The url of the recipe source
      * @param vegetarian @param vegan @param glutenFree @param diaryFree The user's dietary options
      */
    public abstract Builder setRecipeId(long recipeId);
    public abstract Builder setTitle(String title);
    public abstract Builder setImage(String imageUrl);
    public abstract Builder setSource(String sourceUrl);
    public abstract Builder setVegetarian(boolean vegetarian);
    public abstract Builder setVegan(boolean vegan);
    public abstract Builder setGlutenFree(boolean GlutenFree);
    public abstract Builder setDairyFree(boolean dairyFree);
    public abstract SavedRecipe build();
  }
}