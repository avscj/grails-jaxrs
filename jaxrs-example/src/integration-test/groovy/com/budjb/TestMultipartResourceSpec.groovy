package com.budjb

import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.ContentType
import grails.test.mixin.integration.Integration
import org.grails.plugins.jaxrs.test.JaxrsIntegrationSpec
import org.grails.plugins.jaxrs.test.JaxrsRequestProperties

@Integration
class TestMultipartResourceSpec extends JaxrsIntegrationSpec {

    def 'Ensure can send a multipart file to the resource'() {
        given: "A file to send"
        File file = new File('src/integration-test/groovy/com/budjb/resources/pdf-sample.pdf')
        
        when: "Sending a file to the web service"
        def response = makeRequest(new JaxrsRequestProperties(method: 'POST', 
                uri: '/api/testMultipart/upload',
                headers: ['Content-Type' : ['multipart/form-data']],
                files: ['file': [file]]))
        def result = new groovy.json.JsonSlurper().parseText(response.bodyAsString)

        then: "The response is correct"
        200 == response.status
        'pdf-sample.pdf' == result.name
        result.size > 0
        'application/pdf' == result.mimeType
    }
    
    def 'Ensure can send multipart files to the resource under the same key'() {
        given: "A file to send"
        File pdfFile = new File('src/integration-test/groovy/com/budjb/resources/pdf-sample.pdf')
        File imageFile = new File('src/integration-test/groovy/com/budjb/resources/image-sample.jpg')
        
        when: "Sending a file to the web service"
        def response = makeRequest(new JaxrsRequestProperties(method: 'POST', 
                uri: '/api/testMultipart/uploadmultiple',
                headers: ['Content-Type' : ['multipart/form-data']],
                files: ['file': [pdfFile, imageFile]]))
        def result = new groovy.json.JsonSlurper().parseText(response.bodyAsString)

        then: "The response is correct"
        200 == response.status
        
        and: "The file datas are correct"
        2 == result.size()
        'pdf-sample.pdf' == result[0].name
        7945 == result[0].size
        'application/pdf' == result[0].mimeType
        'image-sample.jpg' == result[1].name
        89229 == result[1].size
        'image/jpeg' == result[1].mimeType
    }
    
    /**
     * Return the list of additional resources to build the JAX-RS servlet with.
     *
     * @return
     */
    @Override
    List getResources() {
        return []
    }
}
