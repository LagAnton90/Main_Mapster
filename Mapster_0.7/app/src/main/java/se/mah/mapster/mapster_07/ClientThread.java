package se.mah.mapster.mapster_07;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread extends Thread {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Bitmap map;
    private String ip;
    private int port;
    private SearchListener searchListener;

    public ClientThread(String ip, int port, SearchListener searchListener) {
        this.ip = ip;
        this.port = port;
        this.searchListener = searchListener;
    }

    public void run() {
        try {
            socket = new Socket(ip, port);

            ois = new ObjectInputStream(socket.getInputStream());

            String map_name = searchListener.getMapName();
            map = receiveFile(ois, map_name);
            searchListener.setBitmap(map);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap receiveFile(ObjectInputStream ois, String fileName) throws Exception {
//        String baseDir = Environment.getExternalStorageDirectory()+File.separator+"Mapster";
//        String fileInES = baseDir + File.separator + fileName;
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Mapster");
        File fileInDir = new File(directory + File.separator + fileName);

        // read 4 bytes containing the file size
        byte[] bSize = new byte[4];
        int offset = 0;

        while (offset < bSize.length) {
            int bRead = ois.read(bSize, offset, bSize.length - offset);
            offset += bRead;
        }

        // Convert the 4 bytes to an int
        int fileSize;
        fileSize = (int) (bSize[0] & 0xff) << 24
                | (int) (bSize[1] & 0xff) << 16
                | (int) (bSize[2] & 0xff) << 8
                | (int) (bSize[3] & 0xff);

        // buffer to read from the socket
        // 8k buffer is good enough
        byte[] data = new byte[8 * 1024];

        int bToRead;
        FileOutputStream fos = new FileOutputStream(fileInDir);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        while (fileSize > 0) {
            // make sure not to read more bytes than filesize
            if (fileSize > data.length) {
                bToRead = data.length;
            } else {
                bToRead = fileSize;
            }

            int bytesRead = ois.read(data, 0, bToRead);
            if (bytesRead > 0) {
                bos.write(data, 0, bytesRead);
                fileSize -= bytesRead;
            }
        }
        bos.close();

        // Convert the received image to a Bitmap
        Bitmap bmp = null;
        FileInputStream fis = new FileInputStream(fileInDir);
        try {
            bmp = BitmapFactory.decodeStream(fis);
            return bmp;
        } finally {
            fis.close();
        }
    }
}