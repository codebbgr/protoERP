/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 06/04/2021 - Validation for vatexempion
 * 06/04/2021 - Work on validation
 * 05/04/2021 - Initial
 */
package gr.codebb.protoerp.items;

import gr.codebb.ctl.CbbBigDecimal;
import gr.codebb.ctl.CbbClearableTextField;
import gr.codebb.dlg.AlertDlg;
import gr.codebb.lib.crud.DetailCrud;
import gr.codebb.lib.crud.annotation.CheckBoxProperty;
import gr.codebb.lib.crud.annotation.TextAreaProperty;
import gr.codebb.lib.crud.annotation.TextFieldProperty;
import gr.codebb.lib.crud.cellFactory.DisplayableListCellFactory;
import gr.codebb.lib.crud.services.ComboboxService;
import gr.codebb.lib.database.GenericDao;
import gr.codebb.lib.database.PersistenceManager;
import gr.codebb.lib.util.DecimalDigits;
import gr.codebb.lib.util.NumberUtil;
import gr.codebb.protoerp.mydata.masterdata.MasterDataQueries;
import gr.codebb.protoerp.mydata.masterdata.VatmdExemptionEntity;
import gr.codebb.protoerp.settings.SettingsHelper;
import gr.codebb.protoerp.settings.company.CompanyUtil;
import gr.codebb.protoerp.tables.measurementUnits.MeasurementUnitsEntity;
import gr.codebb.protoerp.tables.measurementUnits.MeasurementUnitsQueries;
import gr.codebb.protoerp.tables.vat.VatEntity;
import gr.codebb.protoerp.tables.vat.VatQueries;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import lombok.Getter;
import net.synedra.validatorfx.Validator;
import org.controlsfx.control.SearchableComboBox;

public class ItemsDetailView implements Initializable {

  @FXML @Getter private StackPane mainStackPane;
  @FXML private TextField textId;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textCode;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textBarcode;

  @FXML
  @TextFieldProperty(type = TextFieldProperty.Type.STRING)
  private CbbClearableTextField textDescription;

  @FXML @CheckBoxProperty private CheckBox checkBoxActive;
  @FXML private ComboBox<ItemsType> comboItemType;
  @FXML private ComboBox<MeasurementUnitsEntity> comboMeasureUnit;
  @FXML private ComboBox<VatEntity> comboVatSell;
  @FXML private CbbBigDecimal bdecSellPrice;
  @FXML private CbbBigDecimal bdecSellPriceWithVat;
  @FXML @TextAreaProperty private TextArea textAreaNotes;
  @FXML private Label stockLabel;
  @FXML private SearchableComboBox<VatmdExemptionEntity> comboVatExemp;

  private final DetailCrud<ItemsEntity> detailCrud = new DetailCrud<>(this);
  private Validator validator = new Validator();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    new ComboboxService<>(MasterDataQueries.getMdVatExemptionDatabase(true), comboVatExemp).start();
    DisplayableListCellFactory.setComboBoxCellFactory(comboVatExemp);
    new ComboboxService<>(
            MeasurementUnitsQueries.getMeasurementsUnitsDatabase(true), comboMeasureUnit)
        .start();
    DisplayableListCellFactory.setComboBoxCellFactory(comboMeasureUnit);
    new ComboboxService<>(VatQueries.getVatDatabase(true), comboVatSell).start();
    DisplayableListCellFactory.setComboBoxCellFactory(comboVatSell);
    comboItemType.getItems().addAll(ItemsType.getNames());
    DisplayableListCellFactory.setComboBoxCellFactory(comboItemType);

