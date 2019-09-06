package org.techtown.SmartCushion;

public class DailyStatistics {
    String hour;
    int status;

    public DailyStatistics(String hour, int status) {
        this.hour = hour;
        this.status = status;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
