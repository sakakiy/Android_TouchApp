package com.example.touchapplication;

public class SensorData {
    private float  sensorValue = 0;
    private String date        = "";
    private String time        = "";

    public SensorData() {

    }

    public float getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(float sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDateTime() {
        return date + " " + time;
    }

}
