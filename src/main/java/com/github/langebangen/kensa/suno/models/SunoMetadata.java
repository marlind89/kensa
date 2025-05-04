package com.github.langebangen.kensa.suno.models;

public record SunoMetadata(
    String tags,
    String negative_tags,
    String prompt,
    String type,
    double duration,
    boolean refund_credits,
    boolean stream,
    boolean can_remix,
    boolean is_remix,
    int priority
) {}