/*
 * copyright 2013-2020
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 29/11/2020 (georgemoralis) - More WIP
 * 06/11/2020 (georgemoralis) - Initial
 */
package gr.codebb.protoerp.userManagement;

import gr.codebb.ctl.cbbTableView.CbbTableView;
import gr.codebb.ctl.cbbTableView.columns.CbbBooleanTableColumn;
import gr.codebb.ctl.cbbTableView.columns.CbbLongTableColumn;
import gr.codebb.ctl.cbbTableView.columns.CbbStringTableColumn;
import gr.codebb.ctl.cbbTableView.columns.CbbTableColumn;
import gr.codebb.lib.crud.AbstractListView;
import gr.codebb.lib.crud.annotation.ColumnProperty;
import gr.codebb.lib.util.AlertDlgHelper;
import gr.codebb.lib.util.FxmlUtil;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

public class UsersView extends AbstractListView implements Initializable {

  @FXML private StackPane mainStackPane;
  @FXML private Button refreshButton;
  @FXML private Button newButton;
  @FXML private Button openButton;
  @FXML private Button deleteButton;
  @FXML private CbbTableView<UserEntity> usersTable;

  @ColumnProperty(prefWidth = "100.0d")
  CbbTableColumn<UserEntity, Long> columnId;

  @ColumnProperty(prefWidth = "100.0d")
  CbbTableColumn<UserEntity, Boolean> columnActive;

  @ColumnProperty(prefWidth = "180.0d")
  CbbTableColumn<UserEntity, String> columnName;

  @ColumnProperty(prefWidth = "120.0d")
  CbbTableColumn<UserEntity, String> columnUsername;

  /** Initializes the controller class. */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    columnId = new CbbLongTableColumn<>("Id");
    columnId.setCellValueFactory(new PropertyValueFactory<>("id"));
    columnName = new CbbStringTableColumn<>("Όνοματεπώνυμο");
    columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
    columnUsername = new CbbStringTableColumn<>("Όνομα Χρήστη");
    columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
    columnActive = new CbbBooleanTableColumn<>("Ενεργός");
    columnActive.setCellValueFactory(new PropertyValueFactory<>("active"));

    usersTable.getColumns().addAll(columnId, columnActive, columnUsername, columnName);

    init(this);
    selectWithService();
  }

  @FXML
  private void refreshAction(ActionEvent event) {}

  @FXML
  private void newAction(ActionEvent event) {
    FxmlUtil.LoadResult<UsersDetailView> getDetailView =
        FxmlUtil.load("/fxml/userManagement/UsersDetail.fxml");
    Alert alert =
        AlertDlgHelper.saveDialog(
            "Προσθήκη χρήστη", getDetailView.getParent(), mainStackPane.getScene().getWindow());
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
        getDetailView.getController().save();
        selectWithService();
      }
    }
  }

  @FXML
  protected void openAction(ActionEvent event) {}

  @FXML
  private void deleteAction(ActionEvent event) {}

  @Override
  protected CbbTableView getTableView() {
    return usersTable;
  }

  @Override
  protected List getMainQuery() {
    return UserQueries.getUsers();
  }

  @Override
  protected StackPane getMainStackPane() {
    return mainStackPane;
  }

  @Override
  protected Button getDeleteButton() {
    return deleteButton;
  }

  @Override
  protected Button getOpenButton() {
    return openButton;
  }
}
