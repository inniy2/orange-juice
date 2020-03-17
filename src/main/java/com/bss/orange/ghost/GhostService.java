package com.bss.orange.ghost;

import com.bss.orange.Ghost.APIResponse;
import com.bss.orange.Ghost.Empty;
import com.bss.orange.Ghost.definitionRequest;
import com.bss.orange.Ghost.diskRequest;
import com.bss.orange.Ghost.ghostRequest;
import com.bss.orange.ghostGrpc.ghostImplBase;

import io.grpc.stub.StreamObserver;

public class GhostService extends ghostImplBase{

	@Override
	public void diskcheck(diskRequest request, StreamObserver<APIResponse> responseObserver) {
		// Get request params
		String dir = request.getDir();
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Create messages
		response.setResponsecode(0).setResponsemessage("300G");
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
		// Create response build object
		APIResponse.Builder response = APIResponse.newBuilder();
		// Create messages
		response.setResponsecode(0).setResponsemessage("CREATE TABLE test_tbl1 (\n ...)");
		// Send message by building response
		responseObserver.onNext(response.build());
		// Close call
		responseObserver.onCompleted();
	}

	@Override
	public void cutover(Empty request, StreamObserver<APIResponse> responseObserver) {
		// TODO Auto-generated method stub
		super.cutover(request, responseObserver);
	}

	@Override
	public void dryrun(ghostRequest request, StreamObserver<APIResponse> responseObserver) {
		// TODO Auto-generated method stub
		super.dryrun(request, responseObserver);
	}

	@Override
	public void execute(ghostRequest request, StreamObserver<APIResponse> responseObserver) {
		// TODO Auto-generated method stub
		super.execute(request, responseObserver);
	}
	
	

}
