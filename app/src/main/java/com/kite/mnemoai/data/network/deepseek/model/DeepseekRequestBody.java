package com.kite.mnemoai.data.network.deepseek.model;

import com.kite.mnemoai.data.network.PromptConstants;

import java.util.ArrayList;
import java.util.List;

public class DeepseekRequestBody {

    private String model;
    private List<Message> messages = new ArrayList<>();

    private Thinking thinking;
    private ResponseFormat responseFormat = new ResponseFormat();

    private static Message systemMessage = new Message(PromptConstants.SYS_CONTENT, PromptConstants.SYS_ROLE);

    public DeepseekRequestBody(String word, boolean isEnableThinking) {
        this.messages.add(systemMessage);
        this.messages.add(new Message(word, PromptConstants.USER_ROLE));
        this.model = PromptConstants.DEEPSEEK_FLASH_MODEL;
        this.thinking = new Thinking(isEnableThinking);
    }

    static class Thinking{
        private String type;

        public Thinking(boolean enable) {
            this.type = enable ? "enabled" : "disabled";
        }
    }

    static class ResponseFormat {

        private String type;

        public ResponseFormat() {
            this.type = "json_object";
        }
    }
}
