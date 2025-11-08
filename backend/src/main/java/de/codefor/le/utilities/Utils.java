package de.codefor.le.utilities;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Generate sha1 hash for url.
     *
     * @param url the url
     * @return sha1 hash of given url
     * @see <a href="https://github.com/google/guava/wiki/HashingExplained">Guava hashing explained</a>
     */
    public static String generateHashForUrl(final String url) {
        final String result = Hashing.sha1().newHasher().putString(Strings.nullToEmpty(url), StandardCharsets.UTF_8).hash().toString();
        logger.debug("generated hash {} for url '{}'", result, url);
        return result;
    }
}
