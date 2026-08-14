package com.kite.mnemoai.network.deepseek.model;

import java.util.List;

public class DeepseekResponseBody {
    private String id;
    private List<Choice> choices;
    public static class Choice{
        private String finish_reason;
        private Message message;

        public Choice(String finish_reason, Message message) {
            this.finish_reason = finish_reason;
            this.message = message;
        }

        public String getFinish_reason() {
            return finish_reason;
        }

        public Message getMessage() {
            return message;
        }
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public String getId() {
        return id;
    }


}
