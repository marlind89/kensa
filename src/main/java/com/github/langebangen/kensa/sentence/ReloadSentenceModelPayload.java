package com.github.langebangen.kensa.sentence;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReloadSentenceModelPayload
{
    @JsonProperty("messagesFilename")
    private String messagesFilename;

    public String getMessagesFilename()
    {
        return messagesFilename;
    }

    public void setMessagesFilename(String messagesFilename)
    {
        this.messagesFilename = messagesFilename;
    }
}
