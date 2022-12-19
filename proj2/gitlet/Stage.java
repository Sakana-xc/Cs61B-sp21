package gitlet;


import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
    private HashMap<String, String> added;
    private HashSet<String> removed;



    public Stage() {
        added = new HashMap<>();
        removed = new HashSet<>();
    }

    public void addFiles(String filename, String blobId) {
        added.put(filename, blobId);
        removed.remove(filename);

    }

    public void removeFiles(String filename) {
        added.remove(filename);
        removed.add(filename);
    }
    public  HashMap<String, String> toBeAdded() {
        return added;
    }

    public HashSet<String> toBeRemoved() {
        return removed;
    }
    public boolean isEmpty(){
        return added.isEmpty() && removed.isEmpty();
    }

    public  ArrayList<String> getStagedFileName() {
        ArrayList<String> names = new ArrayList<>();
        names.addAll(added.keySet());
        names.addAll(removed);
        return names;
    }


}
