import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> words;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        List<String> stopWords = Files.readAllLines(Paths.get(Main.STOP_WORDS_FILE_PATH));
        List<File> PDFList = List.of(Objects.requireNonNull(pdfsDir.listFiles()));
        words = new HashMap<>();
        for (File pdf : PDFList) {
            var doc = new PdfDocument(new PdfReader(pdf));
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(i + 1));
                var words = Stream.of(text.toLowerCase().split("\\P{IsAlphabetic}+"))
                        .collect(Collectors.toCollection(ArrayList<String>::new));
                words.removeAll(stopWords);

                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    freqs.put(word.toLowerCase(), freqs.getOrDefault(word.toLowerCase(), 0) + 1);
                }
                int count;
                for (var word : freqs.keySet()) {
                    if (freqs.containsKey(word)) {
                        count = freqs.get(word);
                        this.words.computeIfAbsent(word, w -> new ArrayList<>()).add(new PageEntry(pdf.getName(), i + 1, count));
                    }
                }
                freqs.clear();
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> tempList = new ArrayList<>();
        List<PageEntry> result = new ArrayList<>();
        String[] request = word.toLowerCase().split("\\P{IsAlphabetic}+");
        for (String newRequest : request) {
            if (words.get(newRequest) != null) {
                tempList.addAll(words.get(newRequest));
            }
        }
        Map<String, Map<Integer, Integer>> pageNumberAndCounter = new HashMap<>();
        for (PageEntry pageEntry : tempList) {
            pageNumberAndCounter.computeIfAbsent(pageEntry.getPdfName(), key -> new HashMap<>()).merge(pageEntry.getPage(), pageEntry.getCount(), Integer::sum);
        }

        pageNumberAndCounter.forEach((key, value) -> {
            for (var tempPage : value.entrySet()) {
                result.add(new PageEntry(key, tempPage.getKey(), tempPage.getValue()));
            }
        });
        Collections.sort(result);
        return result;
    }
}
