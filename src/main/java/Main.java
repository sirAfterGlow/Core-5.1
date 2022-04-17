import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {


    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString("data.json", json);


        list.clear();
        fileName = "data.xml";
        try {
            list = parseXML(fileName);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        json = listToJson(list);
        writeString("data2.json", json);

        list.clear();
        fileName = "data2.json";
        json = readString(fileName);
        list = jsonToList(json);

        for (Employee employee : list) {
            System.out.println(employee.toString());
        }
    }

    private static List<Employee> jsonToList(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        JsonParser parser = new JsonParser();
        JsonArray elements = parser.parse(json).getAsJsonArray();

        List<Employee> result = new ArrayList<>();

        for (JsonElement element : elements) {
            Employee employee = gson.fromJson(element, Employee.class);
            result.add(employee);
        }
        return  result;
    }

    private static String readString(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return sb.toString();
    }

    private static List<Employee> parseXML(String fileName)
            throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));

        List<Employee> resultList = new ArrayList<>();

        Node root = doc.getDocumentElement();
        NodeList employeeList = doc.getElementsByTagName("employee");

        for (int i = 0; i < employeeList.getLength(); i++) {
            Node node = employeeList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                resultList.add(new Employee(
                        Integer.parseInt(element.getElementsByTagName("id").item(0).getTextContent()),
                        element.getElementsByTagName("firstName").item(0).getTextContent(),
                        element.getElementsByTagName("lastName").item(0).getTextContent(),
                        element.getElementsByTagName("country").item(0).getTextContent(),
                        Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent())));
            }
        }
        return resultList;
    }

    private static void writeString(String fileName, String text) {
        try (FileWriter writer = new FileWriter(fileName)){
            writer.write(text);
            writer.flush();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder
                .setPrettyPrinting()
                .create();

        Type listType = new TypeToken<List<Employee>>() {}.getType();

        String result = gson.toJson(list, listType);
        return result;
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = null;
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            staff = csv.parse();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return staff;
    }
}
