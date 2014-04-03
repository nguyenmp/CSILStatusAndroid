package com.nguyenmp.csilstatus.app;

public class Computer {
    public String hostname;
    public String ipAddress;

    @Override
    public String toString() {
        return String.format("{%s, %s}", ipAddress, hostname);
    }

    @Override
    public int hashCode() {
        return hostname.hashCode() + ipAddress.hashCode();
    }
}