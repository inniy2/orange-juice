package com.bss.orange.ghost;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bss.orange.Ghost.APIResponse;
import com.bss.orange.Ghost.Empty;
import com.bss.orange.Ghost.definitionRequest;
import com.bss.orange.Ghost.diskRequest;
import com.bss.orange.Ghost.ghostRequest;
import com.bss.orange.Ghost.interactiveRequest;
import com.bss.orange.ghostGrpc.ghostImplBase;
import io.grpc.stub.StreamObserver;

public class GhostService extends ghostImplBase{

	private static Singleton singleton = null;
	public GhostService() {
		singleton = Singleton.getInstance(); 
	}
	
	@Override
	public void diskcheck(diskRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String dir = request.getDir();
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		ProcessBuilder processBuilder = new ProcessBuilder();
		String[] cmd = {"df",dir}; 
		processBuilder.command(cmd);
		List<String> sucessMesg = new ArrayList<String>();
		List<String> errorMesg = new ArrayList<String>();
		int sucess = 0;
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sucessMesg.add(line);
				System.out.println(line);
			}
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorLine = null;
			while ((errorLine = error.readLine()) != null) {
				sucess = 1;
				errorMesg.add(errorLine);
				System.out.println(errorLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
			response.setResponsecode(1).setResponsemessage(e.toString());
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		}
		// Create messages
		if(sucess == 0) {
			response.setResponsecode(0).setResponsemessage(sucessMesg.toString());
		}else {
			response.setResponsecode(1).setResponsemessage(errorMesg.toString());
		}
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}

