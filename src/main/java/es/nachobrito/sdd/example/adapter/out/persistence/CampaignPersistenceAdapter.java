package es.nachobrito.sdd.example.adapter.out.persistence;

import es.nachobrito.sdd.example.application.port.out.CampaignRepositoryPort;
import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import io.micronaut.data.model.Sort;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Persistence adapter implementing CampaignRepositoryPort with Micronaut Data JDBC.
 */
@Singleton
@DbQueryMetrics
public class CampaignPersistenceAdapter implements CampaignRepositoryPort {

  private final CampaignJdbcRepository campaignRepository;
  private final CampaignPredicateJdbcRepository predicateRepository;

  /**
   * Constructs the persistence adapter with campaign and predicate repositories.
   *
   * @param campaignRepository the campaign JDBC repository
   * @param predicateRepository the campaign predicate JDBC repository
   */
  public CampaignPersistenceAdapter(
      CampaignJdbcRepository campaignRepository,
      CampaignPredicateJdbcRepository predicateRepository) {
    this.campaignRepository = campaignRepository;
    this.predicateRepository = predicateRepository;
  }

  @Override
  @Transactional
  public Campaign save(Campaign campaign) {
    Objects.requireNonNull(campaign, "Campaign cannot be null");
    CampaignEntity saved = campaignRepository.insert(CampaignEntity.fromDomain(campaign));
    insertPredicates(saved.id(), campaign.predicates());
    return campaign;
  }

  @Override
  @Transactional
  public Campaign update(Campaign campaign) {
    Objects.requireNonNull(campaign, "Campaign cannot be null");
    CampaignEntity updated = campaignRepository.update(CampaignEntity.fromDomain(campaign));
    predicateRepository.deleteByCampaignId(updated.id());
    insertPredicates(updated.id(), campaign.predicates());
    return campaign;
  }

  @Override
  public Optional<Campaign> findById(UUID id) {
    Objects.requireNonNull(id, "Campaign id cannot be null");
    return campaignRepository.findById(id).map(this::loadWithPredicates);
  }

  @Override
  public boolean existsById(UUID id) {
    Objects.requireNonNull(id, "Campaign id cannot be null");
    return campaignRepository.existsById(id);
  }

  @Override
  public void deleteById(UUID id) {
    Objects.requireNonNull(id, "Campaign id cannot be null");
    campaignRepository.deleteById(id);
  }

  @Override
  public List<Campaign> findAll(
      @Nullable String name,
      @Nullable String description,
      @Nullable CampaignStatus status,
      @Nullable Instant startDate,
      @Nullable Instant endDate) {
    String statusName = status == null ? null : status.name();
    List<CampaignEntity> entities = campaignRepository.search(
        normalizeFilter(name),
        normalizeFilter(description),
        statusName,
        startDate,
        endDate,
        Sort.of(Sort.Order.asc("name")));
    return hydratePredicates(entities);
  }

  @Override
  public List<Campaign> findActiveAt(Instant now) {
    Objects.requireNonNull(now, "Timestamp cannot be null");
    List<CampaignEntity> entities =
        campaignRepository.findActiveAt(now, Sort.of(Sort.Order.asc("name")));
    return hydratePredicates(entities);
  }

  private void insertPredicates(UUID campaignId, List<CampaignPredicate> predicates) {
    for (CampaignPredicate predicate : predicates) {
      predicateRepository.insert(
          CampaignPredicateEntity.fromDomain(UUID.randomUUID(), campaignId, predicate));
    }
  }

  private Campaign loadWithPredicates(CampaignEntity entity) {
    List<CampaignPredicate> predicates = predicateRepository.findByCampaignId(entity.id()).stream()
        .map(CampaignPredicateEntity::toDomain)
        .toList();
    return entity.toDomain(predicates);
  }

  private List<Campaign> hydratePredicates(List<CampaignEntity> entities) {
    if (entities.isEmpty()) {
      return List.of();
    }
    List<UUID> campaignIds = entities.stream().map(CampaignEntity::id).toList();
    Map<UUID, List<CampaignPredicate>> predicatesByCampaign =
        predicateRepository.findByCampaignIdIn(campaignIds).stream()
            .collect(Collectors.groupingBy(
                CampaignPredicateEntity::campaignId,
                Collectors.mapping(CampaignPredicateEntity::toDomain, Collectors.toList())));
    return entities.stream()
        .map(entity -> entity.toDomain(predicatesByCampaign.getOrDefault(entity.id(), List.of())))
        .toList();
  }

  private static @Nullable String normalizeFilter(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}

