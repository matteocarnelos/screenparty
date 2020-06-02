package it.unipd.dei.es.screenparty.party;

import it.unipd.dei.es.screenparty.media.MediaParams;

public class PartyParams {

    public enum Role { HOST, CLIENT }
    public enum Position { LEFT, CENTER, RIGHT }

    private Role role;
    private Position position;
    private float screenWidth;
    private float screenHeight;
    private float screenXdpi;
    private float screenYdpi;
    private MediaParams mediaParams;

    public Role getRole() {
        return role;
    }

    public Position getPosition() {
        return position;
    }

    public float getScreenWidth() {
        return screenWidth;
    }

    public float getScreenHeight() {
        return screenHeight;
    }

    public float getXdpi() {
        return screenXdpi;
    }

    public float getYdpi() {
        return screenYdpi;
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

    public void setScreenWidth(float screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(float screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void setScreenXdpi(float screenXdpi) {
        this.screenXdpi = screenXdpi;
    }

    public void setScreenYdpi(float screenYdpi) {
        this.screenYdpi = screenYdpi;
    }

    public void setMediaParams(MediaParams mediaParams) {
        this.mediaParams = mediaParams;
    }
}
