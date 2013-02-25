package org.jmxtrans.embedded.samples.cocktail.cart;

import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@Repository
public class ShoppingCartRepository {

    @Nonnull
    public ShoppingCart getCurrentShoppingCart(@Nonnull HttpServletRequest request) {
        HttpSession session = request.getSession();
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute(ShoppingCart.class.getName());
        if (shoppingCart == null) {
            shoppingCart = new ShoppingCart();
            session.setAttribute(ShoppingCart.class.getName(), shoppingCart);
        }
        return shoppingCart;
    }

    public void resetCurrentShoppingCart(@Nonnull HttpServletRequest request){
        request.getSession().removeAttribute(ShoppingCart.class.getName());
    }
}
