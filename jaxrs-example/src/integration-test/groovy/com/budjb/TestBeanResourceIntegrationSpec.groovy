package com.budjb

import org.grails.spring.beans.factory.InstanceFactoryBean
import grails.testing.mixin.integration.Integration
import org.grails.plugins.jaxrs.test.JaxrsRequestProperties
import org.grails.plugins.jaxrs.test.JaxrsIntegrationSpec
import grails.spring.BeanBuilder

@Integration
class TestBeanResourceIntegrationSpec extends JaxrsIntegrationSpec {

    interface MyBean {
        String testMethod()
    }

    private def mybean = Mock(MyBean)

    @Override
    void doExtraSetup() {
        BeanBuilder bb = new BeanBuilder(grailsApplication.getParentContext(), new GroovyClassLoader(grailsApplication.getClassLoader()))

        bb.beans {
            mybean(InstanceFactoryBean, mybean, MyBean) { bean ->
                bean.autowire = 'byName'
            }
        }
        bb.registerBeans(grailsApplication.getMainContext())
    }

    @Override
    List getResources() {
        return [TestBeanResource]
    }

    def "Execute a GET request"() {
        when:
        def response = makeRequest(new JaxrsRequestProperties(
                uri: '/api/testbean/mybean',
                method: 'GET'
        ))

        then: 'Correct methods called on bean'
        1 * mybean.testMethod() >> 'test bean return'

        then:
        response.status == 200
        response.bodyAsString == 'test bean return'
    }

}
