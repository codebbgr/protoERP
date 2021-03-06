/*
 * copyright 2013-2021
 * codebb.gr
 * ProtoERP - Open source invocing program
 * info@codebb.gr
 */
/*
 * Changelog
 * =========
 * 25/03/2021 (gmoralis) - Added getTradersPerCompany
 */
package gr.codebb.protoerp.trader;

import eu.taxofficer.protoerp.company.entities.CompanyEntity;
import gr.codebb.lib.database.PersistenceManager;
import gr.codebb.protoerp.settings.company.CompanyUtil;
import java.util.List;
import javax.persistence.EntityManager;
import org.jinq.jpa.JinqJPAStreamProvider;

/** @author snow */
public class TraderQueries {

  public static List<TraderEntity> getTradersPerCompany() {
    CompanyEntity current = CompanyUtil.getCurrentCompany();
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<TraderEntity> results =
        streams
            .streamAll(em, TraderEntity.class)
            .where(p -> p.getCompany().equals(current))
            .toList();
    em.close();
    return results;
  }

  public static List<TraderPlantsEntity> getTraderPlants(TraderEntity trader) {
    JinqJPAStreamProvider streams = new JinqJPAStreamProvider(PersistenceManager.getEmf());
    EntityManager em = PersistenceManager.getEmf().createEntityManager();
    List<TraderPlantsEntity> results =
        streams
            .streamAll(em, TraderPlantsEntity.class)
            .where(p -> p.getTrader().equals(trader))
            .toList();
    em.close();
    return results;
  }
}
