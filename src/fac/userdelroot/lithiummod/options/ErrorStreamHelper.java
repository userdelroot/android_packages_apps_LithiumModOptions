/**
 * LithiumMod Options
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ErrorStreamHelper extends Thread {
    
    private static final String TAG = "ErrorStreamHelper ";
    private static final String CON_STDERR = "stderr>";
    private InputStream mInputStream;
    private ifaceStdio stdio;
    
    ErrorStreamHelper(InputStream is, ifaceStdio io) {
        mInputStream = is;
        stdio = io;
        
    }
    
    public void run() {
        
            BufferedReader br = null;
            String result = null;
        try {
            br = new BufferedReader(new InputStreamReader(mInputStream));
            String line = null;
            result = "";

            while ((line = br.readLine()) != null ) {
                result += line + " ";
                Log.i(TAG + CON_STDERR + "" + line);
            }            
            
            br.close();
            mInputStream.close();
        }
        catch (IOException e) {
            Log.e(TAG + "run()>" + e.getLocalizedMessage() );
        }
        finally {
            if (stdio != null) 
                stdio.setStdErr(result);
        }
    }
}
