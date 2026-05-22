package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BaseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class FileBaseRepository {
    public FileBaseRepository(){}

    private static void checkDirectory(Path file){
        if (!Files.exists(file.getParent())) {
            try {
                Files.createDirectories(file.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static <T extends BaseEntity> HashMap<UUID,T> load(Path path) {
        checkDirectory(path);

        HashMap<UUID,T> res = new HashMap<>();
        try (
//                FileInputStream fis = new FileInputStream(this.path.toFile());
                BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(path));
                ObjectInputStream ois = new ObjectInputStream(bis)
        ){
            while(true){
                T i = (T) ois.readObject();
                res.put(i.getId(), i);
            }
        }
        catch (EOFException e){
            // file read done.
        }
        catch (NoSuchFileException e){
            System.out.println("No such file or directory");
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return res;
    }

    static <T> void save(HashMap<UUID,T> ent, Path path){
        checkDirectory(path);

        try (
//                FileOutputStream fos = new FileOutputStream(this.path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(
//                        fos
                new BufferedOutputStream(Files.newOutputStream(path))
                )
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
