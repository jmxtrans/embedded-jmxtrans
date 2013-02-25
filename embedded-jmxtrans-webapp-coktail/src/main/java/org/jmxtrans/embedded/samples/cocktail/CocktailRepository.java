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

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@Repository
public class CocktailRepository {

    private Map<Long, Cocktail> cocktails = new HashMap<Long, Cocktail>();
    private AtomicLong cocktailSequence = new AtomicLong();

    public CocktailRepository() {
        Cocktail sexOnTheBeach = buildSexOnTheBeachCocktail();
        cocktails.put(sexOnTheBeach.getId(), sexOnTheBeach);
        Cocktail longIslandIcedTea = buildLongIslandCocktail();
        cocktails.put(longIslandIcedTea.getId(), longIslandIcedTea);
    }

    /**
     * @param ingredientName
     * @param cocktailName
     * @return
     */
    public Collection<Cocktail> find(@Nullable String ingredientName, @Nullable String cocktailName) {
        SortedSet<Cocktail> result = new TreeSet<Cocktail>();
        for (Cocktail cocktail : cocktails.values()) {
            if (cocktailName == null && ingredientName == null) {
                result.add(cocktail);
            }
            if (StringUtils.hasLength(cocktailName)) {
                if (cocktail.getName().toLowerCase().contains(cocktailName.toLowerCase())) {
                    result.add(cocktail);
                    break;
                }
            }
            if (ingredientName != null) {
                for (String cocktailIngredient : cocktail.getIngredientNames()) {
                    if (cocktailIngredient.toLowerCase().contains(ingredientName.toLowerCase())) {
                        result.add(cocktail);
                        break;
                    }
                }
            }

        }
        return result;
    }

    public Cocktail get(long id) {
        return cocktails.get(id);
    }

    public List<String> suggestCocktailIngredientWords(String query) {
        return Collections.singletonList("#TODO#");
    }

    public List<String> suggestCocktailNameWords(String query) {
        return Collections.singletonList("#TODO#");
    }

    public void update(Cocktail cocktail) {
        this.cocktails.put(cocktail.getId(), cocktail);
    }

    protected Cocktail buildSexOnTheBeachCocktail() {
        Cocktail sexOnTheBeach = new Cocktail()
                .withId(cocktailSequence.incrementAndGet())
                .withName("Sex On The Beach")
                .withPriceInCents(550)
                .withIngredient("1 shot", "vodka")
                .withIngredient("1 shot", "peach schnapps (archers)")
                .withIngredient("200 ml", "orange juice")
                .withIngredient("200 ml", "cranberry juice")
                .withIngredient("2 shots", "raspberry syrup")
                .withPhotoUrl("http://xebia-cocktail.s3-website-us-east-1.amazonaws.com/4703755392347885371.jpg")
                .withRecipeUrl("http://www.cocktailmaking.co.uk/displaycocktail.php/321-Sex-On-The-Beach")
                .withInstructions(
                        "Add ice to glass pour in shot of vodka add peach shnapps mix with orange, cranberry and raspberry\n" //
                                + "\n" //
                                + "Serve with an umbrella and a mixer stick and a fancy straw and an orange slice on side of "
                                + "glass this one is gorgeous can't believe you don't already have it on here!");
        return sexOnTheBeach;
    }

    protected Cocktail buildLongIslandCocktail() {
        Cocktail longIslandIcedTea = new Cocktail()
                .withId(cocktailSequence.incrementAndGet())
                .withName("Long Island Iced tea")
                .withPriceInCents(650)
                .withIngredient("1 Measure", "vodka")
                .withIngredient("1 Measure", "gin")
                .withIngredient("1 Measure", "white rum")
                .withIngredient("1 Measure", "tequila")
                .withIngredient("1 Measure", "triple sec")
                .withIngredient("3 measures", "orange juice")
                .withIngredient("to topp up the glass", "coke")
                .withPhotoUrl("http://xebia-cocktail.s3-website-us-east-1.amazonaws.com/6762530443361434570.jpg")
                .withRecipeUrl("http://www.cocktailmaking.co.uk/displaycocktail.php/1069-Long-Island-Iced-tea")
                .withInstructions(
                        "In a tall glass , add ice and all the ingredients and stir well. It should have the appearance of cloudy tea. Top with a piece of lemon\n"
                                + "\n"
                                + "Very yummy & very very deceiving. It will get you hammered after only about 2 so drink with caution");
        return longIslandIcedTea;
    }
}
