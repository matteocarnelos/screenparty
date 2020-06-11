package it.unipd.dei.es.screenparty.party;

import it.unipd.dei.es.screenparty.media.MediaParams;

/**
 * Manages the party parameters of a connected device.
 */
public class PartyParams {

    public enum Role { HOST, CLIENT }
    public enum Position { LEFT, CENTER, RIGHT }

    private Role role;
    private Position position;
    private String deviceName;
    private ScreenParams screenParams;
    private MediaParams mediaParams = new MediaParams();

    /**
     * Constructor of {@link PartyParams}. Create a new {@link PartyParams} and sets the screen
     * parameters as indicated by the given {@link ScreenParams} parameter.
     * @param screenParams: The {@link ScreenParams} of the new {@link PartyParams}.
     */
    public PartyParams(ScreenParams screenParams) {
        this.screenParams = screenParams;
    }

    /**
     * Get the {@link Role} of the device.
     * @return the {@link Role} of the device.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Get the {@link Position} of the device.
     * @return the {@link Position} of the device.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Get the name of the device.
     * @return the name of the device.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Get the {@link ScreenParams} of the device.
     * @return the {@link ScreenParams} of the device.
     */
    public ScreenParams getScreenParams() {
        return screenParams;
    }

    /**
     * Get the {@link MediaParams} of the device.
     * @return the {@link MediaParams} of the device.
     */
    public MediaParams getMediaParams() {
        return mediaParams;
    }

    /**
     * Set the {@link Role} of the device.
     * @param role: the {@link Role} to be set as role of the device.
     */
    public void setRole(Role role) {
        this.role = role;
        if(role == Role.HOST) this.position = Position.CENTER;
    }

    /**
     * Set the {@link Position} of the device.
     * @param position: the {@link Position} to be set as position of the device.
     */
    public void setPosition(Position position) {
        this.position = position;
        if(position == Position.CENTER) this.role = Role.HOST;
        else this.role = Role.CLIENT;
    }

    /**
     * Set the name of the device.
     * @param deviceName: the {@link String} to be set as name of the device.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * Set the {@link MediaParams} of the device.
     * @param mediaParams: the {@link MediaParams} to be set as MediaParams of the device.
     */
    public void setMediaParams(MediaParams mediaParams) {
        this.mediaParams = mediaParams;
    }
}
