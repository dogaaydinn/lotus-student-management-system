package com.lotus.lotusSPM.dto;

public class MfaSetupResponse {
    private String secret;
    private String qrCodeUrl;
    private String backupCodes;

    public MfaSetupResponse() {
    }

    public MfaSetupResponse(String secret, String qrCodeUrl, String backupCodes) {
        this.secret = secret;
        this.qrCodeUrl = qrCodeUrl;
        this.backupCodes = backupCodes;
    }

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getBackupCodes() {
        return backupCodes;
    }

    public void setBackupCodes(String backupCodes) {
        this.backupCodes = backupCodes;
    }
}
