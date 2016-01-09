package com.pitsu.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.xml.stream.FactoryConfigurationError;
import java.xml.stream.XMLStreamException;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.*;

import org.junit.matchers.JUnitMatchers;

public class JndiEvaluatorTest {

	JndiEvaluator test = new JndiEvaluator();
	String file1;
	String file2;
	Collection<Property> properties1;
	Collection<Property> properties2;
	int matchingKeys;
	Collection<Property> differentProperties1;
	Collection<Property> differentProperties2;
	Collection<Property> differentPropertyValues;
	Map<String, Collection<Property>> valueDifferencesMap;
	String duplicateWarning;
	String fileName;
	List<String> securityKeys;
	Collection<Property> securityProperties;
	boolean hasDuplicates;
	Collection<String> keysNotFound;
	List<String> securityPasswords;
	
	public static void setUpClass() throws Exception {
		
	}
	
	@Before
	public void setUp() throws Exception {
		file1 = "testDuplicates.xml";
		file2 = "testNonduplicates.xml";
		properties1 = test.parse(file1);
		properties2 = test.parse(file2);
		matchingKeys = test.countMatchingKeys(properties1, properties2);
		differentProperties1 = test.findDifferences(properties1, properties2);
		differentProperties2 = test.findDifferences(properties2, properties1);
		differentPropertyValues = test.valueDifferences(properties1, properties2);
		valueDifferencesMap = test.makeValueDifferencesMap(differentPropertyValues);
		duplicateWarning = test.checkDuplicates(properties1, properties2);
		fileName = "testReplacePasswords1";
		securityKeys = test.read("testReplacePasswords1.txt");
		securityProperties = test.parse("testReplacePasswords1.txt");
		hasDuplicates = test.hasDuplicates(securityProperties);
		keysNotFound = test.keysNotFound(securityProperties, securityKeys);
		securityPasswords = test.getSecurityPasswords(securityProperties, securityKeys);
	}
	
	@Test
	public void testExecuteForPrintUsage() {
		String[] args = new String[2];
		args[0] = "nothing1";
		args[1] = "nothing2";
		test.execute(args);
		assertNotNull(args);
	}
	
	// Test when duplicate XML file is used
	@Test
	public void testExecuteForDuplicatesAnalysis1() {
		String[] args = new String[2];
		args[0] = "--dup";
		args[1] = "testDuplicates.xml";
		test.execute(args);
		assertNotNull(args);
	}
	
