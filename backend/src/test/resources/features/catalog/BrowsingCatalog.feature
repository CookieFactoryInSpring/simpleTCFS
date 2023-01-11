Feature: Browsing Catalog

  This feature simply browses the catalog of Cookie recipes

  Scenario: The catalog contains 3 cookies
    When one check the catalog contents
    Then there are 3 items in it

