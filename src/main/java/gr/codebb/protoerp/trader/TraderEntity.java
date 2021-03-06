/*
 * copyright 2013-2020
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * changelog
 * =========
 * 19/03/2021 (gmoralis) - Αρχικό βασισμένο στο companyEntity
 */
package gr.codebb.protoerp.trader;

import eu.taxofficer.protoerp.company.entities.CompanyEntity;
import eu.taxofficer.protoerp.tables.entities.DoyEntity;
import gr.codebb.lib.crud.intf.Displayable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "protoerp_traders")
public class TraderEntity implements Serializable, Displayable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private Long id;

  @Getter @Setter private String name;
  @Getter @Setter private String registeredName;
  @Getter @Setter private String job;
  @Getter @Setter private String vatNumber;

  @ManyToOne
  @JoinColumn(name = "doy_id")
  @Getter
  @Setter
  private DoyEntity doy;

  @Getter @Setter private String email;
  @Getter @Setter private String mobilePhone;
  @Getter @Setter private Boolean active;

  @ManyToOne
  @JoinColumn(name = "company_id")
  @Getter
  @Setter
  private CompanyEntity company;

  @OneToMany(mappedBy = "trader", cascade = CascadeType.ALL, orphanRemoval = true)
  @Getter
  private List<TraderPlantsEntity> plantLines = new ArrayList<>();

  public void addPlantLine(TraderPlantsEntity line) {
    plantLines.add(line);
    line.setTrader(this);
  }

  public void removePlantLine(TraderPlantsEntity line) {
    plantLines.remove(line);
    line.setTrader(null);
  }

  @Override
  public String getComboDisplayValue() {
    return String.format("%9s - %s", vatNumber, name);
  }
}
