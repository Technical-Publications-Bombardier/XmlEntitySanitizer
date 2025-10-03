import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLCharacterValidator {

    public static String validCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789~#$`\t*';&@_\\,!=:\"<>/+- ?.{}()[]àâçéèêëîïôùûüÿñÀÂÄÇÉÈÊËÎÏÔÙÛÜŸÑ";
    private static String inXml;
    private static boolean verbose = false;
    private static String cirXml = null;
    private static String propertiesFile = "app.properties";

    private static final List<String> warningList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(
                    "Usage: java XMLCharacterValidator [--cir <cirs.xml>] [--properties <app.properties>] [--v] <inputXmlFilePath> ");
            return;
        }

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    System.out.println(
                            "Usage: java XMLCharacterValidator [--cir=<cirs.xml>] [--properties=<app.properties>] [--v] <inputXmlFilePath> ");
                    break;
                case "--cir":
                    if (i + 1 < args.length) {
                        cirXml = args[++i];
                    } else {
                        System.out.println("Error: Expected value after --cir parameter.");
                        return;
                    }
                    break;
                case "--properties":
                    if (i + 1 < args.length) {
                        propertiesFile = args[++i];
                    } else {
                        System.out.println("Error: Expected value after --properties parameter.");
                        return;
                    }
                    break;
                case "--v":
                    verbose = true;
                    break;
                default:
                    if (inXml == null) {
                        inXml = args[i];
                    } else {
                        System.out.println("Error: Unexpected parameter or value.");
                        return;
                    }
                    break;
            }
        }

        if (inXml == null) {
            System.out.println("Error: inputxml is a mandatory parameter.");
            return;
        }

        warningList.add("WARNING_NOT_FOUND");
        warningList.add("APPLIC_NOT_FOUND");
        warningList.add("CAUTION_NOT_FOUND");

        String inputFilePath = inXml;
        Map<String, String> cirMap = new HashMap<>();
        if (cirXml != null) {
            cirMap = buildMapping(cirXml);
        }

        String outputFilePath = getDefaultOutputFileName(inputFilePath);
        String propertiesFilePath = propertiesFile;
        boolean validateOnly = args.length >= 2 && args[args.length - 1].equals("--v");
        int i = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            String line;

            Map<Integer, String> xmlEntityMap = loadXMLProperties(propertiesFilePath);

            while ((line = reader.readLine()) != null) {
                i++;
                if (validateOnly) {
                    if (!isValidXML(line, xmlEntityMap, inputFilePath)) {
                        System.out.println("Source File :[" + inputFilePath + "] " + "Line number:" + i
                                + "  Line content: " + line);
                    }
                } else {
                    sanitizeXML(line, xmlEntityMap, cirMap, i);
                }
                warningOnNotFounds(line);

            }

            reader.close();

            if (!validateOnly) {
                saveSanitizedXML(inputFilePath, outputFilePath, xmlEntityMap, cirMap);
                System.out.println("Sanitized XML saved to: " + outputFilePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, String> loadXMLProperties(String propertiesFilePath) {
        Map<Integer, String> xmlEntityMap = new HashMap<>();

        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(propertiesFilePath));

            Enumeration<?> keys = properties.propertyNames();
            while (keys.hasMoreElements()) {
                String keyStr = (String) keys.nextElement();
                if (keyStr.startsWith("ASCII_")) {
                    int asciiNumber = Integer.parseInt(keyStr.substring(6));
                    String entity = properties.getProperty(keyStr);
                    xmlEntityMap.put(asciiNumber, entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xmlEntityMap;
    }

    public static String sanitizeXML(String text, Map<Integer, String> xmlEntityMap, Map<String, String> cirMap, int lineNum) {
        StringBuilder sanitizedText = new StringBuilder();
        String cirConvertedLine = replaceAttribute(text, cirMap);
        for (int i = 0; i < cirConvertedLine.length(); i++) {
            char c = cirConvertedLine.charAt(i);
            if (validCharacters.indexOf(c) != -1) {
                sanitizedText.append(c);
            } else if (xmlEntityMap.containsKey((int) c)) {
                sanitizedText.append(xmlEntityMap.get((int) c));
            } else {
                System.out.printf("[Warning] Character at line %d not in pre-defined valid set: %c # %d\n", lineNum, c, (int) c);
                sanitizedText.append(c);
            }
        }
        return sanitizedText.toString();
    }

    public static boolean isValidXML(String text, Map<Integer, String> xmlEntityMap, String inputFilePath) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (validCharacters.indexOf(c) == -1) {
                if (validCharacters.indexOf(c) != -1) {

                } else if (xmlEntityMap.containsKey((int) c)) {
                    System.out.println("Character not in pre-defined valid set :" + c + " #" + (int) c
                            + " to_be_replaced : " + xmlEntityMap.get((int) c));
                    return false;
                } else {
                    System.out.println("Source [" + inputFilePath + "] Character not in pre-defined valid set :" + c
                            + " #" + (int) c + " can not find to_be_replaced ");
                    return false;
                }
            }
        }
        return true;
    }

    public static void warningOnNotFounds(String line) {
        for (String entry : warningList) {
            if (line.contains(entry)) {
                System.out.println("[Warning] Find NOT_FOUND in source xml :" + line);
            }
        }

    }

    public static void saveSanitizedXML(String inputFilePath, String outputFilePath, Map<Integer, String> xmlEntityMap,
            Map<String, String> cirMap) {
        int i = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            String line;

            while ((line = reader.readLine()) != null) {
                i++;
                String sanitizedLine = sanitizeXML(line, xmlEntityMap, cirMap, i);
                writer.write(sanitizedLine);
                writer.newLine();
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDefaultOutputFileName(String inputFilePath) {
        int lastDotIndex = inputFilePath.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return inputFilePath.substring(0, lastDotIndex) + "_sanitized" + inputFilePath.substring(lastDotIndex);
        } else {
            return inputFilePath + "_sanitized";
        }
    }

    public static Map<String, String> buildMapping(String filename) throws Exception {
        Map<String, String> map = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filename));
        NodeList list = document.getElementsByTagName("applic");
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String id = element.getAttribute("id");
            String desc = element.getAttribute("desc");
            map.put("app" + desc, id);
        }
        return map;
    }

    public static String replaceAttribute(String line, Map<String, String> map) {
        Pattern pattern = Pattern.compile("=\"(app[^\"]+)\"");
        Matcher matcher = pattern.matcher(line);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String foundKey = matcher.group(1);
            if (map.containsKey(foundKey)) {
                matcher.appendReplacement(result, "=\"" + map.get(foundKey) + "\"");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
