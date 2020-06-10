package it.unipd.dei.es.screenparty.party;

import it.unipd.dei.es.screenparty.media.MediaParams;

public class PartyParams {

    public enum Role { HOST, CLIENT }
    public enum Position { LEFT, CENTER, RIGHT }

    private Role role;
    private Position position;
    private String deviceName;
    private ScreenParams screenParams;
    private MediaParams mediaParams = new MediaParams();
    private boolean partyReady = false;

    public PartyParams(ScreenParams screenParams) {
        this.screenParams = screenParams;
    }

    public Role getRole() {
        return role;
    }

    public Position getPosition() {
        return position;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public ScreenParams getScreenParams() {
        return screenParams;
    }

    public MediaParams getMediaParams() {
        return mediaParams;
    }

    public boolean isPartyReady() {
        return partyReady;
    }

    public void setRole(Role role) {
        this.role = role;
        if(role == Role.HOST) this.position = Position.CENTER;
    }

    public void setPosition(Position position) {
        this.position = position;
        if(position == Position.CENTER) this.role = Role.HOST;
        else this.role = Role.CLIENT;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setMediaParams(MediaParams mediaParams) {
        this.mediaParams = mediaParams;
    }

    public void setPartyReady(boolean partyReady) {
        this.partyReady = partyReady;
    }
}
