/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
package gr.codebb.protoerp.invoices;

import eu.taxofficer.lib.jasper.viewer.JRFXPrintPreview;
import gr.codebb.codebblib.validatorfx.Validator;
import gr.codebb.ctl.CbbBigDecimalLabel;
import gr.codebb.ctl.cbbDateTimePicker.CbbDateTimePicker;
import gr.codebb.dlg.AlertDlg;
import gr.codebb.lib.crud.cellFactory.BigDecimalFactory;
import gr.codebb.lib.crud.cellFactory.DisplayableListCellFactory;
import gr.codebb.lib.crud.services.ComboboxService;
import gr.codebb.lib.database.GenericDao;
import gr.codebb.lib.database.PersistenceManager;
import gr.codebb.lib.util.AlertDlgHelper;
import gr.codebb.lib.util.AlertHelper;
import gr.codebb.lib.util.DecimalDigits;
import gr.codebb.lib.util.FxmlUtil;
import gr.codebb.lib.util.NumberUtil;
import gr.codebb.lib.util.TableViewUtil;
import gr.codebb.protoerp.paymentMethods.PaymentMethodsEntity;
import gr.codebb.protoerp.paymentMethods.PaymentMethodsQueries;
import gr.codebb.protoerp.settings.SettingsHelper;
import gr.codebb.protoerp.settings.company.CompanyUtil;
import gr.codebb.protoerp.tables.InvoiceTypes.InvoiceTypesEntity;
import gr.codebb.protoerp.trader.TraderEntity;
import gr.codebb.protoerp.trader.TraderPlantsEntity;
import gr.codebb.protoerp.trader.TraderQueries;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import lombok.Getter;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.controlsfx.control.SearchableComboBox;

public class Invoice1DetailView implements Initializable {

  @FXML @Getter private Label invoiceTypeLabel;
  @FXML private Label plantLabel;
  @FXML private SearchableComboBox<TraderEntity> traderCombo;
  @FXML private CbbDateTimePicker dateTimePicker;

  private InvoiceTypesEntity invoiceType;
  @FXML private ComboBox<TraderPlantsEntity> traderPlantCombo;
  @FXML private Button invoiceLinesNewButton;
  @FXML private Button invoiceLinesEditButton;
  @FXML private Button invoiceLinesDeleteButton;
  @FXML private TableView<InvoiceLinesEntity> invoiceLinesTable;
  @FXML private TableColumn<InvoiceLinesEntity, String> linesCodeCol;
  @FXML private TableColumn<InvoiceLinesEntity, String> linesBarcodeCol;
  @FXML private TableColumn<InvoiceLinesEntity, String> linesEidosCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesFPACol;
  @FXML private TableColumn<InvoiceLinesEntity, Integer> linesSeiraEmCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesMonCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesPosotCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesTimMonCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesValueBeforeDiscCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesDiscPerCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesDiscValueCol;
  @FXML private TableColumn<InvoiceLinesEntity, BigDecimal> linesValueCol;
  @FXML private CbbBigDecimalLabel total_no_disc;
  @FXML private CbbBigDecimalLabel total_disc;
  @FXML private CbbBigDecimalLabel total_no_vat;
  @FXML private CbbBigDecimalLabel vat;
  @FXML private CbbBigDecimalLabel total_with_vat;
  @FXML private CbbBigDecimalLabel pliroteo;
  @FXML private SearchableComboBox<PaymentMethodsEntity> paymentCombo;

  private ObservableList<InvoiceLinesEntity> invoicerow;
  @FXML private TextField textRelativeInvoices;
  @FXML private TextField textInvoiceNumber;
  @FXML private Label invoiceStatusLabel;
  @FXML @Getter private Button printButton;

  private Validator validator = new Validator();
  private InvoicesEntity currentInvoice = null;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    new ComboboxService<>(TraderQueries.getTradersPerCompany(), traderCombo).start();
    DisplayableListCellFactory.setComboBoxCellFactory(traderCombo);
    new ComboboxService<>(PaymentMethodsQueries.getPaymentMethodsDatabase(true), paymentCombo)
        .start();
    DisplayableListCellFactory.setComboBoxCellFactory(paymentCombo);

