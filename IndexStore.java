package csc435.app;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Data structure that stores a document number and the number of time a word/term appears in the document
class DocFreqPair {
    public long documentNumber;
    public long wordFrequency;

    public DocFreqPair(long documentNumber, long wordFrequency) {
        this.documentNumber = documentNumber;
        this.wordFrequency = wordFrequency;
    }
}

public class IndexStore {
    private ConcurrentHashMap<Long, String> documentMap;
    private ConcurrentHashMap<String, ArrayList<DocFreqPair>> termInvertedIndex;
    private AtomicLong docCounter;
    private ReadWriteLock docMapLock;
    private ReadWriteLock termInvertedIndexLock;

    public IndexStore() {
        documentMap = new ConcurrentHashMap<>();
        termInvertedIndex = new ConcurrentHashMap<>();
        docCounter = new AtomicLong(0);
        docMapLock = new ReentrantReadWriteLock();
        termInvertedIndexLock = new ReentrantReadWriteLock();
    }

    public long putDocument(String documentPath) {
        long documentNumber = 0;
        docMapLock.writeLock().lock();
        try {
            documentNumber = docCounter.incrementAndGet();
            documentMap.put(documentNumber, documentPath);
        } finally {
            docMapLock.writeLock().unlock();
        }
        return documentNumber;
    }

    public String getDocument(long documentNumber) {
        String documentPath = "";
        docMapLock.readLock().lock();
        try {
            documentPath = documentMap.getOrDefault(documentNumber, "");
        } finally {
            docMapLock.readLock().unlock();
        }
        return documentPath;
    }

    public void updateIndex(long documentNumber, HashMap<String, Long> wordFrequencies) {
        termInvertedIndexLock.writeLock().lock();
        try {
            for (HashMap.Entry<String, Long> entry : wordFrequencies.entrySet()) {
                String term = entry.getKey();
                Long frequency = entry.getValue();
                
                termInvertedIndex.computeIfAbsent(term, k -> new ArrayList<>())
                    .add(new DocFreqPair(documentNumber, frequency));
            }
        } finally {
            termInvertedIndexLock.writeLock().unlock();
        }
    }

    public ArrayList<DocFreqPair> lookupIndex(String term) {
        ArrayList<DocFreqPair> results = new ArrayList<>();
        termInvertedIndexLock.readLock().lock();
        try {
            results = new ArrayList<>(termInvertedIndex.getOrDefault(term, new ArrayList<>()));
        } finally {
            termInvertedIndexLock.readLock().unlock();
        }
        return results;
    }
}