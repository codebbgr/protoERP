/*
 * copyright 2013-2020
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 20/10/2020 (georgemoralis) - Added webserver send for issues
 * 20/10/2020 (georgemoralis) - Validation support using validatorfx lib
 * 20/10/2020 (georgemoralis) - Window implementation
 * 19/10/2020 (georgemoralis) - Initial commit
 */
package gr.codebb.protoerp.issues;

import static gr.codebb.lib.util.ThreadUtil.runAndWait;

import gr.codebb.codebblib.validatorfx.Validator;
import gr.codebb.ctl.CbbClearableTextField;
import gr.codebb.dlg.AlertDlg;
import gr.codebb.protoerp.MainSettings;
import gr.codebb.util.serial.Hardware;
import gr.codebb.webserv.rest.Util.Calls;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import org.controlsfx.control.MaskerPane;

public class GenericIssueView implements Initializable {

  @FXML private StackPane stackPaneMain;
  @FXML private TextArea detailsTextArea;
  @FXML private CbbClearableTextField titleTextField;
  @FXML private CbbClearableTextField nameTextField;
  @FXML private CbbClearableTextField emailTextField;

  private SendService runningservice;
  private MaskerPane masker = new MaskerPane();
  private Validator validator = new Validator();

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    runningservice = new SendService();
    stackPaneMain.getChildren().add(masker);
    masker.setVisible(false);
    validator
        .createCheck()
        .dependsOn("name", nameTextField.textProperty())
        .withMethod(
            c -> {
              String name = c.get("name");
              if (name.isEmpty()) {
                c.error("Το όνομα δεν μπορεί να είναι κενό");
              }
            })
        .decorates(nameTextField)
        .immediate();
    validator
        .createCheck()
        .dependsOn("title", titleTextField.textProperty())
        .withMethod(
            c -> {
              String title = c.get("title");
              if (title.isEmpty()) {
                c.error("O Τίτλος δεν μπορεί να είναι κενός");
              }
            })
        .decorates(titleTextField)
        .immediate();
    validator
        .createCheck()
        .dependsOn("email", emailTextField.textProperty())
        .withMethod(
            c -> {
              String email = c.get("email");
              if (email.isEmpty()) {
                c.error("Το email δεν μπορεί να είναι κενό");
              }
            })
        .decorates(emailTextField)
        .immediate();
    validator
        .createCheck()
        .dependsOn("detail", detailsTextArea.textProperty())
        .withMethod(
            c -> {
              String detail = c.get("detail");
              if (detail.isEmpty()) {
                c.error("Η ανάλυση του προβλήματος δεν μπορεί να είναι κενή");
              }
            })
        .decorates(detailsTextArea)
        .immediate();
  }

  public void setTitle(String title) {
    titleTextField.setText(title);
  }

  public void setDetail(String detail) {
    detailsTextArea.setText(detail);
  }

  @FXML
  private void sendAction(ActionEvent event) {
    if (validator.containsErrors()) {
      AlertDlg.create()
          .type(AlertDlg.Type.ERROR)
          .message("Ελέξτε την φόρμα για λάθη")
          .title("Πρόβλημα")
          .owner(nameTextField.getScene().getWindow())
          .modality(Modality.APPLICATION_MODAL)
          .showAndWait();
      return;
    }
    masker.setText("Αποστολή μυνήματος\nΠαρακαλώ περιμένετε");
    masker.setVisible(true);
    runningservice.restart();
  }

  private class SendService extends Service<String> {

    @Override
    protected void succeeded() {
      masker.setVisible(false);
      if (getValue().startsWith("{success}")) {
        try {
          runAndWait(
              () -> {
                AlertDlg.create()
                    .type(AlertDlg.Type.INFORMATION)
                    .message(
                        "Έχουμε παραλάβει το αίτημα σας\nΘα σας ειδοποιήσουμε σύντομα για τη εξέλιξη του")
                    .title("Ευχαριστούμε")
                    .owner(nameTextField.getScene().getWindow())
                    .modality(Modality.APPLICATION_MODAL)
                    .showAndWait();
              });
        } catch (InterruptedException | ExecutionException ex) {
          ex.printStackTrace();
        }
      } else {
        try {
          runAndWait(
              () -> {
                AlertDlg.create()
                    .type(AlertDlg.Type.ERROR)
                    .message("Δεν ήταν δυνατή η αποστολή μυνήματος\nΠαρακαλώ Δοκιμάστε αργότερα")
                    .title("Πρόβλημα")
                    .owner(nameTextField.getScene().getWindow())
                    .modality(Modality.APPLICATION_MODAL)
                    .showAndWait();
              });
        } catch (InterruptedException | ExecutionException ex) {
          ex.printStackTrace();
        }
      }
    }

    @Override
    protected void failed() {
      masker.setVisible(false);
      try {
        runAndWait(
            () -> {
              AlertDlg.create()
                  .type(AlertDlg.Type.ERROR)
                  .message("Δεν ήταν δυνατή η αποστολή μυνήματος\nΠαρακαλώ Δοκιμάστε αργότερα")
                  .title("Πρόβλημα")
                  .owner(nameTextField.getScene().getWindow())
                  .modality(Modality.APPLICATION_MODAL)
                  .showAndWait();
            });
      } catch (InterruptedException | ExecutionException ex) {
        ex.printStackTrace();
      }
    }

    @Override
    protected Task<String> createTask() {
      return new Task<String>() {
        @Override
        protected String call() throws Exception {
          String response = null;
          try {
            response =
                Calls.CreateIssueRequest(
                    "K8PBC-HTURS-BH5E9-2B039-56044",
                    titleTextField.getText(),
                    detailsTextArea.getText(),
                    nameTextField.getText(),
                    emailTextField.getText(),
                    MainSettings.getInstance().getAppName(),
                    MainSettings.getInstance().getVersion(),
                    Hardware.getSerialNumber());
          } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            return null;
          }
          return response;
        }
      };
    }
  }
}
