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
 * Inbound port for updating a promotional campaign.
 */
@FunctionalInterface
public interface UpdateCampaignUseCase {

  /**
   * Updates an existing promotional campaign and replaces its predicates.
   *
   * @param command the campaign update attributes
   * @return the updated campaign
   */
  Campaign updateCampaign(UpdateCampaignCommand command);

  /**
   * Command model for updating a promotional campaign.
   *
   * @param id the target campaign identifier
   * @param name the updated campaign name
   * @param description the updated optional description
   * @param status the updated lifecycle status
   * @param startDate the updated activation timestamp in UTC
   * @param endDate the updated expiration timestamp in UTC
   * @param discountPercent the updated discount percentage from 0 to 100
   * @param predicates the replacement eligibility predicates
   */
  record UpdateCampaignCommand(
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
    public UpdateCampaignCommand {
      predicates = predicates == null ? List.of() : List.copyOf(predicates);
    }

    @Override
    public List<CampaignPredicate> predicates() {
      return Collections.unmodifiableList(predicates);
    }
  }
}

