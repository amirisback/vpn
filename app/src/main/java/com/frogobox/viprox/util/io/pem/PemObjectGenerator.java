

package com.frogobox.viprox.util.io.pem;

public interface PemObjectGenerator
{
    PemObject generate()
        throws PemGenerationException;
}
