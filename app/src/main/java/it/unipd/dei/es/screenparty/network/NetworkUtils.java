package it.unipd.dei.es.screenparty.network;

import android.os.Handler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkUtils {

    public static final String SPACE_CHAR_ENCODING = "%20";
    public static final String INVALID_IP = "0.0.0.0";
    private final static int CHUNK_SIZE = 8192;

    public static void send(final NetworkMessage message, final Socket socket, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { socket.getOutputStream().write(message.toString().getBytes()); }
                catch(IOException e) { handler.obtainMessage(NetworkEvents.COMMUNICATION_FAILED, e); }
            }
        }).start();
    }

    @NotNull
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for(NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for(InetAddress addr : addrs) {
                    if(!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
        return INVALID_IP;
    }

    @NotNull
    public static String encodeDeviceName(@NotNull String deviceName) {
        return deviceName.trim().replaceAll("\\s", SPACE_CHAR_ENCODING);
    }

    @NotNull
    public static String decodeDeviceName(@NotNull String deviceName) {
        return deviceName.trim().replaceAll(SPACE_CHAR_ENCODING, " ");
    }

    public static void transferFile(@NotNull List<Socket> sockets, InputStream fileInputStream) throws IOException {
        List<OutputStream> outputStreams = new ArrayList<>();
        for(Socket socket : sockets) outputStreams.add(socket.getOutputStream());
        byte[] chunk = new byte[CHUNK_SIZE];
        int k;
        while((k = fileInputStream.read(chunk)) != -1)
            for(OutputStream outputStream : outputStreams)
                outputStream.write(chunk, 0, k);
        fileInputStream.close();
    }

    public static void receiveFile(@NotNull Socket socket, OutputStream fileOutputStream, long size) throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] chunk = new byte[CHUNK_SIZE];
        int bytes, k;
        for(bytes = 0; (k = inputStream.read(chunk)) != -1 && bytes < size; bytes += k) {
            fileOutputStream.write(chunk, 0, k);
        }
        fileOutputStream.close();
    }
}
