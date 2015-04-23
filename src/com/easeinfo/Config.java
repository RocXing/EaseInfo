package com.easeinfo;

public class Config {
	
	public static boolean SYN_CONFIG = true; //是否开启即时同步
	public static boolean REMOTE_CONFIG = true; //web端远程同步
	public static boolean SMS_SYN = true; //短信同步
	public static boolean PHONE_SYN = true; //电话同步
	public static boolean ADDRESS_SYN = true;//通讯录同步
	public static long TIME_DELAY = 500; //同步周期延迟
	
	public static int SMS_LATENCY = 2;  //短信数据库查询距离当前的时间
	public static int PHONE_LATENCY = 15; //来电数据库查询距离当前时间
	public static int UPLOAD_LATENCY = 3 * 24 * 60 * 60; //MQTT上传查询距离当前时间
	
	public static String DB_NAME = "easeinfo_database";
	
	public static String accountsURL = "http://ef-server.jlxy.cz/accounts";
	public static String sessionsURL = "http://ef-server.jlxy.cz/sessions";
	public static String messagesURL = "http://ef-server.jlxy.cz/messages";
	public static String callsURL = "http://ef-server.jlxy.cz/calls";
	public static String contactsURL = "http://ef-server.jlxy.cz/contacts";
	public static String webuploadURL = "http://ef-server.jlxy.cz/webupload";
	
}
