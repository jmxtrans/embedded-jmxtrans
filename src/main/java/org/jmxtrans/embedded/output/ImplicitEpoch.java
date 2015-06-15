package org.jmxtrans.embedded.output;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the OutputWriter is not exporting the time (epoch) at which the metric was collected.
 * This is generally implied by the protocol being used to transport the metric.
 * In this case, it's the server who's receiving the data that will set the metric epoch to the time it received it.
 * <p/>
 * The support for explicit epoch may be important when the export is happening a while after it was collected.
 * OutputWriters implementing ImplicitEpoch will need to be exported sooner after the collection, and retry on error may not be appropriate.
 * OutputWriters implementing ExplicitEpoch will support export delayed from collection, and retry on error will be remain accurate.
 */
@Retention(RetentionPolicy.SOURCE)
@interface ImplicitEpoch {
}
