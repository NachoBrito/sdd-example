package es.nachobrito.sdd.example.application.port.out;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Outbound port defining promotional campaign persistence operations.
 */
public interface CampaignRepositoryPort {

  /**
   * Saves a new campaign to persistence.
   *
   * @param campaign the campaign to save
   * @return the saved campaign
   */
  Campaign save(Campaign campaign);

  /**
   * Updates an existing campaign and replaces its predicates.
   *
   * @param campaign the updated campaign
   * @return the updated campaign
   */
  Campaign update(Campaign campaign);

  /**
   * Finds a campaign by its unique identifier.
   *
   * @param id the campaign identifier
   * @return an Optional containing the campaign if found
   */
  Optional<Campaign> findById(UUID id);

  /**
   * Checks if a campaign exists with the given identifier.
   *
   * @param id the campaign identifier
   * @return true if the campaign exists
   */
  boolean existsById(UUID id);

  /**
   * Deletes a campaign by its unique identifier.
   *
   * @param id the campaign identifier
   */
  void deleteById(UUID id);

  /**
   * Finds all campaigns matching optional filters.
   *
   * @param name optional case-insensitive name substring filter
   * @param description optional case-insensitive description substring filter
   * @param status optional status filter
   * @param startDate optional lower overlap bound in UTC
   * @param endDate optional upper overlap bound in UTC
   * @return the matching campaigns
   */
  List<Campaign> findAll(
      @Nullable String name,
      @Nullable String description,
      @Nullable CampaignStatus status,
      @Nullable Instant startDate,
      @Nullable Instant endDate);

  /**
   * Finds all campaigns whose status is ACTIVE and window contains the timestamp.
   *
   * @param now the evaluation timestamp in UTC
   * @return the active campaigns at that instant
   */
  List<Campaign> findActiveAt(Instant now);
}

