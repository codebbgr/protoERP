/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 05/04/2021 (gmoralis) - Προσθήκη getMdVatExemptionDatabase
 * 30/03/2021 (gmoralis) - Copied from prototype with minimal changes
 */
package gr.codebb.protoerp.mydata.masterdata;

import gr.codebb.lib.database.PersistenceManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.jinq.jpa.JinqJPAStreamProvider;

public class MasterDataQueries {

  public static List<MeasureUnitmdEntity> getMdMeasureDatabase(boolean activeonly) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<MeasureUnitmdEntity> results;
    if (activeonly) {
      results = streams.streamAll(em, MeasureUnitmdEntity.class).where(c -> c.getActive()).toList();
    } else {
      results = streams.streamAll(em, MeasureUnitmdEntity.class).toList();
    }

    em.close();
    return results;
  }

  public static List<VatmdEntity> getMdVatDatabase(boolean activeonly) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<VatmdEntity> results;
    if (activeonly) {
      results = streams.streamAll(em, VatmdEntity.class).where(c -> c.getActive()).toList();
    } else {
      results = streams.streamAll(em, VatmdEntity.class).toList();
    }

    em.close();
    return results;
  }

  public static List<VatmdExemptionEntity> getMdVatExemptionDatabase(boolean activeonly) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<VatmdExemptionEntity> results;
    if (activeonly) {
      results =
          streams.streamAll(em, VatmdExemptionEntity.class).where(c -> c.getActive()).toList();
    } else {
      results = streams.streamAll(em, VatmdExemptionEntity.class).toList();
    }

    em.close();
    return results;
  }

  public static VatmdEntity getmdVatFromVatRate(BigDecimal vatRate) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<VatmdEntity> result;
    result =
        streams
            .streamAll(em, VatmdEntity.class)
            .where(c -> c.getActive())
            .where(p -> p.getVatRate().equals(vatRate))
            .findFirst();

    em.close();
    if (result.isPresent()) {
      return result.get();
    } else {
      return null;
    }
  }

  public static MeasureUnitmdEntity getmdMeasureFromId(long id) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    Optional<MeasureUnitmdEntity> result;
    result =
        streams
            .streamAll(em, MeasureUnitmdEntity.class)
            .where(c -> c.getActive())
            .where(p -> p.getId() == id)
            .findFirst();

    em.close();
    if (result.isPresent()) {
      return result.get();
    } else {
      return null;
    }
  }
}
