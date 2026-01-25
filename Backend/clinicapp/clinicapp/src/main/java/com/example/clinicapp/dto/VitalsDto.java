package com.example.clinicapp.dto;



public class VitalsDto {
    private String temp;
    private String bp;
    private String pulse;
    private String spo2;
    private String height;
    private String weight;
    private String bmi;
    private String waistHip;
    
    
    
	public VitalsDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public VitalsDto(String temp, String bp, String spo2, String pulse, String height, String weight, String bmi,
			String waistHip) {
		super();
		this.temp = temp;
		this.bp = bp;
		this.spo2 = spo2;
		this.pulse = pulse;
		this.height = height;
		this.weight = weight;
		this.bmi = bmi;
		this.waistHip = waistHip;
	}
	public String getTemp() {
		return temp;
	}
	public void setTemp(String temp) {
		this.temp = temp;
	}
	public String getBp() {
		return bp;
	}
	public void setBp(String bp) {
		this.bp = bp;
	}
	public String getSpo2() {
		return spo2;
	}
	public void setSpo2(String spo2) {
		this.spo2 = spo2;
	}

	public String getPulse() {
		return pulse;
	}
	public void setPulse(String pulse) {
		this.pulse = pulse;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getBmi() {
		return bmi;
	}
	public void setBmi(String bmi) {
		this.bmi = bmi;
	}
	public String getWaistHip() {
		return waistHip;
	}
	public void setWaistHip(String waistHip) {
		this.waistHip = waistHip;
	}


}
