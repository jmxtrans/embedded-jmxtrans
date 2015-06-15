package org.jmxtrans.embedded.output;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * It's the contrary of ImplicitEpoch
 *
 * @see ImplicitEpoch
 */
@Retention(RetentionPolicy.SOURCE)
@interface ExplicitEpoch {
}
