

package com.frogobox.evpn.util.io.pem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.frogobox.evpn.util.encoders.Base64;


@SuppressWarnings("all")
public class PemWriter
    extends BufferedWriter
{
    private static final int LINE_LENGTH = 64;

    private final int nlLength;
    private char[]  buf = new char[LINE_LENGTH];

    
    public PemWriter(Writer out)
    {
        super(out);

        String nl = System.getProperty("line.separator");
        if (nl != null)
        {
            nlLength = nl.length();
        }
        else
        {
            nlLength = 2;
        }
    }

    
    public int getOutputSize(PemObject obj)
    {
        
        int size = (2 * (obj.getType().length() + 10 + nlLength)) + 6 + 4;

        if (!obj.getHeaders().isEmpty())
        {
            for (Iterator it = obj.getHeaders().iterator(); it.hasNext();)
            {
                PemHeader hdr = (PemHeader)it.next();

                size += hdr.getName().length() + ": ".length() + hdr.getValue().length() + nlLength;
            }

            size += nlLength;
        }

        
        int dataLen = ((obj.getContent().length + 2) / 3) * 4;
        
        size += dataLen + (((dataLen + LINE_LENGTH - 1) / LINE_LENGTH) * nlLength);

        return size;
    }
    
    public void writeObject(PemObjectGenerator objGen)
        throws IOException
    {
        PemObject obj = objGen.generate();

        writePreEncapsulationBoundary(obj.getType());

        if (!obj.getHeaders().isEmpty())
        {
            for (Iterator it = obj.getHeaders().iterator(); it.hasNext();)
            {
                PemHeader hdr = (PemHeader)it.next();

                this.write(hdr.getName());
                this.write(": ");
                this.write(hdr.getValue());
                this.newLine();
            }

            this.newLine();
        }
        
        writeEncoded(obj.getContent());
        writePostEncapsulationBoundary(obj.getType());
    }

    private void writeEncoded(byte[] bytes)
        throws IOException
    {
        bytes = Base64.encode(bytes);

        for (int i = 0; i < bytes.length; i += buf.length)
        {
            int index = 0;

            while (index != buf.length)
            {
                if ((i + index) >= bytes.length)
                {
                    break;
                }
                buf[index] = (char)bytes[i + index];
                index++;
            }
            this.write(buf, 0, index);
            this.newLine();
        }
    }

    private void writePreEncapsulationBoundary(
        String type)
        throws IOException
    {
        this.write("-----BEGIN " + type + "-----");
        this.newLine();
    }

    private void writePostEncapsulationBoundary(
        String type)
        throws IOException
    {
        this.write("-----END " + type + "-----");
        this.newLine();
    }
}
