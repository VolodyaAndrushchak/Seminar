package com.Sck;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Implementation 
{
	
	/*
	   DataInputStream and DataOutputStream allows you to record and read data primitive types.
	   
	   After you create a Socket object, you can take the input and output streams of the socket. 
	   Input stream will allow you to read from the socket, 
	   and gives the output stream to write to the socket.	   
	   
	 */
	
	public void ping(Socket socket) throws IOException 
	{
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
				
		dos.writeInt(1);		// how many bytes we pass;
		dos.writeByte(1);		// ID byte (records in this byte value of 1 corresponding to the ID ping command);
		
		byte array[] = new byte[dis.readInt()]; 
		dis.readFully(array);	// full reading from array
		
		if(array.length == 1 && array[0] == 2) 
			System.out.println("Ping success");
		else 
			System.out.println("Ping not success!!");
	}
	
	public void echo(Socket socket, String str) throws IOException 
	{
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		byte bytes_String[] = str.getBytes();
		dos.writeInt(bytes_String.length+1);
		dos.writeByte(3);
		dos.write(bytes_String);
		
		byte array[] = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length > 1) 
		{
			String str_received = new String(array, 0, array.length);
			System.out.println(str_received);
		}
		else 
			System.out.println("Error " + array[0]);
	}
	
	public boolean login(Socket socket, String name, String password) throws IOException, ClassNotFoundException 
	{
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		byte[] bytes = serialize(new String[]{name, password});
		dos.writeInt(bytes.length+1);
		dos.writeByte(5);
		dos.write(bytes);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length == 1 && (array[0]==6 || array[0]==7)) 
		{
			System.out.println(array[0] == 6 ? "new user, registration ok" : "login ok");
		}
		else 
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
		return(true); 
	}
	
	public void list(Socket socket) throws IOException, ClassNotFoundException 
	{
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		dos.writeInt(1);
		dos.writeByte(10);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length > 1) 
		{
			String[] obj = (String[])deserialize(array); 
			System.out.println("List of active users:"+Arrays.toString(obj));
		}
		else 
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
	}
	
	public void msg(Socket socket, String user, String message) throws IOException, ClassNotFoundException 
	{
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		byte[] bytes = serialize(new String[]{user, message});
		dos.writeInt(bytes.length+1);
		dos.writeByte(15);
		dos.write(bytes);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length == 1 && (array[0]==16)) 
		{
			 System.out.println("message sent");
		}
		else 
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
	}
	
	public void file(Socket socket, String user, String file) throws IOException, ClassNotFoundException 
	{
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		
		Path filePath = Paths.get(file); 
		byte[] content = Files.readAllBytes(filePath);
		Path filename = filePath.getFileName();
		
		byte[] bytes = serialize(new Object[]{user, filename.toString(), content});
		dos.writeInt(bytes.length+1);
		dos.writeByte(20);
		dos.write(bytes);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length == 1 && (array[0]==21)) 
		{
			System.out.println("file sent");
		}
		else 
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
	}
	
	public void receiveMsg(Socket socket_message) throws IOException, ClassNotFoundException 
	{
		DataOutputStream dos = new DataOutputStream(socket_message.getOutputStream());
		DataInputStream dis = new DataInputStream(socket_message.getInputStream());
		
		dos.writeInt(1);
		dos.writeByte(25);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		if(array.length > 1 && array[0] != 26) 
		{
			String[] obj = (String[])deserialize(array); 
			System.out.println("Incoming Message:"+Arrays.toString(obj));
		}
		else if(array[0] != 26)
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
	}
	
	@SuppressWarnings("unused")
	public void receiveFile(Socket socket_message) throws IOException, ClassNotFoundException 
	{
		DataInputStream dis = new DataInputStream(socket_message.getInputStream());
		DataOutputStream dos = new DataOutputStream(socket_message.getOutputStream());
		
		dos.writeInt(1);
		dos.writeByte(30);
		
		byte[] array = new byte[dis.readInt()]; 
		dis.readFully(array);
		
		if(array.length > 1 && array[0] != 31) 
		{
			Object[] obj = (Object[]) deserialize(array);
			Path path = Paths.get("D:\\Desktop", (String)obj[1]);
			byte[] filecontent = ((byte[])obj[2]);
			Path content = Files.write(path, filecontent, StandardOpenOption.CREATE);
			System.out.println("Incoming File:"+Arrays.toString(obj));
		}
		else if(array[0] != 31)
			System.out.println("Error "+(array.length == 1 ? array[0] : Arrays.toString(array)));
		else
			System.out.println("ERROR!!");
	}
	
	
	
	
	//==============
	
	static byte[] serialize(Object obj) throws IOException 
	{
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(out);
	    oos.writeObject(obj);
	    return out.toByteArray();
	}
	
	static Object deserialize(byte[] data) throws IOException, ClassNotFoundException 
	{
	    ByteArrayInputStream bin = new ByteArrayInputStream(data);
	    ObjectInputStream ois = new ObjectInputStream(bin);
	    return ois.readObject();
	}
	
	public void run()
	{
		try 
		{
			Socket socket_message = null;
			receiveMsg(socket_message);
			receiveFile(socket_message);
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println("ClassNotFoundException!");
		} 
		catch (Exception e) 
		{
			System.out.println("Exception!");
		}
	}
	
	
}


