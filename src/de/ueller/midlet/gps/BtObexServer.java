package de.ueller.midlet.gps;
/*
 * GpsMid - Copyright (c) 2008 Kai Krueger apm at users dot sourceforge dot net 
 * See Copying
 */

//#if polish.api.obex
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;
//#endif

public class BtObexServer
//#if polish.api.obex
	extends ServerRequestHandler implements Runnable, UploadListener
//#endif
	{
//#if polish.api.obex
	private final static Logger logger=Logger.getInstance(BtObexServer.class,Logger.DEBUG);
	
	private UploadListener feedbackListener;
	private Thread processorThread;
	private int response;
	private float maxDistance;
	
	
	public BtObexServer(UploadListener feedback, float maxDistance) {
		feedbackListener = feedback;
		this.maxDistance = maxDistance;
		processorThread = new Thread(this);
		processorThread.start();
	}
	
	public int onConnect(HeaderSet request, HeaderSet reply) {
		//At the moment wie accept anything.
		//Should we be asking the user if this is OK?
		return ResponseCodes.OBEX_HTTP_OK;
	}
	
	public synchronized int onPut (Operation op) {

		try {
		   java.io.InputStream is = op.openInputStream();
		   
		   String fileName = op.getReceivedHeaders().getHeader(HeaderSet.NAME).toString();
		   
		   if (!fileName.endsWith(".gpx")) {
			   logger.error("Can only except GPX files. File not valid: " + fileName);
			   return ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE;
		   }
		   					
		   Trace.getInstance().gpx.receiveGpx(is, this, maxDistance);
		   
		   wait();
		} catch (Exception e) {
			logger.error("Receiving of file went wrong: " + e.getMessage());
			e.printStackTrace(); 
		}
		
		feedbackListener.completedUpload(true, "");		
		return response;
		}



	public void run() {
		try {
			  UUID uuid = new UUID("1105", true); //This is the Obex Push Profile UUID
			  String url = "btgoep://localhost:" + uuid 
				  + ";name=GPXreceive;authenticate=false;master=false;encrypt=false";

			  SessionNotifier sn = (SessionNotifier)Connector.open(url);
			  //Wait for an incomming connection
			  sn.acceptAndOpen(this);
			  
			} catch (Exception ex){
			}
		
	}
	
	public void startProgress(String title) {
		// Not supported/used at the moment.
	}

	public void setProgress(String message) {
		// Not supported/used at the moment.
		
	}

	public void updateProgress(String message)
	{
		// Not supported/used at the moment.

	}
	
	public void updateProgressValue(int value){
		//TO-DO
	}

	public synchronized void completedUpload(boolean success, String message) {
		if (success) {
			response = ResponseCodes.OBEX_HTTP_OK;
		} else {
			response = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
		}
		notify();
		
	}

	public synchronized void uploadAborted() {
		response = ResponseCodes.OBEX_HTTP_RESET;
		notify();
	}
//#endif

}
