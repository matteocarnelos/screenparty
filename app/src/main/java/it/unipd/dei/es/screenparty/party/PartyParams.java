package it.unipd.dei.es.screenparty.party;

import it.unipd.dei.es.screenparty.media.MediaParams;

public class PartyParams {

    public enum Role { HOST, CLIENT }
    public enum Position { LEFT, CENTER, RIGHT }

    private Role role;
    private Position position;
    private ScreenParams screenParams;
    private MediaParams mediaParams;

    public PartyParams(ScreenParams screenParams) {
        this.screenParams = screenParams;
    }

    public Role getRole() {
        return role;
    }

    public Position getPosition() {
        return position;
    }

    public ScreenParams getScreenParams() {
        return screenParams;
    }

    public MediaParams getMediaParams() {
        return mediaParams;
    }

    public void setRole(Role role) {
        this.role = role;
        if(role == Role.HOST) this.position = Position.CENTER;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setMediaParams(MediaParams mediaParams) {
        this.mediaParams = mediaParams;
    }
}
