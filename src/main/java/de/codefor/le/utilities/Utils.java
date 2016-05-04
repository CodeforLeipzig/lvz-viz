package de.codefor.le.utilities;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Generate sha1 hash for url.
     *
     * @see https://code.google.com/p/guava-libraries/wiki/HashingExplained
     * @param url
     * @return sha1 hash of given url
     */
    public static String generateHashForUrl(final String url) {
        final String result = Hashing.sha1().newHasher().putString(Strings.nullToEmpty(url), StandardCharsets.UTF_8).hash().toString();
        logger.debug("generated hash {} for url '{}'", result, url);
        return result;
    }
}
