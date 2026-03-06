package com.vestshop.Chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRule {
    private String intent;
    private List<String> keywords;
    private String response;
    private int priority;
}