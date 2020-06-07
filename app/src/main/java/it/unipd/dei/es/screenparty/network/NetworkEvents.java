package it.unipd.dei.es.screenparty.network;

public final class NetworkEvents {

    public static final int CONNECTION_FAILED = 1;
    public static final int JOIN_FAILED = 2;
    public static final int COMMUNICATION_FAILED = 3;
    public static final int FILE_TRANSFER_FAILED = 4;

    public static final class Host {
        public static final int NOT_STARTED = 10;
        public static final int WAITING_DEVICES = 11;
        public static final int CLIENT_JOINED = 12;
        public static final int PARTY_READY = 13;
        public static final int CLIENT_LEFT = 14;
    }

    public static final class Client {
        public static final int PARTY_CONNECTING = 20;
        public static final int PARTY_JOINED = 21;
        public static final int PARTY_FULL = 22;
        public static final int HOST_PLAY = 23;
        public static final int HOST_PAUSE = 24;
        public static final int HOST_RESUME = 25;
        public static final int HOST_STOP = 26;
        public static final int HOST_EXIT = 27;
        public static final int HOST_LEFT = 28;
    }
}
