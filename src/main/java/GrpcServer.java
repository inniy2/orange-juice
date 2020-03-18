import java.io.IOException;

import com.bss.orange.ghost.GhostService;
import com.bss.orange.ghost.Singleton;

import io.grpc.Server;
import io.grpc.ServerBuilder;


public class GrpcServer {

	public static void main(String[] args) throws IOException, InterruptedException {
		Singleton singleton = Singleton.getInstance();
		int port = Integer.parseInt(singleton.prop.getProperty("service.port"));
		
		Server server = ServerBuilder.
				forPort(port).addService(new GhostService()).build();
		
		server.start();
		
		System.out.println("Server started at " + server.getPort());
		
		server.awaitTermination();

	}
}
