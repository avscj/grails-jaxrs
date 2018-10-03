// Place your Spring DSL code here
import org.grails.spring.beans.factory.InstanceFactoryBean
beans = {
    println "ADDING mybean!!"

    mybean(InstanceFactoryBean, [
            testMethod : {
                return "bean return"
            }
    ], Object)
}
