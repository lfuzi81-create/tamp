package com.tamp.client.service;

import com.tamp.client.entity.Client;
import com.tamp.client.repository.ClientRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 客户/店铺 AUM 持久化同步，独立 Bean 避免 {@link ClientService} 自调用导致事务失效。
 */
@Service
public class ClientAumSyncService {

    private final ClientRepository clientRepository;
    private final JdbcTemplate jdbcTemplate;

    public ClientAumSyncService(ClientRepository clientRepository, JdbcTemplate jdbcTemplate) {
        this.clientRepository = clientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void syncClientAum(Long clientId, BigDecimal totalAmount) {
        Client client = clientRepository.findByIdAndDeleted(clientId, 0).orElse(null);
        if (client == null || client.getShopId() == null) {
            return;
        }
        BigDecimal normalized = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        client.setAumTotal(normalized);
        clientRepository.save(client);
        refreshShopTotalAum(client.getShopId());
    }

    @Transactional
    public void refreshShopTotalAum(Long shopId) {
        if (shopId == null) {
            return;
        }
        jdbcTemplate.update(
                """
                UPDATE biz_shop SET total_aum = (
                    SELECT COALESCE(SUM(aum_total), 0) FROM biz_client
                    WHERE shop_id = ? AND is_deleted = 0
                ), updated_at = NOW()
                WHERE id = ? AND is_deleted = 0
                """,
                shopId,
                shopId
        );
    }
}
