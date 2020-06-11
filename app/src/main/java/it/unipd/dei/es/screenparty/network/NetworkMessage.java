package it.unipd.dei.es.screenparty.network;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that represent a network message.
 * A network message is composed by two fields: <br>
 * - A command, typically a {@link NetworkCommands} command. <br>
 * - One or more arguments. <br>
 */
public class NetworkMessage {

    public static final String SEP = " ";
    public static final String TERMINATOR = "\r\n";

    private String command;
    private List<String> arguments;

    /**
     * Create a new, empty, {@link NetworkMessage}.
     */
    public NetworkMessage() {
        this("", new ArrayList<String>());
    }

    /**
     * Create a new {@link NetworkMessage} with the given command.
     * @param command The command as a string.
     */
    public NetworkMessage(String command) {
        this(command, new ArrayList<String>());
    }

    /**
     * Create a new {@link NetworkMessage} with the given command and arguments.
     * @param command The command of the message.
     * @param arguments The list of arguments.
     */
    public NetworkMessage(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    /**
     * Convert the given message string to a {@link NetworkMessage} object.
     * @param message The message string.
     * @return The correspondent {@link NetworkMessage} object.
     */
    @NotNull
    public static NetworkMessage parseString(String message) {
        message = message.replace(TERMINATOR, "");
        String command = message.split(SEP)[0];
        List<String> arguments = new LinkedList<>(Arrays.asList(message.split(SEP)));
        arguments.remove(0);
        return new NetworkMessage(command, arguments);
    }

    /**
     * Get the command of the message.
     * @return A string representing the command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Get the argument at the corresponding index.
     * @param index The index of the argument.
     * @return The argument as a string.
     */
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

    /**
     * Builder class for {@link NetworkMessage} objects.
     */
    public static class Builder {

        private NetworkMessage message = new NetworkMessage();

        /**
         * Set the command of the message.
         * @param command The command as a string.
         * @return The {@link NetworkMessage.Builder} object with the changes applied.
         */
        public NetworkMessage.Builder setCommand(String command) {
            this.message.command = command;
            return this;
        }

        /**
         * Add an argument to the message.
         * @param argument The argument to add as a string.
         * @return The {@link NetworkMessage.Builder} object with the changes applied.
         */
        public NetworkMessage.Builder addArgument(String argument) {
            this.message.arguments.add(argument);
            return this;
        }

        /**
         * Build the message with all the settings previously set.
         * @return The built {@link NetworkMessage} object.
         */
        public NetworkMessage build() {
            return message;
        }
    }
}
