package com.budjb

import org.grails.spring.beans.factory.InstanceFactoryBean
import grails.testing.mixin.integration.Integration
import grails.testing.spring.AutowiredTest
import org.grails.plugins.jaxrs.test.JaxrsRequestProperties
import org.grails.plugins.jaxrs.test.JaxrsIntegrationSpec

@Integration
class TestBeanResourceIntegrationSpec extends JaxrsIntegrationSpec implements AutowiredTest {

    interface MyBean {
        String testMethod()
    }
    MyBean myBean

    Closure doWithSpring() {
        return {
            mybean(InstanceFactoryBean, Mock(MyBean), MyBean)
        }

    }

    @Override
    List getResources() {
        return [TestBeanResource]
    }

    def "Execute a GET request"() {
        when:
        def response = makeRequest(new JaxrsRequestProperties(
                uri: '/testbean/mybean',
                method: 'GET'
        ))

        then: 'Correct methods called on bean'
        1 * mybean.testMethod() >> 'test bean return'

        then:
        response.status == 200
        response.bodyAsString == 'test bean return'
    }

}
