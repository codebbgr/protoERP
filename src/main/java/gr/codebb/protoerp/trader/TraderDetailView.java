/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 09/04/2021 (gmoralis) - Fixed mitroo crendentals
 * 08/04/2021 (gmoralis) - Used stored company's mitroo codes
 * 25/03/2021 (gmoralis) - Copied from company  with minimal changes
 */
package gr.codebb.protoerp.trader;

import eu.taxofficer.protoerp.tables.entities.DoyEntity;
import eu.taxofficer.protoerp.tables.queries.DoyQueries;
import gr.codebb.ctl.CbbClearableTextField;
import gr.codebb.dlg.AlertDlg;
import gr.codebb.lib.crud.DetailCrud;
import gr.codebb.lib.crud.annotation.CheckBoxProperty;
import gr.codebb.lib.crud.annotation.TextFieldProperty;
import gr.codebb.lib.crud.cellFactory.CheckBoxFactory;
import gr.codebb.lib.crud.cellFactory.DisplayableListCellFactory;
import gr.codebb.lib.crud.services.ComboboxService;
import gr.codebb.lib.database.GenericDao;
import gr.codebb.lib.database.PersistenceManager;
import gr.codebb.lib.util.AlertDlgHelper;
import gr.codebb.lib.util.FxmlUtil;
import gr.codebb.protoerp.settings.company.CompanyUtil;
import gr.codebb.protoerp.settings.countries.CountriesQueries;
import gr.codebb.protoerp.settings.internetSettings.MitrooPassView;
import gr.codebb.protoerp.util.validation.CustomValidationDecoration;
import gr.codebb.protoerp.util.validation.Validators;
import gr.codebb.protoerp.util.validation.VatValidator;
import gr.codebb.webserv.mitroo.MitrooService;
import gr.codebb.webserv.mitroo.ResponsedCompanyKad;
import gr.codebb.webserv.mitroo.ResponsedMitrooData;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;

public class TraderDetailView implements Initializable {

  @FXML private StackPane mainStackPane;
  @FXML private VBox mainVBox;
  @FXML private TabPane tabPane;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textName;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textJob;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textVatNumber;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textEmail;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textMobilePhone;

  @FXML private SearchableComboBox<DoyEntity> doyCombo;
  @FXML private TextField textId;
  @FXML @CheckBoxProperty private CheckBox checkBoxActive;
  @FXML private StackPane mainStackPane1;
  @FXML private Button newPlantButton;
  @FXML private Button openPlantButton;
  @FXML private Button deletePlantButton;
  @FXML private TableView<TraderPlantsEntity> tablePlants;
  @FXML private TableColumn<TraderPlantsEntity, Long> columnId;
  @FXML private TableColumn<TraderPlantsEntity, Boolean> columnActive;
  @FXML private TableColumn<TraderPlantsEntity, Integer> columnCode;
  @FXML private TableColumn<TraderPlantsEntity, String> columnDescription;

  private MaskerPane masker = new MaskerPane();
  private ObservableList<TraderPlantsEntity> traderplantrow;
  private ValidationSupport validation;
  private final DetailCrud<TraderEntity> detailCrud = new DetailCrud<>(this);

  /** Initializes the controller class. */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    mainStackPane.getChildren().add(masker);
    masker.setVisible(false);
    new ComboboxService<>(DoyQueries.getDoyDatabase(true), doyCombo).start();
    DisplayableListCellFactory.setComboBoxCellFactory(doyCombo);

