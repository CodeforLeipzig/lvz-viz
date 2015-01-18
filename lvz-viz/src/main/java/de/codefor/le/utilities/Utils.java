package de.codefor.le.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import de.codefor.le.web.PoliceTickerController;

public final class Utils {

    private static final Logger logger = LoggerFactory.getLogger(PoliceTickerController.class);

    private static final Pattern PATTERN_ARTICLE_ID = Pattern.compile("[0-9]{6}");

    public static String getArticleId(String detailLink) {
        String result = "";
        if (Strings.isNullOrEmpty(detailLink)) {
            return result;
        }
        final int indexOfSlash = detailLink.lastIndexOf("/");
        if (indexOfSlash == -1) {
            return result;
        }
        final String lastOne = detailLink.substring(indexOfSlash);
        logger.debug("last one {}", lastOne);
        final Matcher m = PATTERN_ARTICLE_ID.matcher(lastOne);
        if (m.find()) {
            result = lastOne.substring(m.start(), m.end());
        }
        return result;
    }
}
