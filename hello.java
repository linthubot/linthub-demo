function main(a, b) {
  switch (cond) {
  case "one":
    doSomething(); // JSHint will warn about missing 'break' here.
  case "three":
    doSomethingElse();
  case "two":

    doSomethingElse();
  }
  return a == null;
}