package com.nguyenmp.csilstatus.app;

public interface ShowDetailsCallback {
    public static enum Type {
        Computer,
        User
    }
    public void showDetails(Type type, String hostname);
}