package DatabaseWork;

import Exceptions.NoInternetException;
import TechnicalInspection.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import Enum.*;

public class InspectionDAO {
    private static InspectionDAO instance;
    private Connection conn;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);


    public static InspectionDAO getInstance() {
        if (instance == null) instance = new InspectionDAO();
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }

    public static void removeInstance() {
        if (instance == null) return;
        instance.close();
        instance = null;
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private InspectionDAO() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/dbinspection", "root", "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // GET request methods
    private JSONArray connectToURL(String path) {
        URL url = null;
        JSONArray jsonArray = null;
        try {
            url = new URL("http://localhost:8080/api/" + path);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader entry = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String json = "", line = "";
            while ((line = entry.readLine()) != null) {
                json = json + line;
            }
            jsonArray = new JSONArray(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public ArrayList<Vehicle> vehicles() {
        ArrayList<Vehicle> result = new ArrayList<>();
        JSONArray jsonArray = connectToURL("vehicle");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            String date = jo.getString("date_of_use");
            LocalDate releaseDate = LocalDate.parse(date, formatter);
            String previousInspectionDate = jo.getString("previous_inspection");
            LocalDate previousInspection = LocalDate.parse(previousInspectionDate, formatter);
            int id = jo.getInt("id");
            ArrayList<Malfunction> malfunctions = getVehicleMalfunctions(id);
            VehicleType type = VehicleType.getVehicleType(jo.getString("type"));
            Vehicle vehicle = new Vehicle(jo.getString("owner_name"), jo.getString("brand"), type,
                    jo.getString("serial_number"), jo.getInt("production_year"), releaseDate, previousInspection, malfunctions);
            result.add(vehicle);
        }
        return result;
    }

    public Vehicle getVehicle(int id) {
        URL url = null;
        Vehicle vehicle = null;
        try {
            url = new URL("http://localhost:8080/api/vehicle/" + id);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader entry = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String json = "", line = "";
            while ((line = entry.readLine()) != null) {
                json = json + line;
            }
            JSONObject jo = new JSONObject(json);
            VehicleType vT = VehicleType.getVehicleType(jo.getString("type"));
            String date = jo.getString("date_of_use");
            LocalDate releaseDate = LocalDate.parse(date, formatter);
            String date1 = jo.getString("previous_inspection");
            LocalDate previous = LocalDate.parse(date1, formatter);
            ArrayList<Malfunction> malfunctions = getVehicleMalfunctions(id);
            vehicle = new Vehicle(jo.getString("owner_name"), jo.getString("brand"), vT, jo.getString("serial_number"), jo.getInt("production_year"), releaseDate, previous, malfunctions);
        } catch (IOException e) {
            new NoInternetException();
        }
        return vehicle;
    }

    public ArrayList<Malfunction> malfunctions() {
        ArrayList<Malfunction> result = new ArrayList<>();
        JSONArray jsonArray = connectToURL("failure");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            String date = jo.getString("accurrence_date");
            LocalDate emergenceDate = LocalDate.parse(date, formatter);

            LocalDate repairDate;
            if (!jo.isNull("repair_date")) {
                String rDate = jo.getString("repair_date");
                repairDate = LocalDate.parse(rDate, formatter);
            } else repairDate = null;

            Malfunction malfunction = new Malfunction(jo.getString("name"), jo.getInt("vehicle"), emergenceDate, repairDate);
            result.add(malfunction);
        }
        return result;
    }

    public ArrayList<Malfunction> getVehicleMalfunctions(int id) {
        ArrayList<Malfunction> result = new ArrayList<>();
        JSONArray jsonArray = connectToURL("failure");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            int idCheck = jo.getInt("vehicle");
            if (idCheck == id) {
                String date = jo.getString("accurrence_date");
                LocalDate emergenceDate = LocalDate.parse(date, formatter);
                LocalDate repairDate;
                if (!jo.isNull("repair_date")) {
                    String date2 = jo.getString("repair_date");
                    repairDate = LocalDate.parse(date2, formatter);
                } else repairDate = null;
                Malfunction malfunction = new Malfunction(jo.getString("name"), jo.getInt("vehicle"), emergenceDate, repairDate);
                result.add(malfunction);
            }
        }
        return result;
    }

    public ArrayList<Equipment> equipment() {
        ArrayList<Equipment> equipment = new ArrayList<>();
        JSONArray jsonArray = connectToURL("part");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            Equipment eq = new Equipment(jo.getInt("id"), jo.getString("name"), jo.getBoolean("availability"));
            equipment.add(eq);
        }
        return equipment;
    }

    public ArrayList<User> users() {
        ArrayList<User> users = new ArrayList<>();
        JSONArray jsonArray = connectToURL("user");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            String date = jo.getString("birth_date");
            LocalDate birthDate = LocalDate.parse(date, formatter);
            User user = new User(jo.getInt("id"), jo.getString("first_name"), jo.getString("last_name"), jo.getString("jmbg"), birthDate, jo.getString("adress"),jo.getString("zip_code"), jo.getString("mail"), jo.getString("phone_number"), jo.getString("user_name"), jo.getString("password"), jo.getString("position"));
            users.add(user);
        }
        return users;
    }

    public ArrayList<TechnicalInspection> inspections() {
        ArrayList<TechnicalInspection> inspections = new ArrayList<>();
        JSONArray jsonArray = connectToURL("review");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            InspectionType inspectionType = InspectionType.getInspectionType(jo.getString("kind"));
            WarrantState warrantState = WarrantState.getWarrantState(jo.getString("state"));
            TechnicalInspection technicalInspection = new TechnicalInspection(jo.getInt("id"), inspectionType, jo.getInt("responsible_person"), jo.getInt("vehicle"), warrantState);
            inspections.add(technicalInspection);
        }
        return inspections;
    }

    // POST request methods
    public void addVehicle(Vehicle vehicle) { // TODO: Add check method
        URL url = null;
        try {
            url = new URL("http://localhost:8080/api/vehicle");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JSONObject vehicleObj = new JSONObject();
        vehicleObj.put("id", vehicle.getId());
        vehicleObj.put("owner_name", vehicle.getVehicleOwner());
        vehicleObj.put("brand", vehicle.getBrand());
        vehicleObj.put("type", vehicle.getType());
        vehicleObj.put("serial_number", vehicle.getSerialNumber());
        vehicleObj.put("production_year", vehicle.getProductionYear());
        vehicleObj.put("date_of_use", vehicle.getReleaseDate());
        vehicleObj.put("previous_inspection", vehicle.getPreviousInspection());
        int id = addViaHttp(vehicleObj, url);
        vehicle.setId(id);
    }

    public void addMalfunction(Malfunction malfunction) { // TODO: Add check method
        URL url = null;
        try {
            url = new URL("http://localhost:8080/api/failure");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JSONObject jsonMalfunction = new JSONObject();
        jsonMalfunction.put("name", malfunction.getMalfunctionName());
        jsonMalfunction.put("vehicle", malfunction.getVehicleId());
        jsonMalfunction.put("accurrence_date", malfunction.getEmergenceDate());
        jsonMalfunction.put("repair_date", malfunction.getRepairDate());
        addViaHttp(jsonMalfunction, url);
    }

    private int addViaHttp (JSONObject jsonObject, URL url) {
        HttpURLConnection con = null;
        JSONObject jsonObject1 = null;
        try {
            byte[] data = jsonObject.toString().getBytes();
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.write(data);
            out.flush();
            out.close();

            BufferedReader entry = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String json = "", line = "";
            while ((line = entry.readLine()) != null) {
                json = json + line;
            }
            jsonObject1 = new JSONObject(json);
            entry.close();
        } catch (IOException e) {
            new NoInternetException();
        }
        return jsonObject1.getInt("id");
    }


    // PUT request methods


    // DELETE request methods


    // Check if exists in database

    public boolean checkVehicleExists(Vehicle vehicle) {  // TODO: implement method
        return true;
    }

    public boolean checkMalfunctionExists(Vehicle vehicle) {  // TODO: implement method
        return true;
    }

}
