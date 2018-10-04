// Place your Spring DSL code here
import org.grails.spring.beans.factory.InstanceFactoryBean
beans = {
    mybean(InstanceFactoryBean, [
            testMethod : {
                return "bean return"
            }
    ], Object)
}
