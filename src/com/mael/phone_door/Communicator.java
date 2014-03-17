
package com.mael.phone_door;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Communicator implements SerialPortEventListener
{

    //for containing the ports that will be found
    @SuppressWarnings("rawtypes")
	private String[] ports = null;
    //map the port names to CommPortIdentifiers
    @SuppressWarnings("rawtypes")
	private HashMap portMap = new HashMap();

    //this is the object that contains the opened port
   
    private SerialPort serialPort = null;

    //input and output streams for sending and receiving data, buffer for reading
    private InputStream input = null;
    private OutputStream output = null;
    private byte[] readBuffer = new byte[10240]; //10k buffer with 8k effective
    private int buffersize = 10240; //10k buffer with 8k effective
    private int HW_inputbuffer = 16384; //16k HW buffer, used for Beaglebone Black, set it lower depending your use
    private int buffertoread = 0;
    private BufferedInputStream is;
    private BufferedReader reader;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
/*    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
*/
    //a string for recording what goes on in the program
    //this string is written to the GUI
    String logText = "";

    // add constructor without GUI
    public Communicator()
    {
    }
    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    @SuppressWarnings("unchecked")
	public void searchForPorts()
    {
    	String[] ports = SerialPortList.getPortNames();
        for(int i = 0; i < ports.length; i++){
            System.out.println(ports[i]);
        }

    }

    // adding connection function with COM port as a parameter and settings
    //connect to the selected port in the combo box
    //pre: ports are already found by using the searchForPorts method
    //post: the connected comm port is stored in commPort, otherwise,
    //an exception is generated
    public void connect(String port,int baudrate)
    { 
    	 
        String selectedPort = port;
        SerialPort serialPort = new SerialPort(port);

        try
        {
            //the method below returns an object of type CommPort
        	
        	serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

            //for controlling GUI elements (still required ?)
            setConnected(true);
            //logging
            logText = selectedPort + " opened successfully.";
        }
        catch (SerialPortException  e)
        {
            logText = selectedPort + " is in use. (" + e.toString() + ")";            
        }
        catch (Exception e)
        {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
        }
    }
    
    //open the input and output streams
    public boolean initIOStream()
    {
        //return value for whather opening the streams is successful or not
  /*      boolean successful = false;
        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            is = new BufferedInputStream(is);
            reader = new BufferedReader(new InputStreamReader(is));
            successful = true;
            return successful;
        }
        catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
   
            return successful;
           
        } */
    	return true; //or simply de activate it it the main function
    }

    //starts the event listener that knows whenever data is available to be read
    //pre: an open serial port
    //post: an event listener for the serial port that knows when data is recieved
    public void initListener()
    {
        //			serialPort.addEventListener(this);
		serialPort.notifyAll();
        
    }

    //disconnect the serial port
    public void disconnect()
    {
        try
        {
            serialPort.removeEventListener();
            serialPort.closePort();
  //          input.close();
   //         output.close();
    //        is.close();
      //      reader.close();
            setConnected(false);
    
            logText = "Disconnected.";
        }
        catch (Exception e)
        {
            logText = "Failed to close serialPort " + serialPort.getPortName() + "(" + e.toString() + ")";
       }
    }

    final public boolean getConnected()
    {
        return bConnected;
    }

    public void setConnected(boolean bConnected)
    {
        this.bConnected = bConnected;
    }

    //what happens when data is received
    //pre: serial event is triggered
    //post: processing on the data it reads
    public void serialEvent(SerialPortEvent evt) {
        if (evt.isRXCHAR())
        {
        //	readSerial();
        }
   
    }

    //method that can be called to send data
    //pre: open serial port
    //post: data sent to the other device
  /*  public void writeData(int leftThrottle, int rightThrottle)
    {
        try
        {
            output.write(leftThrottle);
            output.flush();
            //this is a delimiter for the data
            output.write(DASH_ASCII);
            output.flush();
            
            output.write(rightThrottle);
            output.flush();
            //will be read as a byte so it is a space key
            output.write(SPACE_ASCII);
            output.flush();
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";

        }
    }
 */
    // add byte sending capacity
    //method that can be called to send data
     
     
    public void writebytes(byte[] data,int returnlentgh)
    {
    	try {
			serialPort.writeBytes(data);
		} catch (SerialPortException e1) {
			logText = "Failed to write data. (" + e1.toString() + ")";
		}
 /*   	buffertoread = returnlentgh;
        try
        {
        	for (int i=0; i < data.length; i++){
        		output.write(data[i]);
        		output.flush();
//        		System.out.println("sent step " + i);
        	}	
        }
        catch (Exception e)
        {
            logText = "Failed to write data. (" + e.toString() + ")";

        }
   */ }
 
    @SuppressWarnings("resource")

    String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
      
    byte[] readSerial() {
    	
        		try {
        	//sleep(2); // let the time to the camera to answer, otherwise the  buffer will be field with bad data
 //       	waitForBytes(buffertoread);
                // Read the serial port
        		int Bytes_to_read;
        		byte[] data_read = {0};
				try {
					Bytes_to_read = serialPort.getInputBufferBytesCount();
					System.out.println("trying to read the serial data, data length: " + Bytes_to_read );
					data_read = serialPort.readBytes(Bytes_to_read);
					System.out.println("in read function            " + new String(readBuffer, 0, 10));
				} catch (SerialPortException e) {
					System.out.println("Bytes_to_read error " + e);
				}
        	
                //input.read(readBuffer, 0, buffertoread);  // the number 10 is originally buffertoread, whioch is set to  0 !!
                
                // Print it out
                
                return data_read;
        		}
        		finally{} //just because the return is inside try / catch
    }
    
 /*   private void waitForBytes(int numBytes) {
    	try {
			while (( is.available()) <  buffertoread) sleep(1);
		} catch (IOException e) {
	//		System.out.println("waitForBytes error " + e);;
		}
    }
    */
    public int available() throws Throwable  {
       

    	try {
            return is.available();
        } catch (IOException e) {
            throw new Exception(e);
        }
    }
    
    public void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
        	System.out.println("Sleep interrupted " + e);
        }
    }
       
}
