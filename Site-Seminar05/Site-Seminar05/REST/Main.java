package com.REST;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

	public class Main 
	{
		static String senderLogin;
		static Client client;

		static String[] partsout(String[] array, int index) {
			String[] result = new String[array.length - index];
			for (int i = index; i < (array.length); i++) {
				result[i - index] = array[i];

			}
			return result;
		}
	
	
	protected static String[] receiveMessage() throws IOException 
	{
		try {
					Response response = client.target("http://localhost:8080/chat/server/" + senderLogin + "/messages").request(MediaType.APPLICATION_JSON_TYPE).get();

					if (response.getStatus() >= 300)
						throw new IOException(String.format("%s: Failed to retrieve the message list: %s", response.getStatus(),
								response.getEntity()));

					// no messages available.
					if (response.getStatus() == Status.NO_CONTENT.getStatusCode() || !response.hasEntity())
					return null;

					WrappedList msgs = response.readEntity(WrappedList.class);

					if (msgs == null || msgs.items == null || msgs.items.size() == 0)
					return null;

					// receiving the first message.
					String messageId = msgs.items.get(0);
					Message msg = client.target("http://localhost:8080/chat/server/" + senderLogin + "/messages/"+messageId).request(MediaType.APPLICATION_JSON_TYPE).get(Message.class);

					if (msg == null || msg.getSender() == null || msg.getSender().length() == 0)
						throw new IOException("Received message is corrupted: Message sender is not specified.");

					// deleting the message.
					response = client.target("http://localhost:8080/chat/server/" + senderLogin + "/messages/"+messageId).request(MediaType.APPLICATION_JSON_TYPE).delete();

					if (response.getStatus() >= 300)
						throw new IOException(String.format("%s: Failed to delete the message %s on the server: %s",
								response.getStatus(), messageId, response.getEntity()));

					return new String[] { msg.getSender(), msg.getMessage() };

				} 
	catch (IOException ex) 
	{
		throw ex;
	} 
	catch (Exception ex) 
	{
		throw new IOException("Failed to receive message", ex);
	}
			}

			protected static Object[] receiveFile() throws IOException {
				try {
					// receiving the list of files.
					Response response = client.target("http://localhost:8080/chat/server/"+senderLogin+"/files").request(MediaType.APPLICATION_JSON_TYPE).get();

					if (response.getStatus() >= 300)
						throw new IOException(String.format("%s: Failed to retrieve the file list: %s", response.getStatus(),response.getEntity()));

					// no files available.
					if (response.getStatus() == Status.NO_CONTENT.getStatusCode() || !response.hasEntity())
						return null;

					WrappedList files = response.readEntity(WrappedList.class);

					if (files == null || files.items == null || files.items.size() == 0)
						return null;

					// receiving the first file.
					String fileId = files.items.get(0);
					FileInfo file = client.target("http://localhost:8080/chat/server/"+senderLogin+"/files/"+fileId).request(MediaType.APPLICATION_JSON_TYPE).get(FileInfo.class);

					if (file == null || file.getSender() == null || file.getSender().length() == 0)
						throw new IOException("Received file is corrupted: File sender is not specified.");

					if (file.getFilename() == null || file.getFilename().length() == 0)
						throw new IOException("Received file is corrupted: File name is not specified.");

					// deleting the file.
					response = client.target("http://localhost:8080/chat/server/" + senderLogin + "/files/"+fileId).request(MediaType.APPLICATION_JSON_TYPE).delete();

					if (response.getStatus() >= 300)
						throw new IOException(String.format("%s: Failed to delete the file %s on the server: %s",
								response.getStatus(), fileId, response.getEntity()));

					// returning the file.
					return new Object[] { file.getSender(), file.getFilename(), file.getContentBytes() };

				} 
				catch (IOException ex) 
				{
					throw ex;
				} 
				catch (Exception ex) 
				{
					throw new IOException("Failed to receive the file", ex);
				}
			}

			private static class MyTimerTask extends TimerTask 
			{
				public void run() 
				{
					String[] printMessage;
					Object[] receivedFile;
					try 
					{
						printMessage = receiveMessage();
						receivedFile = receiveFile();
						if (printMessage != null)
								System.out.println("Message from" + printMessage[0] + ":" + printMessage[1]);
						
						if (receivedFile != null)
						{
							Path path = Paths.get("D:\\Documents\\Desktop", receivedFile[1].toString());
							Path content = Files.write(path, (byte[]) receivedFile[2], StandardOpenOption.CREATE);
							System.out.println("Incoming File:" + receivedFile[2] + "from" + receivedFile[0]);
						}
					} 
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				
			}

			@SuppressWarnings({ "rawtypes" })
			public static void main(String[] args) 
			{
				try 
				{
					client = ClientBuilder.newClient();
					String[] parts;
					InputStreamReader isr = new InputStreamReader(System.in);
					BufferedReader br = new BufferedReader(isr);
					String s = null;
					Timer timeToReceiveMsg = new Timer();
					
					boolean isClosed = false;
					boolean isTimerStarted = false;
					while (!isClosed) {

						s = br.readLine();
						parts = s.split(" ");
						switch (parts[0]) {
						case "ping":
							String response = client.target("http://localhost:8080/chat/server/ping").request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
							if (response != null)
								System.out.println("Ping successfull");
							break;
						
						case "echo":
							response = client.target("http://localhost:8080/chat/server/echo").request(MediaType.TEXT_PLAIN_TYPE).post(Entity.text(String.join(" ", partsout(parts, 1))), String.class);
							System.out.println(response);
							break;
						
						case "login":
							UserInfo userInfo = new UserInfo();
							userInfo.setLogin(parts[1]);
							userInfo.setPassword(parts[2]);
							Entity userInfoEntity = Entity.entity(userInfo, MediaType.APPLICATION_JSON_TYPE);
							
							Response responseOnLogin = client.target("http://localhost:8080/chat/server/user").request(MediaType.TEXT_PLAIN_TYPE).put(userInfoEntity);
							if (responseOnLogin.getStatus() == Status.CREATED.getStatusCode())
								System.out.println("New user registered");
							else
								System.out.println("Error code" + responseOnLogin.getStatus());
							
							client.register(HttpAuthenticationFeature.basic(userInfo.getLogin(), userInfo.getPassword()));
							senderLogin = userInfo.getLogin();
							if (userInfo != null && !isTimerStarted) isTimerStarted = true; 
							timeToReceiveMsg.schedule(new MyTimerTask(), 0, 1000);
							break;
						
						case "list":
							try {
								WrappedList users = client.target("http://localhost:8080/chat/server/users").request(MediaType.APPLICATION_JSON_TYPE).get(WrappedList.class);
								System.out.println("List of active users:" + users.items);
							} 
							catch (Exception ex) 
							{
								throw new IOException("Failed to retrieve the list of users", ex);
							}
							break;
						
						case "msg":
							try {
									Entity messageEntity = Entity.entity(String.join(" ", partsout(parts, 2)), MediaType.APPLICATION_JSON_TYPE);
									Response responseOnMessage = client.target("http://localhost:8080/chat/server/"+parts[1]+"/messages").request(MediaType.APPLICATION_JSON_TYPE).post(messageEntity);
									if (responseOnMessage.getStatus() >= 300)
										throw new IOException(String.format("%s: %s", responseOnMessage.getStatus(),responseOnMessage.getEntity()));
							} 
							catch (IOException ex) 
							{
								throw ex;
							} 
							catch (Exception ex) 
							{
								throw new IOException("Failed to send message", ex);
							}
							break;
						
						case "file":
							try {
									if (senderLogin != null) 
									{
										FileInfo fileInfo = new FileInfo(senderLogin, new File(parts[2]));
										Entity fileInfoEntity = Entity.entity(fileInfo, MediaType.APPLICATION_JSON_TYPE);
										if (fileInfo!=null){
										
											Response responseOnFile = client.target("http://localhost:8080/chat/server/"+parts[1]+"/files").request(MediaType.APPLICATION_JSON_TYPE).post(fileInfoEntity);
										if (responseOnFile.getStatus() >= 300)
											throw new IOException(String.format("%sFailed to send the file: %s",
													responseOnFile.getStatus(), responseOnFile.getEntity()));
										}
									}
							} 
							catch (IOException ex) 
							{
								throw ex;
							} 
							catch (Exception ex) 
							{
								throw new IOException("Failed to send the file.", ex);
							}

							break;
						
						case "exit": 	
									if (client != null) 
									{
										client.close();
										isClosed = true;
										isr.close();
										br.close();
										timeToReceiveMsg.cancel();
										timeToReceiveMsg.purge();
									}
							break;
						
						default:  System.out.println("Invalid command");
							break;
						}
					}

				} 
				catch (UnknownHostException e) 
				{
					System.out.println("Unknown host");
					System.exit(1);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}

			}

		}
	}


