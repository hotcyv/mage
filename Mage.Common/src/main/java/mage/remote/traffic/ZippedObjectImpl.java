package mage.remote.traffic;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import mage.MageObject;
import mage.view.ChatMessage;
import mage.view.GameClientMessage;
import mage.view.GameView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementation for compressing and decompressing objects using {@link GZIPInputStream} and {@link GZIPOutputStream}.
 * Can be used to send any {@link Object} over internet to reduce traffic usage.
 *
 * @author ayrat
 */
public class ZippedObjectImpl<T> implements ZippedObject<T>, Serializable {

    private byte[] data;
    private boolean dictTrain = false;

    public ZippedObjectImpl(T object) {
        zip(object);
    }

    public void zip(T object) {
        try {
            byte[] dict = null;

            if (dictTrain) {
                saveObjectsAsFile(object);
            }
            //ZipRatio.instance.reset();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
/*            GZIPOutputStream gz = new GZIPOutputStream(bos);
            ObjectOutputStream oos = new ObjectOutputStream(gz);*/
/*            byte [] dict = ZstdDict.instance.getDict(object.getClass().getSimpleName());
            if (dict != null){
                zst.setDict(dict);
            }*/
            ZstdOutputStream zst = null;
            dict = ZstdDict.instance.getDictBySample(object);
            if (dict != null) {
                if (object instanceof GameClientMessage)
                    bos.write(1);
                if (object instanceof GameView)
                    bos.write(2);
                if (object instanceof ChatMessage)
                    bos.write(3);
                if (ZstdDict.instance.isNewDict(object.getClass())) {
                    bos.write(dict);
                }
                zst = new ZstdOutputStream(bos);
                zst.setDict(dict);
            } else {
                zst = new ZstdOutputStream(bos);
            }

            ObjectOutputStream oos = new ObjectOutputStream(zst);
            oos.writeObject(object);
            oos.close();
            data = bos.toByteArray();

            ZipRatio.instance.addZstSize(object.getClass(),data.length);

            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
            GZIPOutputStream gz2 = new GZIPOutputStream(bos2);
            ObjectOutputStream oos2 = new ObjectOutputStream(gz2);
            oos2.writeObject(object);
            oos2.close();

            ZipRatio.instance.addGzipSize(object.getClass(),bos2.toByteArray().length);

/*          if (object instanceof GameClientMessage){
                System.out.println(GraphLayout.parseInstance(object).toFootprint());
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public T unzip() {
        try {
            byte[] dictId = new byte[1];
            Class cls = MageObject.class;
            ZstdInputStream zst = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
/*            GZIPInputStream gz = new GZIPInputStream(bis);
            ObjectInputStream ois = new ObjectInputStream(gz);*/
            byte[] dict = ZstdDict.instance.getDictByFrame(data);
            if (dict != null) {
                bis.read(dictId,0,1); //skip dictId byte
                switch (dictId[0]){
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
                if (ZstdDict.instance.isNewDict(cls)) {
                    bis.skip(112640); //skip dictionary bytes
                }
                zst = new ZstdInputStream(bis);
                zst.setDict(dict);
            } else {
                zst = new ZstdInputStream(bis);
            }
            ObjectInputStream ois = new ObjectInputStream(zst);
            Object o = ois.readObject();
            return (T) o;
        } catch (Exception e) {
            return null;
        }
    }

    private void saveObjectsAsFile(T object) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            String className = object.getClass().getSimpleName();
            FileOutputStream fileOut;
            fileOut = new FileOutputStream("/home/victor/tmp/objects/"
                    + className + "/" + sdf.format(new Date()));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            objectOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final long serialVersionUID = 1L;
}
