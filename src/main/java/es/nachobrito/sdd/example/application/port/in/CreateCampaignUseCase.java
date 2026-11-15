package es.nachobrito.sdd.example.application.port.in;

import es.nachobrito.sdd.example.domain.model.Campaign;
import es.nachobrito.sdd.example.domain.model.CampaignPredicate;
import es.nachobrito.sdd.example.domain.model.CampaignStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Inbound port for creating a promotional campaign.
 */
@FunctionalInterface
public interface CreateCampaignUseCase {

  /**
   * Creates a promotional campaign.
   *
   * @param command the campaign creation attributes
   * @return the created campaign
   */
  Campaign createCampaign(CreateCampaignCommand command);

  /**
   * Command model for creating a promotional campaign.
   *
   * @param id the client-generated campaign identifier
   * @param name the campaign name
   * @param description the optional campaign description
   * @param status the lifecycle status
   * @param startDate the inclusive activation timestamp in UTC
   * @param endDate the inclusive expiration timestamp in UTC
   * @param discountPercent the integer discount percentage from 0 to 100
   * @param predicates the eligibility predicates
   */
  record CreateCampaignCommand(
      UUID id,
      String name,
      @Nullable String description,
      CampaignStatus status,
      Instant startDate,
      Instant endDate,
      int discountPercent,
      List<CampaignPredicate> predicates) {

    /**
     * Compact constructor that copies the predicate list.
     */
    public CreateCampaignCommand {
      predicates = predicates == null ? List.of() : List.copyOf(predicates);
    }

    @Override
    public List<CampaignPredicate> predicates() {
      return Collections.unmodifiableList(predicates);
    }
  }
}

