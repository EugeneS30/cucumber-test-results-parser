package com.github.eugene.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;

public class ParseResults {

	private static final String filePath = "C:\\Users\\Polar\\Google Drive\\work\\376\\cucumber.json";
	private static final String myDirectoryPath = "C:\\Users\\Polar\\Google Drive\\work";

	public static void main(String[] args) {
		File dir = new File(myDirectoryPath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				System.out.println(child);
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
		try {
			FileReader reader = new FileReader(filePath);
			JSONParser jsonParser = new JSONParser();

			JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);

			List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
			allJSONFileElementsList.addAll(jsonFileData);

			List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();

			for (JSONObject featureFileElement : allJSONFileElementsList) {
				String name = (String) featureFileElement.get("name");
				String uri = (String) featureFileElement.get("uri");
				JSONArray elements = (JSONArray) featureFileElement
						.get("elements");

				featureFileObjectsList.add(new FeatureFileElement(name, uri,
						elements));
			}

			for (FeatureFileElement featureFileElement : featureFileObjectsList) {
				featureFileElement.getScenariosResults();
				System.out.println("=-=-=-=-=-=-=-=-=-");
			}

		}

		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
