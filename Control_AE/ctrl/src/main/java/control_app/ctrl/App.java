package control_app.ctrl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/**
 * Hello world!
 *
 */
public class App 
{
	final static String middle_ip = "127.0.0.1";			//Middle node ip
	final static String middle_id = "niccolo-mn-cse";		//Middle node id
	final static String middle_name = "niccolo-mn-name";	//Middle node name
	final static String AE_name = "Control_AE";				//Name of the Application Entity
	final static String localServerAddr = "coap://127.0.0.1:5685/";	//IP and port of the local server to receive notifications
	
	public void createSubscription(String cse, String ResourceName, String notificationUrl) throws Exception{
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", ResourceName);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		if(responseBody == null) {
			throw new Exception("Request timeout");
		}
		String response = new String(responseBody.getPayload());
		System.out.println(response);
				
	}
	
    public static void main( String[] args ) throws InterruptedException{
    	App ctrl_app = new App();
    	CoapServer server = new CoapServer(5685);
    	
    	final AE_Control adn = new AE_Control();
    	//create applications entities for control and security
		AE ae = adn.createAE_Control("coap://127.0.0.1:5683/~/" + middle_id, "ControlApp-ID", "Control_AE");
		//AE ae_security = adn.createAE_Control("coap://127.0.0.1:5683/~/mn-cse", "SecurityApp-ID", "Security_AE");
		AE ae_prova = adn.createAE_Control("coap://127.0.0.1:5683/~/" + middle_id, "provaApp-ID", "Prova_AE");
		//create containers to test the application(in fact these containers are unneeded, they are just for test
		adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name, "Sector1");
		adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + "Sector1", "Sensor");
		adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + "Sector1" + "/Sensor", "Humid");
		adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + "Sector1" + "/Sensor/Humid", "Sensor0");
		adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + "Sector1" + "/Sensor/Humid/Sensor0", "10");
		/*adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE", "Sector2");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2", "Sensor");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor", "Temp");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor/Temp", "Sensor0");
		adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector2/Sensor/Temp/Sensor0", "8");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1", "Actuators");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators", "Irrigators");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators/Irrigators", "Irrigator0");
		adn.createContainer("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/Sector1/Actuators/Irrigators", "Irrigator1");*/
		
		
		Integer i = new Integer(0);
		
		Integer num_resources_mn = new Integer(0);//number of resources i.e paths returned by the discovery
		//String[] resources_paths_mn = new String[100];
		//String[] resources_mn_full_path_mn = new String[100];
		 List<String> resources_paths_mn = new ArrayList<String>();
		 List<String> actuators_paths_mn = new ArrayList<String>();
		 List<String> resources_paths_mn_security = new ArrayList<String>();
		 List<String> discovery = new ArrayList<String>();
		 List<String> resources_mn_full_path_mn = new ArrayList<String>();
		//the service_ae is created by niccolo, i dont create it, i just do a discovery of its resources
		//the argument in the discovery should be just the port of the middle node without the service_ae or with it?
		//perform the discovery
		try {
			discovery = adn.Discovery("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + "Service_AE");//this is the port of the middle node?
		}catch(Exception e) {
			System.err.println("[ERROR] " + e);
			System.out.println("[INFO] Terminating program");
			System.exit(1);
		}
		
		System.out.println(discovery.get(0));
		num_resources_mn = discovery.size();
		System.out.println("[DEBUG] num of resources: " + num_resources_mn);
		String pom;
		//exclude the actuators from the list returned by the discovery
		for (i = 0; i< num_resources_mn; i++) {
			if (discovery.get(i).contains("sensor")) {
				pom = discovery.get(i);
				resources_paths_mn.add(pom);
			}else {
				pom = discovery.get(i);
				actuators_paths_mn.add(pom);
			}
		}

		num_resources_mn = resources_paths_mn.size();
		System.out.println("[DEBUG] discovered devices: " + num_resources_mn);
		
		//find the number of sectors
		final ArrayList<Sector> sectors = new ArrayList<Sector>();
		for (i = 0; i< num_resources_mn; i++) {
			String[] subpaths = resources_paths_mn.get(i).split("/");
			if(sectors.contains(subpaths[3]) == false) {
				//new sector
				sectors.add(new Sector(subpaths[3]));
			}
		}
		
		System.out.println("[INFO] number of sectors: " + sectors.size());
		
		//Container control_app_cont = new Container();
		//control_app_cont = adn.createContainer("coap://127.0.0.1:5684/~/mn-cse/mn-name/Control_AE");
		//mi sottoscrivo sul coap monitor server per tutti i contenitori dal applicazione dei servizi(quelli che mi servono)
		//quelli che mi servono per laplication entity del control sono tutti aparte di contenitore per la camera e per il pir
		//quelli che mi servono per laplicazione del control sono i contenitori della camera e del pir
		//post per tutti i contenitori con il payload settato 
		
		List<String> target_temp = new ArrayList<String>();
		List<String> target_humid = new ArrayList<String>();
		List<String> target_light = new ArrayList<String>();
		List<String> target_soilmoist = new ArrayList<String>();
		

		int num_sectors = sectors.size();

		String target;
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		for(i = 0;i < num_sectors; i++) {
			System.out.println("Enter the target temp for sector " + sectors.get(i));
			target = reader.next();
			target_temp.add(target);
			//Create resource to receive notifications about target temperature changes
			server.add(new CoapResource(sectors.get(i).sectorName).add(new CoapResource("targetTemperature") {
				
				public void handlePOST(CoapExchange exchange) {
					for(int i=0; i < sectors.size(); i++) {
						if(sectors.get(i).sectorName.equals(this.getParent().getName())) {
							//Update target temperature of the correct sector
							sectors.get(i).targetTemp = Integer.parseInt(exchange.getRequestText());
						}
					}
				}
				
			}));
			
			System.out.println("Enter the target humidity for sector " + sectors.get(i));
			target = reader.next();
			target_humid.add(target);
			//Create resource to receive notifications about target humidity changes
			server.add(new CoapResource(sectors.get(i).sectorName).add(new CoapResource("targetHumidity") {
				
				public void handlePOST(CoapExchange exchange) {
					for(int i=0; i < sectors.size(); i++) {
						if(sectors.get(i).sectorName.equals(this.getParent().getName())) {
							//Update target humidity of the correct sector
							sectors.get(i).targetHumidity = Integer.parseInt(exchange.getRequestText());
						}
					}
				}
				
			}));
			
			
			System.out.println("Enter the target light for sector " + sectors.get(i));
			target = reader.next();
			target_light.add(target);
			//Create resource to receive notifications about target light changes
			server.add(new CoapResource(sectors.get(i).sectorName).add(new CoapResource("targetLight") {
				
				public void handlePOST(CoapExchange exchange) {
					for(int i=0; i < sectors.size(); i++) {
						if(sectors.get(i).sectorName.equals(this.getParent().getName())) {
							//Update target light of the correct sector
							sectors.get(i).targetLight = Integer.parseInt(exchange.getRequestText());
						}
					}
				}
				
			}));
			
			
			System.out.println("Enter the target soil moisture for sector " + sectors.get(i));
			target = reader.next();
			target_soilmoist.add(target);
			//Create resource to receive notifications about target light changes
			server.add(new CoapResource(sectors.get(i).sectorName).add(new CoapResource("targetSoil") {
				
				public void handlePOST(CoapExchange exchange) {
					for(int i=0; i < sectors.size(); i++) {
						if(sectors.get(i).sectorName.equals(this.getParent().getName())) {
							//Update target temperature of the correct sector
							sectors.get(i).targetSoil = Integer.parseInt(exchange.getRequestText());
						}
					}
				}
				
			}));
			
		}
		reader.close();
				
		for (i = 0; i< num_sectors ;i++) {
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name, sectors.get(i).sectorName);
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName, "TargetTemp");
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName + "/TargetTemp", target_temp.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName, "TargetHumid");
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName + "/TargetHumid", target_humid.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName, "TargetLight");
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName + "/TargetLight", target_light.get(i));
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName, "TargetSoilMoist");
			adn.createContentInstance("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + sectors.get(i).sectorName + "/TargetSoilMoist", target_soilmoist.get(i));
		
			try {
				ctrl_app.createSubscription("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + sectors.get(i).sectorName + "/TargetTemp", "targetTempMonitor", "coap://127.0.0.1:5685/" + sectors.get(i).sectorName + "targetTemperature");
			} catch (Exception e) {
				System.err.println("[ERROR] Fail to subscribe to TargetTemperature: " + e.getMessage());
			}
			try {
				ctrl_app.createSubscription("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + sectors.get(i).sectorName + "/TargetHumid", "targetHumMonitor", "coap://127.0.0.1:5685/" + sectors.get(i).sectorName + "targetHumidity");
			} catch (Exception e) {
				System.err.println("[ERROR] Fail to subscribe to TargetHumidity: " + e.getMessage());
			}
			try {
				ctrl_app.createSubscription("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + sectors.get(i).sectorName + "/TargetLight", "targetLightMonitor", "coap://127.0.0.1:5685/" + sectors.get(i).sectorName + "targetLight");
			} catch (Exception e) {
				System.err.println("[ERROR] Fail to subscribe to TargetLight: " + e.getMessage());
			}
			try {
				ctrl_app.createSubscription("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + AE_name + "/" + sectors.get(i).sectorName + "/TargetSoilMoist", "targetSoilMonitor", "coap://127.0.0.1:5685/" + sectors.get(i).sectorName + "targetSoil");
			} catch (Exception e) {
				System.err.println("[ERROR] Fail to subscribe to TargetSoil: " + e.getMessage());
			}
			
			
			for(int j = 0; j < actuators_paths_mn.size(); j++) {
				String[] sub = actuators_paths_mn.get(j).split("/");
				String sector = sub[0];
				String type = sub[2];
				if(sectors.get(i).equals(sector)) {
					//Actuator belongs to this sector
					if(type.equals("fan")) {
						sectors.get(i).fans.add(actuators_paths_mn.get(j));
					}else if(type.equals("irrigator_soilm")) {
						sectors.get(i).irrigators_soilm.add(actuators_paths_mn.get(j));
					}else if(type.equals("irrigator_humid")) {
						sectors.get(i).irrigators_humid.add(actuators_paths_mn.get(j));
					}else if(type.equals("lamp")) {
						sectors.get(i).lamps.add(actuators_paths_mn.get(j));
					}else if(type.equals("sprinkler")) {
						sectors.get(i).sprinklers.add(actuators_paths_mn.get(j));
					}else if(type.equals("alarm")) {
						sectors.get(i).alarms.add(actuators_paths_mn.get(j));
					}
				}
			}
			
			//create movement status containers in the security ae 
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + "Security_AE", sectors.get(i).sectorName);
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + "Security_AE" + sectors.get(i).sectorName + "/", "MovementAlarmStatus");
			adn.createContainer("coap://127.0.0.1:5683/~/" + middle_id + "/" + middle_name + "/" + "Security_AE" + sectors.get(i).sectorName + "/", "FireAlarmStatus");
		}
		System.out.println("[INFO] local target and status containers setup completed");
		
		final ArrayList<CI> publishActuation = new ArrayList<CI>();
		
		for(i = 0; i< num_resources_mn; i++) {
			//first create the resource in the monitor
			//resource name is the one of the subscribed resource path
			
			String[] parts = resources_paths_mn.get(i).split("/");
			String sector = parts[0];
			server.add(new CoapResource(parts[0]).add(new CoapResource(parts[1]).add(new CoapResource(parts[2]).add(new Resource(parts[3]) {
				
				public void handlePOST(CoapExchange exchange) {
			    	String path_resource = exchange.getRequestOptions().getUriPathString();//in the case of the lab04 exercise this returns the path of the resource in the coap server(monitor)
			    	//in fact it returned monitor because we only had one resource whose name was monitor
			    	//this is also the name of the resource right? yes
			    	//System.out.println(path_resource);//the path of the resource i could have gotten by this.name,right?
			    	//System.out.println(exchange.getRequestText());
			        System.out.println("[INFO] received notification of update for device " + this.getName());

			        //get info about the sector
			        
			        int val;
			        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			        try {
						DocumentBuilder builder = factory.newDocumentBuilder();
						try {
							InputStream inputStream = new ByteArrayInputStream(exchange.getRequestText().getBytes());
						    Document doc = builder.parse(inputStream);
							NodeList item_list = doc.getElementsByTagName("con");
							if (item_list.getLength() ==1) {
								Node p = item_list.item(0);
								Element con = (Element) p;
								System.out.println(con.getTextContent());
								val = Integer.parseInt(con.getTextContent());
								System.out.println("[INFO] valore e "+val);
								
								String[] subpaths = path_resource.split("/");
								String sector = subpaths[0];
								String type = subpaths[2];						
								
								if(type.equals("temperature")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											this.targetValue = sectors.get(j).targetTemp;
											this.numActuators = sectors.get(j).fans.size();
											this.controlActuator = sectors.get(j).fans;
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.temperatureControl(val, this.targetValue, this.controlActuator);
									
									//Add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
									
									
								}
								if(type.equals("humidity")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											this.targetValue = sectors.get(j).targetHumidity;
											this.numActuators = sectors.get(j).irrigators_humid.size();
											this.controlActuator = sectors.get(j).irrigators_humid;
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.humidityControl(val, this.targetValue, this.controlActuator);
								
									//add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
								
								}
								if(type.equals("light")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											this.targetValue = sectors.get(j).targetLight;
											this.numActuators = sectors.get(j).lamps.size();
											this.controlActuator = sectors.get(j).lamps;
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.lightControl(val, this.targetValue, this.controlActuator);
								
									//add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
								
								}
								if(type.equals("soilm")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											this.targetValue = sectors.get(j).targetSoil;
											this.numActuators = sectors.get(j).irrigators_soilm.size();
											this.controlActuator = sectors.get(j).irrigators_soilm;
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.soilmControl(val, this.targetValue, this.controlActuator);
								
									//add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
								
								}
								if(type.equals("smoke")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											//this.targetValue = sectors.get(j).targetSoil; dont need a target value for the smoke
											this.numActuators = sectors.get(j).sprinklers.size();
											this.controlActuator = sectors.get(j).sprinklers;
											adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Security_AE/" + sector + "/FireAlarmStatus", Integer.toString(val));
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.smokeControl(val, 0, this.controlActuator);
								
									//add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
								
								}
								if(type.equals("PIR")) {
									
									for(int j = 0; j < sectors.size(); j++) {
										if(sectors.get(j).sectorName.equals(sector)) {
											//Info about the sector I have to control
											//this.targetValue = sectors.get(j).targetSoil; dont need a target value for the pir
											this.numActuators = sectors.get(j).alarms.size();
											this.controlActuator = sectors.get(j).alarms;
											//fill the corresponding movement status container
											adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Security_AE/" + sector + "/MovementAlarmStatus", Integer.toString(val));
											break;
										}
									}
									
									ArrayList<CI> actuation = Controls.movementControl(val, 0, this.controlActuator);
								
									//add to the list of values to publish the commanded action
									for(int j = 0; j < actuation.size(); j++) {
										publishActuation.add(actuation.get(j));
									}
								
								}
								
								
							}
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        exchange.respond(ResponseCode.CREATED);
			    
			    }
				
			}))));
		}
		
		server.start();
		
		//Subscribe to the sensors to receive updates
		for(i = 0; i< num_resources_mn; i++) {
			System.out.println("[DEBUG] subscribe to: " + resources_paths_mn.get(i));
			try {
				ctrl_app.createSubscription("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/" + resources_paths_mn.get(i),
				resources_paths_mn.get(i).replaceAll("/", "_"), "coap://127.0.0.1:5685/" + resources_paths_mn.get(i));
			} catch (Exception e) {
				System.err.println("[ERROR] fail to subscribe to " + resources_paths_mn.get(i) + " Error: " + e.getMessage());
			}
		}	
		
		//While loop that checks if there are values to post to command actuators
		while(true) {
			while(publishActuation.size() > 0) {
				CI toPublish = publishActuation.get(0);
				publishActuation.remove(0);
				
				adn.createContentInstance(toPublish.topic, toPublish.value);
			}
			
			//Publish content instance to test the system
			adn.createContentInstance("coap://127.0.0.1:5683/~/mn-cse/mn-name/Prova_AE/" + resources_paths_mn.get(0), "15");
			Thread.sleep(500);	//Sleep for half second to avoid wasting resources
		}
		
    }
}
