package de.codefor.le.ner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

@Component
public class NER {
    String serializedClassifier = "dewac_175m_600.crf.ser.gz";
    AbstractSequenceClassifier<CoreLabel> classifier;
    List<String> blackListedLocations;
    private static final Logger logger = LoggerFactory.getLogger(NER.class);

    public NER() {
        try {
            classifier = CRFClassifier.<CoreLabel> getClassifier(new File(serializedClassifier));
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getLocations(String text, boolean removeBlackListed) {
        List<String> result = new ArrayList<>();
        List<List<CoreLabel>> classify = classifier.classify(text);
        for (List<CoreLabel> list : classify) {
            for (CoreLabel coreLabel : list) {
                if (coreLabel.get(AnswerAnnotation.class).equals("I-LOC")) {
                    logger.info("{}", coreLabel.originalText());
                    result.add(coreLabel.originalText());
                }
            }
        }
        if (removeBlackListed) {
            if (blackListedLocations == null) {
                getBlackListedLocations();
            }
            blackListCheck(result);

        }
        return result;
    }

    private void blackListCheck(List<String> result) {
        Iterator<String> iterator = result.iterator();

        while (iterator.hasNext()) {
            String next = iterator.next();
            if (blackListedLocations.contains(next)) {
                logger.info("found {} in blacklist. remove it", next);
                iterator.remove();
            }
        }
    }

    private void getBlackListedLocations() {
        URL u = getClass().getClassLoader().getResource("locationBlacklist");
        logger.info("u {}", u);
        blackListedLocations = new ArrayList<>();
        try {
            Path p = Paths.get(u.toURI());
            blackListedLocations = Files.readAllLines(p, Charset.forName("UTF-8"));
        } catch (IOException | URISyntaxException e) {
            logger.error("could not find the location Blacklist", e);
            e.printStackTrace();
        }
    }

}