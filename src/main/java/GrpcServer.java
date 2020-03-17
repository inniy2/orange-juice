import java.io.IOException;

import com.bss.orange.ghost.GhostService;

import io.grpc.Server;
import io.grpc.ServerBuilder;


public class GrpcServer {

	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = ServerBuilder.
				forPort(9090).addService(new GhostService()).build();
		
		server.start();
		
		System.out.println("Server started at " + server.getPort());
		
		server.awaitTermination();

	}

}
