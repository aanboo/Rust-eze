# Rust-eze Group Hi_Chan Project
```text
+---------------------------------------------------------------------+
| Hi_Chan - Interstate 90 GPS Highway Channel Matcher                 |
| from Idaho to Washington                                            |
| Team: Rust-eze | Lab 6                                              |
| Names: Aaliyah, Martin, Rykeam                                      |
+---------------------------------------------------------------------+

+------------------+--------------------------------------------------+
| Directory        | Description                                      |
+------------------+--------------------------------------------------+ 
| src/             | Directory containing Java source code files      |
| test/            | Directory JUnit 5 tests for src (26 tests Total) |
| i90_channels.csv | Preprocessed highway data indexed by Geohash     |
| README.md        | Project overview and steps to run Hi_Chan        |
+------------------+--------------------------------------------------+
```

# Run Instructions for Hi_Chan

```text
1. BUILDING IN INTELLIJ
    - Open the root project folder in IntelliJ IDEA.
    - From the top menu, select:
        Build -> Build Project (or Ctrl+F9).

2. CONFIGURATION
    - Go to Run -> Edit Configurations...
    - Under Application, select HiChan
    - In the Program arguments field, enter:
        src/GPSLog_1.csv output.csv
    - Click Apply, then OK.
    
2. RUNNING HI_CHAN
    - Right-click HiChan.java and click:
        Run 'HiChan.main()'
    - If output.csv already exists in the project root,
        you will be prompted to confirm the
        overwrite in the terminal (y/n)
    - Once finished, check the project root folder
        for the generated output.csv file.

3. RUNNING TESTS
    - In the Project tool window,
        right-click the 'test' directory.
    - Select: Run 'All Tests'
    - Expected Results: 26 passed, 0 failed (exit code 0).
```