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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Repository for cocktails (MongoDB + Solr).
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@Repository
public class CocktailRepository {

    @VisibleForTesting
    protected Cache<String, Cocktail> cocktails;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public CocktailRepository() {
        cocktails = CacheBuilder.newBuilder().maximumSize(100).build();

        insert(buildLongIslandCocktail());
        insert(buildSexOnTheBeachCocktail());
    }

    public void delete(Cocktail cocktail) {
        Preconditions.checkNotNull(cocktail.getId(), "Given id must not be null in %s", cocktail);
        cocktails.invalidate(cocktail.getId());
    }

    public Collection<Cocktail> find(@Nullable final String ingredient, @Nullable final String name) {


        Predicate<Cocktail> ingredientPredicate;
        if (Strings.isNullOrEmpty(ingredient)) {
            ingredientPredicate = Predicates.alwaysTrue();
        } else {
            ingredientPredicate = new Predicate<Cocktail>() {
                @Override
                public boolean apply(@Nullable Cocktail cocktail) {
                    for (String cocktailIngredient : cocktail.getIngredientNames()) {
                        if (StringUtils.containsIgnoreCase(cocktailIngredient, ingredient)) {
                            return true;
                        }
                    }
                    return false;
                }
            };
        }

        Predicate<Cocktail> namePredicate;
        if (Strings.isNullOrEmpty(name)) {
            namePredicate = Predicates.alwaysTrue();
        } else {
            namePredicate = new Predicate<Cocktail>() {
                @Override
                public boolean apply(@Nullable Cocktail cocktail) {
                    return StringUtils.containsIgnoreCase(cocktail.getName(), name);
                }
            };
        }

        return Lists.newArrayList(Collections2.filter(cocktails.asMap().values(), Predicates.and(namePredicate, ingredientPredicate)));
    }

    public Cocktail get(String id) {
        return cocktails.getIfPresent(id);
    }

    public void insert(Cocktail cocktail) {
        Preconditions.checkArgument(cocktail.getId() == null, "Given id must be null in %s", cocktail);

        cocktail.setId(RandomStringUtils.randomAlphanumeric(5));

        cocktails.put(cocktail.getId(), cocktail);
    }


    public void purgeRepository() {
        cocktails.invalidateAll();
    }

    public List<String> suggestCocktailIngredientWords(String query) {
        return Lists.newArrayList("#TODO#");
    }

    public List<String> suggestCocktailNameWords(String query) {
        return Lists.newArrayList("#TODO#");
    }

    public void update(Cocktail cocktail) {
        Preconditions.checkNotNull(cocktail.getId(), "Given objectId must not be null in %s", cocktail);
        cocktails.put(cocktail.getId(), cocktail);
    }

    protected Cocktail buildSexOnTheBeachCocktail() {
        Cocktail sexOnTheBeach = new Cocktail()
                .withName("Sex On The Beach")
                .withIngredient("1 shot", "vodka")
                .withIngredient("1 shot", "peach schnapps (archers)")
                .withIngredient("200 ml", "orange juice")
                .withIngredient("200 ml", "cranberry juice")
                .withIngredient("2 shots", "raspberry syrup")
                .withPhotoUrl("http://xebia-cocktail.s3-website-us-east-1.amazonaws.com/4703755392347885371.jpg")
                .withSourceUrl("http://www.cocktailmaking.co.uk/displaycocktail.php/321-Sex-On-The-Beach")
                .withInstructions(
                        "Add ice to glass pour in shot of vodka add peach shnapps mix with orange, cranberry and raspberry\n" //
                                + "\n" //
                                + "Serve with an umbrella and a mixer stick and a fancy straw and an orange slice on side of "
                                + "glass this one is gorgeous can't believe you don't already have it on here!");
        return sexOnTheBeach;
    }

    protected Cocktail buildLongIslandCocktail() {
        Cocktail longIslandIcedTea = new Cocktail()
                .withName("Long Island Iced tea")
                .withIngredient("1 Measure", "vodka")
                .withIngredient("1 Measure", "gin")
                .withIngredient("1 Measure", "white rum")
                .withIngredient("1 Measure", "tequila")
                .withIngredient("1 Measure", "triple sec")
                .withIngredient("3 measures", "orange juice")
                .withIngredient("to topp up the glass", "coke")
                .withPhotoUrl("http://xebia-cocktail.s3-website-us-east-1.amazonaws.com/6762530443361434570.jpg")
                .withSourceUrl("http://www.cocktailmaking.co.uk/displaycocktail.php/1069-Long-Island-Iced-tea")
                .withInstructions(
                        "In a tall glass , add ice and all the ingredients and stir well. It should have the appearance of cloudy tea. Top with a piece of lemon\n"
                                + "\n"
                                + "Very yummy & very very deceiving. It will get you hammered after only about 2 so drink with caution");
        return longIslandIcedTea;
    }
}
