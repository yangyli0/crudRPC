package com.alibaba.middleware.race.rpc.serialization;

import java.io.*;

/**
 * Created by lee on 7/20/17.
 */
public class JavaSerialization {
    public static byte[] encode(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();   // TODO: 待删除
        } catch (IOException e ) {
            e.printStackTrace();
        }
        finally {
            try {
                bos.close();
            } catch(IOException e) {}
        }
        return bos.toByteArray();

    }

    public static Object decode(byte[] objBytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(objBytes);
        ObjectInputStream ois = null;
        Object obj = null;
        try {
            ois = new ObjectInputStream(bis);
             obj = ois.readObject();
             return obj;        // TODO: 待删除
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch(IOException e) {}
            }
        }
        return obj;
    }
}
















