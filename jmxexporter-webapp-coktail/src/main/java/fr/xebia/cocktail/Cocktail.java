/*
 * Copyright 2008-2012 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.cocktail;

import java.util.Collection;
import java.util.Deque;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

/**
 * The recipe of a cocktail. Root model object.
 * 
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class Cocktail implements Comparable<Cocktail> {

    private List<Ingredient> ingredients = Lists.newArrayList();

    private String instructions;

    @Nonnull
    private String name;

    private String id;

    private String photoUrl;

    /**
     * URL of the cocktail recipe that has been used
     */
    private String sourceUrl;

    private Deque<String> comments = Lists.newLinkedList();

    @Override
    public int compareTo(Cocktail other) {
        return ComparisonChain.start() //
                .compare(this.name, other.name) //
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cocktail other = (Cocktail) obj;

        return Objects.equal(this.id, other.id);
    }

    public Deque<String> getComments() {
        return comments;
    }

    public String getId() {
        return id;
    }

    public Collection<String> getIngredientNames() {
        return Collections2.transform(this.ingredients, new Function<Ingredient, String>() {
            @Override
            public String apply(Ingredient ingredient) {
                return ingredient.getName();
            }
        });
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getInstructionsAsHtml() {
        return Strings.nullToEmpty(instructions).replace("\n", "<br />\n");
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    public void setComments(Deque<String> comments) {
        this.comments = comments;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this) //
                .add("id", this.id) //
                .add("name", this.name) //
                .toString();
    }

    public Cocktail addComment(String comment) {
        getComments().addFirst(comment);
        return this;
    }

    public Cocktail withId(String id) {
        setId(id);
        return this;
    }

    public Cocktail withIngredient(String quantity, String name) {
        this.ingredients.add(new Ingredient(quantity, name));
        return this;
    }

    public Cocktail withInstructions(String instructions) {
        setInstructions(instructions);
        return this;
    }

    public Cocktail withName(String name) {
        setName(name);
        return this;
    }

    public Cocktail withPhotoUrl(String photoUrl) {
        setPhotoUrl(photoUrl);
        return this;
    }

    public Cocktail withSourceUrl(String sourceUrl) {
        setSourceUrl(sourceUrl);
        return this;

    }

}