	// Test when non-duplicate XML file is used
	@Test
	public void testExecuteForDuplicateAnalysis2() {
		String[] args = new String[2];
		args[0] = "--dup";
		args[1] = "testNonduplicates.xml";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExecuteForDifferencesAnalysis1() {
		String[] args = new String[3];
		args[0] = "--diff";
		args[1] = "testDifferences1.xml";
		args[2] = "testDifferences2.xml";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExecuteForDifferencesAnalysis2() {
		String[] args = new String[3];
		args[0] = "--diff";
		args[1] = "testValueDifferences1.xml";
		args[2] = "testValueDifferences2.xml";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExecuteForSecurityAnalysis1() {
		String[] args = new String[4];
		args[0] = "--sec";
		args[1] = "testReplacePasswords1.xml";
		args[2] = "testReplacePasswords2.xml";
		args[3] = "testReplacePasswords1.txt";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExceptionForDuplicateAnalysis() {
		String[] args = new String[1];
		args[0] = "--dup";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExceptionForDifferenceAnalysis() {
		String[] args = new String[1];
		args[0] = "--diff";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testExceptionForSecurityAnalysis() {
		String[] args = new String[1];
		args[0] = "--sec";
		test.execute(args);
		assertNotNull(args);
	}
	
	@Test
	public void testFileNotFoundException() {
		try {
			String[] args = new String[2];
			args[0] = "--dup";
			args[1] = "nothing.xml";
			test.execute(args);
		} catch(Exception e) {
			assertTrue(e.getMessage().equals("Error: File(s) not found: Please input valid file(s)"));
		}
	}
	
	@Test
	public void testDuplicates() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties = test.parse("testDuplicates.xml");
		Map<String, Collection<Property>> duplicates = test.findDuplicates(properties);
		String duplicateKey = "urls/jboss";
		
		for(Collection<Property> values: duplicates.values()) {
			for(Property p: values) {
				assertEquals(duplicateKey, p.getKey());
			}
		}
	}
	
	@Test
	public void testNonduplicates() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties = test.parse("testNonduplicates.xml");
		Map<String, Collection<Property>> noDuplicates = test.findDuplicates(properties);
		
		assertTrue(noDuplicates.isEmpty());
	}
	
	@Test
	public void testDifferences() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties1 = test.parse("testDifferences1.xml");
		Collection<Property> properties2 = test.parse("testDifferences2.xml");
		Collection<Property> differences1 = test.findDifferences(properties1, properties2);
		Property differentProperty = new Property();
		differentProperty.setKey("urls/jboss");
		differentProperty.setValue("http://www.jboss.org");
		differentProperty.setLineNumber(12);
		differentProperty.setFileName("testDifferences1.xml");
		
		assertTrue(differences1.contains(differentProperty));	
	}
	
	public void testCheckDuplicates() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties1 = test.parse("testDuplicates.xml");
		Collection<Property> properties2 = test.parse("testValueDifferences1.xml");
		String checkDuplicates = test.checkDuplicates(properties1, properties2);
		String duplicateWarning = "Warning duplicates found in testDuplicates.xml, " + "testValueDifferences1.xml";
		
		assertEquals(duplicateWarning, checkDuplicates);
	}
	
	@Test
	public void testValueDifferences() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties1 = test.parse("testValueDifferences1.xml");
		Collection<Property> properties2 = test.parse("testValueDifferences2.xml");
		Collection<Property> differentPropertyValues = test.valueDifferences(properties1, properties2);
		Map<String, Collection<Property>> valueDifferencesMap = test.makeValueDifferencesMap(differentPropertyValues);
		String key = "urls/jboss";
		
		for(String s: valueDifferencesMap.keySet()) {
			assertEqual(key, s);
		}
	}
	
	@Test
	public void testReplacePasswords() throws FileNotFoundException, XMLStreamException {
		Collection<Property> securityProperties = test.parse("testReplacePasswords1.xml");
		List<String> securityKeys = test.read("testReplacePasswords1.txt");
		List<String> securityPasswords = test.getSecurityPasswords(securityProperties, securityKeys);
		
		test.replacePasswords("testReplacePasswords1.xml", "testReplacePasswords2.xml", securityPasswords);
		Collection<Property> replacedProperties = test.parse("testReplacePasswords2.xml");
		
		for(String password: securityPasswords) {
			assertFalse(replacedProperties.contains(password));
		}	
	}
	
	@Test
	public void testEqualsAndHashCode() throws FileNotFoundException, XMLStreamException {
		Collection<Property> properties = test.parse("testDifferences1.xml");
		
		for(Property p1: properties) {
			assertFalse(p1.equals(null)); 
			for(Property p2: properties) {
				if(p1.equals(p2)) {
					assertIsEqual(p1,p2);
				} else {
					assertIsNotEqual(p1,p2);
				}
			}
		}
	}
	
	public static void assertIsEqual(Property p1, Property p2) {
		assertTrue(p1.equals(p2));
		assertTrue(p2.equals(p1));
		assertEquals(p1.hashCode(), p2.hashCode());
	}
	
	public static void assertIsNotEqual(Property p1, Property p2) {
		assertReflexiveAndNull(p1);
		assertReflexiveAndNull(p2);
		assertFalse(p1.equals(p2));
		assertFalse(p2.equals(p1));
	}
	
	public static void assertReflexiveAndNull(Property p) {
		assertTrue(p.equals(p));
		assertFalse(p.equals(null));
	}
	
	@Test
	public void testPrintDuplicatesException() {
		try {
			Printer.printDuplicates(null);
			fail("expected IllegalArgumentException");
		} catch(IllegalArgumentException e) {
			assertTrue(e.getMessage().equals("When printing a problem occurred because \"duplicates\" was missing"));
		}
	}
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testIllegalArgumentException1() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"file1\" was missing.");
		Printer.printDifferenceSummary(null, file2, properties1, properties2, matchingKeys, duplicateWarning);
	}
	
	public void testIllegalArgumentException2() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"file2\" was missing.");
		Printer.printDifferenceSummary(file1, null, properties1, properties2, matchingKeys, duplicateWarning);
	}
	
	public void testIllegalArgumentException3() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"properties1\" was missing.");
		Printer.printDifferenceSummary(file1, file2, null, properties2, matchingKeys, duplicateWarning);
	}
	
	public void testIllegalArgumentException4() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"properties2\" was missing.");
		Printer.printDifferenceSummary(file1, file2, properties1, null, matchingKeys, duplicateWarning);
	}
	
	public void testIllegalArgumentException5() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"matchingKeys\" was missing. An int can't be negative.");
		Printer.printDifferenceSummary(file1, file2, properties1, properties2, -1, duplicateWarning);
	}
	
	public void testIllegalArgumentException6() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"duplicateWarning\" was missing.");
		Printer.printDifferenceSummary(file1, file2, properties1, properties2, matchingKeys, null);
	}
	
	public void testIllegalArgumentException7() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"file1\" was missing.");
		Printer.printMissingProperties(null, file2, differentProperties1, differentProperties2);
	}
	
	public void testIllegalArgumentException8() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"file2\" was missing.");
		Printer.printMissingProperties(file1, null, differentProperties1, differentProperties2);
	}
	
	public void testIllegalArgumentException9() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"differentProperties1\" was missing.");
		Printer.printMissingProperties(file1, file2, null, differentProperties2);
	}
	
	public void testIllegalArgumentException10() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"differentProperties2\" was missing.");
		Printer.printMissingProperties(file1, file2, differentProperties1, null);
	}
	
	public void testIllegalArgumentException11() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"differentPropertyValues\" was missing.");
		Printer.printValueDifferences(null, matchingKeys, valueDifferencesMap);
	}
	
	public void testIllegalArgumentException12() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"matchingKeys\" was missing. An int can't be negative.");
		Printer.printValueDifferences(differentPropertyValues, -1, valueDifferencesMap);
	}
	
	public void testIllegalArgumentException13() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"valueDifferencesMap\" was missing.");
		Printer.printValueDifferences(differentPropertyValues, matchingKeys, null);
	}
	
	public void testIllegalArgumentException14() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"fileName\" was missing.");
		Printer.printSecurityOutput(null, securityPasswords, securityKeys, securityProperties, keysNotFound, hasDuplicates);
	}
	
	public void testIllegalArgumentException15() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"securityPasswords\" was missing.");
		Printer.printSecurityOutput(fileName, null, securityKeys, securityProperties, keysNotFound, hasDuplicates);
	}
	
	public void testIllegalArgumentException16() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"securityKeys\" was missing.");
		Printer.printSecurityOutput(fileName, securityPasswords, null, securityProperties, keysNotFound, hasDuplicates);
	}
	
	public void testIllegalArgumentException17() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"securityProperties\" was missing.");
		Printer.printSecurityOutput(fileName, securityPasswords, securityKeys, null, keysNotFound, hasDuplicates);
	}
	
	public void testIllegalArgumentException18() {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("When printing a problem occurred because \"keysNotFound\" was missing.");
		Printer.printSecurityOutput(fileName, securityPasswords, securityKeys, securityProperties, null, hasDuplicates);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@AfterClass 
	public static void tearDownClass() throws Exception {
		
	}
}
