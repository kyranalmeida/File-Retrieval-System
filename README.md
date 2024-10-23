# File Retrieval System

A multi-threaded server application that provides document indexing and search capabilities. This system allows clients to connect, index documents, and perform search queries across the indexed content.

## System Architecture

### Components

1. **FileRetrievalServer**
   - Main entry point of the application
   - Initializes core components and starts the server
   - Handles command-line configuration for server port

2. **IndexStore**
   - Core data structure maintaining document indexes
   - Thread-safe implementation using ReentrantLocks
   - Manages:
     - Document mapping (document number → path)
     - Inverted index (term → document-frequency pairs)

3. **IndexWorker**
   - Handles individual client connections
   - Processes indexing and search requests
   - Maintains client-specific input/output streams

4. **ServerProcessingEngine**
   - Manages server operations
   - Dispatches client connections to workers
   - Maintains thread pool for worker execution
   - Tracks connected clients

5. **ServerAppInterface**
   - Provides command-line interface for server management
   - Supports commands for listing clients and shutting down the server

## Setup and Running

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Java build tool (e.g., Maven, Gradle) or direct compilation capability

### Compilation
```bash
javac csc435/app/*.java
```

### Running the Server
```bash
java csc435.app.FileRetrievalServer [port]
```
- `[port]`: Optional port number (default: 8080)

## Server Commands

1. `list`
   - Lists all currently connected clients
   - Shows client IDs and connection information

2. `quit`
   - Gracefully shuts down the server
   - Closes all client connections
   - Cleans up resources

## Protocol Specification

### Index Request
```
Command: "INDEX"
Parameters: 
1. Document Path (String)
2. Word Frequencies (HashMap<String, Long>)
Response: 
1. "INDEX_REPLY"
2. Document Number (Long)
```

### Search Request
```
Command: "SEARCH"
Parameters:
1. Search Terms (ArrayList<String>)
Response:
1. "SEARCH_REPLY"
2. Top 10 Results (ArrayList<String>)
   Format: "documentPath (score: frequency)"
```

### Quit Request
```
Command: "QUIT"
Parameters: None
Response: None (Connection closes)
```

## Thread Safety

The system implements thread safety through several mechanisms:
- ReentrantLocks for document map and term index access
- ConcurrentHashMap for client tracking
- AtomicLong for document number generation
- Thread pool for worker management

## Performance Considerations

1. **Search Operations**
   - Results are sorted by frequency score
   - Limited to top 10 matches for efficiency
   - Combines scores across multiple search terms

2. **Index Operations**
   - Thread-safe document number generation
   - Atomic updates to prevent data corruption
   - Efficient lock management to minimize contention

3. **Connection Management**
   - Dynamic thread pool scaling
   - Efficient resource cleanup
   - Non-blocking client acceptance

## Error Handling

The system implements robust error handling:
- Socket connection errors
- Invalid client requests
- Resource cleanup on client disconnection
- Graceful server shutdown

## Limitations

1. Memory Usage
   - All indexes are stored in memory
   - No persistent storage implementation
   - Memory usage grows with indexed documents

2. Search Capabilities
   - Basic term-frequency based ranking
   - No advanced text analysis or stemming
   - Limited to exact term matches

## Future Improvements

1. Add persistent storage support
2. Implement advanced text analysis
3. Add support for document deletion
4. Implement connection timeout handling
5. Add support for batch indexing
6. Implement query result pagination

## Contributing

When contributing to this project:
1. Follow existing code style and patterns
2. Maintain thread safety in new features
3. Add appropriate error handling
4. Update documentation as needed
5. Include unit tests for new functionality
