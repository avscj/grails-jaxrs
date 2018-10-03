package com.budjb

import io.swagger.annotations.Api

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * @author Alex Stoia
 */
@Path('/api/testbean')
@Api('testbean')
class TestBeanResource {
    /**
     * Some injected bean
     */
    def mybean

    @GET
    @Produces([MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML])
    @Path('/mybean')
    Response mybean() {
        // run with :
        // curl -i -X GET http://localhost:8080/api/testbean/mybean
        String result = mybean.testMethod()
        return Response.ok(result).build()
    }
}