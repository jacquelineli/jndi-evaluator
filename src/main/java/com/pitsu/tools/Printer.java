package com.pitsu.tools;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Printer {

	public static void printDuplicates(Map<String, Collection<Property>> duplicates) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		int count = 0;
		boolean valuesDifferent = false;
		
		if(duplicates == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"duplicates\" was missing");
		} else {
			for(Collection<Property> values: duplicates.values()) {
				for(Property p1: values) {
					for(Property p2: values) {
						if(!p1.getValue().equals(p2.getValue())) {
							valuesDifferent = true;
							break;
						} else {
							valuesDifferent = false;
						}
					}
				}
				if(valuesDifferent) {
					count++;
				}
				sb2.append("Value Different: " + valuesDifferent + "\n");
				for(Property p: values) {
					sb2.append(p + "\n");
				}
				sb2.append("\n");
			}
			sb1.append("Warning: " + duplicates.size() + " duplicate(s) found!" + "\n");
			sb1.append("Value differences: " + count + "\n");
			System.out.println(sb1.toString() + "\n" + sb2.toString());
		}
	}
	
	public static void printDifferenceSummary(String file1, String file2, Collection<Property> properties1, 
			Collection<Property> properties2, int matchingKeys, String duplicateWarning) {
		if(file1 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"file1\" was missing.");
		} if(file2 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"file2\" was missing.");
		} if(properties1 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"properties1\" was missing.");
		} if(properties2 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"properties2\" was missing.");
		} if(matchingKeys < 0) {
			throw new IllegalArgumentException("When printing a problem occurred because \"matchingKeys\" was missing. An int can't be negative.");
		} if(duplicateWarning == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"duplicateWarning\" was missing.");
		} else {
			System.out.println("Summary:");
			System.out.println("----------");
			System.out.println("Properties found in " + file1 + ": " + properties1.size());
			System.out.println("Properties found in " + file2 + ": " + properties2.size());
			System.out.println("Matching keys: " + matchingKeys);
			System.out.println(duplicateWarning);
		}
	}
	
	public static void printMissingProperties(String file1, String file2, Collection<Property> differentProperties1, 
			Collection<Property> differentProperties2) {
		if(file1 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"file1\" was missing.");
		} if(file2 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"file2\" was missing.");
		} if(differentProperties1 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"properties1\" was missing.");
		} if(differentProperties2 == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"properties2\" was missing.");
		} else {
			System.out.println();
			System.out.println("Missing Keys:");
			System.out.println("-------------------------");
			System.out.println("Keys(s) present in " + file1 + " but missing in " + file2 + ": " + differentProperties1.size());
			printDifferences(differentProperties1);
			System.out.println("\n" + "Keys(s) present in " + file2 + " but missing in " + file1 + ": " + differentProperties2.size());
			printDifferences(differentProperties2);
			System.out.println();
		}
	}
	
	public static void printDifferences(Collection<Property> differentProperties) {
		for(Property p: differentProperties) {
			System.out.println(p);
		}
	}
	
	public static void printValueDifferences(Collection<Property> differentPropertyValues, int matchingKeys, 
			Map<String, Collection<Property>> valueDifferencesMap) {
		if(differentPropertyValues == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"differentPropertyValues\" was missing");
		} if(matchingKeys < 0 ) {
			throw new IllegalArgumentException("When printing a problem occurred because \"matchingKeys\" was missing. An int can't be negative.");
		} if(valueDifferencesMap == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"valueDifferencesMap\" was missing.");
		} else {
			System.out.println("ValueDifferences:");
			System.out.println("---------------------");
			System.out.println("Value differences found: " + differentPropertyValues.size() + " different value(s) for " + 
			matchingKeys + " matching key(s)");
			printValueDifferencesMap(valueDifferencesMap);
		}
	}
	
	public static void printValueDifferencesMap(Map<String, Collection<Property>> valueDifferencesMap) {
		for(Collection<Property> properties: valueDifferencesMap.values()) {
			System.out.println();
			for(Property p: properties) {
				System.out.println(p.toString(true));
			}
		}
	}
	
	public static void printSecurityOutput(String fileName, List<String> securityPasswords, List<String> securityKeys, 
			Collection<Property> securityProperties, Collection<String> keysNotFound, boolean hasDuplicates) {
		if(fileName == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"fileName\" was missing.");
		} if(securityPasswords == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"securityPasswords\" was missing.");
		} if(securityKeys == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"securityKeys\" was missing.");
		} if(securityProperties == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"securityKeys\" was missing.");
		} if(keysNotFound == null) {
			throw new IllegalArgumentException("When printing a problem occurred because \"keysNotFound\" was missing.");
		} else {
			System.out.println(securityPasswords.size() + "properties replaced out of the " + securityKeys.size() + "properties received");
			printSecurityDuplicateCheck(fileName, hasDuplicates);
			System.out.println();
			System.out.println("Properties for which values changed: " + securityPasswords.size());
			printPropertiesChanged(securityProperties, securityKeys);
			System.out.println();
			printPropertiesNotFound(fileName, securityProperties, keysNotFound);
		}
	}
	
	public static void printSecurityDuplicateCheck(String fileName, boolean hasDuplicates) {
		if(hasDuplicates == true) {
			System.out.println("Warning duplicates found in" + fileName);
		}
	}
	
	public static void printPropertiesNotFound(String file1, Collection<Property> securityProperties, Collection<String> keysNotFound) {
		if(keysNotFound.size() != 0) {
			System.out.println("Properties not found in " + file1 + ": " + keysNotFound.size());
			for(String s: keysNotFound) {
				System.out.println(s);
			}
		}
	}
	
	public static void printPropertiesChanged(Collection<Property> securityProperties, List<String> securityKeys) {
		for(String s: securityKeys) {
			for(Property p: securityProperties) {
				if(s.equals(p.getKey())) {
					System.out.println(p.toStringSimple());
				}
			}
		}
	}
}
