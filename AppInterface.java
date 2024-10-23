package csc435.app;

import java.lang.System;
import java.util.Scanner;
import java.util.ArrayList;

public class ClientAppInterface {
    private ClientProcessingEngine engine;

    public ClientAppInterface(ClientProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            command = sc.nextLine();

            if (command.compareTo("quit") == 0) {
                engine.disconnect();
                break;
            }

            if (command.length() >= 7 && command.substring(0, 7).compareTo("connect") == 0) {
                String[] parts = command.split(" ");
                if (parts.length == 3) {
                    engine.connect(parts[1], parts[2]);
                } else {
                    System.out.println("Invalid connect command. Usage: connect <serverIP> <serverPort>");
                }
                continue;
            }
            
            if (command.length() >= 5 && command.substring(0, 5).compareTo("index") == 0) {
                String[] parts = command.split(" ");
                if (parts.length == 2) {
                    IndexResult result = engine.indexFiles(parts[1]);
                    System.out.printf("Indexing completed in %.2f seconds. Total bytes read: %d\n", result.executionTime, result.totalBytesRead);
                } else {
                    System.out.println("Invalid index command. Usage: index <folderPath>");
                }
                continue;
            }

            if (command.length() >= 6 && command.substring(0, 6).compareTo("search") == 0) {
                String[] parts = command.split(" ");
                if (parts.length > 1) {
                    ArrayList<String> terms = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        terms.add(parts[i]);
                    }
                    SearchResult result = engine.searchFiles(terms);
                    System.out.printf("Search completed in %.2f seconds.\n", result.excutionTime);
                    System.out.println("Top 10 search results:");
                    int count = 0;
                    for (DocPathFreqPair pair : result.documentFrequencies) {
                        System.out.printf("%s (Frequency: %d)\n", pair.documentPath, pair.wordFrequency);
                        if (++count == 10) break;
                    }
                } else {
                    System.out.println("Invalid search command. Usage: search <term1> <term2> ...");
                }
                continue;
            }

            System.out.println("unrecognized command!");
        }

        sc.close();
    }
}