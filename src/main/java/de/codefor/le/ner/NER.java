package de.codefor.le.ner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
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

    private static final String UNSPECIFIC_LOCATIONS_FILE = "classpath:unspecific-locations.txt";

    private static final String COMMENT = "#";

    private static final String SERIALIZED_CLASSIFIER = "dewac_175m_600.crf.ser.gz";

    private final ResourceLoader resourceLoader;

    @Getter(lazy = true, onMethod = @__({ @SuppressWarnings({ "all", "unchecked" }) }), value = AccessLevel.PRIVATE)
    private final AbstractSequenceClassifier<CoreLabel> classifier = initClassifier();

    @Getter(lazy = true, onMethod = @__({ @SuppressWarnings({ "all", "unchecked" }) }), value = AccessLevel.PROTECTED)
    private final Collection<String> unspecificLocations = initUnspecificLocations();

    private static AbstractSequenceClassifier<CoreLabel> initClassifier() {
        logger.info("Init classifier for Named-entity recognition (NER).");
        try {
            return CRFClassifier.getClassifier(new File(SERIALIZED_CLASSIFIER));
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<String> getLocations(final String text, final boolean removeUnspecificLocations) {
        final var locations = getClassifier().classify(normalizeKnownLocations(Strings.nullToEmpty(text))).stream()
            .flatMap(Collection::stream)
            .filter(coreLabel -> coreLabel.get(AnswerAnnotation.class).equals("I-LOC"))
            .map(CoreLabel::originalText)
            .peek(logger::trace)
            .filter(loc -> !removeUnspecificLocations || isAllowed(loc))
            .collect(Collectors.toUnmodifiableList());

        logger.debug("{} location(s) found: {}", locations.size(), locations);
        return locations;
    }

    private boolean isAllowed(final String location) {
        if (getUnspecificLocations().contains(location)) {
            logger.trace("ignore location {}", location);
            return false;
        }
        logger.trace("allow location {}", location);
        return true;
    }

    private Collection<String> initUnspecificLocations() {
        logger.info("Init unspecific locations from {}", UNSPECIFIC_LOCATIONS_FILE);
        try (var br = new BufferedReader(
            new InputStreamReader(Objects.requireNonNull(resourceLoader).getResource(UNSPECIFIC_LOCATIONS_FILE).getInputStream(),
                StandardCharsets.UTF_8));
            var lines = br.lines()) {
            final var locations = lines
                    .filter(line -> !Strings.isNullOrEmpty(line) && !line.startsWith(COMMENT))
                    .collect(Collectors.toUnmodifiableSet());
            logger.debug("Initialized unspecific locations: {}", locations);
            return locations;
        } catch (final IOException e) {
            throw new UncheckedIOException("Error during init of unspecific locations", e);
        }
    }

    private String normalizeKnownLocations(String text) {
        return text.replace("König-Albert Brücke", "König-Albert-Brücke");
    }
}
