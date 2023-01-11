Feature: Bad Ordering

  This feature supports the way Customer actions can lead to a bad ordering process

  Background:
    Given a bad customer

  Scenario: validating an empty cart
    When He validates an empty cart
    Then the order is not created


