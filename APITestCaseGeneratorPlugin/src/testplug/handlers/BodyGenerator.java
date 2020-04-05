package testplug.handlers;
import java.util.HashSet;
import java.util.Map;

import io.swagger.inflector.examples.ExampleBuilder;
import io.swagger.inflector.examples.models.Example;
import io.swagger.models.Model;

public class BodyGenerator {
	
	
	public  BodyGenerator() {
		
		
	}
	
	
	public String GetValidBody(String modelName,Map<String, Model> definitions) {
		
		String body="";

		Model Model = definitions.get(modelName);
		
		Example example = ExampleBuilder.fromModel("Model", Model, definitions, new HashSet<String>());
		// Another way:
		// Example example = ExampleBuilder.fromProperty(new RefProperty("ApiCreateDeviceResponse"), swagger.getDefinitions());
		
		// Convert the Example object to string

		// JSON example
		
		
		return body;
	}
	
	

}
