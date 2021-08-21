package org.jna;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JNASockOpt {

    private static Logger logger = LoggerFactory.getLogger(JNASockOpt.class);
    private static Field fdField;
   
    static {
    	Native.register("c");
        try {
            fdField = FileDescriptor.class.getDeclaredField("fd");
            fdField.setAccessible(true);
        } catch (Exception ex) {
            fdField = null;
        }
    }

    private JNASockOpt() {
    }

    public static int getInputFd(Socket s) {
        try {
            FileInputStream in = (FileInputStream)s.getInputStream();
            FileDescriptor fd = in.getFD();
            return fdField.getInt(fd);
        } catch (Exception e) { } 
        return -1; 
    }   
 
    public static int getOutputFd(Socket s) {
        try {
            FileOutputStream in = (FileOutputStream)s.getOutputStream();
            FileDescriptor fd = in.getFD();
            return fdField.getInt(fd);
        } catch (Exception e) { } 
        return -1; 
    }   
 
    public static int getFd(Socket s) {
    	int fd = getInputFd(s);
    	if (fd != -1)
    		return fd;
    	return getOutputFd(s);
    }

    private static native int setsockopt(int fd, int level, int option_name, Pointer option_value, int option_len) throws LastErrorException;
    private static native int getsockopt(int fd, int level, int option_name, Pointer option_value, Pointer option_len) throws LastErrorException;
    public static native String strerror(int errnum);

    public static int getSockOpt (Socket socket, JNASockOptionLevel level, JNASockOption option) throws IOException {
        Integer option_value = 1;
        Integer option_len = Integer.BYTES;

        if (socket == null)
            throw new IOException("Null socket");

        int fd = getFd(socket);
        if (fd == -1)
            throw new IOException("Bad socket FD");

        JNASockOptionDetails instance = JNASockOptionDetails.getInstance();
        IntByReference val = new IntByReference(option_value);
        IntByReference optlen = new IntByReference(option_len);
        int lev = instance.getLevel(level);
        int opt = instance.getOption(option);
        logger.debug("lev=" + lev);
        logger.debug("opt=" + opt);
        try {
            getsockopt(fd, lev, opt, val.getPointer(), optlen.getPointer());
            logger.debug("val=" + val.getValue());
            logger.debug("len=" + optlen.getValue());
        } catch (LastErrorException ex) {
            throw new IOException("getSockOpt: " + strerror(ex.getErrorCode()));
        }
        return val.getValue();
    }

    public static void setSockOpt (Socket socket, JNASockOptionLevel level, JNASockOption option, int option_value) throws IOException {
    	if (socket == null)
    		throw new IOException("Null socket");

    	int fd = getFd(socket);
    	if (fd == -1)
    		throw new IOException("Bad socket FD");

        JNASockOptionDetails instance = JNASockOptionDetails.getInstance();
        logger.debug("option_value = " + option_value);
        IntByReference val = new IntByReference(option_value);
        logger.debug("option_value by ref = " + val.getValue());

        int lev = instance.getLevel(level);
    	int opt = instance.getOption(option);
    	try {
    	    setsockopt(fd, lev, opt, val.getPointer(), Integer.BYTES);
    	} catch (LastErrorException ex) {
    	    throw new IOException("setSockOpt: " + strerror(ex.getErrorCode()));
    	}
    }
}
