package com.budjb
import io.swagger.annotations.Api

import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.Response
import javax.ws.rs.core.MediaType

import com.sun.jersey.multipart.FormDataParam
import com.sun.jersey.multipart.FormDataBodyPart

import grails.converters.JSON
@Path('/api/testMultipart')
@Api('test')
class TestMultipartResource {
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA )
    @Produces(MediaType.APPLICATION_JSON )
    @Path('/upload')
    Response uploadFile(
        @FormDataParam('file') File file,
        @FormDataParam('file') FormDataBodyPart fileBodyPart) {
        // try with 
        // curl -v -X POST -H "Content-Type: multipart/form-data" -F "file=@src/integration-test/groovy/com/budjb/resources/pdf-sample.pdf" http://localhost:8060/api/testMultipart/upload
        JSON ret = [
            name: fileBodyPart.getContentDisposition().getFileName(),
            contentype: fileBodyPart.getContentDisposition().getType(),
            size: file.size(),
            mimeType: fileBodyPart.getMediaType().toString()
        ] as JSON
        return Response.status(200).entity(ret).build()
    }
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA )
    @Produces(MediaType.APPLICATION_JSON )
    @Path('/uploadmultiple')
    Response uploadMultipleFiles(
        @FormDataParam('file') Collection<FormDataBodyPart> fileBodyParts) {
        // try with 
        // curl -v -X POST -H "Content-Type: multipart/form-data" -F "file=@src/integration-test/groovy/com/budjb/resources/pdf-sample.pdf" -F "file=@src/integration-test/groovy/com/budjb/resources/pdf-sample2.pdf" http://localhost:8060/api/testMultipart/uploadmultiple
        Collection result = []
        fileBodyParts.each { fileBodyPart ->
            File file = fileBodyPart.getValueAs(File)
            Map ret = [
                name: fileBodyPart.getContentDisposition().getFileName(),
                contentype: fileBodyPart.getContentDisposition().getType(),
                creationDate: fileBodyPart.getContentDisposition().getCreationDate(),
                modificationDate: fileBodyPart.getContentDisposition().getModificationDate(),
                readDate: fileBodyPart.getContentDisposition().getReadDate(),
                size: file.size(),
                mimeType: fileBodyPart.getMediaType().toString()
            ]
            result.add(ret)
        }
        return Response.status(200).entity(result as JSON).build()
    }
}