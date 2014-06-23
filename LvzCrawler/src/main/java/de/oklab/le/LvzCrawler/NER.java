package de.oklab.le.LvzCrawler;

import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class NER {

	public static void main(String[] args) throws IOException {
		String serializedClassifier = "dewac_175m_600.crf.ser.gz";
		String text = "Leipzig. In Leipzig ist erneut eine Video-Überwachungskamera entdeckt worden. Laut Mitteilung des linken Netzwerks Indymedia befand sich die Anlage in dem leerstehenden Haus Gießerstraße 47 in Plagwitz. Offenbar handelt es sich um eine staatliche Überwachungsmaßnahme. Auf der Technik habe sich der Aufkleber „Polizei Sachsen“ befunden. "
				+ "Es sei nicht erkennbar gewesen, ob die Anlage schon in Betrieb war, heißt es bei Indymedia weiter. Auf zwei Stativen seien Kameras montiert gewesen. \"Vermutlich waren dieser Überwachung zahlreiche Menschen ausgesetzt\", spekuliert Indymedia: In der Nähe des Eckhauses befindet sich die Bushaltestelle Siemensstraße. Die technische Anlage soll auch ein Übertragungsgerät und ein SIM-Modul besitzen. Damit hätten die Kamerabilder live übertragen werden können."
				+ "Die Leipziger Linken-Stadträtin Juliane Nagel forderte am Samstag mehr Transparenz und Aufklärung über den Einsatz der Observationsanlagen in Connewitz und Plagwitz. Videoüberwachung sei ein erheblicher Eingriff in die Privatsphäre und Bewegungsfreiheit des Einzelnen. Bei verdeckten Aktionen könnten außerdem unbeteiligte Dritte in Mitleidenschaft gezogen werden."
				+ "Rätselraten um Kamera in Connewitz"
				+ "Im März war in Leipzig-Connewitz, Simildenstraße, ebenfalls eine behördliche Anlage entdeckt worden. Diese war damals aber nicht gekennzeichnet, und es herrschte mehrere Tage Rätselraten um die Herkunft der Geräte. Lorenz Haase, Sprecher der Dresdner Staatsanwaltschaft, hatte dann auf Anfrage der LVZ eingeräumt, dass es sich bei der Kamera um Observationstechnik im Rahmen einer Ermittlung der Behörde handelte. In welchem Zusammenhang ermittelt wurde, ließ er offen."
				+ "Auf eine kleine Anfrage der Grünen im Landtag erklärte das Justizministerium, dass die Kamera in Connewitz noch nicht in Betrieb war. Die Staatsanwaltschaft Dresden hätte eine längerfristige Observation angeordnet.;Jusitzminister Jürgen Martens (FDP) erklärte ebenfalls, dass zwei weitere Kameras in Leipzig zu Überwachungszwecken eingesetzt würden. Eine sei mobil, die andere an einem geheimen Standort. Indiymedia vermutet, dass es sich in Plagwitz um diese Kamera handeln könnte.";
		for (List<CoreLabel> list : runNER(text, serializedClassifier)) {
			for (CoreLabel coreLabel : list) {
				if(coreLabel.get(AnswerAnnotation.class).equals("I-LOC")){
				System.out.println(coreLabel.originalText());
			}}
		}
	}

	public static List<List<CoreLabel>> runNER(String text,
			String serializedClassifier) throws IOException {
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier
				.getClassifierNoExceptions(serializedClassifier);
		return classifier.classify(text);
	}

}