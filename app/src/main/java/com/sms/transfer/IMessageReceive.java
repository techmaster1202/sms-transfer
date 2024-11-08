package com.sms.transfer;

public interface IMessageReceive {
    void onSMSReceive(String from, String to, String content);
}
