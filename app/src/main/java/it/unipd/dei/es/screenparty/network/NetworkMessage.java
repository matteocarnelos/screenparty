package it.unipd.dei.es.screenparty.network;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NetworkMessage {

    public static final String SEP = " ";
    public static final String TERMINATOR = "\r\n";

    private String command;
    private List<String> arguments;

    public NetworkMessage() {
        this("", new ArrayList<String>());
    }

    public NetworkMessage(String command) {
        this(command, new ArrayList<String>());
    }

    public NetworkMessage(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public static NetworkMessage parseString(String message) {
        message = message.replace(TERMINATOR, "");
        String command = message.split(SEP)[0];
        List<String> arguments = new LinkedList<>(Arrays.asList(message.split(SEP)));
        arguments.remove(0);
        return new NetworkMessage(command, arguments);
    }

    public String getCommand() {
        return command;
    }

    public String getArgument(int index) {
        return arguments.get(index);
    }

    @Override
    @NonNull
    public String toString() {
        String message = command;
        for(String argument : arguments) message += SEP + argument;
        return message + TERMINATOR;
    }

    public static class Builder {

        private NetworkMessage message = new NetworkMessage();

        public NetworkMessage.Builder setCommand(String command) {
            this.message.command = command;
            return this;
        }

        public NetworkMessage.Builder addArgument(String argument) {
            this.message.arguments.add(argument);
            return this;
        }

        public NetworkMessage build() {
            return message;
        }
    }
}