    total_no_disc.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_disc.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_no_vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_with_vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    pliroteo.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));

    traderCombo
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (options, oldValue, newValue) -> {
              traderPlantCombo.getSelectionModel().clearSelection();
              List<TraderPlantsEntity> tplants = TraderQueries.getTraderPlants(newValue);
              Platform.runLater(
                  () -> {
                    if (newValue != null) {
                      new ComboboxService<>(tplants, traderPlantCombo).start();
                      DisplayableListCellFactory.setComboBoxCellFactory(traderPlantCombo);
                      if (tplants.size() == 1) // select if it is only one
                      {
                        traderPlantCombo.getSelectionModel().select(tplants.get(0));
                      }
                    }
                  });
            });
    intializeInvoiceLineTableHeader();
    invoiceLinesSetup();
    initialazeTotalFields();

    invoicerow = FXCollections.observableArrayList();
    invoicerow.addListener(
        new ListChangeListener<InvoiceLinesEntity>() {
          @Override
          public void onChanged(Change<? extends InvoiceLinesEntity> c) {
            // c.getFrom -> line that did the change
            /*if (c.next()) {
                 System.out.println("Changed on " + c.getFrom());
              invoiceLinesTable.getItems().add(c.getFrom(),invoicerow.get(c.getFrom()));
              c.g
            }*/
            while (c.next()) {
              if (c.wasPermutated()) {
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                  System.out.println("permuted");
                }
              } else if (c.wasUpdated()) {
                System.out.println("updated");
              } else {
                for (InvoiceLinesEntity remitem : c.getRemoved()) {
                  invoiceLinesTable.getItems().remove(remitem);
                  CalculateTotals();
                }
                for (InvoiceLinesEntity additem : c.getAddedSubList()) {
                  invoiceLinesTable.getItems().add(additem);
                  CalculateTotals();
                }
              }
            }
          }
        });

    /* validator
    .createCheck()
    .dependsOn("date", dateTimePicker.valueProperty())
    .withMethod(
        c -> {
          LocalDate date = c.get("date");
          if (date == null) {
            c.error("Η ημερομηνία δεν μπορεί να είναι κενή");
          }
        })
    .decorates(dateTimePicker)
    .immediate();*/
    validator
        .createCheck()
        .dependsOn("trader", traderCombo.valueProperty())
        .withMethod(
            c -> {
              TraderEntity trader = c.get("trader");
              if (trader == null) {
                c.error("Ο Συναλλασσόμενος δεν μπορεί να είναι κενός");
              }
            })
        .decorates(traderCombo)
        .immediate();
    validator
        .createCheck()
        .dependsOn("traderplant", traderPlantCombo.valueProperty())
        .withMethod(
            c -> {
              TraderPlantsEntity traderplant = c.get("traderplant");
              if (traderplant == null) {
                c.error("Η εγκατάσταση δεν μπορεί να είναι κενή");
              }
            })
        .decorates(traderPlantCombo)
        .immediate();

    validator
        .createCheck()
        .dependsOn("payment", paymentCombo.valueProperty())
        .withMethod(
            c -> {
              PaymentMethodsEntity payment = c.get("payment");
              if (payment == null) {
                c.error("Ο τρόπος πληρωμής είναι υποχρεωτικός");
              }
            })
        .decorates(paymentCombo)
        .immediate();
  }

  public void newInvoice(InvoiceTypesEntity invoiceType) {
    setInvoiceType(invoiceType);
    dateTimePicker.setDateTimeValue(LocalDateTime.now());
  }

  public void setCurrentInvoice(InvoicesEntity invoice) {
    this.currentInvoice = invoice;
  }

  public void fillData(InvoicesEntity invoice) {
    invoicerow.clear();
    printButton.setDisable(false); // button is enabled on edit mode
    invoiceStatusLabel.setText(invoice.getInvoiceStatusS());
    setInvoiceType(invoice.getInvoiceType());
    dateTimePicker.setDateTimeValue(invoice.getDateCreated());
    textInvoiceNumber.setText(invoice.getInvoiceNumber().toString());
    traderCombo.getSelectionModel().select(invoice.getTraderPlant().getTrader());
    traderPlantCombo.getSelectionModel().select(invoice.getTraderPlant());
    textRelativeInvoices.setText(invoice.getRelativeInvoices());
    paymentCombo.getSelectionModel().select(invoice.getPayMethod());
    // get totals
    total_no_disc.setNumber(invoice.getTotalNoVatValue());
    total_disc.setNumber(invoice.getTotalDiscount());
    total_no_vat.setNumber(invoice.getTotalNoVatAfterDiscValue());
    vat.setNumber(invoice.getTotalVatValue());
    total_with_vat.setNumber(invoice.getTotalValue());
    pliroteo.setNumber(invoice.getTotalPayValue());
    for (InvoiceLinesEntity a : invoice.getInvoiceLines()) {
      invoicerow.add(a);
    }
  }

  public void setInvoiceType(InvoiceTypesEntity invoiceType) {
    this.invoiceType = invoiceType;
    if (invoiceType.getSeira() == null) {
      invoiceTypeLabel.setText(invoiceType.getShortName() + " - " + invoiceType.getName());
      invoiceTypeLabel.setUserData(invoiceType);
    } else {
      invoiceTypeLabel.setText(
          invoiceType.getShortName()
              + " - "
              + invoiceType.getName()
              + " (ΣΕΙΡΑ : "
              + invoiceType.getSeira()
              + " )");
      invoiceTypeLabel.setUserData(invoiceType);
    }
    plantLabel.setText(invoiceType.getPlantS());
  }

  @FXML
  private void invoiceLinesNewAction(ActionEvent event) {
    FxmlUtil.LoadResult<InvoiceLinesView> getDetailView =
        FxmlUtil.load("/fxml/invoices/InvoiceLinesView.fxml");
    Alert alert =
        AlertDlgHelper.saveDialog(
            "Προσθήκη Γραμμής Παραστατικού",
            getDetailView.getParent(),
            invoiceTypeLabel.getScene().getWindow());
    Button okbutton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okbutton.addEventFilter(
        ActionEvent.ACTION,
        (event1) -> {
          if (!getDetailView.getController().validateControls()) {
            event1.consume();
          } else {
            if (!(AlertHelper.saveConfirm(
                        getDetailView.getController().getTextCode().getScene().getWindow())
                    .get()
                == ButtonType.OK)) {
              event1.consume();
            }
          }
        });
    // add line position based on table size * 10
    getDetailView.getController().setPos(invoiceLinesTable.getItems().size() * 10);
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      if (getDetailView.getController() != null) {}
      invoicerow.add((getDetailView.getController().getResult()));
      Platform.runLater(() -> invoiceLinesTable.scrollTo(invoiceLinesTable.getItems().size() - 1));
    }
  }

  @FXML
  private void invoiceLinesEditAction(ActionEvent event) {
    FxmlUtil.LoadResult<InvoiceLinesView> getDetailView =
        FxmlUtil.load("/fxml/invoices/InvoiceLinesView.fxml");
    Alert alert =
        AlertDlgHelper.editDialog(
            "Μεταβολή Γραμμής Παραστατικού",
            getDetailView.getParent(),
            invoiceTypeLabel.getScene().getWindow());
    getDetailView
        .getController()
        .EditRecord(invoicerow, invoiceLinesTable.getSelectionModel().getSelectedIndex());
    Button okbutton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
    okbutton.addEventFilter(
        ActionEvent.ACTION,
        (event1) -> {
          if (!getDetailView.getController().validateControls()) {
            event1.consume();
          } else {
            if (!(AlertHelper.editConfirm(
                        getDetailView.getController().getTextCode().getScene().getWindow())
                    .get()
                == ButtonType.OK)) {
              event1.consume();
            }
          }
        });
    Optional<ButtonType> result = alert.showAndWait();
    if (result.get() == ButtonType.OK) {
      if (getDetailView.getController() != null) {
        InvoiceLinesEntity line = invoiceLinesTable.getSelectionModel().getSelectedItem();
        invoicerow.set(
            invoiceLinesTable.getSelectionModel().getSelectedIndex(),
            getDetailView.getController().saveEdited(line));
        invoiceLinesTable.sort();
        invoiceLinesTable.refresh();
        // invoiceLinesTable.scrollTo();
      }
    }
  }

  @FXML
  private void invoiceLinesDeleteAction(ActionEvent event) {
    ButtonType response =
        AlertDlg.create()
            .message(
                "Είστε σιγουροι ότι θέλετε να διαγράψετε την γραμμή : \n"
                    + invoiceLinesTable.getSelectionModel().getSelectedItem().getDescription())
            .title("Διαγραφή")
            .modality(Modality.APPLICATION_MODAL)
            .owner(invoiceLinesTable.getScene().getWindow())
            .showAndWaitConfirm();
    if (response == ButtonType.OK) {
      InvoiceLinesEntity selected = invoiceLinesTable.getSelectionModel().getSelectedItem();
      invoicerow.remove(selected);
    }
  }

  private void intializeInvoiceLineTableHeader() {
    linesCodeCol.setPrefWidth(80.0d);
    linesBarcodeCol.setPrefWidth(80.0d);
    linesEidosCol.setPrefWidth(240.0d);
    linesFPACol.setPrefWidth(60.0d);
    linesSeiraEmCol.setPrefWidth(100.0d);
    linesMonCol.setPrefWidth(100.0d);
    linesPosotCol.setPrefWidth(80.0d);
    linesTimMonCol.setPrefWidth(80.0d);
    linesValueBeforeDiscCol.setPrefWidth(80.0d);
    linesDiscPerCol.setPrefWidth(80.0d);
    linesDiscValueCol.setPrefWidth(80.0d);
    linesValueCol.setPrefWidth(80.0d);

    TableViewUtil.makeHeaderWrappable(linesSeiraEmCol, 2);
    TableViewUtil.makeHeaderWrappable(linesMonCol, 2);
    TableViewUtil.makeHeaderWrappable(linesPosotCol, 2);
    TableViewUtil.makeHeaderWrappable(linesTimMonCol, 2);
    TableViewUtil.makeHeaderWrappable(linesValueBeforeDiscCol, 2);
    TableViewUtil.makeHeaderWrappable(linesDiscPerCol, 2);
    TableViewUtil.makeHeaderWrappable(linesDiscValueCol, 2);
    TableViewUtil.makeHeaderWrappable(linesValueCol, 2);

    linesSeiraEmCol.setStyle("-fx-alignment: CENTER-RIGHT;");
  }

  private void invoiceLinesSetup() {
    // enable edit and delete button only if a line is selected
    invoiceLinesEditButton
        .disableProperty()
        .bind(Bindings.isEmpty(invoiceLinesTable.getSelectionModel().getSelectedItems()));
    invoiceLinesDeleteButton
        .disableProperty()
        .bind(Bindings.isEmpty(invoiceLinesTable.getSelectionModel().getSelectedItems()));
    invoiceLinesTable.setPlaceholder(new Label("Δεν υπάρχουν σειρές στο παραστατικό"));

    Platform.runLater(
        () -> {
          linesCodeCol.setCellValueFactory(new PropertyValueFactory("code"));
          linesBarcodeCol.setCellValueFactory(new PropertyValueFactory("barcode"));
          linesEidosCol.setCellValueFactory(new PropertyValueFactory("description"));
          linesFPACol.setCellValueFactory(new PropertyValueFactory("vatRate"));
          linesSeiraEmCol.setCellValueFactory(new PropertyValueFactory("posIndex"));
          linesMonCol.setCellValueFactory(new PropertyValueFactory("measureShortName"));
          linesPosotCol.setCellValueFactory(new PropertyValueFactory("quantity"));
          linesTimMonCol.setCellValueFactory(new PropertyValueFactory("unitPrice"));
          linesValueBeforeDiscCol.setCellValueFactory(new PropertyValueFactory("totalNoDisc"));
          linesDiscPerCol.setCellValueFactory(new PropertyValueFactory("percentDisc"));
          linesDiscValueCol.setCellValueFactory(new PropertyValueFactory("discount"));
          linesValueCol.setCellValueFactory(new PropertyValueFactory("total"));

          linesFPACol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(
                              DecimalDigits.PERCENT_VAT.getSettingName())))
                  .cellFactory);
          linesPosotCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(DecimalDigits.QUANTITY.getSettingName())))
                  .cellFactory);
          linesTimMonCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(DecimalDigits.UNIT.getSettingName())))
                  .cellFactory);
          linesValueCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())))
                  .cellFactory);
          linesValueBeforeDiscCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())))
                  .cellFactory);
          linesDiscPerCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(
                              DecimalDigits.PERCENT_DISC.getSettingName())))
                  .cellFactory);
          linesDiscValueCol.setCellFactory(
              new BigDecimalFactory<InvoiceLinesEntity>(
                      new DecimalFormat(
                          DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())))
                  .cellFactory);
        });
    // lines are sorted by position that appears in invoice
    invoiceLinesTable
        .sortPolicyProperty()
        .set(
            t -> {
              Comparator<InvoiceLinesEntity> comparator =
                  (r1, r2) -> r1.getPosIndex().compareTo(r2.getPosIndex());
              FXCollections.sort(invoiceLinesTable.getItems(), comparator);
              return true;
            });
  }

  private void initialazeTotalFields() {
    total_no_vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_with_vat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    pliroteo.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_no_disc.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
    total_disc.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.VALUES.getSettingName())));
  }

  private void CalculateTotals() {
    BigDecimal total = BigDecimal.ZERO;
    BigDecimal vat_total = BigDecimal.ZERO;
    BigDecimal totalnodisc = BigDecimal.ZERO;
    BigDecimal totaldisc = BigDecimal.ZERO;
    BigDecimal finaltotal;
    BigDecimal pliroteocal;

    for (InvoiceLinesEntity ipe : invoicerow) {
      total =
          NumberUtil.add(total, ipe.getTotal(), SettingsHelper.loadIntegerSetting("valuesDecimal"));
      BigDecimal _vat =
          NumberUtil.percent(
              ipe.getTotal(), ipe.getVatRate(), SettingsHelper.loadIntegerSetting("valuesDecimal"));
      vat_total =
          NumberUtil.add(vat_total, _vat, SettingsHelper.loadIntegerSetting("valuesDecimal"));
      totalnodisc =
          NumberUtil.add(
              totalnodisc,
              ipe.getTotalNoDisc(),
              SettingsHelper.loadIntegerSetting("valuesDecimal"));
      totaldisc =
          NumberUtil.add(
              totaldisc, ipe.getDiscount(), SettingsHelper.loadIntegerSetting("valuesDecimal"));
    }
    total_no_disc.setNumber(totalnodisc);
    total_disc.setNumber(totaldisc);
    total_no_vat.setNumber(total);
    vat.setNumber(vat_total);

    finaltotal =
        NumberUtil.add(total, vat_total, SettingsHelper.loadIntegerSetting("valuesDecimal"));
    total_with_vat.setNumber(finaltotal);

    pliroteocal = finaltotal;
    pliroteo.setNumber(pliroteocal);
  }

  public boolean validateControls() {
    validator.validate();
    if (validator.containsErrors()) {
      AlertDlg.create()
          .type(AlertDlg.Type.ERROR)
          .message("Ελέξτε την φόρμα για λάθη")
          .title("Πρόβλημα")
          .owner(traderPlantCombo.getScene().getWindow())
          .modality(Modality.APPLICATION_MODAL)
          .showAndWait();
      return false;
    }
    if (invoicerow.isEmpty()) {
      AlertHelper.errorDialog(
          traderPlantCombo.getScene().getWindow(), "Το παραστατικό δεν έχει γραμμές");
      return false;
    }
    return true;
  }

  public void saveNewInvoice() {
    GenericDao gdao = new GenericDao(InvoicesEntity.class, PersistenceManager.getEmf());
    InvoicesEntity invoice = new InvoicesEntity();
    // invoice.setDateCreated(
    //    LocalDateTime.now()); // save the creation time the time that user press save
    // dateTimePicker.setDateTimeValue(
    //    invoice.getDateCreated()); // and reload with the control with the new time
    invoice.setDateCreated(dateTimePicker.getDateTimeValue());
    invoice.setInvoiceType((InvoiceTypesEntity) invoiceTypeLabel.getUserData());
    invoice.setTraderPlant(traderPlantCombo.getSelectionModel().getSelectedItem());

    // save totals
    invoice.setTotalNoVatValue(total_no_disc.getNumber());
    invoice.setTotalDiscount(total_disc.getNumber());
    invoice.setTotalNoVatAfterDiscValue(total_no_vat.getNumber());
    invoice.setTotalVatValue(vat.getNumber());
    invoice.setTotalValue(total_with_vat.getNumber());
    invoice.setTotalPayValue(pliroteo.getNumber());
    // pay method
    invoice.setPayMethod(paymentCombo.getSelectionModel().getSelectedItem());

    // invoice number is set to 0
    invoice.setInvoiceNumber(0);
    textInvoiceNumber.setText(invoice.getInvoiceNumber().toString());
    // invoice is temp
    invoice.setInvoiceStatus(InvoiceStatus.TEMP);
    invoiceStatusLabel.setText(invoice.getInvoiceStatus().toString());

    invoice.setRelativeInvoices(textRelativeInvoices.getText());
    invoice.setCompany(CompanyUtil.getCurrentCompany());

    invoicerow.forEach(
        (invoicepos) -> {
          invoice.addInvoiceLine(invoicepos);
        });
    InvoicesEntity saved = (InvoicesEntity) gdao.createEntity(invoice);
    setCurrentInvoice(saved); // to be able to print
  }

  public void editInvoice(InvoicesEntity invoice) {
    if (invoice.getInvoiceStatus() != InvoiceStatus.TEMP) { // only temp can be modified
      AlertHelper.errorDialog(
          traderPlantCombo.getScene().getWindow(),
          "Το παραστατικό έχει οριστικοποιηθεί δεν επιτρέπονται αλλαγές");
      return;
    }
    invoice.setDateCreated(dateTimePicker.getDateTimeValue());
    invoice.setInvoiceType((InvoiceTypesEntity) invoiceTypeLabel.getUserData());
    invoice.setTraderPlant(traderPlantCombo.getSelectionModel().getSelectedItem());

    // save totals
    invoice.setTotalNoVatValue(total_no_disc.getNumber());
    invoice.setTotalDiscount(total_disc.getNumber());
    invoice.setTotalNoVatAfterDiscValue(total_no_vat.getNumber());
    invoice.setTotalVatValue(vat.getNumber());
    invoice.setTotalValue(total_with_vat.getNumber());
    invoice.setTotalPayValue(pliroteo.getNumber());
    // pay method
    invoice.setPayMethod(paymentCombo.getSelectionModel().getSelectedItem());

    // invoice number is set to 0
    invoice.setInvoiceNumber(0);
    textInvoiceNumber.setText(invoice.getInvoiceNumber().toString());
    // invoice is temp
    invoice.setInvoiceStatus(InvoiceStatus.TEMP);
    invoiceStatusLabel.setText(invoice.getInvoiceStatus().toString());

    invoice.setRelativeInvoices(textRelativeInvoices.getText());
    invoice.setCompany(CompanyUtil.getCurrentCompany());
    invoice.getInvoiceLines().clear();
    invoicerow.forEach(
        (invoicepos) -> {
          invoice.addInvoiceLine(invoicepos);
        });
    GenericDao gdao = new GenericDao(InvoicesEntity.class, PersistenceManager.getEmf());
    gdao.updateEntity(invoice);
  }

  @FXML
  private void printAction(ActionEvent event) {
    String formaektiposis = currentInvoice.getInvoiceType().getPrintFormVer();
    HashMap<String, Object> params = new HashMap<>();
    params.put(JRParameter.REPORT_LOCALE, new Locale("el", "GR"));
    JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
    jasperReportsContext.setProperty(
        "net.sf.jasperreports.default.pdf.font.name", "reports/sansserif.ttf");
    jasperReportsContext.setProperty("net.sf.jasperreports.default.pdf.encoding", "Identity-H");
    jasperReportsContext.setProperty("net.sf.jasperreports.default.pdf.embedded", "true");
    JasperReport jasperReport;
    JasperPrint jp = null;
    try {
      List<JasperPrint> list = new ArrayList<>();
      List<InvoicesEntity> listp = new ArrayList<>();
      listp.add(currentInvoice);
      jasperReport = (JasperReport) JRLoader.loadObject(new File(formaektiposis));
      JRBeanCollectionDataSource beanCollectionDataSource =
          new JRBeanCollectionDataSource(listp, false);

      JasperPrint jasperPrint1 =
          JasperFillManager.fillReport(jasperReport, params, beanCollectionDataSource);
      list.add(jasperPrint1);

      JRFXPrintPreview printPreview = null;
      printPreview = new JRFXPrintPreview(list, null);

      printPreview.show();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
