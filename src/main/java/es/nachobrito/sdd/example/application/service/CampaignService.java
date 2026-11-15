package es.nachobrito.sdd.example.application.service;

import es.nachobrito.sdd.example.application.port.in.CampaignUseCase;
import es.nachobrito.sdd.example.application.port.in.ResolveProductDiscountsUseCase;
import es.nachobrito.sdd.example.application.port.out.CampaignRepositoryPort;
import es.nachobrito.sdd.example.domain.model.ActiveDiscount;
import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignNotFoundException;
import es.nachobrito.sdd.example.domain.model.DuplicateCampaignIdentifierException;
import es.nachobrito.sdd.example.domain.model.InvalidCampaignDataException;
import es.nachobrito.sdd.example.domain.model.Product;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service implementing campaign management and discount resolution use cases.
 */
@Singleton
public class CampaignService implements CampaignUseCase, ResolveProductDiscountsUseCase {

  private final CampaignRepositoryPort campaignRepository;
  private final Clock clock;

  /**
   * Constructs the service with the default UTC clock.
   *
   * @param campaignRepository the campaign repository port
   */
  @Inject
  public CampaignService(CampaignRepositoryPort campaignRepository) {
    this(campaignRepository, Clock.systemUTC());
  }

  /**
   * Constructs the service with a custom clock for testing.
   *
   * @param campaignRepository the campaign repository port
   * @param clock the clock instance
   */
  public CampaignService(CampaignRepositoryPort campaignRepository, Clock clock) {
    this.campaignRepository = campaignRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Campaign createCampaign(CreateCampaignCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    Campaign campaign = new Campaign(
        command.id(),
        command.name(),
        command.description(),
        command.status(),
        command.startDate(),
        command.endDate(),
        command.discountPercent(),
        command.predicates());
    if (campaignRepository.existsById(campaign.id())) {
      throw new DuplicateCampaignIdentifierException(campaign.id());
    }
    return campaignRepository.save(campaign);
  }

  @Override
  @Transactional
  public Campaign updateCampaign(UpdateCampaignCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    Objects.requireNonNull(command.id(), "Campaign id cannot be null");
    if (!campaignRepository.existsById(command.id())) {
      throw new CampaignNotFoundException(command.id());
    }
    Campaign updated = new Campaign(
        command.id(),
        command.name(),
        command.description(),
        command.status(),
        command.startDate(),
        command.endDate(),
        command.discountPercent(),
        command.predicates());
    return campaignRepository.update(updated);
  }

  @Override
  public List<Campaign> listCampaigns(ListCampaignsQuery query) {
    Objects.requireNonNull(query, "Query cannot be null");
    Instant startDate = query.startDate();
    Instant endDate = query.endDate();
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new InvalidCampaignDataException("Filter endDate cannot be before startDate");
    }
    return campaignRepository.findAll(
        query.name(),
        query.description(),
        query.status(),
        startDate,
        endDate);
  }

  @Override
  @Transactional
  public void deleteCampaign(UUID id) {
    Objects.requireNonNull(id, "Campaign id cannot be null");
    if (!campaignRepository.existsById(id)) {
      throw new CampaignNotFoundException(id);
    }
    campaignRepository.deleteById(id);
  }

  @Override
  public List<ActiveDiscount> resolveActiveDiscounts(Product product) {
    Objects.requireNonNull(product, "Product cannot be null");
    return resolveActiveDiscounts(List.of(product)).getOrDefault(product.id(), List.of());
  }

  @Override
  public Map<UUID, List<ActiveDiscount>> resolveActiveDiscounts(List<Product> products) {
    Objects.requireNonNull(products, "Products list cannot be null");
    if (products.isEmpty()) {
      return Map.of();
    }
    Instant now = clock.instant();
    List<Campaign> activeCampaigns = campaignRepository.findActiveAt(now).stream()
        .filter(campaign -> campaign.isActiveAt(now))
        .toList();
    Map<UUID, List<ActiveDiscount>> discountsByProduct = new ConcurrentHashMap<>();
    for (Product product : products) {
      Objects.requireNonNull(product, "Product element cannot be null");
      discountsByProduct.put(product.id(), matchActiveDiscounts(product, activeCampaigns));
    }
    return discountsByProduct;
  }

  private static List<ActiveDiscount> matchActiveDiscounts(
      Product product, List<Campaign> activeCampaigns) {
    return activeCampaigns.stream()
        .filter(campaign -> campaign.matches(product))
        .map(Campaign::toActiveDiscount)
        .toList();
  }
}

