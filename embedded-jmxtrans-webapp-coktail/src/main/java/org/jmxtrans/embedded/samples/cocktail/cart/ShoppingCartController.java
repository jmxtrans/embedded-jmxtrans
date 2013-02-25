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
package org.jmxtrans.embedded.samples.cocktail.cart;

import org.jmxtrans.embedded.samples.cocktail.Cocktail;
import org.jmxtrans.embedded.samples.cocktail.CocktailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@ManagedResource("cocktail:type=ShoppingCartController,name=ShoppingCartController")
@Controller
public class ShoppingCartController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final AtomicInteger shoppingCartsPriceInCents = new AtomicInteger();
    private final AtomicInteger ordersPriceInCents = new AtomicInteger();
    private final AtomicInteger orderItemsCount = new AtomicInteger();
    private final AtomicInteger ordersCount = new AtomicInteger();
    @Autowired
    ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private CocktailRepository cocktailRepository;

    @RequestMapping(method = RequestMethod.POST, value = "/cart/add")
    public String addItem(@RequestParam("cocktail") long cocktailId, @RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity, HttpServletRequest request) {

        Cocktail cocktail = cocktailRepository.get(cocktailId);
        if (cocktail == null) {
            logger.warn("No cocktail found with id " + cocktailId + ". Silently redirect to home page");
            return "redirect:/";
        }

        ShoppingCart shoppingCart = shoppingCartRepository.getCurrentShoppingCart(request);
        shoppingCartsPriceInCents.addAndGet(quantity * cocktail.getPriceInCents());
        shoppingCart.addItem(cocktail, quantity);

        return "redirect:/cocktail/" + cocktailId;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/cart/")
    public String view(HttpServletRequest request) {
        return "cart/view";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/cart/buy")
    public String buy(HttpServletRequest request) {
        ShoppingCart shoppingCart = shoppingCartRepository.getCurrentShoppingCart(request);

        ordersPriceInCents.addAndGet(shoppingCart.getPriceInCents());
        orderItemsCount.addAndGet(shoppingCart.getItemsCount());
        ordersCount.incrementAndGet();

        shoppingCartRepository.resetCurrentShoppingCart(request);
        return "redirect:/";
    }

    @ManagedMetric
    public int getShoppingCartsPriceInCents() {
        return shoppingCartsPriceInCents.get();
    }

    @ManagedMetric
    public int getOrdersPriceInCents() {
        return ordersPriceInCents.get();
    }

    @ManagedMetric
    public int getOrderItemsCount() {
        return orderItemsCount.get();
    }

    @ManagedMetric
    public int getOrdersCount() {
        return ordersCount.get();
    }
}