    // bigDecimal
    bdecSellPrice.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.UNIT.getSettingName())));
    bdecSellPriceWithVat.initBigDecimal(
        BigDecimal.ZERO,
        new DecimalFormat(DecimalDigits.getDecimalFormat(DecimalDigits.UNIT.getSettingName())));

    bdecSellPrice
        .focusedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              forologisiSell();
            });

    bdecSellPriceWithVat
        .focusedProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              apoforologisiSell();
            });
    // if you use barcode scanner which "enter" after scan just ignore it
    textBarcode.setOnKeyPressed(
        (KeyEvent keyEvent) -> {
          if (keyEvent.getCode() == KeyCode.ENTER) {
            keyEvent.consume();
          }
        });
    // enable/disable vatexemption if vat is zero or anthing else
    comboVatSell
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (options, oldValue, newValue) -> {
              if (newValue.getVatRate().compareTo(BigDecimal.ZERO) == 0) // activate vatexmp
              {
                comboVatExemp.setDisable(false);
              } else {
                comboVatExemp.setDisable(true);
                comboVatExemp.getSelectionModel().select(null);
              }
            });
    validator
        .createCheck()
        .dependsOn("code", textCode.textProperty())
        .withMethod(
            c -> {
              String code = c.get("code");
              if (code.isEmpty()) {
                c.error("Ο Κωδικός δεν μπορεί να είναι κενός");
              }
            })
        .decorates(textCode)
        .immediate();
    validator
        .createCheck()
        .dependsOn("description", textDescription.textProperty())
        .withMethod(
            c -> {
              String description = c.get("description");
              if (description.isEmpty()) {
                c.error("Η περιγραφή δεν μπορεί να είναι κενή");
              }
            })
        .decorates(textDescription)
        .immediate();

    validator
        .createCheck()
        .dependsOn("itemtype", comboItemType.valueProperty())
        .withMethod(
            c -> {
              ItemsType item = c.get("itemtype");
              if (item == null) {
                c.error("Ο τύπος δεν μπορεί να είναι κενός");
              }
            })
        .decorates(comboItemType)
        .immediate();
    validator
        .createCheck()
        .dependsOn("vat", comboVatSell.valueProperty())
        .withMethod(
            c -> {
              VatEntity vat = c.get("vat");
              if (vat == null) {
                c.error("Η κατηγορία φ.π.α. είναι υποχρεωτική");
              }
            })
        .decorates(comboVatSell)
        .immediate();

    validator
        .createCheck()
        .dependsOn("measureunit", comboMeasureUnit.valueProperty())
        .withMethod(
            c -> {
              MeasurementUnitsEntity measure = c.get("measureunit");
              if (measure == null) {
                c.error("Η μονάδα μέτρησης είναι υποχρεωτική");
              }
            })
        .decorates(comboMeasureUnit)
        .immediate();

    validator
        .createCheck()
        .dependsOn("vatexemp", comboVatExemp.disabledProperty())
        .withMethod(
            c -> {
              boolean vatex = c.get("vatexemp");
              if (!vatex) {
                c.error("Η Αιτία εξαίρεσης ειναι υποχρεωτικη");
              }
            })
        .decorates(comboVatExemp)
        .immediate();

    checkBoxActive.setSelected(true); // if new then it is active
  }

  private void forologisiSell() {
    if (comboVatSell.getSelectionModel().getSelectedItem() != null) {
      VatEntity ent = comboVatSell.getSelectionModel().getSelectedItem();
      bdecSellPriceWithVat.setNumber(
          NumberUtil.forologisi(
              bdecSellPrice.getNumber(),
              ent.getVatRate(),
              SettingsHelper.loadIntegerSetting("unitDecimal")));
    } else {
      bdecSellPriceWithVat.setNumber(BigDecimal.ZERO);
    }
  }

  private void apoforologisiSell() {
    if (comboVatSell.getSelectionModel().getSelectedItem() != null) {
      VatEntity ent = comboVatSell.getSelectionModel().getSelectedItem();
      bdecSellPrice.setNumber(
          NumberUtil.apoforologisi(
              bdecSellPriceWithVat.getNumber(),
              ent.getVatRate(),
              SettingsHelper.loadIntegerSetting("unitDecimal")));
    } else {
      bdecSellPrice.setNumber(BigDecimal.ZERO);
    }
  }

  public boolean validateControls() {
    validator.validate();
    if (validator.containsErrors()) {
      AlertDlg.create()
          .type(AlertDlg.Type.ERROR)
          .message("Ελέξτε την φόρμα για λάθη")
          .title("Πρόβλημα")
          .owner(textDescription.getScene().getWindow())
          .modality(Modality.APPLICATION_MODAL)
          .showAndWait();
      return false;
    }
    return true;
  }

  public void fillData(ItemsEntity e) {
    detailCrud.loadModel(e);
    textId.setText(e.getId().toString());
    // comboboxes
    comboVatSell.getSelectionModel().select(e.getVatSell());
    comboVatExemp.getSelectionModel().select(e.getVatExemp());
    comboMeasureUnit.getSelectionModel().select(e.getMeasureUnit());
    comboItemType.getSelectionModel().select(e.getItemType());
    // prices
    bdecSellPrice.setNumber(e.getSellPrice());
    forologisiSell();
  }

  public void save() {
    GenericDao gdao = new GenericDao(ItemsEntity.class, PersistenceManager.getEmf());
    detailCrud.saveModel(new ItemsEntity());
    ItemsEntity item = detailCrud.getModel();
    // non-support on detailCrud
    item.setMeasureUnit(comboMeasureUnit.getSelectionModel().getSelectedItem());
    item.setVatSell(comboVatSell.getSelectionModel().getSelectedItem());
    item.setItemType(comboItemType.getSelectionModel().getSelectedItem());
    item.setSellPrice(bdecSellPrice.getNumber());
    item.setCompany(CompanyUtil.getCurrentCompany());
    ItemsEntity saved = (ItemsEntity) gdao.createEntity(item);
    textId.setText(Long.toString(saved.getId()));
  }

  public void saveEdit() {
    GenericDao gdao = new GenericDao(ItemsEntity.class, PersistenceManager.getEmf());
    detailCrud.saveModel((ItemsEntity) gdao.findEntity(Long.valueOf(textId.getText())));
    ItemsEntity item = detailCrud.getModel();
    // non-support on detailCrud
    item.setMeasureUnit(comboMeasureUnit.getSelectionModel().getSelectedItem());
    item.setVatSell(comboVatSell.getSelectionModel().getSelectedItem());
    item.setItemType(comboItemType.getSelectionModel().getSelectedItem());
    item.setSellPrice(bdecSellPrice.getNumber());
    item.setCompany(CompanyUtil.getCurrentCompany());
    gdao.updateEntity(item);
  }
}
