package org.example;

import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.commons.text.similarity.CosineSimilarity;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws URISyntaxException, FileNotFoundException {
        DecimalFormat df = new DecimalFormat("#.00");

        //parse csv
        ClassLoader classLoader = Main.class.getClassLoader();
        URL url = classLoader.getResource("amazon_co-ecommerce_sample.csv");
        Path path = Paths.get(url.toURI());
        List<ToyProduct> toys = new CsvToBeanBuilder(new FileReader(path.toFile()))
                .withType(ToyProduct.class)
                .withSkipLines(1)
                .withQuoteChar('\"')
                .build()
                .parse();

        Map<String, Double> similarityMap = new HashMap<>();

        for (int i = 0; i < toys.size(); i++) {
            for (int j = i + 1; j < toys.size(); j++) {
                if(toys.get(i).getDescription() == null ||toys.get(j).getDescription() == null){
                    continue;
                }
                double sim = computeCosineSimilarity(toys.get(i).getDescription(), toys.get(j).getDescription());
                similarityMap.put(toys.get(i).getName() + " (" + toys.get(i).getId() + ") " + " and "+toys.get(j).getName()+ " ("  +toys.get(j).getId()+ ")", sim);
            }
        }

        List<Map.Entry<String, Double>> results = similarityMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).toList();

        results.stream().limit(5).forEach(result -> System.out.println(result.getKey() + " ,similarity: " + df.format(result.getValue())));

    }


    public static double computeCosineSimilarity(String text1, String text2) {
        Map<CharSequence, Integer> vectorA = toFrequencyMap(text1);
        Map<CharSequence, Integer> vectorB = toFrequencyMap(text2);
        CosineSimilarity cosine = new CosineSimilarity();
        return cosine.cosineSimilarity(vectorA, vectorB);
    }

    private static Map<CharSequence, Integer> toFrequencyMap(String text) {
        Map<CharSequence, Integer> freq = new HashMap<>();
        text = text.replace("Product Description", "");
        String[] textParts = Normalizer.normalize(text, Normalizer.Form.NFD).toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\W+");
        for (String token : textParts) {
            if (token.isBlank()) continue;
            freq.put(token, freq.getOrDefault(token, 0) + 1);
        }
        return freq;
    }
}