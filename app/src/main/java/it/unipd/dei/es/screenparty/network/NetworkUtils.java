package it.unipd.dei.es.screenparty.network;

import android.os.Handler;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

/**
 * Class containing all the necessary utilities for the network.
 */
public class NetworkUtils {

    public static final String SEP_ENCODING = "%20";
    public static final String INVALID_IP = "0.0.0.0";

    /**
     * Send a {@link NetworkMessage} to the given {@link Socket}.
     * @param message The {@link NetworkMessage} object to send.
     * @param socket The {@link Socket} to use for the communication.
     * @param handler The {@link Handler} for the handling of communication events.
     */
    public static void send(final NetworkMessage message, final Socket socket, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { socket.getOutputStream().write(message.toString().getBytes()); }
                catch(IOException e) { handler.obtainMessage(NetworkEvents.COMMUNICATION_FAILED, e); }
            }
        }).start();
    }

    /**
     * Get the IP address of the device.
     * @param useIPv4 Set it to true in order to obtain the IPv4 address, otherwise the IPv6 address
     *                will be returned.
     * @return A string containing the ip address.
     */
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

    /**
     * Encode the device name for transmission over the network. In particular, substitute every
     * occurrence of the character used as the separator with the encoded version.
     * @param deviceName The device name as a string.
     * @return The encoded string.
     */
    @NotNull
    public static String encodeDeviceName(@NotNull String deviceName) {
        return deviceName.trim().replaceAll(NetworkMessage.SEP, SEP_ENCODING);
    }

    /**
     * Decode the given device name received from the network, in particular substitute every
     * encoded separator character with the original separator character.
     * @param deviceName The encoded device name.
     * @return The original device name.
     */
    @NotNull
    public static String decodeDeviceName(@NotNull String deviceName) {
        return deviceName.trim().replaceAll(SEP_ENCODING, NetworkMessage.SEP);
    }
}
