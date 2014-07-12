package de.codefor.le.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
                    logger.debug("{}", coreLabel.originalText());
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
                logger.debug("found {} in blacklist. remove it", next);
                iterator.remove();
            }
        }
    }

    private void getBlackListedLocations() {
        blackListedLocations = new ArrayList<>();

        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader()
                .getResourceAsStream("locationBlacklist")))) {
            while ((line = br.readLine()) != null) {
                blackListedLocations.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}