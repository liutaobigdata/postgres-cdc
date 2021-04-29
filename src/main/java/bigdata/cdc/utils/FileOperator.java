package bigdata.cdc.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileOperator {
    private static File file;

    /**
     * @desc write lsn to disk
     */
    public static void writeFile(String fileName, long lsn) {
        try {

            file = new File(fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos = null;
            Runtime.getRuntime().exec("chmod 777 " + fileName);

            //覆盖写入文件
            fos = new FileOutputStream(file, false);
            FileChannel fileChannel = fos.getChannel();
            ByteBuffer buffer = ByteBuffer.wrap(String.valueOf(lsn).getBytes());
            fileChannel.write(buffer);
            fileChannel.close();
            fos.close();
            buffer.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String readLsnFile(String fileName) {
        String lsn = "";
        try {
            File file = new File(fileName);
            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), "utf-8");// 考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);

            while ((lsn = bufferedReader.readLine()) != null) {
                return lsn.trim();
            }
            bufferedReader.close();
            read.close();
        } catch (Exception e) {
            System.out.println("读取lsn文件异常" + e.getLocalizedMessage());
        }
        return lsn;
    }

}
