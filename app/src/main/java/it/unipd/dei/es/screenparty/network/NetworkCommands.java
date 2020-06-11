package it.unipd.dei.es.screenparty.network;

/**
 * Final class containing all the commands that can be sent through the network as constants.
 */
public final class NetworkCommands {

    /**
     * Subset of {@link NetworkCommands} containing all the host commands.
     */
    public static final class Host {
        public static final String OK = "OK";
        public static final String NEXT = "NEXT";
        public static final String FULL = "FULL";
        public static final String UNKNOWN = "UNKNOWN";
        public static final String PLAY = "PLAY";
        public static final String PAUSE = "PAUSE";
        public static final String SEEK = "SEEK";
    }

    /**
     * Subset of {@link NetworkCommands} containing all the client commands.
     */
    public static final class Client {
        public static final String JOIN = "JOIN";
        public static final String READY = "READY";
        public static final String EXIT_PLAYER = "EXIT";
        public static final String ENTER_PLAYER = "ENTER";
    }
}