	@Override
	public void checkdefinition(definitionRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String schemaName = request.getSchemaname();
		String tableName  = request.getTablename();
		String user     = singleton.prop.getProperty("db.user");
		String password = singleton.prop.getProperty("db.password");
		String port     = singleton.prop.getProperty("db.port");
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		Connection connect = null;
		Statement stmt = null;
	    ResultSet resultSet = null;
	    String sucessMesg = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager.getConnection(  
					"jdbc:mysql://localhost:"+port+"/"+schemaName,user,password);
			stmt = connect.createStatement();  
			resultSet = stmt.executeQuery("SHOW CREATE TABLE "+tableName);
			while(resultSet.next()) {
				sucessMesg = resultSet.getString(2);
			}
		}catch (Exception e) {
			e.printStackTrace();
			response.setResponsecode(1).setResponsemessage(e.toString());
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		}finally {
			try {
				resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
				response.setResponsecode(1).setResponsemessage(e.toString());
				responseObserver.onNext(response.build());
				responseObserver.onCompleted();
			}
			try {
				connect.close();
			} catch (SQLException e) {
				e.printStackTrace();
				response.setResponsecode(1).setResponsemessage(e.toString());
				responseObserver.onNext(response.build());
				responseObserver.onCompleted();
			}
		}
		// Create messages
		response.setResponsecode(0).setResponsemessage(sucessMesg.toString());
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}

	@Override
	public void cutover(Empty request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		File file = new File("/tmp/ghost.postpone.flag"); 
		if(file.delete()) {
			response.setResponsecode(0).setResponsemessage("sucess");
		}else {
			response.setResponsecode(1).setResponsemessage("failed");
		}
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}

	@Override
	public void dryrun(ghostRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String schemaName = request.getSchemaname();
		String tableName  = request.getTablename();
		String statement  = request.getStatement();
		String ghost      = singleton.prop.getProperty("ghost.path");
		String mycnf      = singleton.prop.getProperty("db.cnf");
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		String[] ghostRunComand = new String[] {
				ghost,                                       // 0
				"--max-load=Threads_running=50",             // 1
				"--critical-load=Threads_running=1500",      // 2
				"--chunk-size=500",                          // 3
				"--max-lag-millis=1500",                     // 4
				"--conf="+mycnf,                             // 5
				"--host=127.0.0.1",                          // 6
				"--allow-on-master",                         // 7  *
				"--database="+schemaName,                    // 8
				"--table="+tableName,                        // 9
				"--alter="+statement,                        // 10
				"--switch-to-rbr",                           // 11
				"--cut-over=default",                        // 12
				"--exact-rowcount",                          // 13
				"--concurrent-rowcount",                     // 14
				"--default-retries=120",                     // 15
				"--timestamp-old-table",                     // 16
				"--postpone-cut-over-flag-file=/tmp/ghost.postpone.flag", //17
				"--verbose"                                  // 18
		};
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(ghostRunComand);
		List<String> sucessMesg = new ArrayList<String>();
		List<String> errorMesg = new ArrayList<String>();
		int sucess = 0;
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sucessMesg.add(line);
				System.out.println(line);
			}
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorLine = null;
			while ((errorLine = error.readLine()) != null) {
				sucess = 1;
				errorMesg.add(errorLine);
				System.out.println(errorLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
			response.setResponsecode(1).setResponsemessage(e.toString());
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		}
		// Create messages
		if(sucess == 0) {
			response.setResponsecode(0).setResponsemessage(sucessMesg.toString());
		}else {
			response.setResponsecode(1).setResponsemessage(errorMesg.toString());
		}
		
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}

	@Override
	public void execute(ghostRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String schemaName = request.getSchemaname();
		String tableName  = request.getTablename();
		String statement  = request.getStatement();
		String ghost      = singleton.prop.getProperty("ghost.path");
		String mycnf      = singleton.prop.getProperty("db.cnf");
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		String[] ghostRunComand = new String[] {
				ghost,                                       // 0
				"--max-load=Threads_running=50",             // 1
				"--critical-load=Threads_running=1500",      // 2
				"--chunk-size=500",                          // 3
				"--max-lag-millis=1500",                     // 4
				"--conf="+mycnf,                             // 5
				"--host=127.0.0.1",                          // 6
				"--allow-on-master",                         // 7  *
				"--database="+schemaName,                    // 8
				"--table="+tableName,                        // 9
				"--alter="+statement,                        // 10
				"--switch-to-rbr",                           // 11
				"--cut-over=default",                        // 12
				"--exact-rowcount",                          // 13
				"--concurrent-rowcount",                     // 14
				"--default-retries=120",                     // 15
				"--timestamp-old-table",                     // 16
				"--postpone-cut-over-flag-file=/tmp/ghost.postpone.flag", //18
				"--verbose",                                 // 19
				"--execute"                                  // 20
		};
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(ghostRunComand);
		List<String> sucessMesg = new ArrayList<String>();
		List<String> errorMesg = new ArrayList<String>();
		int sucess = 0;
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sucessMesg.add(line);
				System.out.println(line);
			}
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorLine = null;
			while ((errorLine = error.readLine()) != null) {
				sucess = 1;
				errorMesg.add(errorLine);
				System.out.println(errorLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
			response.setResponsecode(1).setResponsemessage(e.toString());
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		}
		// Create messages
		if(sucess == 0) {
			response.setResponsecode(0).setResponsemessage(sucessMesg.toString());
		}else {
			response.setResponsecode(1).setResponsemessage(errorMesg.toString());
		}
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}
	
	public void interactive(interactiveRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String schemaName   = request.getSchemaname();
		String tableName    = request.getTablename();
		String command      = request.getGhostcommand();
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Logic
		String[] ghostRunComand = new String[] {
			"/bin/bash","-c",
			"echo "+ command+ " | nc -U /tmp/gh-ost."+schemaName+"."+tableName+".sock"
		};
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(ghostRunComand);
		List<String> sucessMesg = new ArrayList<String>();
		List<String> errorMesg = new ArrayList<String>();
		int sucess = 0;
		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sucessMesg.add(line);
				System.out.println(line);
			}
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorLine = null;
			while ((errorLine = error.readLine()) != null) {
				sucess = 1;
				errorMesg.add(errorLine);
				System.out.println(errorLine);
			}
		}catch (IOException e) {
			e.printStackTrace();
			response.setResponsecode(1).setResponsemessage(e.toString());
			responseObserver.onNext(response.build());
			responseObserver.onCompleted();
		}
		// Create messages
		if(sucess == 0) {
			response.setResponsecode(0).setResponsemessage(sucessMesg.toString());
		}else {
			response.setResponsecode(1).setResponsemessage(errorMesg.toString());
		}
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
		
	}

}
