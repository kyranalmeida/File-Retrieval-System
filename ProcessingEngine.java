package csc435.app;

import java.util.ArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class IndexResult {
    public double executionTime;
    public long totalBytesRead;

    public IndexResult(double executionTime, long totalBytesRead) {
        this.executionTime = executionTime;
        this.totalBytesRead = totalBytesRead;
    }
}

class DocPathFreqPair {
    public String documentPath;
    public long wordFrequency;

    public DocPathFreqPair(String documentPath, long wordFrequency) {
        this.documentPath = documentPath;
        this.wordFrequency = wordFrequency;
    }
}

class SearchResult {
    public double excutionTime;
    public ArrayList<DocPathFreqPair> documentFrequencies;

    public SearchResult(double executionTime, ArrayList<DocPathFreqPair> documentFrequencies) {
        this.excutionTime = executionTime;
        this.documentFrequencies = documentFrequencies;
    }
}

public class ProcessingEngine {
    // keep a reference to the index store
    private IndexStore store;

    // the number of worker threads to use during indexing
    private int numWorkerThreads;

    public ProcessingEngine(IndexStore store, int numWorkerThreads) {
        this.store = store;
        this.numWorkerThreads = numWorkerThreads;
    }

    public IndexResult indexFiles(String folderPath) {
        IndexResult result = new IndexResult(0.0, 0);
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<Path> filePaths = Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
        
            ExecutorService executor = Executors.newFixedThreadPool(numWorkerThreads);
            List<Future<Long>> futures = new ArrayList<>();
            
            for (Path filePath : filePaths) {
                futures.add(executor.submit(() -> indexFile(filePath)));
            }

            long totalBytesRead = 0;
            for (Future<Long> future : futures) {
                totalBytesRead += future.get();
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);

            long endTime = System.currentTimeMillis();
            double executionTime = (endTime - startTime) / 1000.0;

            result = new IndexResult(executionTime, totalBytesRead);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    private long indexFile(Path filePath) throws IOException {
        long documentNumber = store.putDocument(filePath.toString());
        String content = new String(Files.readAllBytes(filePath));
        HashMap<String, Long> wordFrequencies = extractWordFrequencies(content);
        store.updateIndex(documentNumber, wordFrequencies);
        return Files.size(filePath);
    }

    private HashMap<String, Long> extractWordFrequencies(String content) {
        HashMap<String, Long> frequencies = new HashMap<>();
        String[] words = content.split("\\W+");
        for (String word : words) {
            if (word.length() > 2 && word.matches("^[a-zA-Z0-9]+$")) {
                frequencies.put(word.toLowerCase(), frequencies.getOrDefault(word.toLowerCase(), 0L) + 1);
            }
        }
        return frequencies;
    }

    public SearchResult searchFiles(ArrayList<String> terms) {
        SearchResult result = new SearchResult(0.0, new ArrayList<DocPathFreqPair>());
        long startTime = System.currentTimeMillis();

        HashMap<Long, Long> documentFrequencies = new HashMap<>();

        for (String term : terms) {
            ArrayList<DocFreqPair> pairs = store.lookupIndex(term.toLowerCase());
            for (DocFreqPair pair : pairs) {
                documentFrequencies.merge(pair.documentNumber, pair.wordFrequency, Long::sum);
            }
        }

        List<Map.Entry<Long, Long>> sortedEntries = documentFrequencies.entrySet().stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());

        ArrayList<DocPathFreqPair> topDocuments = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : sortedEntries) {
            String documentPath = store.getDocument(entry.getKey());
            topDocuments.add(new DocPathFreqPair(documentPath, entry.getValue()));
        }

        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;

        result = new SearchResult(executionTime, topDocuments);
        return result;
    }
}