/**
 *  LithiumMod Options 
 * 
 * Copyright (C) 2010  userdelroot r00t316@gmail.com (Frank C.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package fac.userdelroot.lithiummod.options;

import java.io.DataOutputStream;
import java.io.IOException;

public class OutputStreamHelper extends Thread {

    private static final String TAG = "OutputStreamHelper ";
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
            
            Log.i(TAG + "command>"+mCommand);
            stdio.setStdErr(StdErr);
            stdio.setStdOut(StdOut);
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
