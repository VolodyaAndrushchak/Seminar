package com.SOAP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import com.lpi.server.soap.ArgumentFault;
import com.lpi.server.soap.ChatServer;
import com.lpi.server.soap.IChatServer;
import com.lpi.server.soap.IServer.FileInfo;
import com.lpi.server.soap.IServer.Message;
import com.lpi.server.soap.ServerFault;

public class Main 
{

	public static void main(String[] args) 
	{
		
		
		System.out.println("Welcome to simple java console interprate."
		 		 		 + "Select the appropriate command:\n" 
		 		 		 + "Press 'ping' to selest the command: ping" 
		 		 		 + "\nPress 'echo' to selest the command: echo" 
				 		 + "\nPress 'login' to selest the command: login: <login name>, <login password>" 
				 		 + "\nPress 'list' to selest the command: list"
						 + "\nPress 'msg' to selest the command: msg: <name of the msg>, <text of the msg>"
						 + "\nPress 'file' to selest the command: file: <username>, <filename>"
						 + "\nPress 'exit' to selest the command: exit");

		
		//============
		

		System.out.println();
		System.out.println("\nEnter the command: ");

		//============
		
		try 
		{
			
			String[] parts;
			boolean isClosed = false;
			boolean isTimerStarted = false;
			
			ChatServer serverWrapper = new ChatServer(new URL("http://lv.rst.uk.to:153/chat?wsdl"));
			serverProxy = serverWrapper.getChatServerProxy();
			
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			String s = null;
			
			Timer timeToReceiveMsg = new Timer();
			while (!isClosed) 
			{

				s = br.readLine();
				parts = s.split(" ");
				switch (parts[0]) 
				{
					case "ping": serverProxy.ping();
						break;
					
					case "echo": System.out.println(serverProxy.echo(String.join(" ", partsout(parts, 1))));
						break;
					
					case "login": sessionID = serverProxy.login(parts[1], parts[2]);
					System.out.println(sessionID);
					if (sessionID != null && !isTimerStarted)
						isTimerStarted = true;
					timeToReceiveMsg.schedule(new MyTimerTask(), 0, 1000);
						break;
					
					case "list": System.out.println("List of active users:" + serverProxy.listUsers(sessionID));
						break;
					
					case "msg": serverProxy.sendMessage(sessionID, createMessage(parts[1], String.join(" ", partsout(parts, 2))));
						break;
					
					case "file": serverProxy.sendFile(sessionID, createFileInfo(parts[1], new File(parts[2])));
						break;
					
					case "exit": serverProxy.exit(sessionID);
					isClosed = true;
						break;
					
					default: System.out.println("Invalid command");
						break;
				}
			}

		} 
		catch (UnknownHostException e) 
		{
			System.out.println("Unknown host: 0.0.0.0");
			System.exit(1);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

	}
		
		
	
	
	//======> Static values:
	
	static String sessionID = null;
	static IChatServer serverProxy;
	
	//======> Static methods:
	
	static String[] partsout(String[] array, int index) 
	{
		String[] result = new String[array.length - index];
		for (int i = index; i < (array.length); i++) 
		{
			result[i - index] = array[i];
		}
		return result;
	}
	
	private static FileInfo createFileInfo(String receiver, File file) throws IOException
	{
		 String filename = file.getName();
		 byte[] content =  Files.readAllBytes(file.toPath());

		FileInfo fileInfo = new FileInfo();
		fileInfo.setReceiver(receiver);
		fileInfo.setFilename(filename);
		fileInfo.setFileContent(content);

		return fileInfo;
	}
	
	private static Message createMessage(String receiver, String message)
	{
		Message newMessage = new Message();
		newMessage.setReceiver(receiver);
		newMessage.setMessage(message);
		return newMessage;
		
	}
	
	//======> Static classes:
	
	private static class MyTimerTask extends TimerTask {
		public void run() 
		{
			try 
			{
				Message ReceivedMessage = serverProxy.receiveMessage(sessionID);
				if (ReceivedMessage != null)
					System.out.println("Incoming Message" + ReceivedMessage.getMessage() + "from" + ReceivedMessage.getSender());
				
				FileInfo ReceivedFile = serverProxy.receiveFile(sessionID);
				
				if (ReceivedFile != null) 
				{
					Path path = Paths.get("D:\\Desktop", ReceivedFile.getFilename());
					Path content = Files.write(path, ReceivedFile.getFileContent(), StandardOpenOption.CREATE);
					System.out.println("Incoming File:" + ReceivedFile.getFilename() + "from" + ReceivedFile.getSender());
				}
			} 
			catch (RemoteException ex) 
			{
				System.out.println("Remote Ecxeption!!");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (ArgumentFault e) 
			{
				e.printStackTrace();
			} 
			catch (ServerFault e) 
			{
				e.printStackTrace();
				
			}
		}
	}

}
