package com.sierra_dorada.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mipaquete")
public class MiPaqueteProperties {
    private String baseUrl;
    private String apiKey;
    private String sessionTracker;
    private String originCountryCode = "170";
    private String originDaneCode;
    private String senderName;
    private String senderSurname;
    private String senderEmail;
    private String senderPhone;
    private String senderDocumentType = "NIT";
    private String senderDocument;
    private String senderAddress;
    private String userId;
    private boolean requestPickup;
    private boolean forbiddenProduct;
    private String webhookSecret;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getSessionTracker() { return sessionTracker; }
    public void setSessionTracker(String sessionTracker) { this.sessionTracker = sessionTracker; }
    public String getOriginCountryCode() { return originCountryCode; }
    public void setOriginCountryCode(String originCountryCode) { this.originCountryCode = originCountryCode; }
    public String getOriginDaneCode() { return originDaneCode; }
    public void setOriginDaneCode(String originDaneCode) { this.originDaneCode = originDaneCode; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderSurname() { return senderSurname; }
    public void setSenderSurname(String senderSurname) { this.senderSurname = senderSurname; }
    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
    public String getSenderDocumentType() { return senderDocumentType; }
    public void setSenderDocumentType(String senderDocumentType) { this.senderDocumentType = senderDocumentType; }
    public String getSenderDocument() { return senderDocument; }
    public void setSenderDocument(String senderDocument) { this.senderDocument = senderDocument; }
    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public boolean isRequestPickup() { return requestPickup; }
    public void setRequestPickup(boolean requestPickup) { this.requestPickup = requestPickup; }
    public boolean isForbiddenProduct() { return forbiddenProduct; }
    public void setForbiddenProduct(boolean forbiddenProduct) { this.forbiddenProduct = forbiddenProduct; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
}
