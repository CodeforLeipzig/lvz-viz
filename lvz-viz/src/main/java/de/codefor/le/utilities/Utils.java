package de.codefor.le.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {


    public static String getArticleId(String detailLink) {
        String result = "";
        String lastOne = detailLink.substring(detailLink.lastIndexOf("/"));
        System.out.println("last one " + lastOne);
        Pattern p = Pattern.compile("[0-9]{6}");
        Matcher m = p.matcher(lastOne);
        if (m.find()) {
            result = lastOne.substring(m.start(), m.end());
        }
        return result;
    }
}
