package it.unipd.dei.es.screenparty.network;

public final class NetworkCommands {

    public static final class Host {
        public static final String OK = "OK";
        public static final String NEXT = "NEXT";
        public static final String FULL = "FULL";
        public static final String UNKNOWN = "UNKNOWN";
        public static final String PLAY = "PLAY";
        public static final String PAUSE = "PAUSE";
        public static final String SEEK = "SEEK";
    }

    public static final class Client {
        public static final String JOIN = "JOIN";
        public static final String READY = "READY";
        public static final String EXIT_PLAYER = "EXIT";
        public static final String ENTER_PLAYER = "ENTER";
    }
}
