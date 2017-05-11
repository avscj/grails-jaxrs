package org.grails.plugins.jaxrs.jersey1.multipart

import org.springframework.core.io.InputStreamSource
import com.sun.jersey.multipart.BodyPartEntity

/**
 * Implementation of BodyPartEntity which uses a Spring InputStreamSource
 *
 * @author Alex Stoia
 */
class GrailsBodyPartEntity extends BodyPartEntity {
    
    private InputStream stream
    
    public GrailsBodyPartEntity(InputStreamSource source) {
        super(null)
        this.stream = source.inputStream
    }
    
    @Override
    public InputStream getInputStream() {
        return stream
    }


    @Override
    public void cleanup() {
        stream.close()
    }
}
