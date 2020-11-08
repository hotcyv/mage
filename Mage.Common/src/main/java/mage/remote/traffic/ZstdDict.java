package mage.remote.traffic;

import com.github.luben.zstd.ZstdDictTrainer;
import mage.MageObject;
import mage.view.ChatMessage;
import mage.view.GameClientMessage;
import mage.view.GameView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public enum ZstdDict {

    instance;

    private HashMap<Class, Boolean> newDicts;
    private HashMap<Class, byte[]> dicts;
    private HashMap<Class, ZstdDictTrainer> dictTrainers;

    ZstdDict() {
        dicts = new HashMap<>(3);
        newDicts = new HashMap<>(3);
        dictTrainers = new HashMap<>(3);

        try {
            dicts.put(GameClientMessage.class, Files.readAllBytes(Paths.get(
                    ZstdDict.class.getResource("/zstddicts/GameClientMessage").getPath())));
            dicts.put(GameView.class, Files.readAllBytes(Paths.get(
                    ZstdDict.class.getResource("/zstddicts/GameView").getPath())));
            dicts.put(ChatMessage.class, Files.readAllBytes(Paths.get(
                    ZstdDict.class.getResource("/zstddicts/ChatMessage").getPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getDictBySample(Object object) {
        try {
            if ((object instanceof GameClientMessage) || (object instanceof GameView)
                    || (object instanceof ChatMessage)) {

                Class cls = object.getClass();

                if (dictTrainers.get(cls) == null) {
                    dictTrainers.put(cls, new ZstdDictTrainer(100 * 112640, 112640));
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                oos.close();

                if (!dictTrainers.get(cls).addSample(bos.toByteArray())) {
                    dicts.put(cls, dictTrainers.get(cls).trainSamples());
                    dictTrainers.put(cls, null);
                    newDicts.put(cls, true);
                    return dicts.get(cls);
                }
                if (dicts.get(cls) != null) {
                    newDicts.put(cls, false);
                    return dicts.get(cls);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //TODO: take just some frame bytes, not the role data
    public byte[] getDictByFrame(byte[] frame) {

        try {
            Class cls = MageObject.class;
            byte[] dictIdFrame = new byte[1];
            ByteArrayInputStream bis = new ByteArrayInputStream(frame);
            bis.read(dictIdFrame, 0, 1);// read dictId TODO: how to check id uniques
            switch (dictIdFrame[0]) {
                case 1:
                    cls = GameClientMessage.class;
                    break;
                case 2:
                    cls = GameView.class;
                    break;
                case 3:
                    cls = ChatMessage.class;
                    break;
            }

            if (frame.length >= 112640) {
                byte[] dictFrame = new byte[112640]; //default zstd dict size;
                bis.read(dictFrame, 0, 112640);
                dicts.put(cls, dictFrame);
                newDicts.put(cls, true);
                return dicts.get(cls);
            }
            if (dicts.get(cls) != null) {
                newDicts.put(cls, false);
                return dicts.get(cls);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isNewDict(Class cls) {
        return newDicts.get(cls);
    }
}