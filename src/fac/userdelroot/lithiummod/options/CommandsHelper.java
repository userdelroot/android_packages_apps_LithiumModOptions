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

/**
 * Commands Helper class
 * 
 * @author userdelroot
 * Oct 26, 2010
 */
public class CommandsHelper {

    private static final String TAG = "CommandsHelpers ";
    

    /**
     * Reboot the device, pretty simple
     */
    public static void reboot() {

        Process p = null;
        
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream stdout = new DataOutputStream(p.getOutputStream());
            stdout.writeBytes("reboot\n");
            stdout.flush();
            stdout.close();
            
        } catch (IOException e) {
            if (Log.LOGV)
                Log.e(TAG + "reboot " + e.getLocalizedMessage().toString());
        }
    }
    
    
}
