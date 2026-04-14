package inlamning.Steps.Steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class RegistrationSteps {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String getRegisterUrl() {
        String absolutePath = Paths.get("")
                .toAbsolutePath()
                .getParent()
                .resolve("Register.html")
                .toAbsolutePath()
                .toString();
        // På Unix börjar sökvägen redan med '/', vilket ger file:///home/...
        return "file://" + absolutePath;
    }

    private void clickCheckbox(String id) {
        WebElement checkbox = driver.findElement(By.id(id));
        if (!checkbox.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
        }
    }

    // Fyller i alla vanliga fält. När {@code includeLastName} är falskt lämnas fältet Efternamn tomt för att simulera det negativa testfallet.

    private void fillForm(boolean includeLastName, String confirmPassword) {
        driver.findElement(By.id("member_firstname")).sendKeys("John");

        if (includeLastName) {
            driver.findElement(By.id("member_lastname")).sendKeys("Smith");
        }

        driver.findElement(By.id("member_emailaddress")).sendKeys("john.smith@example.com");
        driver.findElement(By.id("member_confirmemailaddress")).sendKeys("john.smith@example.com");

        // Födelsedatumfältet använder en anpassad jQuery-datumväljare; ställ in dess värde via JavaScript och skicka en change-händelse så att widgeten registrerar valet.
        WebElement dobField = driver.findElement(By.id("dp"));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('change'));",
                dobField, "01/01/1990");

        driver.findElement(By.id("signupunlicenced_password")).sendKeys("SecurePass1");
        driver.findElement(By.id("signupunlicenced_confirmpassword")).sendKeys(confirmPassword);
    }

    // -----------------------------------------------------------------------
    // Hooks
    // -----------------------------------------------------------------------

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Krävs för att Selenium ska kunna ladda lokala filresurser (file://).
        options.addArguments("--allow-file-access-from-files");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private WebDriver driver;

    // -----------------------------------------------------------------------
    // Step definitions
    // -----------------------------------------------------------------------

    @Given("I am on the Basketball England registration page")
    public void i_am_on_the_basketball_england_registration_page() {
        driver.get(getRegisterUrl());
    }

    @When("I complete the registration form with valid details")
    public void i_complete_the_registration_form_with_valid_details() {
        fillForm(true, "SecurePass1");
    }

    @When("I complete the registration form without a last name")
    public void i_complete_the_registration_form_without_a_last_name() {
        fillForm(false, "SecurePass1");
    }

    @When("I complete the registration form with mismatching passwords")
    public void i_complete_the_registration_form_with_mismatching_passwords() {
        fillForm(true, "DifferentPass2");
    }

    @And("I accept the terms and conditions")
    public void i_accept_the_terms_and_conditions() {
        clickCheckbox("sign_up_25");                                     // Terms & Conditions
        clickCheckbox("sign_up_26");                                     // Age confirmation
        clickCheckbox("fanmembersignup_agreetocodeofethicsandconduct");  // Code of Ethics
    }

    @And("I do not accept the terms and conditions")
    public void i_do_not_accept_the_terms_and_conditions() {
        // Acceptera de andra två obligatoriska bekräftelserna så att endast kryssrutan för villkor är den återstående valideringsfelet.
        clickCheckbox("sign_up_26");                                     // Age confirmation
        clickCheckbox("fanmembersignup_agreetocodeofethicsandconduct");  // Code of Ethics
    }
    @And("I submit the registration form")
    public void i_submit_the_registration_form() {
        driver.findElement(By.name("join")).click();

    }

    @Then("I should be redirected to the success page")
    public void i_should_be_redirected_to_the_success_page() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("Success.html"));

        Assert.assertTrue(
                "Expected navigation to Success.html but current URL was: " + driver.getCurrentUrl(),
                driver.getCurrentUrl().contains("Success.html"));
    }
    @Then("I should see the error {string}")
    public void i_should_see_the_error(String expectedError) {
        Assert.assertFalse(
                "Form was submitted successfully when it should have failed",
                driver.getCurrentUrl().contains("Success.html")
        );
    }
}
