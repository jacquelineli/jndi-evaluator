package com.pitsu.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;


public class JndiEvaluator {
	
	public static void main(String[] args) {
		new JndiEvaluator().execute(args);
	}
	
	public void execute(String[] args) {
		try {
			if(args.length > 0) {
				if(args[0].equals("--dup")) {
					processDuplicateAnalysis(args);
				} else if(args[0].equals("--diff")) {
					processDifferencesAnalysis(args);
				} else if(args[0].equals("--sec")) {
					processSecurityAnalysis(args);
				} else {
					printUsage();
				}
			} else {
				printUsage();
			}
		} catch(FileNotFoundException e) {
			log("File(s) not found: Please input valid file(s)", true);
		} catch(XMLStreamException e) {
			log("File parsing exception: " + e.getCause(), true);
		} catch(Exception e) {
			log("Exception occurred during program: " + e.getCause(), true, e);
		}
	}
	
	public void processDuplicateAnalysis(String[] args) throws XMLStreamException, FileNotFoundException {
		if(args.length == 2) {
			Collection<Property> properties = parse(args[1]);
			Map<String, Collection<Property>> duplicates = findDuplicates(properties);
			
			if(duplicates.isEmpty()) {
				System.out.println("No duplicates found.");
			} else {
				Printer.printDuplicates(duplicates);
			}
		} else {
			System.out.println("Please enter 1 file for duplicate analysis");
		}
	}
	
	public Collection<Property> parse(String file) throws FileNotFoundException, XMLStreamException {
		Property p = null;
		String key = null;
		StringBuilder value = new StringBuilder();
		int lineNumber = 0;
		List<Property> properties = new LinkedList<Property>();
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(new File(file)));
		
