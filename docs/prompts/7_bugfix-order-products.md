/speckit-bug-assess
When an order is created, product ids are not validated, making possible to create an order with unexisting products. 

---
then:

/speckit-bug-fix slug=order-product-validation

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?

---
then:

Update the http request files under docs/running/requests to include this error case.