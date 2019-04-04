package sdi.com.pkb.preview;

import androidx.annotation.NonNull;

/**
 * Created by Kartik on 04-Apr-19.
 */
public class RcBook {
    private String regNo;
    private String regDate;
    private String chassisNo;
    private String manufacturer;
    private String engineNo;
    private String classType;
    private String color;
    private String ownerName;
    private String relative;
    private String ownerAddress;
    private String vehicleModel;
    private String noOfCyclinders;
    private String seating;
    private String fuel;
    private String mfgDate;
    private String regExpire;
    private String cc;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getRegDate() {
        return regDate;
    }

    public void setRegDate(String regDate) {
        this.regDate = regDate;
    }

    public String getChassisNo() {
        return chassisNo;
    }

    public void setChassisNo(String chassisNo) {
        this.chassisNo = chassisNo;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getEngineNo() {
        return engineNo;
    }

    public void setEngineNo(String engineNo) {
        this.engineNo = engineNo;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getRelative() {
        return relative;
    }

    public void setRelative(String relative) {
        this.relative = relative;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getNoOfCyclinders() {
        return noOfCyclinders;
    }

    public void setNoOfCyclinders(String noOfCyclinders) {
        this.noOfCyclinders = noOfCyclinders;
    }

    public String getSeating() {
        return seating;
    }

    public void setSeating(String seating) {
        this.seating = seating;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getRegExpire() {
        return regExpire;
    }

    public void setRegExpire(String regExpire) {
        this.regExpire = regExpire;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    @NonNull
    @Override
    public String toString() {
        return "Owner:"+getOwnerName()+"|Chassis no:"+getChassisNo()+"|Engine no:"+getEngineNo();
    }
}
