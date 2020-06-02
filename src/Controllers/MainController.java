package Controllers;

import DatabaseWork.InspectionDAO;
import Reports.PrintReports;
import TechnicalInspection.TechnicalInspection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.util.*;

import TechnicalInspection.*;
import Enum.*;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;


public class MainController {
    public TableView<TechnicalInspection> completedInspections;
    public TableColumn<TechnicalInspection, String> vehicleOwnerCol;
    public TableColumn<TechnicalInspection, String> vehicleCol;
    public TableColumn vehicleTypeCol;
    public TableColumn<TechnicalInspection, String> responsiblePersonCol;
    private ObservableList<TechnicalInspection> listOfTechnicalInspections;
    public TabPane tabPane;
    public ChoiceBox<String> choiceBoxLanguage;
    public Button btnAddUser;
    public Button btnDeleteUser;
    public Button archiveAccountButton;
    public Tab archiveAccounts;

    private InspectionDAO dao = null;

    public MainController() {
        dao = InspectionDAO.getInstance();
        listOfTechnicalInspections = FXCollections.observableArrayList(dao.inspectionsDone());
    }

    @FXML
    public void initialize() {
        initializeInspectionsTable();
        choiceBoxLanguage.getSelectionModel().select(Locale.getDefault().getLanguage());
        choiceBoxLanguage.getItems().add("Bosanski");
        choiceBoxLanguage.getItems().add("Engleski");
        if (dao.checkIfLoggedUserIsAdmin()) {
            archiveAccounts.setDisable(true);
            archiveAccountButton.setDisable(true);
        }

        choiceBoxLanguage.getSelectionModel().selectedItemProperty().addListener((observableValue, oldLanguage, newLanguage) -> {
            if(newLanguage.equals("Bosanski")) {
                Locale.setDefault(new Locale("bs", "BA"));
                try {
                    loadScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Locale.setDefault(new Locale("en", "US"));
                try {
                    loadScene();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void initializeInspectionsTable () {
        completedInspections.setItems(listOfTechnicalInspections);
        vehicleOwnerCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVehicle().getVehicleOwner()));
        vehicleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVehicle().getBrand()));
        responsiblePersonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUser().getName() + " " + data.getValue().getUser().getSurname()));
        vehicleTypeCol.setCellValueFactory(new PropertyValueFactory("inspectionType"));
    }

    public void archiveInspection(ActionEvent actionEvent) {
        TechnicalInspection inspection = completedInspections.getSelectionModel().getSelectedItem();
        if (inspection == null) return;
        dao.updateInspection(inspection.getId(), inspection.getInspectionType(), inspection.getUser(), inspection.getVehicle(), WarrantState.IN_ARCHIVE);
        listOfTechnicalInspections.setAll(FXCollections.observableArrayList(dao.inspectionsDone()));
        completedInspections.setItems(listOfTechnicalInspections);
    }

    public void loadScene() throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("Translation");
        Stage stage = (Stage) tabPane.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader( getClass().getResource("/fxml/main.fxml" ), bundle);
        stage.setScene(new Scene(loader.load()));
    }

    public void openWindowAddUser() {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("Translation");
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/addUser.fxml"), bundle);
            Parent root = fxmlLoader.load();
            Stage newStage = new Stage();
            newStage.setTitle(ResourceBundle.getBundle("Translation").getString("adduser"));
            newStage.setScene(new Scene(root));
            newStage.setResizable(false);
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logOut (ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
        Stage primaryStage = new Stage();
        ResourceBundle bundle = ResourceBundle.getBundle("Translation");
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"), bundle);
        primaryStage.setTitle("Auto kuća Ada");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public void showArchivedAccounts(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportArchivedAccounts(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showAvailableEquipment(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportAvailableEquipment(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showCommonFailures(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportCommonFailures(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showCompletedExaminationsPerWorker(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportCompletedExaminationsPerWorker(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showCompletedInspections(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportCompletedInspections(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showLastDayExaminations(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportLastDayExaminations(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showLastMonthExaminations(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportLastMonthExaminations(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showLastYearExaminations(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportLastYearExaminations(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showListOfWorker(ActionEvent actionEvent) {
        try {
            new PrintReports().showReportListOfWorker(InspectionDAO.getConnection());
        } catch (JRException e1) {
            e1.printStackTrace();
        }
    }

    public void showPercentageOfPassingInspection(ActionEvent actionEvent) {
            try {
                new PrintReports().showReportPercentageOfPassingInspection(InspectionDAO.getConnection());
            } catch (JRException e1) {
                e1.printStackTrace();
            }
        }
}
