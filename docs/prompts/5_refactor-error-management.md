/speckit.specify

Refactor error management in controllers:

- Replace error handling controllers (OrderExceptionHandler, ProductExceptionHandler) with Micronaut ExceptionHandler classes. 
- Use one exception handler per controller, keep this behavior for future features.


---
then:
/speckit-plan
/speckit-tasks
/speckit-implement

When you're done, write a journal entry in docs/journal with your process to implement this feature: did you find any bugs? did you have to fix any issue found in static code analysis? did you need to add tests to meet minimum coverage?