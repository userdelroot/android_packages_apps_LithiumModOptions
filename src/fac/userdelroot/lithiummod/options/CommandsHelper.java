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

import android.content.Context;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * Commands Helper class
 * 
 * @author userdelroot
 * Oct 26, 2010
 */
public class CommandsHelper {

    private static final String TAG = "CommandsHelpers ";
    private static final String BASH_RC = "/mnt/sdcard/.bashrc";
    private static final String BASH_ALIASES = "/mnt/sdcard/.bash_aliases";
    private static final String BASH_BIN = "/mnt/sdcard/bin";
    
    private static final String ASSET_BASH_RC = "bashrc";
    private static final String ASSET_BASH_ALIASES = "bash_aliases";
    
    /**
     * Reboot the device, pretty simple
     * Requires: root
     */
    public static final void reboot() {

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
    
    /**
     * Setup bash enviroment on the sdcard
     * does not require root
     * @param enabled
     * @param c
     */
    public static void bashEnv(boolean enabled, Context c) {
        
        // make sure the device external storage is mounted.
        if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            Toast.makeText(c, R.string.external_storage_umounted, Toast.LENGTH_SHORT).show();
            return;
        }

    
        if (enabled) {
            writeBashRc(c);
        }
        else {
            File file = new File (BASH_RC);
            if (file.exists())
                file.delete();
            
            file = null;
            file = new File(BASH_ALIASES);
            if (file.exists())
                file.delete();
            
            file = null;
        }

        
        Options.dismissProgressDialog();
    }

    
    /**
     * Write bash_aliases
     * does not require root
     * @param c
     */
    private static void writeBashAliases(Context c) {

        InputStream is = null;
        
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        File ofile = new File(BASH_ALIASES);
        

        
        try {
        
            if (!ofile.exists() && !ofile.createNewFile()) {
                Toast.makeText(c, R.string.file_not_writeable, Toast.LENGTH_SHORT).show();
                return;
            }
            
            is = c.getAssets().open(ASSET_BASH_ALIASES);
            fos = new FileOutputStream(ofile);
            osw = new OutputStreamWriter(fos);
            
            int nextChar;
            
            while (( nextChar = is.read() ) != -1) {
                osw.write(nextChar);
                osw.flush();
            }
            
            // Close the streams
            is.close();
            fos.close();
            osw.close();
            ofile = null;
        }
        catch (Exception e) {
            Toast.makeText(c, R.string.unknown_failure, Toast.LENGTH_SHORT).show();
            if (Log.LOGV)
                Log.e(TAG + "Exception " + e.getLocalizedMessage().toString());
            return;
        }    
    }

    /**
     * Write bashrc
     * does not require root
     * @param c
     */
    private static void writeBashRc(Context c) {

        File ofile = new File(BASH_RC);
        InputStream is = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        
        try {
            
            if (!ofile.exists() && !ofile.createNewFile()) {
                Toast.makeText(c, R.string.file_not_writeable, Toast.LENGTH_SHORT).show();
                return;
            }
            
            is = c.getAssets().open(ASSET_BASH_RC);
            fos = new FileOutputStream(ofile);
            osw = new OutputStreamWriter(fos);
            
            int nextChar;
            
            while (( nextChar = is.read() ) != -1) {
                osw.write(nextChar);
                osw.flush();
            }
            
            // Close the streams
            is.close();
            fos.close();
            osw.close();
            ofile = null;
        }
        catch (Exception e) {
            Toast.makeText(c, R.string.unknown_failure, Toast.LENGTH_SHORT).show();
            if (Log.LOGV)
                Log.e(TAG + "Exception " + e.getLocalizedMessage().toString());
            return;
        }        
        
        ofile = new File(BASH_BIN);
       
        if (!ofile.exists()) {
            if (Log.LOGV)
                Log.i(TAG + "creating " + BASH_BIN + " directory\n");
            
            ofile.mkdir();
        }
        writeBashAliases(c);
    }
}
