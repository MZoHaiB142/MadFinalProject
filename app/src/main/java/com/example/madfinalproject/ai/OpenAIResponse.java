package com.example.madfinalproject.ai;

import java.util.List;

final class OpenAIResponse {
    List<Choice> choices;
    static final class Choice { Message message; }
    static final class Message { String content; }
    String content(){return choices==null||choices.isEmpty()||choices.get(0).message==null?"":choices.get(0).message.content;}
}
