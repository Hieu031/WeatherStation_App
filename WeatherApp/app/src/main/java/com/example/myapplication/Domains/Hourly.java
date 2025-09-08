/*** --- Function of module --- 
 * Model for data be used for predict weather according to hour
 */
package com.example.myapplication.Domains;

// Create a class has needed attributes
public class Hourly {
    private String hour;    
    private int temp;
    private String picPath;

    // Constructor: Initialize a object to use
    public Hourly(String hour, int temp, String picPath) {
        this.hour = hour;
        this.temp = temp;
        this.picPath = picPath;
    }

    // use get/set to use attribute private
    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
}
