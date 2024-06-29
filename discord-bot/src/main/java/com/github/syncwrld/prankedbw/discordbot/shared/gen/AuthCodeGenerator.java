package com.github.syncwrld.prankedbw.discordbot.shared.gen;

import java.util.Random;

public class AuthCodeGenerator {
	
	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int CODE_LEN = 10;
	private static final Random RANDOM = new Random();
	
	public static String create() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < CODE_LEN; i++) {
			sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
		}
		
		return sb.toString();
	}
	
}
