package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Stage implements Serializable {
    private HashMap<String,String> added;
    private HashSet<String> removed;

    public Stage(){
        added = new HashMap<>();
        removed = new HashSet<>();
    }

    public void addFiles(String filename, String blobId){
        added.put(filename,blobId);
        removed.remove(filename);

    }

    public void removeFiles(String filename){
        added.remove(filename);
        removed.add(filename);
    }
    public  HashMap<String, String> addition(){
        return added;
    }

    public HashSet<String> removal() {
        return removed;
    }
}