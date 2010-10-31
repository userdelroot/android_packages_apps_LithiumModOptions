package fac.userdelroot.lithiummod.options;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class OutputStreamHelper extends Thread {

    private static final String TAG = "OutputStreamHelper ";
    private static final String CON_STDOUT = "stdin>";
    private ifaceStdio stdio;
    private String mCommand;
    private boolean isSuccess;
    private String StdErr;
    private String StdOut;
    
    private static final int DELAY = 500; // 1000 ms
    
    
    OutputStreamHelper(ifaceStdio io, String cmd) {
        stdio = io;
        mCommand = cmd;
        isSuccess = false;
    }
    

    public void run() {
        
            Process p = null;
            int exitStatus = 0;
        try {
            
            p = Runtime.getRuntime().exec("su");
            
            DataOutputStream out = new DataOutputStream(p.getOutputStream());
            InputStreamHelper ish = new InputStreamHelper(p.getInputStream(), myStdio);
            ErrorStreamHelper esh = new ErrorStreamHelper(p.getErrorStream(), myStdio);
            
            out.writeBytes(mCommand);
            out.flush();

            out.writeBytes("exit\n");
            out.flush();
            
            ish.start();
            esh.start();
            
            ish.join(DELAY);
            esh.join(DELAY);
            out.close();
            exitStatus = p.waitFor();
        }
        catch (IOException e) {
            Log.e(TAG + "run()>" + e.getLocalizedMessage() );
        } catch (InterruptedException e) {
            stdio.setIsSuccess(false);
        }
        finally {
            
            stdio.setStdErr(StdErr);
            stdio.setStdOut(StdOut + "\ncommand>" + mCommand);
            stdio.setExitStatus(exitStatus);
            stdio.setIsSuccess(isSuccess);
            
            if (p != null)
                p.destroy();
        }
     }
    
    ifaceStdio myStdio = new ifaceStdio() {

        @Override
        public void setStdErr(String err) {
            StdErr = err;
        }

        @Override
        public void setStdOut(String out) {
            StdOut = out;            
        }

        @Override
        public void setExitStatus(int code) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setIsSuccess(boolean success) {
            isSuccess = success;
        }
        
    };   
}
