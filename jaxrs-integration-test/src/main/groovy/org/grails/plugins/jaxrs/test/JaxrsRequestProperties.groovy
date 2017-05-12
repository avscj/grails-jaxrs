package org.grails.plugins.jaxrs.test

import org.apache.commons.lang.StringUtils
import org.grails.plugins.jaxrs.core.JaxrsUtil
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartHttpServletRequest
import org.springframework.mock.web.MockMultipartFile

import javax.servlet.ServletContext
import javax.ws.rs.core.HttpHeaders

class JaxrsRequestProperties {
    /**
     * URI of the request.
     */
    String uri

    /**
     * HTTP method of the request.
     */
    String method

    /**
     * Body of the request in bytes.
     */
    byte[] body

    /**
     * Content type of the request.
     */
    String contentType

    /**
     * Requested content type of the response.
     */
    String accept

    /**
     * Request headers.
     */
    Map<String, List<Object>> headers = [:]

    /**
     * Character encoding.
     */
    String characterEncoding = 'UTF-8'
    
    /**
     * Files to be sent, only considered for multipart requests
     */
    Map<String, File> files = [:]
    
    /**
     * Constant for multipart request type
     */ 
    
    private static final String MULTIPART = 'multipart'

    /**
     * Add headers to the request.
     *
     * @param headers
     */
    void addHeaders(Map<String, Object> headers) {
        headers.clear()

        headers.each { entry ->
            if (entry.value instanceof List) {
                this.headers.put(entry.key, entry.value as List)
            }
            else {
                this.headers.put(entry.key, [entry.value])
            }
        }
    }

    /**
     * Creates an HTTP servlet request with the properties of this object.
     *
     * @param servletContext
     * @return
     */
    MockHttpServletRequest createServletRequest(ServletContext servletContext) {
        MockHttpServletRequest httpServletRequest
        if (isMultipart()) {
            httpServletRequest = new MockMultipartHttpServletRequest(servletContext)
            files.each { String name, File file ->
                httpServletRequest.addFile(new MockMultipartFile(name, 
                        file.name, file.toURL().openConnection().getContentType(), file.newInputStream()))       
            }
        } else {
            httpServletRequest = new MockHttpServletRequest(servletContext)
        }
        
        httpServletRequest.characterEncoding = characterEncoding

        URI uri = new URI(uri)
        httpServletRequest.setAttribute(JaxrsUtil.REQUEST_URI_ATTRIBUTE_NAME, uri.path)
        if (uri.query) {
            httpServletRequest.queryString = uri.query
        }

        httpServletRequest.method = method
        httpServletRequest.content = body

        headers.each { entry ->
            if (entry.value instanceof Collection) {
                entry.value.each {
                    httpServletRequest.addHeader(entry.key, it)
                }
            }
            else {
                httpServletRequest.addHeader(entry.key, entry.value)
            }
        }

        httpServletRequest.setContentType(contentType)

        if (accept) {
            httpServletRequest.addHeader('Accept', accept)
        }

        if (body?.size()) {
            String existingContentLength = httpServletRequest.getHeader(HttpHeaders.CONTENT_LENGTH)
            if (StringUtils.isBlank(existingContentLength)) {
                httpServletRequest.addHeader(HttpHeaders.CONTENT_LENGTH, body.size())
            }
        }

        return httpServletRequest
    }
    
    private boolean isMultipart() {
        if (contentType && contentType.toLowerCase().startsWith(MULTIPART)) {
            return true
        } else {
            Object headerContentType =  headers.get('Content-Type')
            if (headerContentType instanceof String) {
                return headerContentType && headerContentType.toLowerCase().startsWith(MULTIPART)
            } else if (headerContentType instanceof Collection) {
                return headerContentType.find { it.toLowerCase().startsWith(MULTIPART) } != null
            }
        }
    }
}
