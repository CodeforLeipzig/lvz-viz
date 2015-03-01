package de.codefor.le.utilities;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;

public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final Pattern PATTERN_ARTICLE_ID = Pattern.compile("[0-9]{6}");

    /**
     * Extract number from end of article url.
     *
     * @param url
     * @return empty string or article id
     */
    public static String extractArticleId(final String url) {
        String result = "";
        if (Strings.isNullOrEmpty(url)) {
            return result;
        }
        final int indexOfSlash = url.lastIndexOf("/");
        if (indexOfSlash == -1) {
            return result;
        }
        final String lastOne = url.substring(indexOfSlash);
        logger.debug("last one {}", lastOne);
        final Matcher m = PATTERN_ARTICLE_ID.matcher(lastOne);
        if (m.find()) {
            result = lastOne.substring(m.start(), m.end());
        }
        return result;
    }

    /**
     * Generate sha1 hash for url.
     *
     * @see https://code.google.com/p/guava-libraries/wiki/HashingExplained
     * @param url
     * @return sha1 hash of given url
     */
    public static String generateHashForUrl(final String url) {
        final String result = Hashing.sha1().newHasher().putString(Strings.nullToEmpty(url), StandardCharsets.UTF_8)
                .hash().toString();
        logger.debug("generated hash {} for url '{}'", result, url);
        return result;
    }
}
