package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    private  byte[] content;

    private  String id;

    private  String  filename;

    private File file;



    public Blob(String filename,File CWD){
        this.filename = filename;
        this.file = join(CWD,filename);
        if (file.exists()){
            this.content = readContents(file);
            this.id = sha1(filename,content);}
        else{this.content = null;
            this.id = sha1(filename);}
    }

    public String getFilename(){
        return filename;
    }

    public String getId(){
        return this.id;
    }

    public byte[] getContent(){
        return this.content;
    }

    public File getFile(){
        return this.file;
    }

    public String getContentAsString(){
        return new String(content, StandardCharsets.UTF_8);
    }





}
