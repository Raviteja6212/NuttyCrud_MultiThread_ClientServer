
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class NC_MultiThread_Server {
	public static void main(String args[]) {
			System.out.println("Server side started running. Waiting for the client request");
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(55102);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while(true) {
				Socket socketForServer=null;
				try {
					socketForServer = ss.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				System.out.println("\nCLIENT REQUEST RECEIVED AND ACCEPTED");
				operations op = new operations(socketForServer);
				op.start();
			}
	}
}

class operations extends Thread {
	  Socket socketForServer;
	  PrintStream responseToClient = null;
	  operations(Socket inSocket) {
		  socketForServer = inSocket;
	  }
	  
	  public void run() {
		  try {
			  Scanner sc = new Scanner(socketForServer.getInputStream());
			  String text = sc.nextLine();
			  String[] details = text.split(",");
			  
			  while(details[1].equals("0") || details[1].equals("1") || details[1].equals("2") || details[1].equals("3") || details[1].equals("4") || details[1].equals("id_check")) {	
				  if(details[1].equals("0")) {
					  user_check uc = new user_check(details,socketForServer);
					  uc.start();
				  }
				  else if(details[1].equals("1")) {
					  //System.out.println("Now you can do create");
					  Create_Thread ct = new Create_Thread(socketForServer,text);
					  ct.start();
				  }
				  else if(details[1].equals("2")) {
					  //System.out.println("Now you can do read");
					  Read_Thread rt = new Read_Thread(socketForServer,text);
					  rt.start();
				  }
				  else if(details[1].equals("3")) {
					  //System.out.println("Now you can do update");
					  Update_Thread ut = new Update_Thread(socketForServer,text);
					  ut.start();
				  }
				  else if(details[1].equals("4")) {
					  //System.out.println("Now you can do delete");
					  Delete_Thread dt = new Delete_Thread(socketForServer,text);
					  dt.start();
				  }
				  else if(details[1].equals("id_check")) {
						 id_check idc = new id_check(details[2],socketForServer);
						 idc.start();
				  }
				  
				  
				  if(sc.hasNextLine()) {
					  text=sc.nextLine();
				  }
				  else {
					  System.out.println("---- " + details[0]+" TERMINATED ----"); break;
				  }
				  System.out.println("\nINPUT RECEIVED FROM - "+text.split(",")[0]);
				  details=text.split(",");
			  }	
		  } 
		  catch (IOException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }		  
	  }
}

class id_check extends Thread{
	String id="";boolean bad=true;
	Socket socketForServer;
	PrintStream responseToClient = null;
	id_check(String d,Socket s){
		id = d;
		socketForServer=s;
	}
	public void run() {
		String line="";
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader("customers.csv"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while ((line = br.readLine()) != null)    
			  {  
				if(line.split(",")[0].equals(id)) {
					bad=false;
					responseToClient = new PrintStream(socketForServer.getOutputStream());
					responseToClient.println("True");
				}
			  }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bad) {
			try {
				responseToClient = new PrintStream(socketForServer.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			responseToClient.println("False");
		}
		
	}
}
class user_check extends Thread{
	String[] details;
	Socket socketForServer;
	PrintStream responseToClient = null;
	user_check(String[] d,Socket s){
		details = d;
		socketForServer=s;
	}
	public void run() {
		String line="";boolean newrecord = true;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("users.csv"));
			while ((line = br.readLine()) != null)    
			{  
				if(line.split(",")[0].equals(details[2]) && line.split(",")[1].equals(details[3]) && line.split(",")[2].equals(details[4])) {
					newrecord=false; 
					responseToClient = new PrintStream(socketForServer.getOutputStream());
					responseToClient.println("True");
					System.out.println("SERVER SENT BACK THE USER CHECK TO "+details[0]);
				}
			}
			if(newrecord) {
				System.out.println(details[0]+" - user doesn't exist.");
				responseToClient = new PrintStream(socketForServer.getOutputStream());
				responseToClient.println("False");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
}

class Create_Thread extends Thread { 
	Socket serverClient;
    String record;
	Create_Thread(Socket inSocket,String r){
		serverClient = inSocket;
    	record = r;
	}
	  
	public void run() {
    	try {
			FileWriter writer = new FileWriter("customers.csv",true);
			writer.append(record.substring(11)+"\n");
			writer.flush();
			writer.close();			
			System.out.println(record.split(",")[0]+" created a record.");
		}
		catch(Exception e) {
			System.out.println("Can't write the record.");
		}
   }
}

class Read_Thread extends Thread {
	Socket socketForServer;
	String record;
	PrintStream responseToClient = null;
	Read_Thread(Socket s,String r){
		socketForServer = s;
		record = r;
	}
	
	public void run() {
		try {
	         File file = new File("customers.csv");
	         FileReader fr = new FileReader(file);
	         BufferedReader br = new BufferedReader(fr);
	         String line = "";
	         String[] tempArr;
;	         while((line = br.readLine()) != null) {
	            if(line.split(",")[0].equals(record.split(",")[2]))
	            {
	            	responseToClient = new PrintStream(socketForServer.getOutputStream());
			    	responseToClient.println(line);
			    	System.out.println(record.split(",")[0]+" read a record.");
	            }
	         }
	     }
		 catch(IOException ioe) {
	         ioe.printStackTrace();
	     }
	}
}

class Update_Thread extends Thread {
	Socket socketForServer;
	String record;
	PrintStream responseToClient = null;
	Update_Thread(Socket s,String r){
		socketForServer = s;
		record = r;
	}
	
	public void run() {
		try {
	         File file = new File("customers.csv");
	         FileReader fr = new FileReader(file);
	         BufferedReader br = new BufferedReader(fr);
	         String line = "",updated_line="";
	         String[] tempArr;
	         List<String> storedata = new ArrayList<String>();
	        		 
	         while((line = br.readLine()) != null) {
	            if(line.split(",")[0].equals(record.split(",")[2]))
	            {
	            	if(record.split(",")[3].equals("name")) {
	            		updated_line =  line.split(",")[0] + "," + record.split(",")[4] + "," + line.split(",")[2] + "," + line.split(",")[3] ;
	            		storedata.add(updated_line);
	            	
	            	}
	            	else if(record.split(",")[2].equals("email")) {
	            		updated_line =  line.split(",")[0] + "," +  line.split(",")[1] + "," + record.split(",")[4] + "," + line.split(",")[3] ;
	            		storedata.add(updated_line);
	            		
	            	}
	            	else if(record.split(",")[2].equals("cp")) {
	            		updated_line =  line.split(",")[0] + "," +  line.split(",")[1] + "," + line.split(",")[2] + "," + record.split(",")[4] ;
	            		storedata.add(updated_line);
	            
	            	}
	            	else {
	            		System.out.println(record.split(",")[0]+" given update id doesn't match with any. please recheck");
	            	}
	            }
	            else {
	            	storedata.add(line);
	            }
	         }
	         br.close();fr.close();
	         
	         try {
	 			FileWriter writer = new FileWriter("customers.csv");
	 			for(String i:storedata) {
		        	 writer.append(i+"\n");
		        	 writer.flush();
		         }	 			
	 			 writer.close();
	 			System.out.println(record.split(",")[0]+" updated a record.");
	 		}
	 		catch(Exception e) {
	 			System.out.println("Can't write the record.");
	 		}
	     }
		 catch(IOException ioe) {
	         ioe.printStackTrace();
	     }
	}
}

class Delete_Thread extends Thread {
	Socket socketForServer;
	String record;
	PrintStream responseToClient = null;
	Delete_Thread(Socket s,String r){
		socketForServer = s;
		record = r;
	}
	
	public void run() {
		try {
	         File file = new File("customers.csv");
	         FileReader fr = new FileReader(file);
	         BufferedReader br = new BufferedReader(fr);
	         String line = "",updated_line="";
	         String[] tempArr;
	         List<String> storedata = new ArrayList<String>();
	        		 
	         while((line = br.readLine()) != null) {
	            if(line.split(",")[0].equals(record.split(",")[2]))
	            {
	            	continue;
	            }
	            else {
	            	storedata.add(line);
	            }
	         }
	         br.close();fr.close();
	         
	         try {
	 			FileWriter writer = new FileWriter("customers.csv");
	 			for(String i:storedata) {
		        	 writer.append(i+"\n");
		        	 writer.flush();
		         }	 			
	 			 writer.close();
	 			System.out.println(record.split(",")[0]+" deleted a record.");
	 		}
	 		catch(Exception e) {
	 			System.out.println("Can't write the record.");
	 		}
	     }
		 catch(IOException ioe) {
	         ioe.printStackTrace();
	     }
	}
}
	




