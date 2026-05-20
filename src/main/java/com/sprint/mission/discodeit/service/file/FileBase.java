package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.BaseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class FileBase  {
//    static final long serialVersionUID = 1L;
    Path path;
    public FileBase(Path path) {
        this.path = path;
    }

    void init(Path file){
        if (!Files.exists(file)){
            try {
                Files.createFile(file);
                System.out.println("file created successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    <T extends BaseEntity> HashMap<UUID,T> load() {
        init(path);
        HashMap<UUID,T> res = new HashMap<>();
        try (
                FileInputStream fis = new FileInputStream(this.path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
        ){
            while(true){
                T i = (T) ois.readObject();
                res.put(i.getId(), i);
            }
        }
        catch (EOFException e){
            System.out.println("read end");
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return res;
    }

    <T> void save(HashMap<UUID,T> ent){
        try (
                FileOutputStream fos = new FileOutputStream(this.path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        )
        {
            ArrayList<UUID> keys = new ArrayList<>(ent.keySet());
            for (UUID key : keys) {
                oos.writeObject(ent.get(key));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
