package fac.userdelroot.lithiummod.options;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;

public class JitCompiler extends Thread {

    private static final String TAG = "JitCompiler ";
    private boolean mDisabled;
    private ifaceStdio mIfaceStdio;
    private volatile Thread destroy;
    private boolean isSuccess;
    private String mStdOut;

    
    JitCompiler(boolean enable, Context c, ifaceStdio stdio) {
        mDisabled = enable;
        mIfaceStdio = stdio;
        destroy = this;
        isSuccess = true;
    }
    
    
    /**
     * stops our thread.  Used if there is an error or has finished.
     */
     private void cleanUp() {
         mIfaceStdio.setIsSuccess(isSuccess);
         myHandler.handleMessage(myHandler.obtainMessage());
         systemRO();
         
         destroy = null;
         isSuccess = false; // need to set this to false

     }
    
    /**
     * Thread.
     * This is the recommended way of stopping an active thread as stop() and destroy() are depreciated
     */
     public void run() {
         Thread mythread = this;
         while (destroy == mythread) {
             
             systemRW();
             if (!isSuccess) {
                 cleanUp();
                 return;
             }
             
             backupBuildProp();
             if (!isSuccess) {
                 cleanUp();
                 return;
             }
             
             initSetup();
             if (!isSuccess) {
                 cleanUp();
                 return;
             }
         
             // enable / disable jit compiler
             setEnableDisableJit();
             if (!isSuccess) {
                 cleanUp();
                 return;
             }

             setBuildProp();
             if (!isSuccess) {
                 cleanUp();
                 return;
             }
             
             cleanUp();
             return;
         }
     }
     

     /**
      * Backup build.prop
      */
     private void backupBuildProp() {
         File file = new File("/system/build.prop.orig");
         
         if (file.exists())
             return;
         
         String cmd = "busybox cp /system/build.prop /system/build.prop.orig || echo failure\n";
         
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     }
     
     /**
      * Used to determine if we have a valid prop
      */
     private void initSetup() {
         String cmd = "busybox cat /system/build.prop | sed -n '/^dalvik.vm.execution-mode.*/p' | sed -e 's: ::g' | cut -d: -f2 || echo failure\n";
         
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     }

     /**
      * Enable /  Disable JIT Compiler
      */
     private void setEnableDisableJit() {
         
         Log.i(TAG + "setEnableDisableJit "  + mStdOut.length() + " string " + mStdOut.toString());
         // if we get nothing then we need to add the prop
         if (mStdOut.length() <= 0) {
             addProp();
           
             return;
         }
         
        
      
         String disable = (!mDisabled) ? "jit" : "fast";
         StringBuilder cmd = new StringBuilder();
         cmd.append("busybox cat /system/build.prop | sed -e 's/dalvik.vm.execution-mode.*");
         cmd.append("/dalvik.vm.execution-mode=int:"+disable);
         cmd.append("/g' > /data/local/tmp/build.prop.temp || echo failure\n");
         
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd.toString());
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     }
     
     /**
      * AddProp if it is not in the build.prop
      */
     private void addProp() {
         
         String str = (!mDisabled) ? "jit" : "fast";
         
         String cmd = "busybox echo dalvik.vm.execution-mode=int:"+str+" | cat - /system/build.prop > /data/local/tmp/build.prop.temp || echo failure\n";
         
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     
     }
     
     /**
      * Move temp build prop to /system/build.prop
      */
     private void setBuildProp() {
        String cmd = "busybox cp /data/local/tmp/build.prop.temp /system/build.prop; busybox rm -rf /data/local/tmp/build.prop.temp || echo failure\n";
         
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
    }

     
     
     /**
      * mount /system read/write
      */
     private void systemRW() {

         String cmd = "busybox mount -o remount,rw /system  ||  echo failure\n";
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     }

     /**
      * mount system read-only
      */
     private void systemRO() {

         String cmd = "busybox mount -o remount,ro /system  || echo failure\n";
         try {
             OutputStreamHelper output = new OutputStreamHelper(stdioSetters, cmd);
             output.start();
             output.join();
         } catch (InterruptedException e) {
             isSuccess = false;
             cleanUp();
             return;
         }
     }
     

     /**
      * handler needed to get back to the ui thread
      */
     Handler myHandler = new Handler() {

         @Override
         public void handleMessage(Message msg) {
             Options.dismissProgressDialog();
         }
     };
     
     /**
      * interface used by other threads when they have finished.
      */
     ifaceStdio stdioSetters = new ifaceStdio() {

         @Override
         public void setStdErr(String err) {
             
             // do nothing
         }

         @Override
         public void setStdOut(String out) {
             mStdOut = out;
             //Log.i(TAG + "stdout:"+ mStdOut);
         }

         @Override
         public void setExitStatus(int code) {
             // do nothing
         }

         @Override
         public void setIsSuccess(boolean success) {
             isSuccess = success;
            // Log.i(TAG + "isSuccess:" + isSuccess);
         }

     };
    
}
