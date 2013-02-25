/*
 * Copyright (c) 2010-2013 the original author or authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.jmxtrans.embedded.samples.cocktail;


import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class Cocktail implements Comparable<Cocktail>, Serializable {

    private List<Ingredient> ingredients = new ArrayList<Ingredient>();
    private String instructions;
    private String name;
    private long id;
    private String photoUrl;
    /**
     * URL of the cocktail recipe that has been used
     */
    private String recipeUrl;
    private Deque<String> comments = new LinkedList<String>();

    private int priceInCents;

    public String getInstructionsAsHtml() {
        return instructions == null ? "" : instructions.replace("\n", "<br />\n");
    }

    public Collection<String> getIngredientNames() {
        List<String> ingredientNames = new ArrayList<String>();
        for (Ingredient ingredient : this.ingredients) {
            ingredientNames.add(ingredient.getName());
        }
        return ingredientNames;
    }

    @Override
    public int compareTo(Cocktail other) {
        if (this.name == null) {
            return other.name == null ? 0 : -1;
        }
        return this.name.compareTo(other.name);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRecipeUrl() {
        return recipeUrl;
    }

    public void setRecipeUrl(String recipeUrl) {
        this.recipeUrl = recipeUrl;
    }

    public Deque<String> getComments() {
        return comments;
    }

    public void setComments(Deque<String> comments) {
        this.comments = comments;
    }

    public Cocktail withId(long id) {
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

    public Cocktail withRecipeUrl(String recipeUrl) {
        setRecipeUrl(recipeUrl);
        return this;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    /**
     * Price in dollars
     */
    public String getPrettyPrice(){
        BigDecimal priceInDollars = new BigDecimal(getPriceInCents()).movePointLeft(2);
        return NumberFormat.getCurrencyInstance(Locale.US).format(priceInDollars);
    }

    public void setPriceInCents(int priceInCents) {
        this.priceInCents = priceInCents;
    }

    public Cocktail withPriceInCents(int priceInCents) {
        this.priceInCents = priceInCents;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cocktail)) return false;

        Cocktail cocktail = (Cocktail) o;

        if (id != cocktail.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    /**
     * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
     */
    public static class Ingredient implements Serializable {

        private String name;
        private String quantity;

        public Ingredient(String quantity, String name) {
            this.quantity = quantity;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "Ingredient{" +
                    "quantity='" + quantity + '\'' +
                    '}';
        }
    }
}
