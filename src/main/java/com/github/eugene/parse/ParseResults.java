package com.github.eugene.parse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.github.eugene.containers.FeatureFileElement;
import com.github.eugene.containers.Scenario;

@Slf4j
public class ParseResults {

    //private static final String filePath = "C:\\Users\\Polar\\Google Drive\\work\\376\\cucumber.json";
    private static final String myDirectoryPath = "C:\\Users\\Polar\\Google Drive\\work";

    public static void main(String[] args) throws Exception {
        
        File dir = new File(myDirectoryPath);
        File[] directoryListing = dir.listFiles();
        
        List<List<FeatureFileElement>> buildResults = new ArrayList<List<FeatureFileElement>>();

        if (directoryListing != null) {

            for (File buildPath : directoryListing) {
                
                try {
                    FileReader reader = new FileReader(String.join("\\", buildPath.toString(), "cucumber.json"));
                    JSONParser jsonParser = new JSONParser();

                    JSONArray jsonFileData = (JSONArray) jsonParser.parse(reader);
                    List<JSONObject> allJSONFileElementsList = new ArrayList<JSONObject>();
                    allJSONFileElementsList.addAll(jsonFileData);

                    List<FeatureFileElement> featureFileObjectsList = new ArrayList<FeatureFileElement>();

                    int iter = 0;
                    for (JSONObject featureFileElement : allJSONFileElementsList) {
                        log.info("Processing new featureFileElement: " + iter);
                        iter ++;
                        String name = (String) featureFileElement.get("name");
                        String uri = (String) featureFileElement.get("uri");
                        JSONArray elements = (JSONArray) featureFileElement.get("elements");

                        featureFileObjectsList.add(new FeatureFileElement(name, uri, elements));
                        
                        buildResults.add(featureFileObjectsList);
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
        } else {
            // Handle the case where dir is not really a directory.
            // Checking dir.isDirectory() above would not be sufficient
            // to avoid race conditions with another process that deletes
            // directories.
        }

        
    }
}
