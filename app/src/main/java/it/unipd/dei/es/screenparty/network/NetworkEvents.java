package it.unipd.dei.es.screenparty.network;

public final class NetworkEvents {

    public static final int CONNECTION_FAILED = 1;
    public static final int JOIN_FAILED = 2;
    public static final int COMMUNICATION_FAILED = 3;

    public static final class Host {
        public static final int NOT_STARTED = 10;
        public static final int WAITING_DEVICES = 11;
        public static final int CLIENT_JOINED = 12;
        public static final int PARTY_READY = 13;
        public static final int CLIENT_LEFT = 14;
        public static final int CLIENT_EXIT_PLAYER = 15;
        public static final int CLIENT_ENTER_PLAYER = 16;
    }

    public static final class Client {
        public static final int PARTY_JOINED = 21;
        public static final int PARTY_FULL = 22;
        public static final int HOST_NEXT = 23;
        public static final int HOST_PLAY = 24;
        public static final int HOST_PAUSE = 25;
        public static final int HOST_SEEK = 26;
        public static final int HOST_LEFT = 27;
    }
}
