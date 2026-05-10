package com.demo.utils;

import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@NoArgsConstructor
public class CodeGenerator {
    public static String generateCode(Long counter){
        return convertLongToBase62(counter);
    }

    public static String hashValue(String value) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            // convert bytes to string
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    //------------------ Helper methods ---------------------------
    private static String convertLongToBase62(Long num){
        String base62Characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();

        while(num > 0){
            result.append(base62Characters.charAt((int) (num % 62)));
            num /= 62L;
        }

        return result.reverse().toString();
    }
}
