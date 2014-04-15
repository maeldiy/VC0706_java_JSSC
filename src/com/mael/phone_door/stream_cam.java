

package com.mael.phone_door;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.apache.commons.codec.binary.Base64;

//import gnu.io.SerialPort;


public class stream_cam {
static InputStream zob;
static int frameptr  = 0;
static int jpglen = 0;

public static void main(String args[]) {
	
/*	@SuppressWarnings("unused")
	//String port = "COM3";  
	*/
	String port = "COM3"; //= "56163"  
	if (args.length > 0) {if (args[0].contains("help")) {System.out.println("1st arg = port, 2nd arg whatever will save to /IMAGE.jpg");}} 
	if (args.length > 0) {	port = args[0];} 	else {port ="COM3";}
	 
/*	String[] serialPortNames = SerialPortList.getPortNames();
	for (String name : serialPortNames) {           
	    System.out.println("one more : " + name);
	}
*/	    
	SerialPort camera = new SerialPort(port);
	try {	
		camera.openPort();
		camera.setParams(SerialPort.BAUDRATE_115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	} catch (SerialPortException e) {
		System.out.println("1st connection @115200 error " + e);
	}
	
    if (camera.isOpened() == true) { 
  //  	System.out.println("init io 115200");
   //     System.out.println("listening ");
    }
    else {System.out.println("port not available");}
  //  reset();// to put when app is killed
    sleep(50); // 20 on a PC
    boolean conn_115200 = getVersion(camera);  
 //   System.out.println("get version at 115200 bds result " + conn_115200 );
             
    if (!conn_115200) {
    	try {
			camera.closePort();
		} catch (SerialPortException e) {
			 System.out.println("port closing error " + e);
		}
    	  System.out.println("disconnecting 115200");
      sleep(50); // 20 on a PC
      try {
		camera.openPort(); 
		camera.setParams(SerialPort.BAUDRATE_38400,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
	} catch (SerialPortException e) {
		 System.out.println("opening 38400 error " + e);
	}
     
  	
  	//System.out.println("connected @38400" + camera.isOpened());
        if (camera.isOpened() == true)
        {// System.out.println("connected @ 38400");
         // 	  camera.initListener();
          //	  System.out.println("listening but listener disabled ;-)");
            }
       
      sleep(50); // 20 on a PC
      boolean conn_38400 = getVersion(camera); 
      sleep(20);  // 10 on a PC
      
      ChangeBaudRate(camera, 115200);
   // 	  System.out.println("Changed BaudRate " );
    	  sleep(15); // 5 on a PC
    	  try {
			camera.closePort(); 
			camera.openPort();
			camera.setParams(SerialPort.BAUDRATE_115200,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
  	
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      sleep(40); // 20 on a PC
    
/*  	System.out.println("connected" + camera.getConnected());
        if (camera.isOpened() == true)
        {// System.out.println("connected @115200");
           // System.out.println("init io");
  //        	  camera.initListener();
          	  System.out.println("listening @155200 but listener disabled ;-)");
            
        }
   */     conn_115200 = getVersion(camera);
 //       camera.sleep(1);
 //       System.out.println("get version at 115200 bds result " + conn_115200 );
      } 
	
/********* end of init phase *******************/        

/***********  settings phase *******************/
//setImageSize and getImageSize function not working
//setImageSize(VC0706_640x480);        // biggest       
//       boolean b = setImageSize(VC0706_320x240);// medium
   	//	setImageSize(VC0706_160x120);          // small
//      camera.sleep(100); //sleep cannot be included into set image size as it be after the command
//      System.out.println("setImageSize result " + b); 
//   int imgsize = getImageSize();
    
 TVoff(camera);
 sleep(30); // 10 on a PC
 boolean comp = setCompression(camera, (byte) 0xCA); // quite high but in line with my requirements, may be to put as an arg ?
 //System.out.println("compression " + comp);
 sleep(30); // 10 on a PC
 int b = getCompression(camera);
// System.out.println("camera compression " + b);
 sleep(10); // 1 on a PC
// System.out.println("sending resume video command from init");
 resumeVideo(camera); 
 ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
 PrintStream[] outs;
 
/***********  END of settings phase ************/   
  
  /*****************  Branching with args(1) : if no args only stdout, if args="save" : save to disk only (.\\) ***********/     
  if (args.length > 1 ) {
//	   System.out.println("saving IMAGE to disk @ local dir");
	   int nb_image = 1;
	   byte[] a = new byte[0];
	   while (nb_image >0){
			nb_image++;
			a = Capture(camera);
		FileOutputStream fileOuputStream;		        
		try { 
	  		    //convert array of bytes into file 
	  		   fileOuputStream = new FileOutputStream("IMAGE.jpg");  
	  		   fileOuputStream.write(a);
	  		   fileOuputStream.close();
//	  		   System.out.println("written IMAGE.jpg to local active dir"); 
		} catch (Exception e) {
				e.printStackTrace();
		  }				
//	captureAndSave(nb_image); //uncomment to save multiple images to disk, auto incrementation
     try {
		camera.closePort();
		sleep(300); // not changed because the sleep is due to the camera not the beaglebone
		camera.openPort();	}
     catch (SerialPortException e) {
    	 System.out.println("eroor in port I/O"); 
	}
//    		System.out.println("connected" + camera.getConnected());
 		if (camera.isOpened() == true)
			{//  System.out.println("connected");
        }
   }
  sleep(15); // 10 on a PC
  conn_115200 = getVersion(camera);  
  sleep(15);  // 10 on a PC 
  }
     
/******  STDOUT byte output only  *************/
	   else {   
//		   System.out.println("STDOUT ONLY");
   	   int nb_image = 1;
   	   byte[] a = new byte[0];
   	   while (nb_image >0){
 			nb_image++;
 			bos.reset();
 			a = Capture(camera);
// 			a = CaptureBase64(); // uncomment to set base 64 output		
/*			bos.write(a, 0, a.length);    	// write bytes to bos ...
			System.out.print(bos);
			System.out.flush();		
			*/
 			try {
				System.out.write(a);
			} catch (IOException e1) {
				System.out.println("problem on serial port : "+ e1);
			}

         try {
			camera.closePort();
			sleep(300); // not changed because the sleep is due to the camera not the beaglebone
			camera.openPort();
		} catch (SerialPortException e) {
			
			System.out.println("problem on serial port : "+ e);
		}
//    		System.out.println("connected" + camera.getConnected());
     		if (camera.isOpened() == true)
    			{//  System.out.println("connected");
        
           }
       }
   
   	  sleep(15); // 10 on a PC
      conn_115200 = getVersion(camera);  
      sleep(15); // 10 on a PC  
	   }
	 }
	 

/**************** Functions *******/

static void captureAndSave(SerialPort camera, int numimage) {
		int nb_image = numimage;
      	long time = System.currentTimeMillis();
      	jpglen = 30000;
  		
      	do {
      	   sleep(15); // 2 on a PC
 //    	   System.out.println("sending resume video command from capture and save");
 			resumeVideo(camera);
 //			stepframe(camera);
 			sleep(15); // 2 on a PC
 //         System.out.println("sending TAKE PICTURE command");
 			takePicture(camera);
 			sleep(15); // 2 on a PC
 			jpglen = frameLength(camera);
     	} while (jpglen > 20000);
      	
//  		System.out.println("Picture taken!");
 		System.out.println("jpglen  " + jpglen); 
        byte[] bufferfile = {(byte) 0x00};// 

        bufferfile = readImageData(camera, jpglen); //never reach above, thus it is a compromise 
        sleep(15); // 2 on a PC
        
        FileOutputStream fileOuputStream;
        
  		try { 
  		    //convert array of bytes into file 
  		   fileOuputStream = new FileOutputStream(".\\IMAGE.jpg");  //".\\IMAGE"+nb_image+".jpg");
  		    fileOuputStream.write(bufferfile);
  		    fileOuputStream.close();
 // 		   System.out.println("written");
 
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
  		
  /*********************   uncomment to get stats on command line   ***************/////////////////		
    /* 	  time = time - System.currentTimeMillis();
          System.out.println("done! time");
          System.out.println(time + " ms");
          System.out.println("Stored ");
          System.out.println(bufferfile.length);
          System.out.println(" byte image.");
*/   
/* }*/

static byte[]  Capture(SerialPort camera) {
	//	int nb_image = numimage;   	
      	jpglen = 30000;
      	do {
      		sleep(15); // 2 on a PC
//    	    System.out.println("sending resume video command within capture");
			resumeVideo(camera);
 			//stepframe(camera);
			sleep(15); // 2 on a PC
			//System.out.println("resuming "+ resume);
  
			takePicture(camera);
			sleep(4); // 2 on a PC
			jpglen = frameLength(camera);
    	} while (jpglen > 20000);

        byte[] bufferfile = {(byte) 0x00};// 
 //       System.out.println("image length " + jpglen);
        bufferfile = readImageData(camera, jpglen); //never reach above, thus it is a compromise 
        return bufferfile;
     
}

public static byte[] readImageData(SerialPort camera, int jpglen){
	
	int wCount = 0; // For counting # of writes
	byte[] globalbuffer = new byte[0];
  
	while (jpglen > 0) {


		int bytesToRead = jpglen;
	
		byte[] buffer = readPicture(camera, bytesToRead, 12288);	//8192 is the size of max size of the picture to determine the wait time to have full block of data
			
		globalbuffer = concat(globalbuffer, buffer);
	
		jpglen -= bytesToRead;
		wCount++;
	}

//	System.out.println("done in " + wCount + " iteration");
	return globalbuffer;
}

static byte[] CaptureBase64() {
	//	int nb_image = numimage;
      	
      	jpglen = 30000;
  		
      	do {
 //    	     camera.sleep(2);
 //			resumeVideo();
 //			camera.sleep(2);
 	//		takePicture();
 //			camera.sleep(2);
 			jpglen = 0;  //frameLength();  TODO reinject framelentgh
     	} while (jpglen > 20000);

        byte[] bufferfile = {(byte) 0x00};// 

  //      bufferfile = readImageData(jpglen,8192); //never reach above, thus it is a compromise 
//        camera.sleep(2);
        ByteArrayOutputStream  image = new ByteArrayOutputStream(); 
        try {
			image.write(bufferfile);		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte[] Base64bufferfile =  Base64.encodeBase64(bufferfile);
  //      String Base64output = new String(Base64bufferfile);
        return  Base64bufferfile;
    //    return Base64output;
     
}

static boolean ChangeBaudRate(SerialPort camera, int baudrate) {
	
	byte arg_buffer1 = 0;
	byte arg_buffer2 = 0;
	
	switch (baudrate) 
	{ 
	case 9600   : arg_buffer1=(byte) 0xAE; arg_buffer2 = (byte) 0xC8; break; 
	case 19200  : arg_buffer1=(byte) 0x56; arg_buffer2 = (byte) 0xE4; break; 
	case 38400  : arg_buffer1=(byte) 0x2A; arg_buffer2 = (byte) 0xF2; break; 
	case 57600  : arg_buffer1=(byte) 0x1C; arg_buffer2 = (byte) 0x4C; break; 
	case 115200 : arg_buffer1=(byte) 0x0D; arg_buffer2 = (byte) 0xA6; break; 
	
	}
	
	byte[] args  = {(byte) 0x03, (byte) 0x01,(byte)   arg_buffer1 ,(byte) arg_buffer2 };
	                    
	runCommand(camera, VC0706_SET_BAUDRATE, args); //data often starts at adress != 0

//	  System.out.println("frameptr = : " + frameptr);
	  sleep(30); // 10 on a PC // let the cam answer
	  boolean result = false; 
	  byte[] answer = null;
	  
  	  while (!result) {
  			  try {
				answer = camera.readBytes();
//				System.out.println("change aud rate result = : " + answer);
			} catch (SerialPortException e) {
				
				e.printStackTrace();
			}
  			  for (int i =0; i < (answer.length - 4); i++ ){
  				  if ((answer[i] == (byte) 0x76) ||
	  				  (answer[i+1] == (byte) 0x00) ||  //looking for camera delay
  					  (answer[i+2] == (byte) 0x24) ||
	  				  (answer[i+3] == (byte) 0x00))
  				  {
  					  result = true;
  					  break;
  				  }
  			  }
  	  }		  
	return result;
	
}

public static void sleep(long millis) {
    try {
        TimeUnit.MILLISECONDS.sleep(millis);
    } catch (InterruptedException e) {
    	System.out.println("Sleep interrupted " + e);
    }
}

	 
static int frameLength(SerialPort camera) {
 byte[] args = new byte[]{(byte) 0x01,(byte) 0x00};
 runCommand(camera, VC0706_GET_FBUF_LEN, args);
 int len, offset = 0;
 sleep(70);  // 40 on a PC
 byte[] answer = null; 
 boolean result = false; 
	  while (!result) {
		  try {
			answer = camera.readBytes();
		} catch (SerialPortException e) {
			System.out.println("error : " + e);
		}
	offset = 0;
	  for (int i =0; i < (answer.length - 4); i++ ){
		  if ((answer[i] == (byte) 0x76) ||
		      (answer[i+1] == (byte) 0x00) ||
		      (answer[i+2] == (byte) VC0706_GET_FBUF_LEN) ||
		      (answer[i+3] == (byte) 0x00) ||
		      (answer[i+4] == (byte) 0x04) ||
		      (answer[i+5] == (byte) 0x00) ||
		      (answer[i+6] == (byte) 0x00)
		      )
		  {
//			  System.out.println("sgot it, i = " + i );
			  offset = i+5;
//			  System.out.println("so, offset = " + offset );
			  result = true;
//			  System.out.println("at the end i =  " + i );
			  break;
		  }
	  }
	  }
//	System.out.println("converting " + answer[offset] + "and " + answer[offset+1] + "and " + answer[offset+2] + "and " + answer[offset+3] );
	byte[] arr = { answer[offset], answer[offset+1],answer[offset+2],answer[offset+3]};
	ByteBuffer wrapped = ByteBuffer.wrap(arr); // big-endian by default
	len = wrapped.getInt(); // 1
	 if ( len  > 50000){//sometimes the offset is 2 ahead, so files are not > 50k
 		System.out.println("image lentgh problem = " + len); 
		byte[]arr1 = { answer[offset-2], answer[offset-1],answer[offset],answer[offset+1]};
	  	wrapped = ByteBuffer.wrap(arr1); // big-endian by default
	  	len = wrapped.getInt(); 
//	 	System.out.println("real lentgh = " + len); 
}
return len;
}

static boolean takePicture(SerialPort camera) {
	frameptr = 0;
	return cameraFrameBuffCtrl(camera, VC0706_STOPCURRENTFRAME);
}

static boolean resumeVideo(SerialPort camera) {
  cameraFrameBuffCtrl(camera,VC0706_RESUMEFRAME );//   
 return true;
}

static boolean stepframe(SerialPort camera) {
	  cameraFrameBuffCtrl(camera,VC0706_STEPFRAME );
	 return true;
	}

static boolean cameraFrameBuffCtrl(SerialPort camera, byte command) {
	  byte[] args = {(byte) 0x1,(byte) command};
	 
	  byte[] answer = null; 
	  boolean result = false;
 	  while (!result) {
 		  try {
 			  runCommand(camera, VC0706_FBUF_CTRL, args);
 			  sleep(20);  // 15 on a PC // lower values involves crash when long time image loading, can go below for a few image
			  answer = camera.readBytes();
		} catch (SerialPortException e) {
			System.out.println("error : " + e);
		}
 		
		  for (int i =0; i < (answer.length - 4); i++ ){
			  if ((answer[i] == 118) &&
			      (answer[i+1] == 00) &&  //dec values
			      (answer[i+2] == 54) &&
			      (answer[i+3] == 00))
			  {
			  result = true;					 
			  break;
			  }
		  }
 	  }
 		return result;
	}

	static boolean getVersion(SerialPort camera){
		byte[] args = new byte[] { (byte) 0x01,  (byte) 0x56,  (byte) 0x57 };  
		runCommand(camera, VC0706_GEN_VERSION, args);
		sleep(30);  // 10 on a PC // let the cam answer
		boolean result = false;		
		try {	
			String answerq = camera.readString() ;
			if (answerq != null) 
			if (answerq.contains("VC0703")) { result = true;}
//			System.out.println("GetVersion result : " + answerq);//.toString());
		} catch (SerialPortException e) {
			System.out.println("GetVersion error : " + e);
		
		}
		return result;
 	}
	
	static boolean reset(SerialPort camera) {
	  byte[] args = {(byte) 0x00};

	  return runCommand(camera, VC0706_RESET, args);
	}

	static boolean setImageSize(SerialPort camera, byte x) {
	  byte[] args = new byte[]{(byte) 0x12, (byte) 0x09, (byte) 0x01,(byte) 0x00, (byte) 0x19,(byte) 0x11 };  
	  runCommand(camera, VC0706_WRITE_DATA, args);
	  sleep(100);
	  
	  boolean result = false; 
	    	while (!result) {
	    	try {
	    	String fileString = camera.readString();
			//String fileString = new String(answer,"UTF-8");
			System.out.println("set imagesize result en string" + fileString);
			result = fileString.contains("Init en");
		} catch (Exception e) {
			System.out.println("problem " + e);
		}
	    	}	   
	    return result;  
}

	static int getImageSize(SerialPort camera) {
		  byte[] args = {(byte)0x40,(byte) 0x40, (byte)0x10,(byte) 0x00, (byte)0x19};
		  runCommand(camera, VC0706_READ_DATA, args);
		    
		  sleep(100); // let the cam answer
		  byte[] answer = null;
		  boolean verify_response = false;
		  while (!verify_response) {
		    	try {
					answer = camera.readBytes();
					String fileString = new String(answer,"UTF-8");
					System.out.println("get image result en string" + fileString);	
				} catch (SerialPortException e) {
					System.out.println("problem " + e);
				} catch (UnsupportedEncodingException e) {
					System.out.println("problem " + e);
				}
		
		    	}
		  
		  return answer[5];
		}
	
	static boolean setCompression(SerialPort camera,byte c) {
	   byte[] args = {(byte)0x5, (byte)0x1, (byte)0x1, (byte)0x12, (byte)0x4,(byte) c};
	  return runCommand(camera, VC0706_WRITE_DATA, args);
	}

	static int getCompression(SerialPort camera) {
		 byte[] args = {(byte)0x4, (byte) 0x1, (byte) 0x1, (byte) 0x12, (byte) 0x04};
	  runCommand(camera, VC0706_READ_DATA, args);
	  sleep(25);  // 10 on a PC 
	  byte[] answer = null;
	try {
		answer = camera.readBytes();
	} catch (SerialPortException e) {
		System.out.println("problem " + e);
	}
	int offset = 0;
	  for (int i =0; i < (answer.length - 4); i++ ){
			  if ((answer[i] ==  118) &&
				  (answer[i+1] == 00) &&  //looking for camera delay
				  (answer[i+2] ==  48) &&
				  (answer[i+3] ==  00))
			  {
//				  System.out.println("got 118 +0 + 48, i = " + i );
				  offset = i;
				  break;
			  }
		  }
	  return answer[offset + 5];
	}
	

	static boolean TVon(SerialPort camera) {
		byte[] args = {(byte) 0x1, (byte) 0x1};
		return runCommand(camera, VC0706_TVOUT_CTRL, args);
			}
	
	static boolean TVoff(SerialPort camera) {
		byte[] args = {(byte) 0x1, (byte) 0x00};
        return runCommand(camera, VC0706_TVOUT_CTRL, args);
			}

	public static byte[] readPicture(SerialPort camera, int bytetoRead, int buffersize) {
	
		byte arg_buffer1=0;
		byte arg_buffer2=0;
		int sleeptime = 0; //based on tests at 38400 bauds
		switch (buffersize) 
		{ 
		case 32  : arg_buffer1=(byte) 0x00; arg_buffer2 = (byte) 0x20; sleeptime = 12; break; 
		case 64  : arg_buffer1=(byte) 0x00; arg_buffer2 = (byte) 0x40; sleeptime = 15; break; 
		case 96  : arg_buffer1=(byte) 0x00; arg_buffer2 = (byte) 0x60; sleeptime = 20; break; 
		case 128 : arg_buffer1=(byte) 0x00; arg_buffer2 = (byte) 0x80; sleeptime = 22; break; 
		case 256 : arg_buffer1=(byte) 0x01; arg_buffer2 = (byte) 0x00; sleeptime = 33; break; 
		case 512 : arg_buffer1=(byte) 0x02; arg_buffer2 = (byte) 0x00; sleeptime = 55; break; 
		case 1024 : arg_buffer1=(byte) 0x04; arg_buffer2 = (byte) 0x00; sleeptime = 100; break;
		case 2048 : arg_buffer1=(byte) 0x08; arg_buffer2 = (byte) 0x00; sleeptime = 200; break;
		case 4096 : arg_buffer1=(byte) 0x10; arg_buffer2 = (byte) 0x00; sleeptime = 390; break;
		case 8192 : arg_buffer1=(byte) 0x20; arg_buffer2 = (byte) 0x00; sleeptime = 480; break;
		case 12288 : arg_buffer1=(byte) 0x21; arg_buffer2 = (byte) 0x40; sleeptime = 580; break;
		case 16384 : arg_buffer1=(byte) 0x30; arg_buffer2 = (byte) 0x80; sleeptime = 700; break;
		}
	/* best result seen
	case 32  : sleeptime = 20@38400 (10,3 s/12k) 12@115200 (2,9 s/5k)
	case 64  : sleeptime = 26@38400 (7,4s/12k)
	case 96  : sleeptime = 45@38400 (6,6s/12k)
	case 128 : sleeptime = 45@38400 (5s/12k) 	22@115200 (1,1 s/5k)
	case 256 : sleeptime = 80@38400 (4,2s/12k) 	33@115200 (1,0 s/5k)
	case 512 : sleeptime = 140@38400 (3,6s/12k) 55@115200 (0,6 s/5k)
	case 1024 :sleeptime =  					100 @115200 (0,5 s/5k)
	case 2048 :sleeptime = 						200 @115200 (0,6 s/5k)
	case 4096 :sleeptime = 						390 @115200 (0,8 s/5k)
	case 8192 :sleeptime = 						480 @115200 (0,5 s/5k)
	*/
   
	    byte[] args  = {(byte) 0x0C, (byte) 0x00, (byte) 0x0A, // last was 0x0A
				(byte) 0x00, (byte) 0x00, (byte) (frameptr >> 8), (byte) (frameptr & 0xFF), 
				(byte) 0x00, (byte) 0x00, (byte) arg_buffer1, (byte) arg_buffer2, 
		                    (byte) 0x00, (byte) 0x06};
		runCommand(camera, VC0706_READ_FBUF, args); //data often starts at adress != 0

	//	  System.out.println("frameptr = : " + frameptr);
		sleep((long) (sleeptime *2)); //1.5 because of beaglebone, remove it on a pc // let the cam answer
		boolean result = false; 
		byte[] answer = null;
		@SuppressWarnings("unused")
		int offset = 0, it=0;
	  	while (!result) {
	  		// try {
//	  		System.out.println("iteration  " + it);
	  		try {
				answer = camera.readBytes();
				} catch (SerialPortException e) {
					 System.out.println("problem " + e);
				}
	  			offset = 0;
	  			for (int i =0; i < (answer.length - 4); i++ ){
	  			  if ((answer[i] ==  118) ||
		  				  (answer[i+1] == 00) ||  //looking for camera delay
	  					  (answer[i+2] ==  50) ||
		  				  (answer[i+3] ==  00))
	  				 {
//	  					  System.out.println("got FFD8, i = " + i );
	  					  offset = i+5;
	//  					  System.out.println("so, offset = " + offset );
	  					  result = true;
	  //					  System.out.println("at the end i =  " + i );
	  	//				  System.out.println("byte contain  " + answer[i] + "  " +  answer[i+1] + "  " + answer[i+2] + "  ");
	  					  break;
	  				  }
	  			 }
	  			it++;
	  	  }		  		  
//		  byte[] resultat = new byte[bytetoRead];
		  byte[] resultat = new byte[answer.length];
//		  System.out.println("result length " + resultat.length );
//		  System.out.println("frameptr value  " + frameptr );	
		  frameptr += bytetoRead;
//		  System.out.println("byte to read number  " + bytetoRead );

/*		  for (int k=0;k<bytetoRead;k++){
  
			  resultat[k] = answer[k];//+5];
		  }		  
*/		  for (int k=0;k<answer.length-offset;k++){
			resultat[k] = answer[k+offset];//+5];
	  	  }		  	  
		  return resultat;	
		}

	
/**************** low level commands */
	static boolean runCommand(SerialPort serial, byte cmd, byte[] args) {

sendCommand(serial, cmd, args);  //to add camera obkect

return true; //logic to be be implemented
}

static void sendCommand(SerialPort camera, byte cmd, byte[] args) {

	int args_len = args.length;
	byte[] tosend = new byte[3 + args_len];
	tosend[0] = (byte) 0x56;
	tosend[1] = (byte) 0x00;
	tosend[2] = cmd;
	System.arraycopy(args, 0, tosend, 3, args_len);
	
  //  camera.writebytes(tosend,resplen);  
    try {
		camera.writeBytes(tosend);
	//	System.out.println("sent " + tosend);
	} catch (SerialPortException e) {
		System.out.println("write bytes error " + e);
	}  
}

static byte[] concat(byte[] A, byte[] B) {
	   int aLen = A.length;
	   int bLen = B.length;
	   byte[] C= new byte[aLen+bLen];
	   System.arraycopy(A, 0, C, 0, aLen);
	   System.arraycopy(B, 0, C, aLen, bLen);
	   return C;
	}

/************* constants ***********/
 static byte VC0706_RESET = 0x26;
 static byte VC0706_GEN_VERSION = 0x11;
 static byte VC0706_SET_BAUDRATE = 0x24;
 static byte VC0706_READ_FBUF = 0x32;
 static byte VC0706_GET_FBUF_LEN = 0x34;
 static byte VC0706_FBUF_CTRL = 0x36;
 static byte VC0706_DOWNSIZE_CTRL = 0x54;
 static byte VC0706_DOWNSIZE_STATUS = 0x55;
 static byte VC0706_READ_DATA = 0x30;
 static byte VC0706_WRITE_DATA = 0x31;
 static byte VC0706_COMM_MOTION_CTRL = 0x37;
 static byte VC0706_COMM_MOTION_STATUS = 0x38;
 static byte VC0706_COMM_MOTION_DETECTED = 0x39;
 static byte VC0706_MOTION_CTRL = 0x42;
 static byte VC0706_MOTION_STATUS = 0x43;
 static byte VC0706_TVOUT_CTRL = 0x44;
 static byte VC0706_OSD_ADD_CHAR = 0x45;

 static byte VC0706_STOPCURRENTFRAME = 0x0;
 static byte VC0706_STOPNEXTFRAME = 0x1;
 static byte VC0706_RESUMEFRAME = 0x3;
 static byte VC0706_STEPFRAME = 0x2;

 static byte VC0706_640x480 = 0x00;
 static byte VC0706_320x240 = 0x11;
 static byte VC0706_160x120 = 0x22;

 static byte VC0706_MOTIONCONTROL = 0x0;
 static byte VC0706_UARTMOTION = 0x01;
 static byte VC0706_ACTIVATEMOTION = 0x01;

 static byte VC0706_SET_ZOOM = 0x52;
 static byte VC0706_GET_ZOOM = 0x53;

 static int CAMERABUFFSIZ = 100;
 static int CAMERADELAY = 10;
}


