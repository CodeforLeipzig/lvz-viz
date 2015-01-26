package de.codefor.le.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.base.Throwables;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

@Component
public class NER {

    private static final Logger logger = LoggerFactory.getLogger(NER.class);

    private static final String serializedClassifier = "dewac_175m_600.crf.ser.gz";
    private AbstractSequenceClassifier<CoreLabel> classifier;
    List<String> blackListedLocations;

    public NER() {
        try {
            classifier = CRFClassifier.<CoreLabel> getClassifier(new File(serializedClassifier));
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            Throwables.propagate(e);
        }
    }

    public List<String> getLocations(String text, boolean removeBlackListed) {
        final List<String> result = new ArrayList<>();
        for (final List<CoreLabel> classifiedSentences : classifier.classify(text)) {
            for (final CoreLabel coreLabel : classifiedSentences) {
                if (coreLabel.get(AnswerAnnotation.class).equals("I-LOC")) {
                    final String originalText = coreLabel.originalText();
                    logger.debug("{}", originalText);
                    result.add(originalText);
                }
            }
        }
        if (removeBlackListed) {
            if (blackListedLocations == null) {
                blackListedLocations = initBlackListedLocations();
            }
            blackListCheck(result);

        }
        return result;
    }

    private void blackListCheck(List<String> result) {
        final Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            final String next = iterator.next();
            if (blackListedLocations.contains(next)) {
                logger.debug("found {} in blacklist. remove it", next);
                iterator.remove();
            }
        }
    }

    static List<String> initBlackListedLocations() {
        final List<String> blacklist = new ArrayList<>();

        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(NER.class.getClassLoader()
                .getResourceAsStream("locationBlacklist")))) {
            while ((line = br.readLine()) != null) {
                blacklist.add(line);
            }
        } catch (final IOException e) {
            logger.error(e.toString(), e);
        }
        return blacklist;
    }

}
