No special instructions for running tests: used Eclipse Maven and PITEST as shown in class.

# Reporting
The coverage report can be found in coverage_report.html.
The pitest mutation report can be found in pitest_report

# AI usage disclosure:
Prompts:
- Draft tests for these classes (provided with the game engine and config), do not repeat tests from the other tests (provided with the model, events, and rules tests).
    - Note: this did not result in immediate 100% coverage or mutation kill rate. We read through all the code it produced and then improved        by hand where required
- Add comments to these files
    - AI improved commenting on the files we wrote ourselves
- Walk me through running PIT mutation tests on Eclipse
    - Just Eclipse UI help
- Why are these lines in DungeonFactory: private DungeonFactory() {} not covered in testing? How do I make sure they are covered? (PITEST counted as not covered lines, coverage counted as covered)

