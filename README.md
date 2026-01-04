# JSON/XML Converter

Hey! This is a simple desktop app that converts JSON to XML and XML to JSON. Pretty straightforward.

## What it does

Basically you can paste JSON or XML into the app, click a button, and it converts it to the other format. You can also load files and save the output.

The cool part is it has two modes:
- **Local mode** - works offline, uses Java libraries
- **API mode** - shows how to work with web APIs using HTTP requests

My professor wanted us to implement both approaches so we could learn the difference.

## How to run it

You need Java 11 or newer and Maven installed.

```bash
git clone https://github.com/Hatimmz25/xml-json-converter
cd json-xml-converter
mvn clean javafx:run
```

That's it. The app should open up.

## Using the app

1. Pick a mode at the top (Local or API)
2. Paste your JSON or XML in the left side, or click "Load File"
3. Hit the convert button (JSON → XML or XML → JSON)
4. Your converted text shows up on the right
5. Save it if you want

Super simple.


## The two modes explained

**Local Mode** - This is the default. Everything happens on your computer using Java libraries (org.json and DOM Parser). It's fast and you don't need internet.

**API Mode** - This one simulates sending data to a web API. I built it to show how HTTP requests work. It has a small delay to mimic network stuff and prints info to the console so you can see what's happening. The actual conversion still happens locally but it demonstrates the API pattern properly.

## What I learned

This project taught me:
- How to build GUIs with JavaFX
- Working with JSON and XML in Java
- Making HTTP requests
- File I/O operations
- Using Maven for builds

## Files

- `ConverterApp.java` - the main GUI stuff
- `FileConverter.java` - handles local conversion
- `ApiConverter.java` - handles the API mode
- `pom.xml` - Maven config

## Requirements

- Java 11+
- Maven
- That's pretty much it

