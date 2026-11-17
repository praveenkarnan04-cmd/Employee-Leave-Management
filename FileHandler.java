package elms.storage;

import java.io.*;

public final class FileHandler {
    private FileHandler() {}

    public static void save(Object object, String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IllegalStateException("Could not create data folder.");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(object);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save system data: " + ex.getMessage(), ex);
        }
    }

    public static <T> T load(String filePath, Class<T> type) {
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object value = in.readObject();
            return type.cast(value);
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            throw new IllegalStateException("Could not load system data. Back up or reset the data file. " + ex.getMessage(), ex);
        }
    }
}
