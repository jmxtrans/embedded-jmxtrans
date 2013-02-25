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
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class ShoppingCart {

    /**
     * Number of added cocktails by Cock
     */
    private SortedMap<Cocktail, ShoppingCartItem> items = new TreeMap<Cocktail, ShoppingCartItem>();

    public void addItem(Cocktail cocktail, int quantity) {
        ShoppingCartItem item = items.get(cocktail);
        if (item == null) {
            items.put(cocktail, new ShoppingCartItem(cocktail, quantity));
        } else {
            item.incrementQuantity(quantity);
        }
    }

    @Nonnull
    public Collection<ShoppingCartItem> getItems() {
        return items.values();
    }

    public int getPriceInCents() {
        int totalPriceInCents = 0;
        for (ShoppingCartItem item : getItems()) {
            totalPriceInCents += item.getTotalPriceInCents();
        }
        return totalPriceInCents;
    }

    /**
     * Price in dollars
     */
    public String getPrettyPrice(){
        BigDecimal priceInDollars = new BigDecimal(getPriceInCents()).movePointLeft(2);
        return NumberFormat.getCurrencyInstance(Locale.US).format(priceInDollars);
    }

    public int getItemsCount() {
        int itemsCount = 0;
        for (ShoppingCartItem item : getItems()) {
            itemsCount += item.getQuantity();
        }
        return itemsCount;
    }

    public static class ShoppingCartItem implements Comparable<ShoppingCartItem> {

        @Nonnull
        private final Cocktail cocktail;
        private int quantity;

        public ShoppingCartItem(@Nonnull Cocktail cocktail, int quantity) {
            Assert.notNull(cocktail, "given Cocktail can NOT be null");
            this.cocktail = cocktail;
            this.quantity = quantity;
        }

        public int getTotalPriceInCents() {
            return quantity * cocktail.getPriceInCents();
        }

        @Override
        public int compareTo(ShoppingCartItem o) {
            return this.cocktail.compareTo(o.cocktail);
        }

        public Cocktail getCocktail() {
            return cocktail;
        }

        /**
         * @param quantity quantity to add for the item
         */
        public void incrementQuantity(int quantity) {
            this.quantity += quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShoppingCartItem)) return false;

            ShoppingCartItem that = (ShoppingCartItem) o;

            if (!cocktail.equals(that.cocktail)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return cocktail.hashCode();
        }
    }
}

