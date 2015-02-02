# Linthub.io demo repository

Be free to fork this repository, do some updates and create a pull request. Our linthubot will automatically check the code for known code quality issues.

Only the files included in the commit will be checked, so for example add a hello.java.

**test.js**
```
function main(a, b) {
  switch (cond) {
  case "one":
    doSomething(); // JSHint will warn about missing 'break' here.
  case "three":
    doSomethingElse();
  case "two":
    
    doSomethingElse();
  }
  
// test for deleting old comments. 


  return a == null;
}
```

An example pull request with line comments can be found at: https://github.com/linthubot/linthub-demo/pull/1.

For any questions, contact us at @dutchcoders or hello@linthub.io.
