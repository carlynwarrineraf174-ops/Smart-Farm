package util;

import farm.FarmManager;

import java.io.*;

public class SaveLoad {
    private static final String SAVE_DIR = "saves";

    public static void ensureSaveDir() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    public static void save(String slotName) throws IOException {
        ensureSaveDir();
        String data = FarmManager.getInstance().serialize();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_DIR + "/" + slotName + ".txt"))) {
            writer.write(data);
        }
    }

    public static boolean load(String slotName) throws IOException {
        File file = new File(SAVE_DIR + "/" + slotName + ".txt");
        if (!file.exists()) return false;
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
        }
        FarmManager.getInstance().deserialize(sb.toString());
        return true;
    }

    public static String[] listSaves() {
        ensureSaveDir();
        File dir = new File(SAVE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".txt"));
        return files != null ? files : new String[0];
    }
}
