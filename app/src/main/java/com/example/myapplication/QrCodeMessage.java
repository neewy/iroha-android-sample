package com.example.myapplication;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

public class QrCodeMessage implements Serializable {

    private final static long serialVersionUID = 1;

    public String accountName;
    public String numberOfAssets;


    public QrCodeMessage(String accountName, String numberOfAssets) {
        this.accountName = accountName;
        this.numberOfAssets = numberOfAssets;
    }

    /** Write the object to a Base64 string. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String toString(Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Read the object from Base64 string. */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
}
