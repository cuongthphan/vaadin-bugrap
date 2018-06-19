Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: invalid email
Given the Login Page
When I enter email field with <email> and password field with <password>
Then I should get the message as <message>

Examples:
|email|password|message|
|cuong@vaadin|123456|Invalid email|

Scenario: wrong email or password
Given the Login Page
When I enter email field with <email> and password field with <password>
Then I should get the message as <message>

Examples:
|email|password|message|
|cuong@vaadin.com|123456|Wrong email or password|

Scenario: successful login
Given the Login Page
When I enter email field with <email> and password field with <password>
Then I should get to the Main Page

Examples:
|email|password|
|developer@bugrap.com|3dacbce532ccd48f27fa62e993067b3c35f094f7|