    // plants table
    columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
    columnCode.setCellValueFactory(new PropertyValueFactory<>("code"));
    columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    columnActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    columnActive.setCellFactory(new CheckBoxFactory<TraderPlantsEntity>().cellFactory);
    openPlantButton
        .disableProperty()
        .bind(Bindings.isEmpty(tablePlants.getSelectionModel().getSelectedItems()));
    deletePlantButton
        .disableProperty()
        .bind(Bindings.isEmpty(tablePlants.getSelectionModel().getSelectedItems()));
    EventHandler plantDoubleMouseClick =
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent e) {
            if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
              openPlantAction(null);
            }
          }
        };

    tablePlants.addEventHandler(MouseEvent.MOUSE_PRESSED, plantDoubleMouseClick);
    Platform.runLater(
        () -> {
          this.validation = new ValidationSupport();
          this.validation.setValidationDecorator(new CustomValidationDecoration());
          Validators.createValidators();
          registerValidators();
        });
    traderplantrow = FXCollections.observableArrayList();
    traderplantrow.addListener(
        new ListChangeListener<TraderPlantsEntity>() {
          @Override
          public void onChanged(
              javafx.collections.ListChangeListener.Change<? extends TraderPlantsEntity> c) {
            // System.out.println("Changed on " + c);//c.getFrom -> line that did the change
            if (c.next()) {
              tablePlants.setItems(traderplantrow);
            }
          }
        });
  }

  private void registerValidators() {
    validation.registerValidator(
        textName,
        Validators.combine(
            Validators.notEmptyValidator(), Validators.onlyLettersValidator(Severity.WARNING)));
    validation.registerValidator(textJob, false, Validators.onlyLettersValidator(Severity.WARNING));
    validation.registerValidator(
        textMobilePhone, false, Validators.onlyNumbersValidator(Severity.WARNING));
    validation.registerValidator(doyCombo, Validators.notEmptyValidator());
    validation.registerValidator(
        textEmail,
        Validators.combine(
            Validators.notEmptyValidator(), Validators.emailValidator(Severity.WARNING)));
    validation.registerValidator(
        textVatNumber,
        Validators.combine(
            Validators.notEmptyValidator(),
            Validators.onlyNumbersValidator(Severity.ERROR),
            new VatValidator()));
  }

  public void newTrader() {
    checkBoxActive.setSelected(true);
  }

  @FXML
  private void onTaxisUpdate(ActionEvent event) {
    if (CompanyUtil.getCurrentCompany().getMitroo_username() == null
        || CompanyUtil.getCurrentCompany().getMitroo_username().isEmpty()) {
      ButtonType response =
          AlertDlg.create()
              .message(
                  "Δεν βρέθηκαν κωδικοι για τη εφαρμογή του μητρώου\nΘέλετε να αποθηκεύσετε τώρα?")
              .title("Αποθήκευση κωδικών")
              .owner(masker.getScene().getWindow())
              .modality(Modality.APPLICATION_MODAL)
              .showAndWaitConfirm();
      if (response == ButtonType.OK) {
        FxmlUtil.LoadResult<MitrooPassView> getDetailView =
            FxmlUtil.load("/fxml/settings/internetServices/MitrooPass.fxml");
        Alert alert =
            AlertDlgHelper.saveDialog(
                "Κωδικοί Μητρώου", getDetailView.getParent(), masker.getScene().getWindow());
        Button okbutton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
          if (getDetailView.getController() != null) {
            getDetailView.getController().save();
          }
        }
      } else {
        return;
      }
    }
    loadService();
  }

  @FXML
  private void newPlantAction(ActionEvent event) {
    FxmlUtil.LoadResult<TraderPlantsDetailView> getDetailView =
        FxmlUtil.load("/fxml/trader/TraderPlantsDetailView.fxml");
    Alert alert =
        AlertDlgHelper.saveDialog(
            "Προσθήκη Κεντρικόυ-υποκαταστήματος",
            getDetailView.getParent(),
            mainStackPane.getScene().getWindow());
    Button okbutton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okbutton.addEventFilter(
        ActionEvent.ACTION,
        (event1) -> {
          if (!getDetailView.getController().validateControls()) {
            event1.consume();
          }
        });
    if (traderplantrow.isEmpty()) { // if no rows create edra
      getDetailView.getController().setNewPlant(true);
    } else {
      getDetailView.getController().setNewPlant(false);
    }

    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      if (getDetailView.getController() != null) {
        TraderPlantsEntity p = getDetailView.getController().saveNewPlant();
        traderplantrow.add(p);
      }
    }
  }

  @FXML
  private void openPlantAction(ActionEvent event) {
    FxmlUtil.LoadResult<TraderPlantsDetailView> getDetailView =
        FxmlUtil.load("/fxml/trader/TraderPlantsDetailView.fxml");
    Alert alert =
        AlertDlgHelper.editDialog(
            "Άνοιγμα/Επεξεργασία Κεντρικόυ-υποκαταστήματος",
            getDetailView.getParent(),
            mainStackPane.getScene().getWindow());
    getDetailView.getController().fillData(tablePlants.getSelectionModel().getSelectedItem());
    Button okbutton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okbutton.addEventFilter(
        ActionEvent.ACTION,
        (event1) -> {
          if (!getDetailView.getController().validateControls()) {
            event1.consume();
          }
        });
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      if (getDetailView.getController() != null) {
        int row = tablePlants.getSelectionModel().getSelectedIndex();
        TraderPlantsEntity selected = tablePlants.getSelectionModel().getSelectedItem();
        TraderPlantsEntity p = getDetailView.getController().saveEdit(traderplantrow.get(row));
        traderplantrow.remove(selected);
        traderplantrow.add(p);
      }
    }
  }

  @FXML
  private void deletePlantAction(ActionEvent event) {
    ButtonType response =
        AlertDlg.create()
            .message(
                "Είστε σιγουροι ότι θέλετε να διαγράψετε την εγκατάσταση : \n"
                    + tablePlants.getSelectionModel().getSelectedItem().getDescription())
            .title("Διαγραφή")
            .modality(Modality.APPLICATION_MODAL)
            .owner(tablePlants.getScene().getWindow())
            .showAndWaitConfirm();
    if (response == ButtonType.OK) {
      TraderPlantsEntity selected = tablePlants.getSelectionModel().getSelectedItem();
      traderplantrow.remove(selected);
    }
  }

  public void fillData(TraderEntity e) {

    detailCrud.loadModel(e);
    textId.setText(Long.toString(e.getId()));
    doyCombo.getSelectionModel().select(e.getDoy());
    for (TraderPlantsEntity a : e.getPlantLines()) {
      traderplantrow.add(a);
    }
  }

  private void loadService() {
    masker.setVisible(true);
    masker.setText("Ανάκτηση στοιχείων απο gsis\nΠαρακαλώ περιμένετε");
    Task<String> service =
        new Task<String>() {
          @Override
          protected String call() throws Exception {
            try {
              // Create a trust manager that does not validate certificate chains
              final TrustManager[] trustAllCerts =
                  new TrustManager[] {
                    new X509TrustManager() {
                      @Override
                      public void checkClientTrusted(
                          final X509Certificate[] chain, final String authType) {}

                      @Override
                      public void checkServerTrusted(
                          final X509Certificate[] chain, final String authType) {}

                      @Override
                      public X509Certificate[] getAcceptedIssuers() {
                        return null;
                      }
                    }
                  };

              // Install the all-trusting trust manager
              final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
              sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
              // Create an ssl socket factory with our all-trusting manager
              final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
              HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            } catch (final Exception e) {
              e.printStackTrace();
            }
            MitrooService sc = new MitrooService();
            // use the TLSv1.2 protocol
            System.setProperty("https.protocols", "TLSv1.2");
            // call to web service
            ResponsedMitrooData returnValue =
                sc.getData(
                    CompanyUtil.getCurrentCompany().getMitroo_username(),
                    CompanyUtil.getCurrentCompany().getMitroo_password(),
                    textVatNumber.getText(),
                    CompanyUtil.getCurrentCompany().getMitroo_vatRepresentant(),
                    new Date());
            if ((returnValue.getErrordescr() == null) || (returnValue.getErrordescr().isEmpty())) {
              textName.setText(returnValue.getName());
              Platform.runLater(
                  () -> {
                    doyCombo
                        .getSelectionModel()
                        .select(DoyQueries.getDoyByCode(returnValue.getDoyCode()));
                  });
              TraderPlantsEntity p = new TraderPlantsEntity();
              p.setCode(0);
              p.setDescription("Ἐδρα");
              p.setAddress(returnValue.getAddress());
              p.setCity(returnValue.getAddressArea());
              p.setTk(returnValue.getTk());
              p.setActive(true);
              p.setArea("");
              p.setFax("");
              p.setPhone("");
              p.setCountry(CountriesQueries.getCountryByCode("GR"));
              // kirios kadi
              for (ResponsedCompanyKad kad : returnValue.getDrastir()) {
                if (kad.getEidosDescr().matches("ΚΥΡΙΑ")) {
                  textJob.setText(kad.getPerigrafi());
                  traderplantrow.add(p);
                }
              }
            } else {
              return returnValue.getErrorcode() + ":" + returnValue.getErrordescr();
            }
            return null;
          }
        };
    service.setOnSucceeded(
        new EventHandler<WorkerStateEvent>() {
          @Override
          public void handle(WorkerStateEvent t) {
            masker.setVisible(false);
            if (service.getValue() != null) // error occured
            {
              String[] msg = service.getValue().split(":");
              AlertDlg.create()
                  .type(AlertDlg.Type.ERROR)
                  .message(msg[1])
                  .title(msg[0])
                  .owner(masker.getScene().getWindow())
                  .modality(Modality.APPLICATION_MODAL)
                  .showAndWait();
            }
          }
        });
    service.setOnFailed(
        new EventHandler<WorkerStateEvent>() {
          @Override
          public void handle(WorkerStateEvent t) {
            masker.setVisible(false);
            if (service.getValue() != null) // error occured
            {
              String[] msg = service.getValue().split(":");
              AlertDlg.create()
                  .type(AlertDlg.Type.ERROR)
                  .message(msg[1])
                  .title(msg[0])
                  .owner(masker.getScene().getWindow())
                  .modality(Modality.APPLICATION_MODAL)
                  .showAndWait();
            }
          }
        });
    Thread thread = new Thread(service);
    thread.start();
  }

  public boolean validateControls() {
    if (validation.isInvalid()) {
      Validators.showValidationResult(validation);
      AlertDlg.create()
          .type(AlertDlg.Type.ERROR)
          .message("Ελέξτε την φόρμα για λάθη")
          .title("Πρόβλημα")
          .owner(doyCombo.getScene().getWindow())
          .modality(Modality.APPLICATION_MODAL)
          .showAndWait();
      return false;
    }
    return true;
  }

  public void SaveNewTrader() {
    GenericDao gdao = new GenericDao(TraderEntity.class, PersistenceManager.getEmf());
    detailCrud.saveModel(new TraderEntity());
    TraderEntity trader = detailCrud.getModel();
    trader.setDoy(doyCombo.getSelectionModel().getSelectedItem());
    trader.setCompany(CompanyUtil.getCurrentCompany());
    traderplantrow.forEach(
        (plantpos) -> {
          trader.addPlantLine(plantpos);
        });
    TraderEntity saved = (TraderEntity) gdao.createEntity(trader);
    textId.setText(Long.toString(saved.getId()));
  }

  public void SaveEditTrader() {
    GenericDao gdao = new GenericDao(TraderEntity.class, PersistenceManager.getEmf());
    detailCrud.saveModel((TraderEntity) gdao.findEntity(Long.valueOf(textId.getText())));
    TraderEntity cp = detailCrud.getModel();
    cp.getPlantLines().clear();
    traderplantrow.forEach(
        (plantpos) -> {
          cp.addPlantLine(plantpos);
        });
    gdao.updateEntity(cp);
  }
}
