package com.easeinfo;

public class Config {
	
	public static boolean SYN_CONFIG = true; //�Ƿ���MonitorService��ͬ���ܿ�
	public static boolean SMS_SYN = true; //����ͬ��
	public static boolean PHONE_SYN = true; //�绰ͬ��
	public static boolean ADDRESS_SYN = true;//ͨѶ¼ͬ��
	public static long TIME_DELAY = 500; //ͬ�������ӳ�
	
	public static int SMS_LATENCY = 2;  //�������ݿ��ѯ���뵱ǰ��ʱ��
	public static int PHONE_LATENCY = 15; //�������ݿ��ѯ���뵱ǰʱ��
	
	public static String DB_NAME = "easeinfo_database";
	
	public static String accountsURL = "http://ef-server.jlxy.cz/accounts";
	public static String sessionsURL = "http://ef-server.jlxy.cz/sessions";
	public static String messagesURL = "http://ef-server.jlxy.cz/messages";
	public static String callsURL = "http://ef-server.jlxy.cz/calls";
	public static String contactsURL = "http://ef-server.jlxy.cz/contacts";
	
}
