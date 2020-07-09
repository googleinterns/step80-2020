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
 * Abstract class representing saved recipe
 */
@AutoValue
public abstract class SavedRecipe {
  public abstract long getId();
  public abstract String getTitle();
  public abstract String getImage();
  public abstract String getSourceUrl();
  public abstract ArrayList<Diet> getDietaryNeeds();
  public static Builder builder() {
    return new AutoValue_SavedRecipe.Builder();
  }

  public enum Diet {
    VEGETARIAN, VEGAN, GLUTENFREE, DAIRYFREE
  }

  @AutoValue.Builder
  public abstract static class Builder {
    /**
      * @param recipeId The ids of the recipe that the tag is attached to
      * @param title The title of the recipe
      * @param imageUrl The url of the recipe image
      * @param sourceUrl The url of the recipe source
      * @param dietaryNeeds The dietary needs of the recipe (ex: vegetarian, vegan)
      */
    public abstract Builder setId(long recipeId);
    public abstract Builder setTitle(String title);
    public abstract Builder setImage(String imageUrl);
    public abstract Builder setSourceUrl(String sourceUrl);
    public abstract Builder setDietaryNeeds(ArrayList<Diet> dietaryNeeds);
    public abstract SavedRecipe build();
  }
}