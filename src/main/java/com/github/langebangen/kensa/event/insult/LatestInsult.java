package com.github.langebangen.kensa.event.insult;

class LatestInsult
{
    private static int lastInsultId = -1;

    static void setLastInsultId(int id)
    {
        lastInsultId = id;
    }

    static int getLastInsultId()
    {
        return lastInsultId;
    }

}
