/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 06/04/2021 - Added company to query results
 * 04/04/2021 - Initial
 */
package gr.codebb.protoerp.items;

import eu.taxofficer.protoerp.company.entities.CompanyEntity;
import gr.codebb.lib.database.PersistenceManager;
import gr.codebb.protoerp.settings.company.CompanyUtil;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.jinq.jpa.JinqJPAStreamProvider;

public class ItemsQueries {

  public static List<ItemsEntity> getItemsDatabase(boolean activeonly) {
    CompanyEntity current = CompanyUtil.getCurrentCompany();
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<ItemsEntity> results;
    if (activeonly) {
      results =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(c -> c.getActive())
              .where(p -> p.getCompany().equals(current))
              .toList();
    } else {
      results =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(p -> p.getCompany().equals(current))
              .toList();
    }

    em.close();
    return results;
  }

  public static ItemsEntity getItemByCodeAndId(String code, long id) {
    CompanyEntity current = CompanyUtil.getCurrentCompany();
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<ItemsEntity> result;
    if (id == -1) {
      result =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(p -> p.getCode().equals(code))
              .where(p -> p.getCompany().equals(current))
              .findFirst();
    } else {
      result =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(p -> p.getCode().equals(code))
              .where(p -> p.getCompany().equals(current))
              .where(p -> p.getId() != id)
              .findFirst();
    }
    em.close();
    if (result.isPresent()) {
      return result.get();
    } else {
      return null;
    }
  }

  /**
   * Get ItemEntity by barcode
   *
   * @param barcode
   * @param id set to -1 if you want to search all entries , other number if you want to search
   *     entries except the one specific by id value
   * @return the resulting first entry , null otherwise
   */
  public static ItemsEntity getItemByBarcodeAndId(String barcode, long id) {
    CompanyEntity current = CompanyUtil.getCurrentCompany();
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<ItemsEntity> result;
    if (id == -1) {
      result =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(p -> p.getBarcode().equals(barcode))
              .where(p -> p.getCompany().equals(current))
              .findFirst();
    } else {
      result =
          streams
              .streamAll(em, ItemsEntity.class)
              .where(p -> p.getBarcode().equals(barcode))
              .where(p -> p.getCompany().equals(current))
              .where(p -> p.getId() != id)
              .findFirst();
    }
    em.close();
    if (result.isPresent()) {
      return result.get();
    } else {
      return null;
    }
  }

  /**
   * Get Item entity by barcode entry
   *
   * @param barcode
   * @return the resulting first entry , null otherwise
   */
  public static ItemsEntity getItemByBarcode(String barcode) {
    return getItemByBarcodeAndId(barcode, -1);
  }
  /**
   * Get Item entity by code entry
   *
   * @param code
   * @return
   */
  public static ItemsEntity getItemByCode(String code) {
    return getItemByCodeAndId(code, -1);
  }
}
