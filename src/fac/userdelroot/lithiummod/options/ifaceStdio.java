package fac.userdelroot.lithiummod.options;

public interface ifaceStdio {
    
    public void setStdErr(String err);
    public void setStdOut(String out);
    public void setExitStatus(int code);
    public void setIsSuccess(boolean success);

}
