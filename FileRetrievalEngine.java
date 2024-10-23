package csc435.app;

public class FileRetrievalEngine 
{
    public static void main(String[] args)
    {
        int numWorkerThreads = 1;
 numWorkerThreads = Integer.parseInt(args[0]);
      
        IndexStore store = new IndexStore();
        ProcessingEngine engine = new ProcessingEngine(store, numWorkerThreads);
        AppInterface appInterface = new AppInterface(engine);
        appInterface.readCommands();
    }
}