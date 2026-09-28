package architecture
import org.junit.jupiter.api.Test
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlinx.serialization.Serializable



class CleanArchitectureTest {
    @Test
    fun `domain package must never depend on the data package`(){
        Konsist.scopeFromProject()
    .assertArchitecture {
            val domain = Layer("Domain", "..domain..")
            val data = Layer("Data", "..data..")
        domain.dependsOnNothing()
        data.dependsOn(domain)
            }
}
    @Test
    fun `domain package must never import third-party libraries`(){
        Konsist.scopeFromPackage("..domain..")
            .files
            .flatMap{it.imports}
            .assertTrue { imports ->
                imports.name.startsWith("domain.") ||
                imports.name.startsWith("kotlin") ||
                imports.name.startsWith("java") }
            }

    @Test
    fun `All classes located in domain usecase must have names ending with UseCase`(){
        Konsist.scopeFromPackage("..domain.usecase..")
            .classes()
            .assertTrue { it.name.endsWith("UseCase") }//
    }
    @Test
    fun `All classes located in domain validator must have names ending with Validator`(){
        Konsist.scopeFromPackage("..domain.validator..")
            .classes()
            .assertTrue { it.name.endsWith("Validator") }
    }
    @Test
    fun `Every UseCase class must define a public function named invoke`() {
        Konsist.scopeFromPackage("..domain.usecase..")
            .classes()
            .assertTrue { useCase ->
                useCase.hasFunction { fn ->
                    fn.name == "invoke" &&
                            fn.hasOperatorModifier &&
                            fn.hasPublicOrDefaultModifier
                }
            }
    }
    @Test
    fun `All DTO classes in data remote dto must end with 'Dto' and be annotated with @Serializable `(){
        Konsist.scopeFromPackage("..data.remote.dto..")
            .classes()
            .assertTrue {
                it.name.endsWith("Dto")&&
               it.hasAnnotationOf(Serializable::class)
            }
    }




    }


