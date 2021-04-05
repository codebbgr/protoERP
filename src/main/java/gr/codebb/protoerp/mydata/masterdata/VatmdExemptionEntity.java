/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 05/04/2021 (gmoralis) - Initial
 */
package gr.codebb.protoerp.mydata.masterdata;

import gr.codebb.lib.crud.intf.Displayable;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "protoerp_mydata_vatExemption")
public class VatmdExemptionEntity implements Serializable, Displayable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private Long id;

  @Getter @Setter private Integer code;
  @Getter @Setter private String description;

  @Override
  public String getComboDisplayValue() {
    return description;
  }
}
