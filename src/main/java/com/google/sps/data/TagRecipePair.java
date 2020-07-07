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
 * Class representing recipe tag
 */
public class TagRecipePair {
  private String userId;
  private String tagName;
  private int recipeId;

  /**
    * @param userId The unique id of the profile.
    * @param tagName The name of the tag.
    * @param recipeId The ids of the recipe that the tag is attached to
    */
  public TagRecipePair(String userId, String tagName, int recipeId) {
    this.userId = userId;
    this.tagName = tagName;
    this.recipeId = recipeId;
  }
}