package com.github.langebangen.kensa.suno;

import com.github.langebangen.kensa.suno.models.SunoClip;
import com.github.langebangen.kensa.suno.models.SunoClipWrapper;
import com.google.gson.*;

import java.lang.reflect.Type;

public class SunoClipWrapperDeserializer implements JsonDeserializer<SunoClipWrapper>
{
    private final Gson gson;

    public SunoClipWrapperDeserializer()
    {
        gson = new Gson();
    }

    @Override
    public SunoClipWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        var attributes = json.getAsJsonArray().get(3).getAsJsonObject();
        var clip = gson.fromJson(attributes.getAsJsonObject("clip"), SunoClip.class);
        return new SunoClipWrapper(clip);
    }
}