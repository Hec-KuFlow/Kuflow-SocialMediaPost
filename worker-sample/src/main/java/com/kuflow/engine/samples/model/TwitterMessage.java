package com.kuflow.engine.samples.model;

import java.time.Instant;

public class TwitterMessage {
    private Instant instant;
	private String social;
	private String message;
	private String asset;


    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }	
	public String getSocial() {
		return this.social;
	}
	public void setSocial(String social) {
	   this.social = social;
	}
	
	public String getMessage() {
		return this.message;
	}
	public void setMessage(String message) {
	   this.message = message;
	}
	public String getAsset() {
		return this.asset;
	}
	public void setAsset(String asset) {
	   this.asset = asset;
	}
}
