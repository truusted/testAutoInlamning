package inlamning;

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

    private WebDriver driver;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------


     //Bygger en fil://-URL som pekar på Register.html, som ligger en katalog ovanför projekt automationTesting/ (där Maven exekverar)

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

    private void fillForm(boolean includeLastName, String password, String confirmPassword) {
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

        driver.findElement(By.id("signupunlicenced_password")).sendKeys(password);
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

    // -----------------------------------------------------------------------
    // Step definitions
    // -----------------------------------------------------------------------

    @Given("I am on the Basketball England registration page")
    public void iAmOnTheRegistrationPage() {
        driver.get(getRegisterUrl());
        Assert.assertTrue(
                "Registration page did not load",
                driver.getTitle().contains("Basketball England"));
    }

    @When("I complete the registration form with valid details")
    public void iCompleteFormWithValidDetails() {
        fillForm(true, "SecurePass1", "SecurePass1");
    }

    @When("I complete the registration form without a last name")
    public void iCompleteFormWithoutLastName() {
        fillForm(false, "SecurePass1", "SecurePass1");
    }

    @When("I complete the registration form with mismatching passwords")
    public void iCompleteFormWithMismatchingPasswords() {
        fillForm(true, "SecurePass1", "DifferentPass2");
    }

    @And("I accept the terms and conditions")
    public void iAcceptTermsAndConditions() {
        clickCheckbox("sign_up_25");                                     // Terms & Conditions
        clickCheckbox("sign_up_26");                                     // Age confirmation
        clickCheckbox("fanmembersignup_agreetocodeofethicsandconduct");  // Code of Ethics
    }

    @And("I do not accept the terms and conditions")
    public void iDoNotAcceptTermsAndConditions() {
        // Acceptera de andra två obligatoriska bekräftelserna så att endast kryssrutan för villkor är den återstående valideringsfelet.
        clickCheckbox("sign_up_26");                                     // Age confirmation
        clickCheckbox("fanmembersignup_agreetocodeofethicsandconduct");  // Code of Ethics
        // sign_up_25 (T&C) is deliberately left unchecked
    }

    @And("I submit the registration form")
    public void iSubmitTheRegistrationForm() {
        driver.findElement(By.name("join")).click();
    }

    @Then("I should be redirected to the success page")
    public void iShouldBeRedirectedToSuccessPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("Success.html"));

        Assert.assertTrue(
                "Expected navigation to Success.html but current URL was: " + driver.getCurrentUrl(),
                driver.getCurrentUrl().contains("Success.html"));
    }

    @Then("I should see the error {string}")
    public void iShouldSeeTheError(String expectedError) {
        // jQuery diskret validering fyller span[data-valmsg-replace='true'] // element med felmeddelandet och ändrar deras klass till // field-validation-error när ett inlämningsförsök misslyckas.
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        // Vänta tills minst ett valideringsavsnitt innehåller synbar text.
        wait.until(d -> {
            List<WebElement> spans = d.findElements(
                    By.cssSelector("span[data-valmsg-replace='true']"));
            return spans.stream().anyMatch(el -> !el.getText().trim().isEmpty());
        });

        List<WebElement> errorSpans = driver.findElements(
                By.cssSelector("span[data-valmsg-replace='true']"));

        boolean errorFound = errorSpans.stream()
                .anyMatch(el -> el.getText().contains(expectedError));

        Assert.assertTrue(
                "Expected validation error '" + expectedError + "' was not found in any error span",
                errorFound);
    }
}
