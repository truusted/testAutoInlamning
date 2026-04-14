Feature:Basketball England Supporter Account Registration
As a basketball enthusiast
I want to register for a Basketball England supporter account
So that I can access member benefits and stay connected with the sport

Background:
Given I am on the Basketball England registration page

Scenario: Successful account creation with all valid details
When I complete the registration form with valid details
And I accept the terms and conditions
And I submit the registration form
Then I should be redirected to the success page

Scenario: Registration fails when last name is missing
When I complete the registration form without a last name
And I accept the terms and conditions
And I submit the registration form
Then I should see the error "Last Name is required"

Scenario: Registration fails when passwords do not match
When I complete the registration form with mismatching passwords
And I accept the terms and conditions
And I submit the registration form
Then I should see the error "Password did not match"

Scenario: Registration fails when terms and conditions are not accepted
When I complete the registration form with valid details
And I do not accept the terms and conditions
And I submit the registration form
Then I should see the error "You must confirm that you have read and accepted our Terms and Conditions"