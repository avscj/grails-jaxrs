package org.grails.plugins.jaxrs.jersey1

import grails.plugins.Plugin
import org.grails.plugins.jaxrs.core.ScanningResourceRegistrar
import org.grails.plugins.jaxrs.swagger.BeanConfigFactoryBean
import org.grails.plugins.jaxrs.swagger.SwaggerInitializationUtil
import org.grails.plugins.jaxrs.jersey1.provider.GrailsMultipartReader

class JaxrsJersey1GrailsPlugin extends Plugin {
    /**
     * Version of Grails the plugin is meant for.
     */
    def grailsVersion = "3.0 > *"

    /**
     * Plugin title.
     */
    def title = "JAX-RS Jersey 1.x Implementation Plugin"

    /**
     * Plugin author.
     */
    def author = "Bud Byrd"

    /**
     * Author email address.
     */
    def authorEmail = "bud.byrd@gmail.com"

    /**
     * Plugin description.
     */
    def description = 'A plugin that provides the Jersey 1.x implementation of the JAX-RS plugin.'

    /**
     * URL to the plugin's documentation.
     */
    def documentation = 'https://budjb.github.io/grails-jaxrs/3.x/latest/'

    /**
     * Project license.
     */
    def license = "APACHE"

    /**
     * Location of the plugin's issue tracker.
     */
    def issueManagement = [url: 'https://github.com/budjb/grails-jaxrs/issues']

    /**
     * Online location of the plugin's browseable source code.
     */
    def scm = [url: 'https://github.com/budjb/grails-jaxrs']

    /**
     * Register Spring beans.
     *
     * @return
     */
    Closure doWithSpring() {
        { ->
            jaxrsServletFactory(JerseyServletFactory) { bean ->
                bean.autowire = true
            }

            if (SwaggerInitializationUtil.isSwaggerEnabled(grailsApplication)) {
                'swaggerResourceRegistrar'(ScanningResourceRegistrar, 'io.swagger.jaxrs.listing')
                'swaggerConfig'(BeanConfigFactoryBean) { bean ->
                    bean.autowire = true
                }
            }
            
            "${GrailsMultipartReader.name}"(GrailsMultipartReader)
        }
    }
}
