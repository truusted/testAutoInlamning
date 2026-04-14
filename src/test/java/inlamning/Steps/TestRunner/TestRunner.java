package inlamning.Steps.TestRunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "C:\\Users\\JohnP\\IdeaProjects\\testAuto\\src\\test\\java\\inlamning\\Steps\\Features\\Features.feature",
        glue = "com.basketballengland.steps",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber-report.json"
        }
)
public class TestRunner {
}
