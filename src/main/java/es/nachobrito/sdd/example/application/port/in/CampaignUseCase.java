package es.nachobrito.sdd.example.application.port.in;

/**
 * Composite inbound port combining all campaign management use cases.
 */
public interface CampaignUseCase
    extends CreateCampaignUseCase,
        UpdateCampaignUseCase,
        ListCampaignsUseCase,
        DeleteCampaignUseCase {
}

