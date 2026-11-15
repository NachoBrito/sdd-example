package es.nachobrito.sdd.example.domain.model;

import java.util.UUID;

/**
 * Request-time projection of an active campaign discount matching a product.
 *
 * @param campaignId the campaign identifier
 * @param campaignName the campaign name
 * @param discountPercent the discount percentage applied by the campaign
 */
public record ActiveDiscount(UUID campaignId, String campaignName, int discountPercent) {
}

