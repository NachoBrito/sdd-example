package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.util.List;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for CampaignPredicateEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface CampaignPredicateJdbcRepository
    extends GenericRepository<CampaignPredicateEntity, UUID> {

  /**
   * Finds all predicates belonging to a campaign.
   *
   * @param campaignId the parent campaign identifier
   * @return the list of predicate entities
   */
  List<CampaignPredicateEntity> findByCampaignId(UUID campaignId);

  /**
   * Finds all predicates belonging to any of the given campaigns.
   *
   * @param campaignIds the parent campaign identifiers
   * @return the list of predicate entities
   */
  List<CampaignPredicateEntity> findByCampaignIdIn(List<UUID> campaignIds);

  /**
   * Deletes all predicates belonging to a campaign.
   *
   * @param campaignId the parent campaign identifier
   */
  void deleteByCampaignId(UUID campaignId);

  /**
   * Inserts a campaign predicate entity.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  CampaignPredicateEntity insert(CampaignPredicateEntity entity);
}

