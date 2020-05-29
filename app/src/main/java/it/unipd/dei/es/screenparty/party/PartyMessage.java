package it.unipd.dei.es.screenparty.party;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PartyMessage {

    public static final String SEP = " ";
    public static final String TERMINATOR = "\r\n";

    private String command;
    private List<String> arguments;

    public PartyMessage() {
        this("", new ArrayList<String>());
    }

    public PartyMessage(String command) {
        this(command, new ArrayList<String>());
    }

    public PartyMessage(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public static PartyMessage parseString(String message) {
        message = message.replace(TERMINATOR, "");
        String command = message.split(SEP)[0];
        List<String> arguments = new LinkedList<>(Arrays.asList(message.split(SEP)));
        arguments.remove(0);
        return new PartyMessage(command, arguments);
    }

    public String getCommand() {
        return command;
    }

    public String getArgument(int index) {
        return arguments.get(index);
    }

    public PartyMessage setCommand(String command) {
        this.command = command;
        return this;
    }

    public PartyMessage addArgument(String argument) {
        arguments.add(argument);
        return this;
    }

    @Override
    @NonNull
    public String toString() {
        String message = command;
        for(String argument : arguments) message += SEP + argument;
        return message + TERMINATOR;
    }
}
