package de.codefor.le.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class NER {

    private static final Logger logger = LoggerFactory.getLogger(NER.class);

    private static final String BLACKLIST_FILE = "classpath:locationBlacklist";

    private static final String BLACKLIST_COMMENT = "#";

    private static final String SERIALIZED_CLASSIFIER = "dewac_175m_600.crf.ser.gz";

    private final ResourceLoader resourceLoader;

    @Getter(lazy = true, onMethod = @__({ @SuppressWarnings({ "all", "unchecked" }) }), value = AccessLevel.PRIVATE)
    private final AbstractSequenceClassifier<CoreLabel> classifier = initClassifier();

    @Getter(lazy = true, onMethod = @__({ @SuppressWarnings({ "all", "unchecked" }) }), value = AccessLevel.PROTECTED)
    private final Collection<String> blackListedLocations = initBlackListedLocations();

    private static AbstractSequenceClassifier<CoreLabel> initClassifier() {
        logger.info("Init classifier for Named-entity recognition (NER).");
        try {
            return CRFClassifier.<CoreLabel> getClassifier(new File(SERIALIZED_CLASSIFIER));
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> getLocations(final String text, final boolean removeBlackListed) {
        final Set<String> result = new HashSet<>();
        for (final List<CoreLabel> classifiedSentences : getClassifier().classify(text)) {
            for (final CoreLabel coreLabel : classifiedSentences) {
                if (coreLabel.get(AnswerAnnotation.class).equals("I-LOC")) {
                    final String originalText = coreLabel.originalText();
                    logger.trace("{}", originalText);
                    result.add(originalText);
                }
            }
        }
        if (removeBlackListed) {
            blackListCheck(result);
        }
        logger.debug("{} locations found: {}", result.size(), result);
        return result;
    }

    private void blackListCheck(final Iterable<String> results) {
        final var iterator = results.iterator();
        while (iterator.hasNext()) {
            final var next = iterator.next();
            if (getBlackListedLocations().contains(next)) {
                logger.debug("remove blacklisted location {}", next);
                iterator.remove();
            }
        }
    }

    private Collection<String> initBlackListedLocations() {
        logger.info("Init location blacklist from {}", BLACKLIST_FILE);
        try (var br = new BufferedReader(
                new InputStreamReader(resourceLoader.getResource(BLACKLIST_FILE).getInputStream()));
                var lines = br.lines()) {
            final var blacklist = lines
                    .filter(line -> !Strings.isNullOrEmpty(line) && !line.startsWith(BLACKLIST_COMMENT))
                    .collect(Collectors.toUnmodifiableSet());
            logger.debug("initialized location blacklist: {}", blacklist);
            return blacklist;
        } catch (final IOException e) {
            throw new UncheckedIOException("Error during init of blacklist", e);
        }
    }
}
