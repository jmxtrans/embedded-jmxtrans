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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Content;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class CocktailRssView extends AbstractRssFeedView {

    private Iterable<Cocktail> cocktails;

    public CocktailRssView(@Nonnull Iterable<Cocktail> cocktails) {
        this.cocktails = cocktails;
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed,
                                     HttpServletRequest request) {

        feed.setTitle("Cocktail App");
        feed.setDescription("Java Tutorials and Examples");
        feed.setLink(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());

        super.buildFeedMetadata(model, feed, request);
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model, final HttpServletRequest request, HttpServletResponse response) throws Exception {


        Function<Cocktail, Item> cocktailToRssItem = new Function<Cocktail, Item>() {
            @Override
            public Item apply(@Nullable Cocktail cocktail) {
                if (cocktail == null) {
                    return null;
                }

                Item item = new Item();
                item.setTitle(cocktail.getName());
                Content content = new Content();
                content.setValue(cocktail.getInstructionsAsHtml());
                content.setType(Content.HTML);
                item.setContent(content);
                item.setLink(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/cocktail/" + cocktail.getId());
                return item;
            }
        };

        return Lists.newArrayList(Iterables.transform(cocktails, cocktailToRssItem));
    }
}
