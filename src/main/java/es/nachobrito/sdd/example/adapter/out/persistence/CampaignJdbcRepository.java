package es.nachobrito.sdd.example.adapter.out.persistence;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.GenericRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Micronaut Data JDBC repository for CampaignEntity.
 */
@JdbcRepository(dialect = Dialect.MYSQL)
public interface CampaignJdbcRepository extends GenericRepository<CampaignEntity, UUID> {

  /**
   * Finds a campaign entity by identifier.
   *
   * @param id the campaign identifier
   * @return an Optional containing the entity if found
   */
  Optional<CampaignEntity> findById(UUID id);

  /**
   * Checks if a campaign entity exists with the specified identifier.
   *
   * @param id the campaign identifier
   * @return true if the entity exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes a campaign entity by identifier.
   *
   * @param id the campaign identifier
   */
  void deleteById(UUID id);

  /**
   * Inserts a campaign entity with a client-assigned identifier.
   *
   * @param entity the entity to insert
   * @return the inserted entity
   */
  CampaignEntity insert(CampaignEntity entity);

  /**
   * Updates an existing campaign entity.
   *
   * @param entity the entity to update
   * @return the updated entity
   */
  CampaignEntity update(CampaignEntity entity);

  /**
   * Queries campaigns by optional substring, status, and overlapping date range filters.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @param status optional status string filter
   * @param startDate optional lower overlap bound in UTC
   * @param endDate optional upper overlap bound in UTC
   * @param sort the sort order
   * @return the matching campaign entities
   */
  @Query(
      "SELECT * FROM campaigns campaign_entity_"
          + " WHERE (:name IS NULL OR :name = ''"
          + " OR LOCATE(LOWER(:name), LOWER(campaign_entity_.name)) > 0)"
          + " AND (:description IS NULL OR :description = ''"
          + " OR (campaign_entity_.description IS NOT NULL"
          + " AND LOCATE(LOWER(:description), LOWER(campaign_entity_.description)) > 0))"
          + " AND (:status IS NULL OR :status = '' OR campaign_entity_.status = :status)"
          + " AND (:startDate IS NULL OR campaign_entity_.end_date >= :startDate)"
          + " AND (:endDate IS NULL OR campaign_entity_.start_date <= :endDate)")
  List<CampaignEntity> search(
      @Nullable String name,
      @Nullable String description,
      @Nullable String status,
      @Nullable Instant startDate,
      @Nullable Instant endDate,
      Sort sort);

  /**
   * Queries campaigns whose status is ACTIVE and window includes the specified instant.
   *
   * @param now the evaluation timestamp in UTC
   * @param sort the sort order
   * @return active campaign entities
   */
  @Query(
      "SELECT * FROM campaigns campaign_entity_"
          + " WHERE campaign_entity_.status = 'ACTIVE'"
          + " AND campaign_entity_.start_date <= :now"
          + " AND campaign_entity_.end_date >= :now")
  List<CampaignEntity> findActiveAt(Instant now, Sort sort);
}

