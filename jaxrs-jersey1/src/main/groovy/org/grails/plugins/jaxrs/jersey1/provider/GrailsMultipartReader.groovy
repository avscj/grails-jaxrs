package org.grails.plugins.jaxrs.jersey1.provider


import groovy.util.logging.Slf4j
import java.io.IOException
import java.lang.annotation.Annotation
import java.lang.reflect.Type

import javax.servlet.http.Part
import javax.servlet.http.HttpServletRequest

import javax.ws.rs.core.Response.Status
import javax.ws.rs.Consumes
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import javax.ws.rs.ext.Providers

import com.sun.jersey.core.header.MediaTypes
import com.sun.jersey.core.header.FormDataContentDisposition
import com.sun.jersey.multipart.BodyPart
import com.sun.jersey.multipart.FormDataBodyPart
import com.sun.jersey.multipart.FormDataMultiPart
import com.sun.jersey.multipart.MultiPart
import com.sun.jersey.spi.CloseableService
import com.sun.jersey.spi.inject.ServerSide
import com.sun.jersey.spi.inject.ConstrainedTo

import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartRequest
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.apache.commons.fileupload.FileItemHeaders
import org.grails.web.util.WebUtils
import org.grails.plugins.jaxrs.jersey1.multipart.GrailsBodyPartEntity

/**
 * Multipart reader server side implementation for Grails similar to 
 * <a href="https://github.com/jersey/jersey-1.x/blob/master/contribs/jersey-multipart/src/main/java/com/sun/jersey/multipart/impl/MultiPartReaderClientSide.java">MultiPartReaderClientSide</a>
 * which uses spring's MultipartRequest to parse the contents.
 * 
 * This is necessary because Grails reads the input stream before it reaches 
 * Jersey, making it return a 400.
 * 
 * @author Alex Stoia
 */
@Slf4j
@Provider
@ConstrainedTo(ServerSide.class)
@Consumes("multipart/*")
class GrailsMultipartReader implements MessageBodyReader<MultiPart> {
    
    /**
     * <P>Injectable helper to look up appropriate {@link Provider}s
     * for our body parts.</p>
     */
    @Context
    final Providers providers
    
    
    @Context
    private final CloseableService closeableService

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
        Annotation[] annotations, MediaType mediaType) {
        return MultiPart.class.isAssignableFrom(type)
    }

    /**
     * <p>Read the entire list of body parts from the Grails request, using the
     * appropriate provider implementation to deserialize each body part's entity.</p>
     *
     * @param type The class of the object to be read (i.e. {@link MultiPart}.class)
     * @param genericType The type of object to be written
     * @param annotations Annotations on the resource method that returned this object
     * @param mediaType Media type (<code>multipart/*</code>) of this entity
     * @param headers Mutable map of HTTP headers for the entire response
     * @param stream Request Input stream, unused
     *
     * @throws java.io.IOException if an I/O error occurs
     * @throws javax.ws.rs.WebApplicationException if an HTTP error response
     *  needs to be produced (only effective if the response is not committed yet)
     */
    @Override
    public MultiPart readFrom(Class<MultiPart> type, Type genericType,
        Annotation[] annotations, MediaType mediaType, 
        MultivaluedMap<String, String> headers,
        InputStream stream) throws IOException, WebApplicationException {
        try {
            return readMultiPart(type, genericType, annotations, mediaType, headers, stream)
        } catch (final IllegalArgumentException e) {
            log.error "Got exception reading multipart", e
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause()
            } else {
                throw new WebApplicationException(e, Status.BAD_REQUEST)
            }
        }
    }

    protected MultiPart readMultiPart(Class<MultiPart> type, Type genericType,
        Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, String> headers,
        InputStream stream) throws IOException, IllegalArgumentException {
        
        boolean formData = false
        MultiPart multiPart
        if (MediaTypes.typeEquals(mediaType, MediaType.MULTIPART_FORM_DATA_TYPE)) {
            multiPart = new FormDataMultiPart()
            formData = true
        } else {
            multiPart = new MultiPart()
        }
        log.debug "Request has form data {}", formData

        multiPart.setProviders(providers)

        MultivaluedMap<String,String> mpHeaders = multiPart.getHeaders()
        for (Map.Entry<String,List<String>> entry : headers.entrySet()) {
            List<String> values = entry.getValue()
            for (String value : values) {
                mpHeaders.add(entry.getKey(), value)
            }
        }

        boolean fileNameFix
        if (!formData) {
            multiPart.setMediaType(mediaType)
            fileNameFix = false
        } else {
            // see if the User-Agent header corresponds to some version of MS Internet Explorer
            // if so, need to set fileNameFix to true to handle issue http://java.net/jira/browse/JERSEY-759
            String userAgent = headers.getFirst(HttpHeaders.USER_AGENT)
            fileNameFix = userAgent != null && userAgent.contains(" MSIE ")
        }
        HttpServletRequest request = WebUtils.retrieveGrailsWebRequest().currentRequest
        if (request instanceof MultipartRequest) {
            for (Map.Entry<String,MultipartFile> fileData : request.fileMap.entrySet()) {
                String fileKey = fileData.key
                MultipartFile fileContent = fileData.value
                
                
                BodyPart bodyPart = formData ? new FormDataBodyPart(fileNameFix) : new BodyPart()
                // Configure providers
                bodyPart.setProviders(providers)
                if (request instanceof MultipartHttpServletRequest) {
                    Part filePart = request.getPart(fileKey)
                    if (filePart) {
                        for (String headerName : filePart.headerNames) {
                            bodyPart.getHeaders().add(headerName, filePart.getHeader(headerName))
                        }
                    } else {
                        log.debug "Part not available, building content disposition from file ${fileContent}"
                        bodyPart.contentDisposition = FormDataContentDisposition.name(fileContent.name).
                            fileName(fileContent.originalFilename).
                            size(fileContent.size).
                            build()
                    }
                }
                bodyPart.setMediaType(MediaType.valueOf(fileContent.getContentType()))
                // Copy data into a BodyPartEntity structure
                bodyPart.setEntity(new GrailsBodyPartEntity(fileContent))
                // Add this BodyPart to our MultiPart
                multiPart.getBodyParts().add(bodyPart)
            }
        } else {
            throw new IllegalArgumentException("Cannot process request of type ${request.getClass()}")
        }
        
        closeableService.add(multiPart)
        return multiPart
    }
}
