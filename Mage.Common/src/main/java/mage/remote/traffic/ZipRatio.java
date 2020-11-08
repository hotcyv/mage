package mage.remote.traffic;

import mage.view.ChatMessage;
import mage.view.GameClientMessage;
import mage.view.GameView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public enum ZipRatio {
    instance;

    /*private double gzipSize;
    private double zstSize;
    private double gzipAll;
    private double zstAll;*/
    private HashMap<Class, Double> gzipSize;
    private HashMap<Class, Double> zstSize;
    private HashMap<Class, Double> gzipAll;
    private HashMap<Class, Double> zstAll;

    ZipRatio() {
        ArrayList<Class> classes = new ArrayList<>(
                Arrays.asList(GameClientMessage.class, ChatMessage.class, GameView.class));
        gzipSize = new HashMap<>(3);
        zstSize = new HashMap<>(3);
        gzipAll = new HashMap<>(3);
        zstAll = new HashMap<>(3);

        for (Class cls : classes) {
            gzipSize.put(cls, 0D);
            zstSize.put(cls, 0D);
            gzipAll.put(cls, 0D);
            zstAll.put(cls, 0D);
        }
        //zstSize = gzipSize = gzipAll = zstAll = 0;
    }

    public void addGzipSize(Class cls, int size) {
        gzipSize.put(cls, (double) size);
        gzipAll.put(cls, gzipAll.get(cls) + (double) size);
    }

    public void addZstSize(Class cls, int size) {
        zstSize.put(cls, (double) size);
        zstAll.put(cls, zstAll.get(cls) + (double) size);
        printStats(cls);
    }

    public void printStats(Class cls) {
        System.out.println(String.format("Partial (%s): gz %,.2f KB, zst %,.2f KB || %,.2f %%",
                cls.getSimpleName(), toKByte(gzipSize.get(cls)), toKByte(zstSize.get(cls)), getPartialRatio(cls)));
        System.out.println(String.format("TOTAL (%s): gz %,.2f MB, zst %,.2f MB // %,.2f %%",
                cls.getSimpleName(), toMByte(gzipAll.get(cls)), toMByte(zstAll.get(cls)), getTotalRatio(cls)));
        //System.out.println("PARTIAL: Gz: " + gzipSize + " | Zst:" + zstSize + " || " + getPartialRatio() + "%");
        //System.out.println("TOTAL: Gz: " + gzipAll + " | Zst:" + zstAll + " || " + getTotalRatio() + "%");
        //System.out.println("Total ratio: " + getTotalRatio() + "%");
    }

    public double getPartialRatio(Class cls) {
        return (1 - (zstSize.get(cls) / gzipSize.get(cls))) * 100;
    }

    public double getTotalRatio(Class cls) {
        return (1 - (zstAll.get(cls) / gzipAll.get(cls))) * 100; }

    public double toMByte(double byteSize) {
        return byteSize / 1000 / 1000;
    }

    public double toKByte(double byteSize) {
        return byteSize / 1000;
    }
}
