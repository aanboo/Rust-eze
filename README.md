# Rust-eze Group Hi_Chan Project
```text
+---------------------------------------------------------------------+
| Hi_Chan - Interstate 90 GPS Highway Channel Matcher                  |
| from Idaho to Washington                                            |
| Team: Rust-eze | Lab 6                                              |
| Names: Aaliyah, Martin, Rykeam                                      |
+---------------------------------------------------------------------+

+------------------+--------------------------------------------------+
| Directory        | Description                                      |
+------------------+--------------------------------------------------+ 
| src/             | Java source code modules                         |
| test/            | JUnit 5 test sources (26 Tests Total)            |
| i90_channels.csv | Preprocessed Geohash highway lookup table        |
+------------------+--------------------------------------------------+
```

# Run Instructions for Hi_Chan

```text
1. BUILDING IN INTELLIJ
   - Open the project folder in IntelliJ IDEA.
   - From the top menu, select: Build -> Build Project (or Ctrl+F9).
   - IntelliJ automatically compiles all .java files into out/production/.

2. RUNNING HI_CHAN
   - In the Project tool window, navigate to: src/HiChan.java
   - Set the input arguments before running:
       1. Top menu: Run -> Edit Configurations...
       2. Under Application -> HiChan, locate the "Program arguments" field.
       3. Enter:
          GPSLog_1.csv output.csv
       4. Click Apply, then OK.
   - Right-click HiChan.java and click: Run 'HiChan.main()'
   - Check the project root folder for the generated output.csv file.

3. RUNNING TESTS
   - In the Project tool window, right-click the 'test' directory.
   - Select: Run 'All Tests'
   - Expected Results: 26 passed, 0 failed (exit code 0).
```