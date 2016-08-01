package com.github.langebangen.kensa;

/**
 * Enum containing the different sources a track can originate from.
 *
 * @author langen
 */
public enum TrackSource
{
	/* Local file on filesystem */
	FILE,
	/* An URL pointing to an audio source */
	URL,
	/* A youtube link */
	YOUTUBE;
}