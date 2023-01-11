Feature: Ordering Cookies

  This feature supports the way a Customer can order cookies through a cart

  Background:
    Given a customer named "Maurice" with credit card "1234896983"

  Scenario: The cart is empty by default
    When "Maurice" asks for his cart contents
    Then there is 0 item inside the cart

  Scenario: adding cookies to a cart
    When "Maurice" orders 1 x "CHOCOLALALA"
    And "Maurice" asks for his cart contents
    Then there is 1 item inside the cart
    And the cart contains the following item: 1 x "CHOCOLALALA"

  Scenario: Ordering multiple cookies
    When "Maurice" orders 1 x "CHOCOLALALA"
    And "Maurice" orders 1 x "SOO_CHOCOLATE"
    And "Maurice" asks for his cart contents
    Then there are 2 items inside the cart
    And the cart contains the following item: 1 x "CHOCOLALALA"
    And the cart contains the following item: 1 x "SOO_CHOCOLATE"

  Scenario: Modifying the number of cookies inside an order
    When "Maurice" orders 2 x "CHOCOLALALA"
    And "Maurice" orders 3 x "DARK_TEMPTATION"
    And "Maurice" orders 3 x "CHOCOLALALA"
    And "Maurice" asks for his cart contents
    Then there are 2 items inside the cart
    And the cart contains the following item: 5 x "CHOCOLALALA"
    And the cart contains the following item: 3 x "DARK_TEMPTATION"

  Scenario: Changing mind while ordering cookies
    When "Maurice" orders 7 x "CHOCOLALALA"
    And "Maurice" decides not to buy 2 x "CHOCOLALALA"
    And "Maurice" asks for his cart contents
    Then there is 1 item inside the cart
    And the cart contains the following item: 5 x "CHOCOLALALA"

  Scenario: Getting the right price for a given cart
    When "Maurice" orders 5 x "CHOCOLALALA"
    And "Maurice" orders 3 x "DARK_TEMPTATION"
    Then the price of "Maurice"'s cart is equals to 12.20

  Scenario: paying a cart with several cookies to a cart
    When "Maurice" orders 5 x "CHOCOLALALA"
    And "Maurice" orders 3 x "DARK_TEMPTATION"
    Then "Maurice" validates the cart and pays through the bank
    And the order amount is equals to 12.20
    And the order status is "IN_PROGRESS"
    And "Maurice" asks for his cart contents
    And there is 0 item inside the cart