		while(reader.hasNext()) {
			int event = reader.next();
			
			switch(event) {
				case XMLStreamConstants.START_ELEMENT: {
					p = new Property();
					
					if("binding".equals(reader.getLocalName())) {
						key = reader.getAttributeValue(0);
						final Location location = reader.getLocation();
						lineNumber = location != null ? location.getLineNumber() : -1;
					}
					break;
				} case XMLStreamConstants.CHARACTERS: {
					value.append(reader.getText());
					break;
				} case XMLStreamConstants.END_ELEMENT: {
					switch(reader.getLocalName()) {
						case "binding": {
							break;
						} case "value": {
							p.setFileName(file);
							p.setKey(key);
							p.setLineNumber(lineNumber);
							p.setValue(value.toString().trim());
							value.setLength(0);
							properties.add(p);
							break;
						}
					}
					break;
				}
			}
		}
		reader.close();
		return properties;
	}
	
	public Map<String, Collection<Property>> findDuplicates(Collection<Property> properties) {
		Map<String, Collection<Property>> duplicates = new HashMap<String, Collection<Property>>();
		Set<String> duplicateKeys = new HashSet<String>();
		// Populate the duplicate "detector" map
		for(Property p: properties) {
			Collection<Property> stored = duplicates.get(p.getKey());
			// If key did not return collection, first occurrence
			if(stored == null) {
				stored = new LinkedList<Property>();
			} else {
				duplicateKeys.add(p.getKey());
			}
			stored.add(p);
			duplicates.put(p.getKey(), stored);
		}
		duplicates.keySet().removeIf(i -> !duplicateKeys.contains(i));
		return duplicates;
	}
	
	public void processDifferencesAnalysis(String args[]) throws FileNotFoundException, XMLStreamException {
		if(args.length == 3) {
			String file1 = args[1];
			String file2 = args[2];
			Collection<Property> properties1 = parse(args[1]);
			Collection<Property> properties2 = parse(args[2]);
			int matchingKeys = countMatchingKeys(properties1, properties2);
			Collection<Property> differentProperties1 = findDifferences(properties1, properties2);
			Collection<Property> differentProperties2 = findDifferences(properties2, properties1);
			Collection<Property> differentPropertyValues = valueDifferences(properties1, properties2);
			Map<String, Collection<Property>> valueDifferencesMap = makeValueDifferencesMap(differentPropertyValues);
			String duplicateWarning = checkDuplicates(properties1, properties2);
			Printer.printDifferenceSummary(file1, file2, properties1, properties2, matchingKeys, duplicateWarning);
			Printer.printMissingProperties(file1, file2, differentProperties1, differentProperties2);
			Printer.printValueDifferences(differentPropertyValues, matchingKeys, valueDifferencesMap);
		} else {
			System.out.println("Please enter 2 files for difference analysis");
		}
	}
	
	public String checkDuplicates(Collection<Property> properties1, Collection<Property> properties2) {
		boolean hasDuplicates1 = hasDuplicates(properties1);
		boolean hasDuplicates2 = hasDuplicates(properties2);
		String fileName1 = fileName(properties1);
		String fileName2 = fileName(properties2);
		StringBuilder sb = new StringBuilder();
		
		if(hasDuplicates1 == true || hasDuplicates2 == true) {
			sb.append("Warning duplicates found in ");
			if(hasDuplicates1 = true) {
				sb.append(fileName1);
				if(hasDuplicates2 == true) {
					sb.append(", " + fileName2);
				}
			} else if(hasDuplicates2 == true) {
				sb.append(fileName2);
				if(hasDuplicates1 == true) {
					sb.append(", " + fileName1);
				}
			}
		}
		return sb.toString();
	}
	
	public boolean hasDuplicates(Collection<Property> properties) {
		boolean containsDuplicates = false;
		Map<String, Collection<Property>> checkDuplicates = findDuplicates(properties);
		
		if(checkDuplicates.size() != 0) {
			containsDuplicates = true;
		}
		return containsDuplicates;
	}
	
	public String fileName(Collection<Property> properties) {
		String fileName = "";
		
		for(Property p: properties) {
			fileName = p.getFileName();
		}
		return fileName;
	}
	
	public Collection<Property> findDifferences(Collection<Property> properties1, Collection<Property> properties2) {
		List<Property> differentKeys = new LinkedList<Property>();
		List<String> matchingKeys = new LinkedList<String>();
		
		for(Property p1: properties1) {
			for(Property p2: properties2) {
				if(!matchingKeys.contains(p1.getKey())) {
					if(!p1.getKey().equals(p2.getKey())) {
						if(!differentKeys.contains(p1)) {
							differentKeys.add(p1);
						}
					} else {
						matchingKeys.add(p1.getKey());
						if(differentKeys.contains(p1)) {
							differentKeys.remove(p1);
						}
					}
				}
			}
		}
		return differentKeys;
	}
	
	public Map<String, Collection<Property>> makeValueDifferencesMap(Collection<Property> properties) {
		Map<String, Collection<Property>> valueDifferencesMap = new HashMap<String, Collection<Property>>();
		for(Property p: properties) {
			Collection<Property> stored = valueDifferencesMap.get((p.getKey()));
			if(stored == null) {
				stored = new LinkedList<Property>();
			}
			stored.add(p);
			valueDifferencesMap.put(p.getKey(), stored);
		}
		return valueDifferencesMap;
	}
	
	public int countMatchingKeys(Collection<Property> properties1, Collection<Property> properties2) {
		List<String> matchingKeys = new LinkedList<String>();
		
		for(Property p1: properties1) {
			for(Property p2: properties2) {
				if(!matchingKeys.contains(p1.getKey())) {
					if(p1.getKey().equals(p2.getKey())) {
						matchingKeys.add(p1.getKey());
					}
				}
			}
		}
		return matchingKeys.size();
	}
	
	public Collection<Property> valueDifferences(Collection<Property> properties1, Collection<Property> properties2) {
		List<Property> differentValues = new LinkedList<Property>();
		
		for(Property p1: properties1) {
			for(Property p2: properties2) {
				if(p1.getKey().equals(p2.getKey()) && !p1.getValue().equals(p2.getValue())) {
					if(!differentValues.contains(p1)) {
						differentValues.add(p1);
					} if(!differentValues.contains(p2)) {
						differentValues.add(p2);
					}
				}
			}
		}
		return differentValues;
	}
	
	public void processSecurityAnalysis(String args[]) throws IOException, XMLStreamException {
		if(args.length == 4) {
			String fileName = args[1];
			List<String> securityKeys = read(args[3]);
			Collection<Property> securityProperties = parse(args[1]);
			boolean hasDuplicates = hasDuplicates(securityProperties);
			Collection<String> keysNotFound = keysNotFound(securityProperties, securityKeys);
			List<String> securityPasswords = getSecurityPasswords(securityProperties, securityKeys);
			
			replacePasswords(args[1], args[2], securityPasswords);
			Printer.printSecurityOutput(fileName, securityPasswords, securityKeys, securityProperties, keysNotFound, hasDuplicates);
		} else {
			System.out.println("Please enter 3 files for security analysis");
		}
	}
	
	public List<String> read(String fileName) throws IOException {
		File file = new File(fileName);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line;
		List<String> securityKeys = new ArrayList<String>();
		while((line = br.readLine()) != null) {
			securityKeys.add(line);
		}
		br.close();
		fr.close();
		return securityKeys;
	}
	
	public List<String> getSecurityPasswords(Collection<Property> securityProperties, List<String> securityKeys) {
		List<String> securityPasswords = new LinkedList<String>();
		
		for(String s: securityKeys) {
			for(Property p: securityProperties) {
				if(s.equals(p.getKey())) {
					securityPasswords.add(p.getValue());
				}
			}
		}
		return securityPasswords;
	}
	
	public Collection<String> keysNotFound(Collection<Property> securityProperties, List<String> securityKeys) {
		List<String> keysNotFound = new LinkedList<String>();
		
		for(String s: securityKeys) {
			keysNotFound.add(s);
			for(Property p: securityProperties) {
				if(s.equals(p.getKey())) {
					keysNotFound.remove(s);
				}
			}
		}
		return keysNotFound;
	}
	
	public void replacePasswords(String file1, String file2, List<String> values) throws XMLStreamException, FactoryConfigurationError, IOException {
		JndiEvaluator run = new JndiEvaluator();
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream(file1));
		XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(new FileWriter(file2));
		
		while(reader.hasNext()) {
			XMLEvent event = (XMLEvent) reader.next();
			if(event.getEventType() == event.CHARACTERS) {
				//replace current characters with ******
				writer.add(run.getNewCharactersEvent(event.asCharacters(), values));
			} else {
				writer.add(event);
			}
		}
		writer.flush();
		writer.close();
	}
	
	Characters getNewCharactersEvent(Characters event, List<String> values) {
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		String passwordChange = "*************";
		
		for(String v: values) {
			if(event.getData().equals(v)) {
				event = eventFactory.createCharacters(passwordChange);
			}
		}
		return event;
	}
	
	public static void printUsage() {
		log("Usage: --dup a.xml");
		log("Usage: --diff a.xml b.xml");
		log("Usage: --sec a.xml a-secure.xml sec.txt");
	}
	
	private static void log(String log) {
		log(log, false, null);
	}
	
	private static void log(String log, boolean isError) {
		log(log, true, null);
	}
	
	private static void log(String log, boolean isError, Exception e) {
		if(isError) {
			System.err.println("Error: " + log);
		} else {
			System.out.println(log);
		}
		if(e != null) {
			e.printStackTrace();
		}
	}
	
}
